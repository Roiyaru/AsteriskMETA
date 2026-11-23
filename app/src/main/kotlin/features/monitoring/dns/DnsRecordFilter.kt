// Copyright 2026, AsteriskMETA contributors
// SPDX-License-Identifier: GPL-3.0

package features.monitoring.dns

import engine.mihomo.runtime.MihomoDnsRecord
import engine.mihomo.runtime.MihomoDnsRoute

/**
 * The filters the DNS monitor offers.
 *
 * The filters fall into two dimensions. The source says how the core reached the
 * upstream: directly or through the proxy. The type says what the core asked for. A
 * record passes when it matches the source filters and the type filters, and the
 * filters within one dimension accept a record that matches any of them. A cached
 * answer is not a source, so the monitor keeps the cache on the record itself.
 */
internal enum class DnsRecordFilter {
    /** The core reached the upstream directly. */
    Direct,

    /** The core reached the upstream through the proxy. */
    Proxy,

    /** An address lookup for IPv4. */
    A,

    /** An address lookup for IPv6. */
    AAAA,

    /** A service binding lookup, which the core reports as `HTTPS` or `SVCB`. */
    Https,
}

/** The filters that say how the core reached the upstream. */
internal val DnsSourceFilter: List<DnsRecordFilter> = listOf(
    DnsRecordFilter.Direct,
    DnsRecordFilter.Proxy,
)

/** The filters that say what the core asked for. */
internal val DnsTypeFilter: List<DnsRecordFilter> = listOf(
    DnsRecordFilter.A,
    DnsRecordFilter.AAAA,
    DnsRecordFilter.Https,
)

/** The filters that say how the core reached the upstream. */
internal val DnsRecordFilter.isSource: Boolean
    get() = this == DnsRecordFilter.Direct || this == DnsRecordFilter.Proxy

/** Whether the record passes the selected filters. */
internal fun MihomoDnsRecord.matchesDnsFilters(filters: Set<DnsRecordFilter>): Boolean {
    if (filters.isEmpty()) return true
    // The dimensions narrow the list together, so a record has to pass both of them.
    return matchesDnsSourceFilters(filters) && matchesDnsTypeFilters(filters)
}

private fun MihomoDnsRecord.matchesDnsSourceFilters(filters: Set<DnsRecordFilter>): Boolean {
    val sources = filters.filter { it.isSource }
    if (sources.isEmpty()) return true
    return sources.any { filter ->
        when (filter) {
            DnsRecordFilter.Direct -> route == MihomoDnsRoute.Direct
            DnsRecordFilter.Proxy -> route == MihomoDnsRoute.Proxy
            else -> false
        }
    }
}

private fun MihomoDnsRecord.matchesDnsTypeFilters(filters: Set<DnsRecordFilter>): Boolean {
    val types = filters.filterNot { it.isSource }
    if (types.isEmpty()) return true
    return types.any { filter ->
        when (filter) {
            DnsRecordFilter.A -> recordType.equals("A", ignoreCase = true)
            DnsRecordFilter.AAAA -> recordType.equals("AAAA", ignoreCase = true)
            // The core reports the service binding type under either name.
            DnsRecordFilter.Https -> recordType.equals("HTTPS", ignoreCase = true) ||
                recordType.equals("SVCB", ignoreCase = true)
            else -> false
        }
    }
}

/** Whether the record matches the search term. */
internal fun MihomoDnsRecord.matchesDnsQuery(query: String): Boolean {
    val term = query.trim()
    if (term.isEmpty()) return true
    return domain.contains(term, ignoreCase = true) ||
        recordType.contains(term, ignoreCase = true) ||
        upstream.contains(term, ignoreCase = true) ||
        addresses.any { it.contains(term, ignoreCase = true) }
}

/** Filters the records by the selected filters and the search term. */
internal fun filterDnsRecords(
    records: List<MihomoDnsRecord>,
    filters: Set<DnsRecordFilter>,
    query: String,
): List<MihomoDnsRecord> = records.filter { record ->
    record.matchesDnsFilters(filters) && record.matchesDnsQuery(query)
}
