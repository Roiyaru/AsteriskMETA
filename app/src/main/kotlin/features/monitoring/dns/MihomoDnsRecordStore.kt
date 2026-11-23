// Copyright 2026, AsteriskMETA contributors
// SPDX-License-Identifier: GPL-3.0

package features.monitoring.dns

import android.content.Context
import engine.mihomo.runtime.MihomoDnsRecord
import features.logs.AndroidAppLogger
import java.io.File

/**
 * The DNS records the core observed during this session.
 *
 * The core runs in the service process while the monitor lives in the app process, so
 * the records travel through a file that both processes can reach.
 */
internal object MihomoDnsRecordStore {
    private const val LogTag = "MihomoDnsRecordStore"
    private const val MaxRecords = 500

    /**
     * Appends the records to the shared file.
     *
     * The service process owns the file, so the app process never writes to it and a
     * restart of either process cannot interleave two writers.
     */
    fun append(context: Context, records: List<MihomoDnsRecord>) {
        if (records.isEmpty()) return
        val file = context.androidDnsRecordFile()
        runCatching {
            file.parentFile?.mkdirs()
            file.appendText(records.joinToString(separator = "\n", postfix = "\n") { it.encode() })
            trim(file)
        }.onFailure { error ->
            AndroidAppLogger.warn(LogTag, "Failed to append DNS records", error)
        }
    }

    /** Drops every stored record, for example when the user clears the monitor. */
    fun clear(context: Context) {
        val file = context.androidDnsRecordFile()
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText("")
        }.onFailure { error ->
            AndroidAppLogger.warn(LogTag, "Failed to clear DNS records", error)
        }
    }

    /** Reads the stored records back. */
    fun read(context: Context): List<MihomoDnsRecord> {
        val file = context.androidDnsRecordFile()
        return runCatching {
            if (!file.exists() || !file.isFile) {
                emptyList()
            } else {
                file.readLines()
                    .mapNotNull { line -> decode(line) }
                    .takeLast(MaxRecords)
            }
        }.onFailure { error ->
            AndroidAppLogger.warn(LogTag, "Failed to read DNS records", error)
        }.getOrDefault(emptyList())
    }

    private fun trim(file: File) {
        val lines = file.readLines()
        if (lines.size <= MaxRecords) return
        file.writeText(lines.takeLast(MaxRecords).joinToString(separator = "\n", postfix = "\n"))
    }
}

private fun MihomoDnsRecord.encode(): String = listOf(
    timestampMillis.toString(),
    domain.replace("|", "/"),
    recordType.replace("|", "/"),
    addresses.joinToString(",") { it.replace("|", "/") },
    upstream.replace("|", "/"),
    elapsedMillis?.toString().orEmpty(),
    cached.toString(),
    failed.toString(),
).joinToString("|")

private fun decode(line: String): MihomoDnsRecord? {
    val parts = line.split("|")
    if (parts.size < 8) return null
    val timestampMillis = parts[0].toLongOrNull() ?: return null
    val domain = parts[1]
    if (domain.isEmpty()) return null
    return MihomoDnsRecord(
        timestampMillis = timestampMillis,
        domain = domain,
        recordType = parts[2],
        addresses = parts[3].split(',').map { it.trim() }.filter { it.isNotEmpty() },
        upstream = parts[4],
        elapsedMillis = parts[5].toLongOrNull(),
        cached = parts[6].toBoolean(),
        failed = parts[7].toBoolean(),
    )
}

private fun Context.androidDnsRecordFile(): File =
    File(File(filesDir, AndroidDnsRecordDirectoryPath), AndroidDnsRecordFileName)

private const val AndroidDnsRecordDirectoryPath = "clash/dns"
private const val AndroidDnsRecordFileName = "records.log"
