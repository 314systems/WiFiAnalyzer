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

package com.vrem.wifianalyzer.about

import android.app.Application
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.AndroidViewModel
import com.vrem.util.EMPTY
import com.vrem.util.packageInfo
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AboutViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(createUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    private fun createUiState(): AboutUiState {
        val context = getApplication<Application>()
        val mainContext = MainContext.INSTANCE
        val wiFiManagerWrapper = mainContext.wiFiManagerWrapper
        val configuration = mainContext.configuration

        return AboutUiState(
            packageName = context.packageName,
            versionInfo = getVersionInfo(context, configuration),
            copyright = getCopyright(context),
            deviceInfo = getDeviceInfo(),
            wiFiThrottlingEnabled = wiFiManagerWrapper.isScanThrottleEnabled(),
            is5GHzBandSupported = wiFiManagerWrapper.is5GHzBandSupported(),
            is6GHzBandSupported = wiFiManagerWrapper.is6GHzBandSupported(),
        )
    }

    private fun getDeviceInfo(): String = "${Build.MANUFACTURER} - ${Build.BRAND} - ${Build.MODEL}"

    private fun getCopyright(context: Application): String {
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        return "${context.getString(R.string.app_copyright)}$year"
    }

    private fun getVersionInfo(
        context: Application,
        configuration: com.vrem.wifianalyzer.Configuration
    ): String {
        val appVersion = runCatching {
            val packageInfo = context.packageInfo()
            "${packageInfo.versionName} - ${PackageInfoCompat.getLongVersionCode(packageInfo)}"
        }.getOrDefault(String.EMPTY)

        val suffix = buildString {
            if (configuration.sizeAvailable) append("S")
            if (configuration.largeScreen) append("L")
        }

        return "$appVersion$suffix (${Build.VERSION.RELEASE}-${Build.VERSION.SDK_INT})"
    }
}
