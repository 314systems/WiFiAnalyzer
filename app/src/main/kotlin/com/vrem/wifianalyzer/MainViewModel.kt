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
package com.vrem.wifianalyzer

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.vrem.util.SPACE_SEPARATOR
import com.vrem.util.specialTrim
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.Security
import com.vrem.wifianalyzer.wifi.model.Strength

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as WiFiAnalyzerApplication
    private val settings = app.settings
    private val filtersAdapter = app.filtersAdapter
    private val permissionService = PermissionService(application)

    var currentMenu by mutableStateOf(settings.selectedMenu())
        private set
    var isScannerRunning by mutableStateOf(false)
        private set
    var isFilterActive by mutableStateOf(false)
        private set
    var currentWiFiBand by mutableStateOf(settings.wiFiBand())
        private set
    var showFilterDialog by mutableStateOf(false)
        set
    var themeStyle by mutableStateOf(settings.themeStyle())
        private set
    var languageLocale by mutableStateOf(settings.languageLocale())
        private set

    fun update() {
        if (app.isScannerServiceInitialized) {
            app.scannerService.update()
            isScannerRunning = app.scannerService.running()
        }
        isFilterActive = filtersAdapter.isActive(currentMenu == NavigationMenu.ACCESS_POINTS)
        currentWiFiBand = settings.wiFiBand()
        themeStyle = settings.themeStyle()
        languageLocale = settings.languageLocale()
    }

    fun shouldReload(): Boolean {
        val newTheme = settings.themeStyle()
        val newLocale = settings.languageLocale()
        return (themeStyle != newTheme || languageLocale != newLocale).also {
            if (it) {
                themeStyle = newTheme
                languageLocale = newLocale
            }
        }
    }

    fun selectMenu(menu: NavigationMenu) {
        currentMenu = menu
        settings.saveSelectedMenu(menu)
        update()
    }

    fun handleBack(onFinish: () -> Unit) {
        val selectedMenu = settings.selectedMenu()
        if (currentMenu == selectedMenu) {
            onFinish()
        } else {
            selectMenu(selectedMenu)
        }
    }

    fun toggleScanner() {
        if (app.isScannerServiceInitialized) {
            app.scannerService.toggle()
            update()
        }
    }

    fun updateWiFiBand(band: WiFiBand) {
        settings.wiFiBand(band)
        update()
    }

    fun applyFilters(
        ssid: String,
        bands: Set<WiFiBand>,
        strengths: Set<Strength>,
        securities: Set<Security>,
    ) {
        val isAccessPoints = currentMenu == NavigationMenu.ACCESS_POINTS
        with(filtersAdapter) {
            ssidAdapter().selections = ssid.specialTrim().split(String.SPACE_SEPARATOR).toSet()
            wiFiBandAdapter().selections = bands
            strengthAdapter().selections = strengths
            securityAdapter().selections = securities
            save(isAccessPoints)
        }
        update()
        showFilterDialog = false
    }

    fun resetFilters() {
        filtersAdapter.reset(currentMenu == NavigationMenu.ACCESS_POINTS)
        update()
        showFilterDialog = false
    }

    fun closeFilters() {
        filtersAdapter.reload()
        showFilterDialog = false
    }

    fun pauseScanner() {
        if (app.isScannerServiceInitialized) {
            app.scannerService.pause()
            update()
        }
    }

    fun resumeScanner() {
        if (app.isScannerServiceInitialized) {
            if (permissionService.permissionGranted()) {
                app.scannerService.resume()
            }
            update()
        }
    }

    fun stopScanner() {
        if (app.isScannerServiceInitialized) {
            app.scannerService.stop()
            update()
        }
    }
}
