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

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vrem.wifianalyzer.export.Export
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.permission.PermissionHandler
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.ui.filter.FilterDialog
import com.vrem.wifianalyzer.ui.main.MainScreen
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import android.provider.Settings as AndroidSettings

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        MainApplication.init(applicationContext)
        MainApplication.initScannerService(applicationContext, this, largeScreen)

        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            val themeStyle by viewModel.themeStyle.collectAsStateWithLifecycle()
            val languageLocale by viewModel.languageLocale.collectAsStateWithLifecycle()
            val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
            val currentMenu by viewModel.currentMenu.collectAsStateWithLifecycle()
            val isScannerRunning by viewModel.isScannerRunning.collectAsStateWithLifecycle()
            val isFilterActive by viewModel.isFilterActive.collectAsStateWithLifecycle()
            val currentWiFiBand by viewModel.currentWiFiBand.collectAsStateWithLifecycle()

            val isSystemInDark = isSystemInDarkTheme()
            val darkTheme = when (themeStyle) {
                ThemeStyle.DARK, ThemeStyle.BLACK -> true
                ThemeStyle.LIGHT -> false
                ThemeStyle.SYSTEM -> isSystemInDark
            }

            LaunchedEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme }
                )
            }

            LaunchedEffect(languageLocale) {
                val appLocales = LocaleListCompat.forLanguageTags(languageLocale.toLanguageTag())
                if (AppCompatDelegate.getApplicationLocales() != appLocales) {
                    AppCompatDelegate.setApplicationLocales(appLocales)
                }
            }

            DisposableEffect(keepScreenOn) {
                if (keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose { }
            }

            AppTheme(darkTheme = darkTheme) {
                PermissionHandler(
                    onPermissionGranted = { viewModel.update() },
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
                        filtersAdapter = MainApplication.filtersAdapter,
                        isAccessPoints = currentMenu == NavigationMenu.ACCESS_POINTS,
                        onApply = { ssid, bands, strengths, securities ->
                            viewModel.applyFilters(ssid, bands, strengths, securities)
                        },
                        onReset = { viewModel.resetFilters() },
                        onClose = { viewModel.closeFilters() }
                    )
                }
            }
        }
    }

    private fun export() {
        val export = Export()
        val wiFiDetails: List<WiFiDetail> =
            MainApplication.scannerService
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
