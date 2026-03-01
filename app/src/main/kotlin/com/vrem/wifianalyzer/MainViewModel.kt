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
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
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
import java.util.Locale
import kotlin.enums.EnumEntries

class MainViewModel(application: Application) :
    AndroidViewModel(application),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val app = application as WiFiAnalyzerApplication
    private val repository = app.repository
    private val filtersAdapter = app.filtersAdapter
    private val permissionService = PermissionService(application)

    var currentMenu by mutableStateOf(readSelectedMenu())
        private set
    var isScannerRunning by mutableStateOf(false)
        private set
    var isFilterActive by mutableStateOf(false)
        private set
    var currentWiFiBand by mutableStateOf(readWiFiBand())
        private set
    var showFilterDialog by mutableStateOf(false)
        set
    var themeStyle by mutableStateOf(readThemeStyle())
        private set
    var languageLocale by mutableStateOf(readLanguageLocale())
        private set
    var keepScreenOn by mutableStateOf(readKeepScreenOn())
        private set

    init {
        repository.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCleared() {
        repository.unregisterOnSharedPreferenceChangeListener(this)
        super.onCleared()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        update()
    }

    fun update() {
        if (app.isScannerServiceInitialized) {
            app.scannerService.update()
            isScannerRunning = app.scannerService.running()
        }
        isFilterActive = filtersAdapter.isActive(currentMenu == NavigationMenu.ACCESS_POINTS)
        currentWiFiBand = readWiFiBand()
        themeStyle = readThemeStyle()
        languageLocale = readLanguageLocale()
        keepScreenOn = readKeepScreenOn()
    }

    fun shouldReload(): Boolean {
        val newTheme = readThemeStyle()
        val newLocale = readLanguageLocale()
        return themeStyle != newTheme || languageLocale != newLocale
    }

    fun selectMenu(menu: NavigationMenu) {
        currentMenu = menu
        saveSelectedMenu(menu)
        update()
    }

    fun handleBack(onFinish: () -> Unit) {
        val selectedMenu = readSelectedMenu()
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
        repository.save(R.string.wifi_band_key, band.ordinal)
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
