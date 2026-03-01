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
package com.vrem.wifianalyzer.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.vrem.util.buildMinVersionQ
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.WiFiAnalyzerApplication
import com.vrem.wifianalyzer.wifi.graphutils.GraphLegend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val app = application as WiFiAnalyzerApplication
    private val settings = app.settings
    private val repository = Repository(application)

    private val _scanSpeed = MutableStateFlow(settings.scanSpeed())
    val scanSpeed: StateFlow<Int> = _scanSpeed.asStateFlow()

    private val _sortBy = MutableStateFlow(settings.sortBy())
    val sortBy: StateFlow<com.vrem.wifianalyzer.wifi.model.SortBy> = _sortBy.asStateFlow()

    private val _groupBy = MutableStateFlow(settings.groupBy())
    val groupBy: StateFlow<com.vrem.wifianalyzer.wifi.model.GroupBy> = _groupBy.asStateFlow()

    private val _accessPointView = MutableStateFlow(settings.accessPointView())
    val accessPointView: StateFlow<com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewType> =
        _accessPointView.asStateFlow()

    private val _connectionViewType = MutableStateFlow(settings.connectionViewType())
    val connectionViewType: StateFlow<com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType> =
        _connectionViewType.asStateFlow()

    private val _graphMaximumY = MutableStateFlow(settings.graphMaximumY())
    val graphMaximumY: StateFlow<Int> = _graphMaximumY.asStateFlow()

    private val _channelGraphLegend = MutableStateFlow(settings.channelGraphLegend())
    val channelGraphLegend: StateFlow<GraphLegend> = _channelGraphLegend.asStateFlow()

    private val _timeGraphLegend = MutableStateFlow(settings.timeGraphLegend())
    val timeGraphLegend: StateFlow<GraphLegend> = _timeGraphLegend.asStateFlow()

    private val _themeStyle = MutableStateFlow(settings.themeStyle())
    val themeStyle: StateFlow<ThemeStyle> = _themeStyle.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(settings.keepScreenOn())
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    private val _wiFiOffOnExit = MutableStateFlow(settings.wiFiOffOnExit())
    val wiFiOffOnExit: StateFlow<Boolean> = _wiFiOffOnExit.asStateFlow()

    private val _countryCode = MutableStateFlow(settings.countryCode())
    val countryCode: StateFlow<String> = _countryCode.asStateFlow()

    private val _languageLocale = MutableStateFlow(settings.languageLocale().toLanguageTag())
    val languageLocale: StateFlow<String> = _languageLocale.asStateFlow()

    private val _cacheOff = MutableStateFlow(settings.cacheOff())
    val cacheOff: StateFlow<Boolean> = _cacheOff.asStateFlow()

    val showWiFiOffOnExit: Boolean = !buildMinVersionQ()

    init {
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        _scanSpeed.value = settings.scanSpeed()
        _sortBy.value = settings.sortBy()
        _groupBy.value = settings.groupBy()
        _accessPointView.value = settings.accessPointView()
        _connectionViewType.value = settings.connectionViewType()
        _graphMaximumY.value = settings.graphMaximumY()
        _channelGraphLegend.value = settings.channelGraphLegend()
        _timeGraphLegend.value = settings.timeGraphLegend()
        _themeStyle.value = settings.themeStyle()
        _keepScreenOn.value = settings.keepScreenOn()
        _wiFiOffOnExit.value = settings.wiFiOffOnExit()
        _countryCode.value = settings.countryCode()
        _languageLocale.value = settings.languageLocale().toLanguageTag()
        _cacheOff.value = settings.cacheOff()
    }

    fun setScanSpeed(value: Int) = repository.save(R.string.scan_speed_key, value)
    fun setSortBy(value: com.vrem.wifianalyzer.wifi.model.SortBy) =
        repository.save(R.string.sort_by_key, value.ordinal)

    fun setGroupBy(value: com.vrem.wifianalyzer.wifi.model.GroupBy) =
        repository.save(R.string.group_by_key, value.ordinal)

    fun setAccessPointView(value: com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewType) =
        repository.save(R.string.ap_view_key, value.ordinal)

    fun setConnectionViewType(value: com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType) =
        repository.save(R.string.connection_view_key, value.ordinal)

    fun setGraphMaximumY(value: Int) = repository.save(R.string.graph_maximum_y_key, value)
    fun setChannelGraphLegend(value: GraphLegend) =
        repository.save(R.string.channel_graph_legend_key, value.ordinal)

    fun setTimeGraphLegend(value: GraphLegend) =
        repository.save(R.string.time_graph_legend_key, value.ordinal)

    fun setThemeStyle(value: ThemeStyle) = repository.save(R.string.theme_key, value.ordinal)
    fun setKeepScreenOn(value: Boolean) = repository.save(R.string.keep_screen_on_key, value)
    fun setWiFiOffOnExit(value: Boolean) = repository.save(R.string.wifi_off_on_exit_key, value)
    fun setCountryCode(value: String) = repository.save(R.string.country_code_key, value)
    fun setLanguage(value: String) = repository.save(R.string.language_key, value)
    fun setCacheOff(value: Boolean) = repository.save(R.string.cache_off_key, value)

    fun reset() {
        repository.clear()
        settings.initializeDefaultValues()
        onSharedPreferenceChanged(null, null)
    }
}
