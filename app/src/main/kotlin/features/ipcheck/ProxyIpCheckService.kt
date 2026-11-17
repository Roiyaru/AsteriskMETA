// Copyright 2026, AsteriskMETA contributors
// SPDX-License-Identifier: GPL-3.0

package features.ipcheck

import android.content.Context
import app.AppState
import engine.mihomo.runtime.MihomoNodeAddress
import engine.mihomo.runtime.MihomoNodeAddressDirectory
import engine.mihomo.runtime.MihomoProxiesState
import engine.mihomo.runtime.MihomoProxyGroup
import engine.mihomo.runtime.MihomoRuntimeRepository
import engine.network.DnsResolutionPolicy
import engine.network.DnsResolver
import engine.network.IpGeolocation
import engine.network.IpGeolocationLookup
import engine.network.LocalProxyTarget
import engine.network.PublicAddressProbe
import engine.network.PublicAddressProbeResult
import engine.proxy.LocalProxyRuntime
import engine.proxy.toLocalProxyOptions
import engine.proxy.toLocalProxyOptionsOrNull
import engine.mihomo.raw.loadSelectedRawConfig
import engine.mihomo.raw.usesRawMihomoConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * One side of the check: the address and the network it belongs to.
 */
internal data class IpCheckEndpoint(
    val address: String = "",
    val province: String = "",
    val organization: String = "",
    val asn: String = "",
    val country: String = "",
    val countryCode: String = "",
    val city: String = "",
    val error: String = "",
) {
    val hasAddress: Boolean
        get() = address.isNotBlank()
}

/**
 * The result of one check.
 */
internal data class IpCheckResult(
    val entry: IpCheckEndpoint = IpCheckEndpoint(),
    val exit: IpCheckEndpoint = IpCheckEndpoint(),
    val nodeName: String = "",
    val nodeHost: String = "",
    val dnsServer: String = "",
    val resolvedAddresses: List<String> = emptyList(),
    /** The group carries the traffic itself, so there is no entry to tell from the exit. */
    val isDirect: Boolean = false,
)

/**
 * Checks the addresses of a proxy group.
 *
 * The check has two sides. The entry side is the address of the node the group selected: the
 * name of the node is followed into the configuration, the domain of the node is resolved with
 * the nameservers the configuration names, and the resulting addresses are looked up. The exit
 * side is the address the tunnel leaves with, which a request through the local proxy reports.
 */
