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

import android.net.wifi.WifiInfo
import androidx.lifecycle.ViewModel
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.wifi.model.WiFiConnection
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.scanner.UpdateNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainConnectionViewModel : ViewModel(), UpdateNotifier {
    private val _state = MutableStateFlow(MainConnectionState())
    val state: StateFlow<MainConnectionState> = _state.asStateFlow()

    private val _selectedWiFiDetail = MutableStateFlow<WiFiDetail?>(null)
    val selectedWiFiDetail: StateFlow<WiFiDetail?> = _selectedWiFiDetail.asStateFlow()

    init {
        MainContext.INSTANCE.scannerService.register(this)
    }

    override fun onCleared() {
        MainContext.INSTANCE.scannerService.unregister(this)
        super.onCleared()
    }

    fun onDismissPopup() {
        _selectedWiFiDetail.value = null
    }

    fun onSelectedWiFiDetail(wiFiDetail: WiFiDetail) {
        _selectedWiFiDetail.value = wiFiDetail
    }

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

        val wifiSupportText =
            if (wiFiBand.available()) null else mainContext.resources.getString(wiFiBand.textResource)

        val isScannerRegistered = mainContext.mainActivity.currentNavigationMenu().registered()
        val isScanThrottleEnabled = mainContext.wiFiManagerWrapper.isScanThrottleEnabled()
        val wiFiDetailsEmpty = wiFiData.wiFiDetails.isEmpty()
        val isPermissionEnabled = mainContext.permissionService.enabled()

        _state.value = MainConnectionState(
            isConnectionVisible = isConnectionVisible,
            connection = connection,
            connectionViewType = connectionViewType,
            linkSpeed = linkSpeed,
            ipAddress = wiFiConnection.ipAddress,
            wifiSupportText = wifiSupportText,
            isWifiThrottlingVisible = false,
            isScannerRegistered = isScannerRegistered,
            isScanThrottleEnabled = isScanThrottleEnabled,
            wiFiDetailsEmpty = wiFiDetailsEmpty,
            isPermissionEnabled = isPermissionEnabled
        )
    }
}
