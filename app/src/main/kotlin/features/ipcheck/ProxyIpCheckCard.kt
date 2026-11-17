// Copyright 2026, AsteriskMETA contributors
// SPDX-License-Identifier: GPL-3.0

package features.ipcheck

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.LocalAppServices
import app.R
import kotlinx.coroutines.launch
import ui.components.AsteriskDropdownAnchor
import ui.components.AsteriskDropdownMenuItem
import ui.components.AsteriskPageCard

/**
 * The card that shows the addresses of a proxy group.
 *
 * The card offers the groups as a dropdown, so a configuration with many groups can be checked
 * group by group, and it shows the two addresses of the checked group: the address the tunnel
 * starts at and the address it leaves with.
 */
@Composable
internal fun ProxyIpCheckCard(
    state: ProxyIpCheckState,
    onSelectGroup: (String) -> Unit,
    onStartCheck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AsteriskPageCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProxyIpCheckHeader(checking = state.checking, hasResult = state.hasResult)
            ProxyIpCheckGroupSelector(
                groups = state.groups.map { group -> group.name },
                selectedGroup = state.effectiveGroup(),
                enabled = state.groups.isNotEmpty() && !state.checking,
                onSelectGroup = onSelectGroup,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ProxyIpCheckEndpointRow(
                entry = state.result.entry,
                exit = state.result.exit,
            )
            ProxyIpCheckAction(
                checking = state.checking,
                enabled = state.groups.isNotEmpty(),
                onStartCheck = onStartCheck,
            )
        }
    }
}

/**
 * The title row with the state of the last check.
 */
@Composable
private fun ProxyIpCheckHeader(
    checking: Boolean,
    hasResult: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_proxy_ip_check_title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(12.dp))
        ProxyIpCheckStatusBadge(checking = checking, hasResult = hasResult)
    }
}

/**
 * The badge that states what the card knows.
 */
@Composable
private fun ProxyIpCheckStatusBadge(
    checking: Boolean,
    hasResult: Boolean,
) {
    val text = when {
        checking -> stringResource(R.string.home_proxy_ip_check_checking)
        hasResult -> stringResource(R.string.home_proxy_ip_check_done)
        else -> stringResource(R.string.home_proxy_ip_check_pending)
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

/**
 * The dropdown that names the group the check runs against.
 */
@Composable
private fun ProxyIpCheckGroupSelector(
    groups: List<String>,
    selectedGroup: String,
    enabled: Boolean,
    onSelectGroup: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.home_proxy_ip_check_strategy),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(enabled = enabled && groups.isNotEmpty(), role = Role.DropdownList) {
                    expanded = !expanded
                },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedGroup.ifBlank { stringResource(R.string.home_proxy_ip_check_no_group) },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AsteriskDropdownAnchor(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    menuModifier = Modifier.widthIn(min = 180.dp, max = 280.dp),
                ) {
                    groups.forEach { group ->
                        AsteriskDropdownMenuItem(
                            text = group,
                            selected = group == selectedGroup,
                            onClick = {
                                expanded = false
                                onSelectGroup(group)
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * The two addresses of the checked group, side by side.
 */
@Composable
private fun ProxyIpCheckEndpointRow(
    entry: IpCheckEndpoint,
    exit: IpCheckEndpoint,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProxyIpCheckEndpointColumn(
            title = stringResource(R.string.home_proxy_ip_check_entry),
            endpoint = entry,
            modifier = Modifier.weight(1f),
        )
        ProxyIpCheckEndpointColumn(
            title = stringResource(R.string.home_proxy_ip_check_exit),
            endpoint = exit,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * One address and the network it belongs to.
 *
 * Every value is on its own line and copies itself when it is tapped, so a user can take the
 * address, the province, the autonomous system and the organization one at a time.
 */
@Composable
private fun ProxyIpCheckEndpointColumn(
    title: String,
    endpoint: IpCheckEndpoint,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        if (endpoint.hasAddress) {
            ProxyIpCheckAddressValue(endpoint.address)
            ProxyIpCheckDetailValue(endpoint.province)
            ProxyIpCheckDetailValue(endpoint.asn)
            ProxyIpCheckDetailValue(endpoint.organization)
        } else {
            Text(
                text = stringResource(R.string.home_proxy_ip_check_pending_value),
                style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.home_proxy_ip_check_unknown_network),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * The address of the endpoint, which copies itself when it is tapped.
 */
@Composable
private fun ProxyIpCheckAddressValue(address: String) {
    ProxyIpCheckCopyableValue(
        text = address,
        style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/**
 * One line of the network an address belongs to, which copies itself when it is tapped. An empty
 * part of the answer takes no room, so a network without an autonomous system does not leave a
 * blank line behind.
 */
@Composable
private fun ProxyIpCheckDetailValue(value: String) {
    if (value.isBlank()) return
    ProxyIpCheckCopyableValue(
        text = value,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * A value that copies itself to the clipboard when it is tapped, and says so with a toast.
 *
 * The clipboard is the platform one and the toast is the platform toast, so the value lands
 * where every other app expects it and the confirmation looks like the rest of the system.
 */
@Composable
private fun ProxyIpCheckCopyableValue(
    text: String,
    style: TextStyle,
    color: Color,
) {
    val context = LocalContext.current
    val services = LocalAppServices.current
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.home_proxy_ip_check_copied, text)
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(role = Role.Button) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.setPrimaryClip(ClipData.newPlainText(IpCheckClipLabel, text))
                scope.launch { services.tipNotifier.show(copiedMessage) }
            }
            .padding(vertical = 3.dp),
        style = style,
        color = color,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

/** The label the copied value carries, which is what a clipboard history reads. */
private const val IpCheckClipLabel = "AsteriskMETA proxy IP check"

/**
 * The button that starts the check.
 */
@Composable
private fun ProxyIpCheckAction(
    checking: Boolean,
    enabled: Boolean,
    onStartCheck: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = enabled && !checking, onClick = onStartCheck),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (checking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(R.string.home_proxy_ip_check_start),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
                    maxLines = 1,
                )
            }
        }
    }
}
