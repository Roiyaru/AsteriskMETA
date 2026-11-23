// Copyright 2026, AsteriskMETA contributors
// SPDX-License-Identifier: GPL-3.0

package features.monitoring.dns

import engine.mihomo.runtime.MihomoDnsRoute

/**
 * Tells whether a DNS lookup reached its upstream through the proxy or directly.
 *
 * The core keeps the proxy side and the direct side in separate name server lists, so the
 * upstream the core names in its log decides which side answered the lookup.
 */
internal class MihomoDnsRouteResolver(
    proxyNameservers: List<String>,
    directNameservers: List<String>,
) {
    private val proxyUpstreams = proxyNameservers.map(::normalizedUpstream).toSet()
    private val directUpstreams = directNameservers.map(::normalizedUpstream).toSet()

    /** Classifies the upstream the core named in its log. */
    fun resolve(upstream: String): MihomoDnsRoute {
        if (upstream.isBlank()) return MihomoDnsRoute.Unknown
        val normalized = normalizedUpstream(upstream)
        return when {
            normalized in proxyUpstreams -> MihomoDnsRoute.Proxy
            normalized in directUpstreams -> MihomoDnsRoute.Direct
            else -> MihomoDnsRoute.Unknown
        }
    }
}

/**
 * Reduces an upstream address to a comparable form.
 *
 * The core names the upstream by its address, such as `https://dns.google/dns-query`,
 * while the configuration lists it with a scheme and a port, such as
 * `https://dns.google/dns-query#`. The port and the trailing marker do not matter.
 */
private fun normalizedUpstream(value: String): String {
    val withoutMarker = value.substringBefore('#').trim()
    val schemeSeparator = withoutMarker.indexOf("://")
    if (schemeSeparator <= 0) return withoutMarker
    val scheme = withoutMarker.substring(0, schemeSeparator)
    val host = withoutMarker.substring(schemeSeparator + 3)
    val withoutPort = host.substringBeforeLast(':', missingDelimiterValue = host)
    return "$scheme://$withoutPort"
}
