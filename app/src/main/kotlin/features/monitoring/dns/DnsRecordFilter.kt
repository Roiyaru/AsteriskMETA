// Copyright 2026, AsteriskMETA contributors
// SPDX-License-Identifier: GPL-3.0

package features.monitoring.dns

import engine.mihomo.runtime.MihomoDnsRecord
import engine.mihomo.runtime.MihomoDnsRoute

/**
 * The filters the DNS monitor offers.
 *
 * The list follows the groups the monitor shows on a record: the queried type, the
 * upstream the core asked and whether the answer came from the core's own cache.
 */
internal enum class DnsRecordFilter {
    A,
    AAAA,
    HTTPS,
    Other,
    Cached,
    Failed,
    Proxy,
    Direct,
}

/** Whether the record passes the selected filters. */
internal fun MihomoDnsRecord.matchesDnsFilters(filters: Set<DnsRecordFilter>): Boolean {
    if (filters.isEmpty()) return true
    return filters.any { filter ->
        when (filter) {
            DnsRecordFilter.A -> recordType.equals("A", ignoreCase = true)
            DnsRecordFilter.AAAA -> recordType.equals("AAAA", ignoreCase = true)
            DnsRecordFilter.HTTPS -> recordType.equals("HTTPS", ignoreCase = true)
            DnsRecordFilter.Other -> !matchesKnownType()
            DnsRecordFilter.Cached -> cached
            DnsRecordFilter.Failed -> failed
            DnsRecordFilter.Proxy -> route == MihomoDnsRoute.Proxy
            DnsRecordFilter.Direct -> route == MihomoDnsRoute.Direct
        }
    }
}

private fun MihomoDnsRecord.matchesKnownType(): Boolean =
    recordType.equals("A", ignoreCase = true) ||
        recordType.equals("AAAA", ignoreCase = true) ||
        recordType.equals("HTTPS", ignoreCase = true)

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
