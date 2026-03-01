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
import androidx.lifecycle.viewModelScope
import com.vrem.util.SPACE_SEPARATOR
import com.vrem.util.defaultLanguageTag
import com.vrem.util.findByLanguageTag
import com.vrem.util.findOne
import com.vrem.util.specialTrim
import com.vrem.wifianalyzer.navigation.MAIN_NAVIGATION
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.Security
import com.vrem.wifianalyzer.wifi.model.Strength
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import kotlin.enums.EnumEntries

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as WiFiAnalyzerApplication
    private val repository = app.repository
    private val filtersAdapter = app.filtersAdapter
    private val permissionService = PermissionService(application)

    private val _currentMenu = MutableStateFlow(readSelectedMenu())
    val currentMenu: StateFlow<NavigationMenu> = _currentMenu.asStateFlow()

    private val _isScannerRunning = MutableStateFlow(false)
    val isScannerRunning: StateFlow<Boolean> = _isScannerRunning.asStateFlow()

    private val _isFilterActive = MutableStateFlow(false)
    val isFilterActive: StateFlow<Boolean> = _isFilterActive.asStateFlow()

    val currentWiFiBand: StateFlow<WiFiBand> = repository.preferenceChanges()
        .map { readWiFiBand() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), readWiFiBand())

    var showFilterDialog by mutableStateOf(false)

    private val _selectedWiFiDetail = MutableStateFlow<WiFiDetail?>(null)
    val selectedWiFiDetail: StateFlow<WiFiDetail?> = _selectedWiFiDetail.asStateFlow()

    val themeStyle: StateFlow<ThemeStyle> = repository.preferenceChanges()
        .map { readThemeStyle() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), readThemeStyle())

    val languageLocale: StateFlow<Locale> = repository.preferenceChanges()
        .map { readLanguageLocale() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), readLanguageLocale())

    val keepScreenOn: StateFlow<Boolean> = repository.preferenceChanges()
        .map { readKeepScreenOn() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), readKeepScreenOn())

    fun update() {
        if (app.isScannerServiceInitialized) {
            app.scannerService.update()
            _isScannerRunning.value = app.scannerService.running()
        }
        _isFilterActive.value =
            filtersAdapter.isActive(currentMenu.value == NavigationMenu.ACCESS_POINTS)
    }

    fun selectMenu(menu: NavigationMenu) {
        _currentMenu.value = menu
        saveSelectedMenu(menu)
        update()
    }

    fun showWiFiDetail(wiFiDetail: WiFiDetail) {
        _selectedWiFiDetail.value = wiFiDetail
    }

    fun closeWiFiDetail() {
        _selectedWiFiDetail.value = null
    }

    fun handleBack(onFinish: () -> Unit) {
        if (_selectedWiFiDetail.value != null) {
            closeWiFiDetail()
            return
        }
        val selectedMenu = readSelectedMenu()
        if (currentMenu.value == selectedMenu) {
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
        repository.save(R.string.wifi_band_key, band.ordinal)
    }

    fun applyFilters(
        ssid: String,
        bands: Set<WiFiBand>,
        strengths: Set<Strength>,
        securities: Set<Security>,
    ) {
        val isAccessPoints = currentMenu.value == NavigationMenu.ACCESS_POINTS
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
        filtersAdapter.reset(currentMenu.value == NavigationMenu.ACCESS_POINTS)
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

    // region Getters/Setters (Logic moved from Settings.kt)
    private fun readSelectedMenu(): NavigationMenu =
        settingsFind(
            NavigationMenu.entries,
            R.string.selected_menu_key,
            NavigationMenu.ACCESS_POINTS
        )

    private fun saveSelectedMenu(navigationMenu: NavigationMenu) {
        if (MAIN_NAVIGATION.contains(navigationMenu)) {
            repository.save(R.string.selected_menu_key, navigationMenu.ordinal)
        }
    }

    private fun readWiFiBand(): WiFiBand =
        settingsFind(WiFiBand.entries, R.string.wifi_band_key, WiFiBand.GHZ2)

    private fun readThemeStyle(): ThemeStyle =
        settingsFind(ThemeStyle.entries, R.string.theme_key, ThemeStyle.DARK)

    private fun readLanguageLocale(): Locale {
        val languageTag = repository.string(R.string.language_key, defaultLanguageTag())
        return findByLanguageTag(languageTag)
    }

    private fun readKeepScreenOn(): Boolean =
        repository.boolean(
            R.string.keep_screen_on_key,
            repository.resourceBoolean(R.bool.keep_screen_on_default)
        )

    private fun <T : Enum<T>> settingsFind(
        values: EnumEntries<T>,
        key: Int,
        defaultValue: T,
    ): T {
        val value = repository.stringAsInteger(key, defaultValue.ordinal)
        return findOne(values, value, defaultValue)
    }
    // endregion
}
