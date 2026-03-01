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
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import com.vrem.wifianalyzer.wifi.scanner.makeScannerService

class WiFiAnalyzerApplication : Application() {
    lateinit var settings: Settings
        private set
    lateinit var vendorService: VendorService
        private set
    lateinit var wiFiManagerWrapper: WiFiManagerWrapper
        private set
    lateinit var filtersAdapter: FiltersAdapter
        private set
    private var _scannerService: ScannerService? = null
    val scannerService: ScannerService
        get() = _scannerService ?: throw IllegalStateException("ScannerService not initialized")

    val isScannerServiceInitialized: Boolean
        get() = _scannerService != null

    lateinit var configuration: Configuration
        private set

    override fun onCreate() {
        super.onCreate()
        settings = Settings(Repository(this))
        vendorService = VendorService(resources)
        val wiFiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        wiFiManagerWrapper = WiFiManagerWrapper(wiFiManager)
        filtersAdapter = FiltersAdapter(settings)
        configuration = Configuration(false) // Default, will be updated by MainActivity
    }

    fun initScannerService(activity: MainActivity, largeScreen: Boolean) {
        configuration = Configuration(largeScreen)
        // Re-initialize WiFiManagerWrapper with activity context to handle settings navigation
        val wiFiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        wiFiManagerWrapper = WiFiManagerWrapper(wiFiManager, activity::startWiFiSettings)

        if (_scannerService == null) {
            _scannerService = makeScannerService(
                this,
                wiFiManagerWrapper,
                Handler(Looper.getMainLooper()),
                settings,
                configuration
            )
        }
    }
}
