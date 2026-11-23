// Copyright 2026, AsteriskMETA contributors
// SPDX-License-Identifier: GPL-3.0

package features.monitoring.connections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.R
import engine.mihomo.runtime.MihomoDnsRecord
import engine.mihomo.runtime.MihomoDnsRoute
import features.monitoring.ConnectionMonitorStatus
import features.monitoring.dns.DnsRecordFilter
import ui.components.AsteriskChipTone
import ui.components.AsteriskExpressiveCard
import ui.components.AsteriskFilterChip
import ui.components.AsteriskInfoChip
import ui.components.AsteriskStatusCard
import ui.icons.AsteriskIcons as Icons
import ui.layout.LocalAdaptiveWindow
import ui.layout.pageListPadding
import ui.layout.pageReadableWidth
import ui.layout.rememberPageGutter
import ui.theme.AsteriskShapeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal val DnsMonitorPageMaxWidth = 1008.dp
private val DnsCardSpacing = 12.dp

/**
 * The DNS records the core answered while the monitor was open.
 *
 * Each card shows the queried type, the name, the addresses the core returned and the
 * upstream it asked, which is what the DNS panel of the reference screenshots shows.
 * The summary card groups the records by the upstream they asked, and tapping one of
 * its blocks narrows the list to the lookups that went through that upstream.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DnsMonitorContent(
    status: ConnectionMonitorStatus,
    records: List<MihomoDnsRecord>,
    filteredRecords: List<MihomoDnsRecord>,
    error: String,
    searching: Boolean,
    selectedUpstream: String?,
    onUpstreamSelect: (String?) -> Unit,
    listState: LazyListState,
    contentPadding: PaddingValues,
) {
    val gutter = rememberPageGutter()
    val timeStyle = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    // The upstream blocks aggregate the whole session, so a server that answered
    // earlier stays reachable even when the search or the filters hide its records.
    val upstreamCounts = remember(records) {
        records.asSequence()
            .filter { it.upstream.isNotBlank() }
            .groupingBy { it.upstream }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }
    }
    val visibleRecords = remember(filteredRecords, selectedUpstream) {
        if (selectedUpstream == null) {
            filteredRecords
        } else {
            filteredRecords.filter { it.upstream == selectedUpstream }
        }
    }

    BoxWithConstraints(Modifier.pageReadableWidth(DnsMonitorPageMaxWidth).fillMaxSize()) {
        val twoColumns = LocalAdaptiveWindow.current.showTwoColumns &&
            maxWidth - gutter * 2 >= 720.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = pageListPadding(contentPadding, bottomExtra = 20.dp, horizontalExtra = gutter),
            verticalArrangement = Arrangement.spacedBy(DnsCardSpacing),
        ) {
            when (status) {
                ConnectionMonitorStatus.ServiceStopped -> {
                    item("dns-stopped") { StatusCard(stringResource(R.string.monitor_service_not_enabled)) }
                    return@LazyColumn
                }
                ConnectionMonitorStatus.Loading -> {
                    item("dns-loading") { StatusCard(stringResource(R.string.monitor_loading), loading = true) }
                    return@LazyColumn
                }
                ConnectionMonitorStatus.Error -> {
                    item("dns-error") {
                        StatusCard(error.ifBlank { stringResource(R.string.monitor_data_unavailable) })
                    }
                    return@LazyColumn
                }
                ConnectionMonitorStatus.Available -> Unit
            }
            item("dns-summary") {
                DnsMonitorStatus(records.size, upstreamCounts, selectedUpstream, onUpstreamSelect)
            }
            if (selectedUpstream != null) {
                item("dns-upstream-filter") {
                    ActiveUpstreamFilter(
                        upstream = selectedUpstream,
                        count = visibleRecords.size,
                        onClear = { onUpstreamSelect(null) },
                    )
                }
            }
            when {
                visibleRecords.isEmpty() -> item("dns-empty") {
                    StatusCard(
                        stringResource(
                            if (searching || selectedUpstream != null) R.string.monitor_dns_no_match
                            else R.string.monitor_dns_empty,
                        ),
                    )
                }
                twoColumns -> items(
                    count = (visibleRecords.size + 1) / 2,
                    key = { row -> "dns-${visibleRecords[row * 2].id}" },
                ) { row ->
                    val first = row * 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DnsCardSpacing),
                    ) {
                        DnsRecordCard(
                            visibleRecords[first],
                            timeStyle,
                            modifier = Modifier.weight(1f),
                        )
                        val next = visibleRecords.getOrNull(first + 1)
                        if (next == null) {
                            Spacer(Modifier.weight(1f))
                        } else {
                            DnsRecordCard(next, timeStyle, modifier = Modifier.weight(1f))
                        }
                    }
                }
                else -> items(
                    visibleRecords,
                    key = { "dns-${it.id}" },
                ) { record ->
                    DnsRecordCard(record, timeStyle, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DnsMonitorStatus(
    total: Int,
    upstreams: List<Pair<String, Int>>,
    selectedUpstream: String?,
    onUpstreamSelect: (String?) -> Unit,
) {
    AsteriskStatusCard {
        Column(verticalArrangement = Arrangement.spacedBy(DnsCardSpacing)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DnsCardSpacing),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.monitor_dns_total),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = total.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum",
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            // The blocks group the records by the upstream they went through, so a tap
            // narrows the list to that server without touching the filter menu.
            if (upstreams.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    upstreams.forEach { (upstream, count) ->
                        AsteriskFilterChip(
                            selected = upstream == selectedUpstream,
                            onClick = {
                                onUpstreamSelect(if (upstream == selectedUpstream) null else upstream)
                            },
                            label = "${upstreamLabel(upstream)} · $count",
                        )
                    }
                }
            }
        }
    }
}

/** The chip that names the upstream the list is narrowed to, with a way out. */
@Composable
private fun ActiveUpstreamFilter(
    upstream: String,
    count: Int,
    onClear: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AsteriskFilterChip(
            selected = true,
            onClick = onClear,
            label = "${upstreamLabel(upstream)} · $count",
            trailingIcon = {
                Icon(
                    Icons.Rounded.Clear,
                    contentDescription = stringResource(R.string.monitor_dns_filter_clear),
                )
            },
        )
    }
}

