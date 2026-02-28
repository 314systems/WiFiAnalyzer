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

import android.net.wifi.WifiInfo
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.vrem.wifianalyzer.MainActivity
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.main.MainConnection
import com.vrem.wifianalyzer.ui.main.MainConnectionState
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.model.WiFiConnection
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiSignal
import com.vrem.wifianalyzer.wifi.scanner.UpdateNotifier

class ConnectionView(
    private val mainActivity: MainActivity,
    private val accessPointPopup: AccessPointPopup = AccessPointPopup(),
) : UpdateNotifier {

    override fun update(wiFiData: WiFiData) {
        val mainContext = MainContext.INSTANCE
        val settings = mainContext.settings
        val connection = wiFiData.connection()
        val wiFiConnection = connection.wiFiAdditional.wiFiConnection
        val connectionViewType = settings.connectionViewType()
        val wiFiBand = settings.wiFiBand()

        val isConnectionVisible = !connectionViewType.hide && wiFiConnection.connected
        val linkSpeed = if (wiFiConnection.linkSpeed != WiFiConnection.LINK_SPEED_INVALID) {
            "${wiFiConnection.linkSpeed}${WifiInfo.LINK_SPEED_UNITS}"
        } else {
            ""
        }

        val wifiSupportText = if (wiFiBand.available()) null else mainActivity.resources.getString(wiFiBand.textResource)

        val connectionDetailContent: @Composable () -> Unit = {
            val onClick = {
                val view = mainActivity.findViewById<View>(R.id.main_connection_compose_view)
                accessPointPopup.show(view, connection)
            }
            if (connectionViewType == ConnectionViewType.COMPLETE) {
                AccessPointViewComplete(
                    wiFiDetail = connection,
                    onClick = onClick
                )
            } else {
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
                    onClick = onClick
                )
            }
        }

        val warningContent: @Composable () -> Unit = {
            val registered = mainActivity.currentNavigationMenu().registered()
            val isScanThrottleEnabled = mainContext.wiFiManagerWrapper.isScanThrottleEnabled()
            val wiFiDetailsEmpty = wiFiData.wiFiDetails.isEmpty()
            val isPermissionEnabled = mainContext.permissionService.enabled()

            WarningView(
                registered = registered,
                isScanThrottleEnabled = isScanThrottleEnabled,
                wiFiDetailsEmpty = wiFiDetailsEmpty,
                isPermissionEnabled = isPermissionEnabled
            )
        }

        val state = MainConnectionState(
            isConnectionVisible = isConnectionVisible,
            currentConnectionName = connection.wiFiIdentifier.title,
            linkSpeed = linkSpeed,
            ipAddress = wiFiConnection.ipAddress,
            wifiSupportText = wifiSupportText,
            isWifiThrottlingVisible = false,
            connectionDetailContent = connectionDetailContent,
            warningContent = warningContent
        )

        mainActivity.findViewById<ComposeView>(R.id.main_connection_compose_view).setContent {
            AppTheme {
                MainConnection(state = state)
            }
        }
    }
}