internal class ProxyIpCheckService(
    private val context: Context,
    private val mihomoRuntime: MihomoRuntimeRepository,
    private val addressDirectory: MihomoNodeAddressDirectory,
    private val dnsResolver: DnsResolver = DnsResolver(),
    private val geolocationLookup: IpGeolocationLookup = IpGeolocationLookup(),
    private val addressProbe: PublicAddressProbe = PublicAddressProbe(),
) {
    /**
     * The groups a check can run against, in the order the runtime reports them.
     */
    fun selectableGroups(proxies: MihomoProxiesState): List<MihomoProxyGroup> {
        return proxies.groups.filter { group -> group.all.isNotEmpty() }
    }

    /**
     * The node a group selected, followed down the nested groups to the node that carries the
     * traffic.
     */
    fun selectedNode(proxies: MihomoProxiesState, groupName: String): MihomoProxyGroup? {
        return proxies.groups.firstOrNull { group -> group.name == groupName }
    }

    /**
     * Runs the check for the group [groupName].
     */
    suspend fun check(
        appState: AppState,
        proxies: MihomoProxiesState,
        groupName: String,
    ): IpCheckResult {
        val group = selectedNode(proxies, groupName)
            ?: return IpCheckResult(entry = IpCheckEndpoint(error = IpCheckErrorUnknownGroup))
        val nodeId = group.resolvedNodeId(proxies)
        val nodeName = nodeId?.name.orEmpty()
        if (nodeName in IpCheckDirectOutbounds) {
            // The group hands the traffic to the network instead of to a node, so there is no
            // entry and no exit to tell apart: both sides are the network of the device.
            return IpCheckResult(entry = resolveDirect(), nodeName = nodeName, isDirect = true)
        }
        val catalog = addressDirectory.read(appState)
        val nodeAddress = catalog.addresses[nodeName]
        val entry = resolveEntry(nodeAddress, catalog.dnsPolicy)
        val exit = resolveExit(appState)
        return IpCheckResult(
            entry = entry.first,
            exit = exit,
            nodeName = nodeName,
            nodeHost = nodeAddress?.host.orEmpty(),
            dnsServer = entry.second,
            resolvedAddresses = entry.third,
        )
    }

    /**
     * The entry side: the addresses of the node the group selected.
     *
     * The domain of the node is resolved with the nameservers the configuration names for the
     * node domains, and every address the resolution returns is looked up, because a domain may
     * point at more than one network.
     */
    private suspend fun resolveEntry(
        nodeAddress: MihomoNodeAddress?,
        policy: DnsResolutionPolicy,
    ): Triple<IpCheckEndpoint, String, List<String>> {
        if (nodeAddress == null || nodeAddress.host.isBlank()) {
            return Triple(IpCheckEndpoint(error = IpCheckErrorUnknownNode), "", emptyList())
        }
        val host = nodeAddress.host
        if (host.isIpAddress()) {
            // An address needs no resolution; it is looked up directly.
            val geolocation = geolocationLookup.lookup(host)
            return Triple(geolocation.toEndpoint(), "", listOf(host))
        }
        val resolution = dnsResolver.resolve(host, policy)
        val addresses = resolution.addresses.map { resolved -> resolved.address }
        if (addresses.isEmpty()) {
            return Triple(IpCheckEndpoint(error = IpCheckErrorResolutionFailed), resolution.server, emptyList())
        }
        val endpoint = withContext(Dispatchers.IO) {
            val geolocations = addresses.map { address -> geolocationLookup.lookup(address) }
            geolocations.first().toEndpoint()
        }
        return Triple(endpoint, resolution.server, addresses)
    }

    /**
     * The exit side: the address the tunnel leaves with. The request goes through the local
     * proxy, so the answer is the address of the tunnel rather than the address of the device.
     */
    private suspend fun resolveExit(appState: AppState): IpCheckEndpoint {
        val target = localProxyTarget(appState)
        return describe(addressProbe.probe(target?.toProxy()))
    }

    /**
     * The network of the device itself, for a group that hands the traffic to the network.
     */
    private suspend fun resolveDirect(): IpCheckEndpoint {
        return describe(addressProbe.probe(proxy = null))
    }

    /**
     * The address of a probe together with the network it belongs to.
     *
     * The endpoint that answers a probe reports the address and its own idea of the network, but
     * its idea has no region: the answer names the country and the operator and nothing between
     * them. The address the probe returned is therefore looked up like any other address, and the
     * two answers are merged with the more specific one winning on each field.
     */
    private suspend fun describe(probe: PublicAddressProbeResult): IpCheckEndpoint {
        if (!probe.isSuccessful) return probe.toEndpoint()
        val location = runCatching { geolocationLookup.lookup(probe.address) }.getOrNull()
            ?: IpGeolocation(address = probe.address)
        return IpCheckEndpoint(
            address = probe.address,
            country = location.country.ifBlank { probe.country },
            countryCode = location.countryCode.ifBlank { probe.countryCode },
            province = location.province.ifBlank { probe.region },
            city = location.city.ifBlank { probe.city },
            // The autonomous system of an address is the one the endpoint that saw the request
            // reports, because that is the network the request actually left from.
            asn = probe.asn.ifBlank { location.asn },
            organization = probe.organization.ifBlank { location.organization },
        )
    }

    /**
     * The local proxy the tunnel publishes. Without a running tunnel the request goes out
     * directly, so the answer is then the address of the device.
     */
    private fun localProxyTarget(appState: AppState): LocalProxyTarget? {
        if (!appState.proxyRunning) return null
        val usesRawConfig = appState.usesRawMihomoConfig()
        val options = LocalProxyRuntime.current() ?: if (usesRawConfig) {
            context.loadSelectedRawConfig(appState)?.snapshot?.toLocalProxyOptionsOrNull()
        } else {
            appState.toLocalProxyOptions()
        }
        val resolved = options ?: return null
        val host = resolved.listenAddress.takeIf { it != "0.0.0.0" } ?: "127.0.0.1"
        return LocalProxyTarget(
            host = host,
            port = resolved.port,
            username = resolved.username,
            password = resolved.password,
            useSocks = usesRawConfig,
        )
    }
}

/**
 * The node a group carries the traffic with. The selection of a group may be another group, so
 * the chain is followed down to the node itself.
 */
internal fun MihomoProxyGroup.resolvedNodeId(proxies: MihomoProxiesState): engine.mihomo.runtime.MihomoProxyNodeId? {
    var current = this
    repeat(proxies.groups.size) {
        val name = current.now
        if (name.isBlank()) return null
        val nested = proxies.groups.firstOrNull { group -> group.name == name } ?: return current.memberId(name)
        current = nested
    }
    return current.now.takeIf { name -> name.isNotBlank() }?.let { name -> current.memberId(name) }
}

private fun MihomoProxyGroup.memberId(name: String): engine.mihomo.runtime.MihomoProxyNodeId {
    return all.firstOrNull { member -> member.name == name } ?: engine.mihomo.runtime.MihomoProxyNodeId(name)
}

/**
 * The lookup answer as the card reads it.
 */
internal fun IpGeolocation.toEndpoint(): IpCheckEndpoint {
    return IpCheckEndpoint(
        address = address,
        province = province,
        organization = organization,
        asn = asn,
        country = country,
        countryCode = countryCode,
        city = city,
    )
}

/**
 * The probe answer as the card reads it.
 */
internal fun PublicAddressProbeResult.toEndpoint(): IpCheckEndpoint {
    return IpCheckEndpoint(
        address = address,
        province = region,
        organization = organization,
        asn = asn,
        country = country,
        countryCode = countryCode,
        city = city,
        error = error,
    )
}

/** Whether a host is an address rather than a name. */
internal fun String.isIpAddress(): Boolean {
    val trimmed = trim()
    if (trimmed.isEmpty()) return false
    return trimmed.contains(':') || trimmed.count { character -> character == '.' } == 3
}

internal const val IpCheckErrorUnknownGroup = "unknown_group"

/**
 * The outbounds of a group that hand the traffic to the network rather than to a node.
 */
internal val IpCheckDirectOutbounds = setOf("DIRECT", "PASS", "COMPATIBLE")
internal const val IpCheckErrorUnknownNode = "unknown_node"
internal const val IpCheckErrorResolutionFailed = "resolution_failed"
