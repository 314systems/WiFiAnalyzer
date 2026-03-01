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
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.vrem.util.createContext
import com.vrem.wifianalyzer.export.Export
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.navigation.NavigationMenuControl
import com.vrem.wifianalyzer.permission.PermissionHandler
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.ui.filter.FilterDialog
import com.vrem.wifianalyzer.ui.main.MainScreen
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import android.provider.Settings as AndroidSettings

class MainActivity :
    AppCompatActivity(),
    NavigationMenuControl,
    OnSharedPreferenceChangeListener {
    private lateinit var settings: Settings
    private val viewModel: MainViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) =
        super.attachBaseContext(newBase.createContext(Settings(Repository(newBase)).languageLocale()))

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = application as WiFiAnalyzerApplication
        app.initScannerService(this, largeScreen)

        settings = app.settings
        settings.initializeDefaultValues()
        settings.themeStyle().setTheme(this)

        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            AppTheme {
                PermissionHandler(
                    onPermissionGranted = { viewModel.update() },
                    onTerminateApp = { finish() }
                )

                MainScreen(
                    currentMenu = viewModel.currentMenu,
                    isScannerRunning = viewModel.isScannerRunning,
                    isFilterActive = viewModel.isFilterActive,
                    currentWiFiBand = viewModel.currentWiFiBand,
                    onMenuSelected = { menu ->
                        if (menu == NavigationMenu.EXPORT) {
                            export()
                        } else {
                            viewModel.selectMenu(menu)
                        }
                    },
                    onToggleScanner = { viewModel.toggleScanner() },
                    onFilterClick = { viewModel.showFilterDialog = true },
                    onWiFiBandClick = { band -> viewModel.updateWiFiBand(band) },
                    onBackPressed = {
                        viewModel.handleBack(onFinish = { finish() })
                    }
                )

                if (viewModel.showFilterDialog) {
                    FilterDialog(
                        filtersAdapter = app.filtersAdapter,
                        isAccessPoints = viewModel.currentMenu == NavigationMenu.ACCESS_POINTS,
                        onApply = { ssid, bands, strengths, securities ->
                            viewModel.applyFilters(ssid, bands, strengths, securities)
                        },
                        onReset = { viewModel.resetFilters() },
                        onClose = { viewModel.closeFilters() }
                    )
                }
            }
        }

        settings.registerOnSharedPreferenceChangeListener(this)
        applyKeepScreenOn()
    }

    private fun export() {
        val app = application as WiFiAnalyzerApplication
        val export = Export()
        val wiFiDetails: List<WiFiDetail> =
            app.scannerService
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

    fun startWiFiSettings() {
        val intent = Intent(AndroidSettings.ACTION_WIFI_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        runCatching { startActivity(intent) }
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
        if (viewModel.shouldReload()) {
            val app = application as WiFiAnalyzerApplication
            app.scannerService.stop()
            recreate()
        } else {
            applyKeepScreenOn()
            viewModel.update()
        }
    }

    private fun applyKeepScreenOn() {
        if (settings.keepScreenOn()) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun updateActionBar() {
        // No longer needed with Compose TopAppBar
    }

    override fun mainConnectionVisibility(visibility: Int) {
        // Compose 側の表示制御が必要な場合にここでステートを更新する
    }

    override fun currentNavigationMenu(): NavigationMenu = viewModel.currentMenu

    override fun currentNavigationMenu(navigationMenu: NavigationMenu) {
        viewModel.selectMenu(navigationMenu)
    }

    public override fun onPause() {
        viewModel.pauseScanner()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        viewModel.resumeScanner()
    }

    public override fun onStop() {
        viewModel.stopScanner()
        super.onStop()
    }

    public override fun onStart() {
        super.onStart()
        viewModel.update()
    }
}
