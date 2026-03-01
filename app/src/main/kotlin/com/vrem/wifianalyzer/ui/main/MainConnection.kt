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

package com.vrem.wifianalyzer.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewCompact
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewComplete
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewData
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType
import com.vrem.wifianalyzer.wifi.accesspoint.WarningView
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiSignal

/**
 * UI State for the Main Connection section.
 */
@Immutable
data class MainConnectionState(
    val isConnectionVisible: Boolean = false,
    val connection: WiFiDetail = WiFiDetail.EMPTY,
    val connectionViewType: ConnectionViewType = ConnectionViewType.HIDE,
    val linkSpeed: String = "",
    val ipAddress: String = "",
    val wifiSupportText: String? = null,
    val isWifiThrottlingVisible: Boolean = false,
    val isScannerRegistered: Boolean = false,
    val isScanThrottleEnabled: Boolean = false,
    val wiFiDetailsEmpty: Boolean = false,
    val isPermissionEnabled: Boolean = false
)

@Composable
fun MainConnection(
    state: MainConnectionState,
    onConnectionClick: (WiFiDetail) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Connection Section
            if (state.isConnectionVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.activity_horizontal_margin))
                        .padding(top = dimensionResource(R.dimen.activity_vertical_margin))
                ) {
                    // Title: Current Connection
                    Text(
                        text = stringResource(R.string.current_connection),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // 2. AccessPointView (Complete or Compact)
                    ConnectionDetail(
                        connection = state.connection,
                        connectionViewType = state.connectionViewType,
                        onConnectionClick = onConnectionClick,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 3. Link Speed and IP Address
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.linkSpeed.isNotEmpty()) {
                            Text(
                                text = state.linkSpeed,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .padding(start = dimensionResource(R.dimen.list_view_item_horizontal_tab))
                                    .padding(end = dimensionResource(R.dimen.list_view_item_horizontal_spacer))
                            )
                        }

                        Text(
                            text = state.ipAddress,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(end = dimensionResource(R.dimen.list_view_item_horizontal_spacer))
                        )
                    }

                    // 4. Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 5. WiFi Support Info
            state.wifiSupportText?.let {
                WifiInfoItem(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 6. WiFi Throttling Warning
            if (state.isWifiThrottlingVisible) {
                WifiInfoItem(
                    text = stringResource(R.string.wifi_throttling_on),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 7. Warning Content (e.g. Scanner Message, Permission Warning)
            WarningView(
                registered = state.isScannerRegistered,
                isScanThrottleEnabled = state.isScanThrottleEnabled,
                wiFiDetailsEmpty = state.wiFiDetailsEmpty,
                isPermissionEnabled = state.isPermissionEnabled
            )
        }
    }
}

@Composable
private fun ConnectionDetail(
    connection: WiFiDetail,
    connectionViewType: ConnectionViewType,
    onConnectionClick: (WiFiDetail) -> Unit,
    modifier: Modifier = Modifier
) {
    if (connectionViewType == ConnectionViewType.COMPLETE) {
        AccessPointViewComplete(
            wiFiDetail = connection,
            modifier = modifier,
            onClick = { onConnectionClick(connection) }
        )
    } else if (connectionViewType == ConnectionViewType.COMPACT) {
        val signal = connection.wiFiSignal
        val data = AccessPointViewData(
            ssid = connection.wiFiIdentifier.title,
            level = "${signal.level} dBm",
            channel = signal.channelDisplay(),
            primaryFrequency = "${signal.primaryFrequency}${WiFiSignal.FREQUENCY_UNITS}",
            distanceText = signal.distance,
            isGrouped = false,
            security = connection.wiFiSecurity.security.name,
            showGroupIndicator = false
        )
        AccessPointViewCompact(
            data = data,
            modifier = modifier,
            onClick = { onConnectionClick(connection) }
        )
    }
}

@Composable
private fun WifiInfoItem(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.activity_horizontal_margin),
                vertical = dimensionResource(R.dimen.activity_vertical_half_margin)
            )
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun MainConnectionPreview() {
    AppTheme {
        MainConnection(
            state = MainConnectionState(
                isConnectionVisible = true,
                linkSpeed = "999Mbps",
                ipAddress = "192.168.111.222",
                wifiSupportText = "6GHz Support",
                isWifiThrottlingVisible = true
            ),
            onConnectionClick = {}
        )
    }
}
