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
import com.vrem.util.defaultCountryCode
import com.vrem.util.defaultLanguageTag
import com.vrem.util.findByLanguageTag
import com.vrem.util.findOne
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewType
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType
import com.vrem.wifianalyzer.wifi.graphutils.GraphLegend
import com.vrem.wifianalyzer.wifi.model.GroupBy
import com.vrem.wifianalyzer.wifi.model.SortBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.enums.EnumEntries

class SettingsViewModel(application: Application) : AndroidViewModel(application),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val repository = Repository(application)

    private val _scanSpeed = MutableStateFlow(getScanSpeed())
    val scanSpeed: StateFlow<Int> = _scanSpeed.asStateFlow()

    private val _sortBy = MutableStateFlow(getSortBy())
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    private val _groupBy = MutableStateFlow(getGroupBy())
    val groupBy: StateFlow<GroupBy> = _groupBy.asStateFlow()

    private val _accessPointView = MutableStateFlow(getAccessPointView())
    val accessPointView: StateFlow<AccessPointViewType> = _accessPointView.asStateFlow()

    private val _connectionViewType = MutableStateFlow(getConnectionViewType())
    val connectionViewType: StateFlow<ConnectionViewType> = _connectionViewType.asStateFlow()

    private val _graphMaximumY = MutableStateFlow(getGraphMaximumY())
    val graphMaximumY: StateFlow<Int> = _graphMaximumY.asStateFlow()

    private val _channelGraphLegend = MutableStateFlow(getChannelGraphLegend())
    val channelGraphLegend: StateFlow<GraphLegend> = _channelGraphLegend.asStateFlow()

    private val _timeGraphLegend = MutableStateFlow(getTimeGraphLegend())
    val timeGraphLegend: StateFlow<GraphLegend> = _timeGraphLegend.asStateFlow()

    private val _themeStyle = MutableStateFlow(getThemeStyle())
    val themeStyle: StateFlow<ThemeStyle> = _themeStyle.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(getKeepScreenOn())
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    private val _wiFiOffOnExit = MutableStateFlow(getWiFiOffOnExit())
    val wiFiOffOnExit: StateFlow<Boolean> = _wiFiOffOnExit.asStateFlow()

    private val _countryCode = MutableStateFlow(getCountryCode())
    val countryCode: StateFlow<String> = _countryCode.asStateFlow()

    private val _languageLocale = MutableStateFlow(getLanguageLocale().toLanguageTag())
    val languageLocale: StateFlow<String> = _languageLocale.asStateFlow()

    private val _cacheOff = MutableStateFlow(getCacheOff())
    val cacheOff: StateFlow<Boolean> = _cacheOff.asStateFlow()

    val showWiFiOffOnExit: Boolean = !buildMinVersionQ()

    init {
        repository.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        _scanSpeed.value = getScanSpeed()
        _sortBy.value = getSortBy()
        _groupBy.value = getGroupBy()
        _accessPointView.value = getAccessPointView()
        _connectionViewType.value = getConnectionViewType()
        _graphMaximumY.value = getGraphMaximumY()
        _channelGraphLegend.value = getChannelGraphLegend()
        _timeGraphLegend.value = getTimeGraphLegend()
        _themeStyle.value = getThemeStyle()
        _keepScreenOn.value = getKeepScreenOn()
        _wiFiOffOnExit.value = getWiFiOffOnExit()
        _countryCode.value = getCountryCode()
        _languageLocale.value = getLanguageLocale().toLanguageTag()
        _cacheOff.value = getCacheOff()
    }

    // region Getters (Logic moved from Settings.kt)
    private fun getScanSpeed(): Int = repository.stringAsInteger(
        R.string.scan_speed_key,
        repository.stringAsInteger(R.string.scan_speed_default, SCAN_SPEED_DEFAULT),
    )

    private fun getSortBy(): SortBy =
        settingsFind(SortBy.entries, R.string.sort_by_key, SortBy.STRENGTH)

    private fun getGroupBy(): GroupBy =
        settingsFind(GroupBy.entries, R.string.group_by_key, GroupBy.NONE)

    private fun getAccessPointView(): AccessPointViewType =
        settingsFind(
            AccessPointViewType.entries,
            R.string.ap_view_key,
            AccessPointViewType.COMPLETE
        )

    private fun getConnectionViewType(): ConnectionViewType =
        settingsFind(
            ConnectionViewType.entries,
            R.string.connection_view_key,
            ConnectionViewType.COMPACT
        )

    private fun getGraphMaximumY(): Int {
        val defaultValue =
            repository.stringAsInteger(R.string.graph_maximum_y_default, GRAPH_Y_DEFAULT)
        return repository.stringAsInteger(
            R.string.graph_maximum_y_key,
            defaultValue
        ) * GRAPH_Y_MULTIPLIER
    }

    private fun getChannelGraphLegend(): GraphLegend =
        settingsFind(GraphLegend.entries, R.string.channel_graph_legend_key, GraphLegend.HIDE)

    private fun getTimeGraphLegend(): GraphLegend =
        settingsFind(GraphLegend.entries, R.string.time_graph_legend_key, GraphLegend.LEFT)

    private fun getThemeStyle(): ThemeStyle =
        settingsFind(ThemeStyle.entries, R.string.theme_key, ThemeStyle.DARK)

    private fun getKeepScreenOn(): Boolean =
        repository.boolean(
            R.string.keep_screen_on_key,
            repository.resourceBoolean(R.bool.keep_screen_on_default)
        )

    private fun getWiFiOffOnExit(): Boolean = if (buildMinVersionQ()) false else {
        repository.boolean(
            R.string.wifi_off_on_exit_key,
            repository.resourceBoolean(R.bool.wifi_off_on_exit_default)
        )
    }

    private fun getCountryCode(): String =
        repository.string(R.string.country_code_key, defaultCountryCode())

    private fun getLanguageLocale(): Locale {
        val languageTag = repository.string(R.string.language_key, defaultLanguageTag())
        return findByLanguageTag(languageTag)
    }

    private fun getCacheOff(): Boolean =
        repository.boolean(
            R.string.cache_off_key,
            repository.resourceBoolean(R.bool.cache_off_default)
        )
    // endregion

    // region Setters
    fun setScanSpeed(value: Int) = repository.save(R.string.scan_speed_key, value)
    fun setSortBy(value: SortBy) = repository.save(R.string.sort_by_key, value.ordinal)
    fun setGroupBy(value: GroupBy) = repository.save(R.string.group_by_key, value.ordinal)
    fun setAccessPointView(value: AccessPointViewType) =
        repository.save(R.string.ap_view_key, value.ordinal)

    fun setConnectionViewType(value: ConnectionViewType) =
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
        // Default values handled by getters
        onSharedPreferenceChanged(null, null)
    }
    // endregion

    private fun <T : Enum<T>> settingsFind(values: EnumEntries<T>, key: Int, defaultValue: T): T {
        val value = repository.stringAsInteger(key, defaultValue.ordinal)
        return findOne(values, value, defaultValue)
    }

    companion object {
        private const val SCAN_SPEED_DEFAULT = 5
        private const val GRAPH_Y_MULTIPLIER = -10
        private const val GRAPH_Y_DEFAULT = 2
    }
}
