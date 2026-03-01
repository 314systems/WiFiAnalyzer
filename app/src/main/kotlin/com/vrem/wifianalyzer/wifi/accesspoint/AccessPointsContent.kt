/*
 * WiFiAnalyzer
 * Copyright (C) 2015 - 2026 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.vrem.wifianalyzer.wifi.accesspoint

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrem.wifianalyzer.WiFiAnalyzerApplication
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiIdentifier
import com.vrem.wifianalyzer.wifi.model.WiFiSignal

@Composable
fun AccessPointsContent(
    viewModel: AccessPointsViewModel = viewModel()
) {
    val context = LocalContext.current
    val app = context.applicationContext as WiFiAnalyzerApplication
    val wiFiDetails by viewModel.wiFiDetails.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val expandedGroups by viewModel.expandedGroups.collectAsStateWithLifecycle()

    val settings = app.settings
    val viewType = settings.accessPointView()
    val groupBy = settings.groupBy()

    var selectedWiFiDetail by remember { mutableStateOf<WiFiDetail?>(null) }

    if (selectedWiFiDetail != null) {
        AccessPointAlertDialog(
            wiFiDetail = selectedWiFiDetail!!,
            onDismiss = { selectedWiFiDetail = null }
        )
    }

    AccessPointsContent(
        wiFiDetails = wiFiDetails,
        isRefreshing = isRefreshing,
        expandedGroups = expandedGroups,
        viewType = viewType,
        groupNameProvider = { groupBy.group(it) },
        onRefresh = { viewModel.refresh() },
        onToggleGroup = { viewModel.toggleGroup(it) },
        onShowPopup = { selectedWiFiDetail = it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessPointsContent(
    wiFiDetails: List<WiFiDetail>,
    isRefreshing: Boolean,
    expandedGroups: Set<String>,
    viewType: AccessPointViewType,
    groupNameProvider: (WiFiDetail) -> String,
    onRefresh: () -> Unit,
    onToggleGroup: (WiFiDetail) -> Unit,
    onShowPopup: (WiFiDetail) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            wiFiDetails.forEach { parent ->
                val groupName = groupNameProvider(parent)
                val isExpanded = expandedGroups.contains(groupName)
                item(key = parent.wiFiIdentifier.bssid) {
                    AccessPointView(
                        wiFiDetail = parent,
                        viewType = viewType,
                        isGroup = parent.children.isNotEmpty(),
                        isExpanded = isExpanded,
                        onClick = {
                            if (parent.children.isNotEmpty()) {
                                onToggleGroup(parent)
                            } else {
                                onShowPopup(parent)
                            }
                        }
                    )
                }
                if (isExpanded) {
                    items(parent.children, key = { it.wiFiIdentifier.bssid }) { child ->
                        AccessPointView(
                            wiFiDetail = child,
                            viewType = viewType,
                            isChild = true,
                            onClick = { onShowPopup(child) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessPointView(
    wiFiDetail: WiFiDetail,
    viewType: AccessPointViewType,
    onClick: () -> Unit,
    isChild: Boolean = false,
    isGroup: Boolean = false,
    isExpanded: Boolean = false,
) {
    if (viewType == AccessPointViewType.COMPLETE) {
        AccessPointViewComplete(
            wiFiDetail = wiFiDetail,
            onClick = onClick,
            isChild = isChild,
            isGroup = isGroup,
            isExpanded = isExpanded,
        )
    } else {
        val signal = wiFiDetail.wiFiSignal
        val data =
            AccessPointViewData(
                ssid = wiFiDetail.wiFiIdentifier.title,
                level = "${signal.level}dBm",
                channel = signal.channelDisplay(),
                primaryFrequency = "${signal.primaryFrequency}${WiFiSignal.FREQUENCY_UNITS}",
                distanceText = signal.distance,
                isGrouped = isChild,
                security = wiFiDetail.wiFiSecurity.security.name,
                showGroupIndicator = isGroup,
            )
        AccessPointViewCompact(
            data = data,
            onClick = onClick
        )
    }
}

@Preview(showBackground = true, name = "Complete View")
@Composable
fun AccessPointsContentCompletePreview() {
    AccessPointsPreviewContent(AccessPointViewType.COMPLETE)
}

@Preview(showBackground = true, name = "Compact View")
@Composable
fun AccessPointsContentCompactPreview() {
    AccessPointsPreviewContent(AccessPointViewType.COMPACT)
}

@Composable
private fun AccessPointsPreviewContent(viewType: AccessPointViewType) {
    val details = listOf(
        WiFiDetail(
            wiFiIdentifier = WiFiIdentifier(ssidRaw = "Network 1", bssid = "00:11:22:33:44:55"),
            children = listOf(
                WiFiDetail(wiFiIdentifier = WiFiIdentifier(ssidRaw = "Network 1 Child", bssid = "00:11:22:33:44:56"))
            )
        ),
        WiFiDetail(
            wiFiIdentifier = WiFiIdentifier(ssidRaw = "Network 2", bssid = "66:77:88:99:AA:BB")
        )
    )
    AppTheme {
        Surface {
            AccessPointsContent(
                wiFiDetails = details,
                isRefreshing = false,
                expandedGroups = setOf("Network 1"),
                viewType = viewType,
                groupNameProvider = { it.wiFiIdentifier.ssidRaw },
                onRefresh = {},
                onToggleGroup = {},
                onShowPopup = {}
            )
        }
    }
}