/**
 * The short name of an upstream, for the blocks and the filter chip.
 *
 * The full address stays in the record cards, because it is what the core asked.
 */
internal fun upstreamLabel(upstream: String): String = when {
    upstream.contains("://") -> upstream.substringAfter("://").substringBefore('/')
    upstream.indexOf('/') in 0 until upstream.length - 1 -> upstream.substringAfter('/')
    else -> upstream
}

@Composable
private fun DnsRecordCard(
    record: MihomoDnsRecord,
    timeStyle: SimpleDateFormat,
    modifier: Modifier = Modifier,
) {
    AsteriskExpressiveCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = timeStyle.format(Date(record.timestampMillis)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AsteriskInfoChip(text = record.recordType)
                when (record.route) {
                    MihomoDnsRoute.Proxy -> AsteriskInfoChip(
                        text = stringResource(R.string.monitor_dns_route_proxy),
                        tone = AsteriskChipTone.Secondary,
                    )
                    MihomoDnsRoute.Direct -> AsteriskInfoChip(
                        text = stringResource(R.string.monitor_dns_route_direct),
                    )
                    MihomoDnsRoute.Unknown -> Unit
                }
                if (record.cached) {
                    AsteriskInfoChip(text = stringResource(R.string.monitor_dns_cached))
                }
                if (record.failed) {
                    AsteriskInfoChip(
                        text = stringResource(R.string.monitor_dns_failed),
                        tone = AsteriskChipTone.Error,
                    )
                }
            }
            Text(
                text = record.domain,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (record.addresses.isNotEmpty()) {
                Text(
                    text = record.addresses.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (record.upstream.isNotBlank()) {
                Surface(
                    shape = AsteriskShapeTokens.SmallContainer,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Text(
                        text = record.upstream,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            record.elapsedMillis?.let { elapsed ->
                Text(
                    text = stringResource(R.string.monitor_dns_elapsed, elapsed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** The label of a DNS filter, used by the filter menu. */
@Composable
internal fun dnsFilterLabel(filter: DnsRecordFilter): String = when (filter) {
    DnsRecordFilter.A -> "A"
    DnsRecordFilter.AAAA -> "AAAA"
    DnsRecordFilter.Https -> stringResource(R.string.monitor_dns_filter_https)
    DnsRecordFilter.Proxy -> stringResource(R.string.monitor_dns_route_proxy)
    DnsRecordFilter.Direct -> stringResource(R.string.monitor_dns_route_direct)
}
