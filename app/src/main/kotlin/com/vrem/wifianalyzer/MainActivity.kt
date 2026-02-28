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

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.navigation.NavigationView
import com.vrem.util.SPACE_SEPARATOR
import com.vrem.util.createContext
import com.vrem.util.specialTrim
import com.vrem.wifianalyzer.export.Export
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.navigation.NavigationMenuControl
import com.vrem.wifianalyzer.permission.PermissionHandler
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.ui.filter.FilterDialog
import com.vrem.wifianalyzer.ui.main.MainScreen
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.scanner.ScannerService

class MainActivity :
    AppCompatActivity(),
    NavigationMenuControl,
    OnSharedPreferenceChangeListener {
    private lateinit var mainReload: MainReload
    private lateinit var settings: Settings

    var currentMenu by mutableStateOf(NavigationMenu.ACCESS_POINTS)
        private set
    var isScannerRunning by mutableStateOf(false)
        private set
    var isFilterActive by mutableStateOf(false)
        private set
    var currentWiFiBand by mutableStateOf(WiFiBand.GHZ2)
        private set
    var showFilterDialog by mutableStateOf(false)

    override fun attachBaseContext(newBase: Context) =
        super.attachBaseContext(newBase.createContext(Settings(Repository(newBase)).languageLocale()))

    override fun onCreate(savedInstanceState: Bundle?) {
        val mainContext = MainContext.INSTANCE
        mainContext.initialize(this, largeScreen)

        settings = mainContext.settings
        settings.initializeDefaultValues()
        settings.themeStyle().setTheme(this)

        mainReload = MainReload(settings)
        currentMenu = settings.selectedMenu()

        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            AppTheme {
                PermissionHandler(
                    onPermissionGranted = { update() },
                    onTerminateApp = { finish() }
                )

                MainScreen(
                    currentMenu = currentMenu,
                    isScannerRunning = isScannerRunning,
                    isFilterActive = isFilterActive,
                    currentWiFiBand = currentWiFiBand,
                    onMenuSelected = { menu ->
                        if (menu == NavigationMenu.EXPORT) {
                            export()
                        } else {
                            currentMenu = menu
                            settings.saveSelectedMenu(menu)
                            update()
                        }
                    },
                    onToggleScanner = {
                        MainContext.INSTANCE.scannerService.toggle()
                        update()
                    },
                    onFilterClick = { showFilterDialog = true },
                    onWiFiBandClick = { band ->
                        settings.wiFiBand(band)
                        update()
                    }
                )

                if (showFilterDialog) {
                    FilterDialog(
                        filtersAdapter = MainContext.INSTANCE.filtersAdapter,
                        isAccessPoints = currentMenu == NavigationMenu.ACCESS_POINTS,
                        onApply = { ssid, bands, strengths, securities ->
                            with(MainContext.INSTANCE.filtersAdapter) {
                                ssidAdapter().selections =
                                    ssid.specialTrim().split(String.SPACE_SEPARATOR).toSet()
                                wiFiBandAdapter().selections = bands
                                strengthAdapter().selections = strengths
                                securityAdapter().selections = securities
                                save()
                            }
                            update()
                            showFilterDialog = false
                        },
                        onReset = {
                            MainContext.INSTANCE.filtersAdapter.reset()
                            update()
                            showFilterDialog = false
                        },
                        onClose = {
                            MainContext.INSTANCE.filtersAdapter.reload()
                            showFilterDialog = false
                        }
                    )
                }
            }
        }

        settings.registerOnSharedPreferenceChangeListener(this)
        applyKeepScreenOn()

        onBackPressedDispatcher.addCallback(this, MainActivityBackPressed(this))
    }

    private fun export() {
        val export = Export()
        val wiFiDetails: List<WiFiDetail> =
            MainContext.INSTANCE.scannerService
                .wiFiData()
                .wiFiDetails
        if (wiFiDetails.isEmpty()) {
            Toast.makeText(this, R.string.no_data, Toast.LENGTH_LONG).show()
            return
        }
        val intent: Intent = export.export(this, wiFiDetails)
        if (intent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, R.string.export_not_available, Toast.LENGTH_LONG).show()
            return
        }
        runCatching { startActivity(intent) }
            .getOrElse {
                Toast
                    .makeText(this, it.localizedMessage, Toast.LENGTH_LONG)
                    .show()
            }
    }

    private val largeScreen: Boolean
        get() {
            val configuration = resources.configuration
            val screenLayoutSize = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
            return screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?,
    ) {
        if (mainReload.shouldReload(settings)) {
            MainContext.INSTANCE.scannerService.stop()
            recreate()
        } else {
            applyKeepScreenOn()
            update()
        }
    }

    fun update() {
        val scannerService = MainContext.INSTANCE.scannerService
        scannerService.update()
        isScannerRunning = scannerService.running()
        isFilterActive = MainContext.INSTANCE.filtersAdapter.isActive()
        currentWiFiBand = settings.wiFiBand()
    }

    private fun applyKeepScreenOn() {
        if (settings.keepScreenOn()) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun closeDrawer(): Boolean {
        return false
    }

    override fun updateActionBar() {
        // No longer needed with Compose TopAppBar
    }

    override fun mainConnectionVisibility(visibility: Int) {
        // Compose 側の表示制御が必要な場合にここでステートを更新する
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        currentMenu = NavigationMenu.find(menuItem.itemId)
        update()
        return true
    }

    override fun currentMenuItem(): MenuItem = throw UnsupportedOperationException()

    override fun currentNavigationMenu(): NavigationMenu = currentMenu

    override fun currentNavigationMenu(navigationMenu: NavigationMenu) {
        currentMenu = navigationMenu
        settings.saveSelectedMenu(navigationMenu)
        update()
    }

    override fun navigationView(): NavigationView = throw UnsupportedOperationException()

    override fun <T : View?> findViewById(id: Int): T {
        return super.findViewById<T>(id)
    }

    public override fun onPause() {
        val scannerService: ScannerService = MainContext.INSTANCE.scannerService
        scannerService.pause()
        update()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        val scannerService: ScannerService = MainContext.INSTANCE.scannerService
        if (MainContext.INSTANCE.permissionService.permissionGranted()) {
            scannerService.resume()
        }
        update()
    }

    public override fun onStop() {
        MainContext.INSTANCE.scannerService.stop()
        update()
        super.onStop()
    }

    public override fun onStart() {
        super.onStart()
        update()
    }
}
