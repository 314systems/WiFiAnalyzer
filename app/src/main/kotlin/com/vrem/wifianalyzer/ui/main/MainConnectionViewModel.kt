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

import android.app.Application
import android.net.wifi.WifiInfo
import androidx.lifecycle.AndroidViewModel
import com.vrem.util.findOne
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.WiFiAnalyzerApplication
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.WiFiConnection
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.scanner.UpdateNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.enums.EnumEntries

class MainConnectionViewModel(application: Application) : AndroidViewModel(application),
    UpdateNotifier {
    private val app = application as WiFiAnalyzerApplication
    private val repository = Repository(application)
    private val permissionService = PermissionService(application)
    private val _state = MutableStateFlow(MainConnectionState())
    val state: StateFlow<MainConnectionState> = _state.asStateFlow()

    private val _selectedWiFiDetail = MutableStateFlow<WiFiDetail?>(null)
    val selectedWiFiDetail: StateFlow<WiFiDetail?> = _selectedWiFiDetail.asStateFlow()

    init {
        if (app.isScannerServiceInitialized) {
            app.scannerService.register(this)
        }
    }

    override fun onCleared() {
        if (app.isScannerServiceInitialized) {
            app.scannerService.unregister(this)
        }
        super.onCleared()
    }

    fun onDismissPopup() {
        _selectedWiFiDetail.value = null
    }

    fun onSelectedWiFiDetail(wiFiDetail: WiFiDetail) {
        _selectedWiFiDetail.value = wiFiDetail
    }

    override fun update(wiFiData: WiFiData) {
        val connection = wiFiData.connection()
        val wiFiConnection = connection.wiFiAdditional.wiFiConnection
        val connectionViewType = settingsFind(
            ConnectionViewType.entries,
            R.string.connection_view_key,
            ConnectionViewType.COMPACT
        )
        val wiFiBand = settingsFind(WiFiBand.entries, R.string.wifi_band_key, WiFiBand.GHZ2)

        val isConnectionVisible = !connectionViewType.hide && wiFiConnection.connected
        val linkSpeed = if (wiFiConnection.linkSpeed != WiFiConnection.LINK_SPEED_INVALID) {
            "${wiFiConnection.linkSpeed}${WifiInfo.LINK_SPEED_UNITS}"
        } else {
            ""
        }

        val wifiSupportText =
            if (wiFiBand.available(app)) null else app.resources.getString(wiFiBand.textResource)

        val selectedMenu = settingsFind(
            NavigationMenu.entries,
            R.string.selected_menu_key,
            NavigationMenu.ACCESS_POINTS
        )
        val isScannerRegistered = selectedMenu.registered()
        val isScanThrottleEnabled = app.wiFiManagerWrapper.isScanThrottleEnabled()
        val wiFiDetailsEmpty = wiFiData.wiFiDetails.isEmpty()
        val isPermissionEnabled = permissionService.enabled()

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

    private fun <T : Enum<T>> settingsFind(
        values: EnumEntries<T>,
        key: Int,
        defaultValue: T,
    ): T {
        val value = repository.stringAsInteger(key, defaultValue.ordinal)
        return findOne(values, value, defaultValue)
    }
}
