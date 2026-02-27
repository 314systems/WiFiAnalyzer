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

import androidx.compose.ui.platform.ComposeView
import com.vrem.wifianalyzer.MainActivity
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.model.WiFiData

class WarningView(
    private val mainActivity: MainActivity,
) {
    fun update(wiFiData: WiFiData): Boolean {
        val registered = mainActivity.currentNavigationMenu().registered()
        val mainContext = MainContext.INSTANCE
        
        val isScanThrottleEnabled = mainContext.wiFiManagerWrapper.isScanThrottleEnabled()
        val wiFiDetailsEmpty = wiFiData.wiFiDetails.isEmpty()
        val isPermissionEnabled = mainContext.permissionService.enabled()
        
        val noData = registered && wiFiDetailsEmpty
        val noLocation = registered && !isPermissionEnabled
        val warning = noData || noLocation

        mainActivity.findViewById<ComposeView>(R.id.warning_compose_view).setContent {
            AppTheme {
                WarningView(
                    registered = registered,
                    isScanThrottleEnabled = isScanThrottleEnabled,
                    wiFiDetailsEmpty = wiFiDetailsEmpty,
                    isPermissionEnabled = isPermissionEnabled
                )
            }
        }
        return warning
    }
}