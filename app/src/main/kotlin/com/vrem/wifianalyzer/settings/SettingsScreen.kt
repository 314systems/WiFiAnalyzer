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

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vrem.util.supportedLanguages
import com.vrem.util.toLanguageTag
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewType
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType
import com.vrem.wifianalyzer.wifi.band.WiFiChannelCountry
import com.vrem.wifianalyzer.wifi.graphutils.GraphLegend
import com.vrem.wifianalyzer.wifi.model.GroupBy
import com.vrem.wifianalyzer.wifi.model.SortBy
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val scanSpeed by viewModel.scanSpeed.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val groupBy by viewModel.groupBy.collectAsStateWithLifecycle()
    val accessPointView by viewModel.accessPointView.collectAsStateWithLifecycle()
    val connectionViewType by viewModel.connectionViewType.collectAsStateWithLifecycle()
    val themeStyle by viewModel.themeStyle.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val wiFiOffOnExit by viewModel.wiFiOffOnExit.collectAsStateWithLifecycle()
    val graphMaximumY by viewModel.graphMaximumY.collectAsStateWithLifecycle()
    val channelGraphLegend by viewModel.channelGraphLegend.collectAsStateWithLifecycle()
    val timeGraphLegend by viewModel.timeGraphLegend.collectAsStateWithLifecycle()
    val countryCode by viewModel.countryCode.collectAsStateWithLifecycle()
    val languageLocale by viewModel.languageLocale.collectAsStateWithLifecycle()
    val cacheOff by viewModel.cacheOff.collectAsStateWithLifecycle()

    SettingsContent(
        scanSpeed = scanSpeed,
        sortBy = sortBy,
        groupBy = groupBy,
        accessPointView = accessPointView,
        connectionViewType = connectionViewType,
        themeStyle = themeStyle,
        keepScreenOn = keepScreenOn,
        wiFiOffOnExit = wiFiOffOnExit,
        graphMaximumY = graphMaximumY,
        channelGraphLegend = channelGraphLegend,
        timeGraphLegend = timeGraphLegend,
        countryCode = countryCode,
        languageLocale = languageLocale,
        cacheOff = cacheOff,
        showWiFiOffOnExit = viewModel.showWiFiOffOnExit,
        onSetScanSpeed = viewModel::setScanSpeed,
        onSetSortBy = viewModel::setSortBy,
        onSetGroupBy = viewModel::setGroupBy,
        onSetAccessPointView = viewModel::setAccessPointView,
        onSetConnectionViewType = viewModel::setConnectionViewType,
        onSetGraphMaximumY = viewModel::setGraphMaximumY,
        onSetChannelGraphLegend = viewModel::setChannelGraphLegend,
        onSetTimeGraphLegend = viewModel::setTimeGraphLegend,
        onSetThemeStyle = viewModel::setThemeStyle,
        onSetKeepScreenOn = viewModel::setKeepScreenOn,
        onSetWiFiOffOnExit = viewModel::setWiFiOffOnExit,
        onSetCountryCode = viewModel::setCountryCode,
        onSetLanguage = viewModel::setLanguage,
        onSetCacheOff = viewModel::setCacheOff,
        onReset = viewModel::reset
    )
}

@Composable
fun SettingsContent(
    scanSpeed: Int,
    sortBy: SortBy,
    groupBy: GroupBy,
    accessPointView: AccessPointViewType,
    connectionViewType: ConnectionViewType,
    themeStyle: ThemeStyle,
    keepScreenOn: Boolean,
    wiFiOffOnExit: Boolean,
    graphMaximumY: Int,
    channelGraphLegend: GraphLegend,
    timeGraphLegend: GraphLegend,
    countryCode: String,
    languageLocale: String,
    cacheOff: Boolean,
    showWiFiOffOnExit: Boolean,
    onSetScanSpeed: (Int) -> Unit,
    onSetSortBy: (SortBy) -> Unit,
    onSetGroupBy: (GroupBy) -> Unit,
    onSetAccessPointView: (AccessPointViewType) -> Unit,
    onSetConnectionViewType: (ConnectionViewType) -> Unit,
    onSetGraphMaximumY: (Int) -> Unit,
    onSetChannelGraphLegend: (GraphLegend) -> Unit,
    onSetTimeGraphLegend: (GraphLegend) -> Unit,
    onSetThemeStyle: (ThemeStyle) -> Unit,
    onSetKeepScreenOn: (Boolean) -> Unit,
    onSetWiFiOffOnExit: (Boolean) -> Unit,
    onSetCountryCode: (String) -> Unit,
    onSetLanguage: (String) -> Unit,
    onSetCacheOff: (Boolean) -> Unit,
    onReset: () -> Unit
) {
    var showScanSpeedDialog by remember { mutableStateOf(false) }
    var showSortByDialog by remember { mutableStateOf(false) }
    var showGroupByDialog by remember { mutableStateOf(false) }
    var showConnectionViewDialog by remember { mutableStateOf(false) }
    var showApViewDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showGraphYDialog by remember { mutableStateOf(false) }
    var showChannelLegendDialog by remember { mutableStateOf(false) }
    var showTimeLegendDialog by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val currentLocale = remember(languageLocale) { Locale.forLanguageTag(languageLocale) }

    if (showScanSpeedDialog) {
        ListPreferenceDialog(
            title = stringResource(R.string.scan_speed_title),
            entries = stringArrayResource(R.array.scan_speed_array).toList(),
            entryValues = stringArrayResource(R.array.scan_speed_array).toList(),
            selectedValue = scanSpeed.toString(),
            onValueSelected = {
                onSetScanSpeed(it.toInt())
                showScanSpeedDialog = false
            },
            onDismiss = { showScanSpeedDialog = false }
        )
    }

    if (showSortByDialog) {
        EnumPreferenceDialog(
            title = stringResource(R.string.sort_by_title),
            entries = stringArrayResource(R.array.sort_by_array).toList(),
            values = SortBy.entries,
            selectedValue = sortBy,
            onValueSelected = {
                onSetSortBy(it)
                showSortByDialog = false
            },
            onDismiss = { showSortByDialog = false }
        )
    }

    if (showGroupByDialog) {
        EnumPreferenceDialog(
            title = stringResource(R.string.group_by_title),
            entries = stringArrayResource(R.array.group_by_array).toList(),
            values = GroupBy.entries,
            selectedValue = groupBy,
            onValueSelected = {
                onSetGroupBy(it)
                showGroupByDialog = false
            },
            onDismiss = { showGroupByDialog = false }
        )
    }

    if (showConnectionViewDialog) {
        EnumPreferenceDialog(
            title = stringResource(R.string.connection_view_title),
            entries = stringArrayResource(R.array.connection_view_array).toList(),
            values = ConnectionViewType.entries,
            selectedValue = connectionViewType,
            onValueSelected = {
                onSetConnectionViewType(it)
                showConnectionViewDialog = false
            },
            onDismiss = { showConnectionViewDialog = false }
        )
    }

    if (showApViewDialog) {
        EnumPreferenceDialog(
            title = stringResource(R.string.ap_view_title),
            entries = stringArrayResource(R.array.ap_view_array).toList(),
            values = AccessPointViewType.entries,
            selectedValue = accessPointView,
            onValueSelected = {
                onSetAccessPointView(it)
                showApViewDialog = false
            },
            onDismiss = { showApViewDialog = false }
        )
    }

    if (showThemeDialog) {
        EnumPreferenceDialog(
            title = stringResource(R.string.theme_title),
            entries = stringArrayResource(R.array.theme_array).toList(),
            values = ThemeStyle.entries,
            selectedValue = themeStyle,
            onValueSelected = {
                onSetThemeStyle(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showGraphYDialog) {
        ListPreferenceDialog(
            title = stringResource(R.string.graph_maximum_y_title),
            entries = stringArrayResource(R.array.graph_maximum_y_array).toList(),
            entryValues = stringArrayResource(R.array.graph_maximum_y_index_array).toList(),
            selectedValue = (graphMaximumY / -10).toString(),
            onValueSelected = {
                onSetGraphMaximumY(it.toInt())
                showGraphYDialog = false
            },
            onDismiss = { showGraphYDialog = false }
        )
    }

    if (showChannelLegendDialog) {
        EnumPreferenceDialog(
            title = stringResource(R.string.channel_graph_legend_title),
            entries = stringArrayResource(R.array.graph_legend_array).toList(),
            values = GraphLegend.entries,
            selectedValue = channelGraphLegend,
            onValueSelected = {
                onSetChannelGraphLegend(it)
                showChannelLegendDialog = false
            },
            onDismiss = { showChannelLegendDialog = false }
        )
    }

    if (showTimeLegendDialog) {
        EnumPreferenceDialog(
            title = stringResource(R.string.time_graph_legend_title),
            entries = stringArrayResource(R.array.graph_legend_array).toList(),
            values = GraphLegend.entries,
            selectedValue = timeGraphLegend,
            onValueSelected = {
                onSetTimeGraphLegend(it)
                showTimeLegendDialog = false
            },
            onDismiss = { showTimeLegendDialog = false }
        )
    }

    if (showCountryDialog) {
        val countries = WiFiChannelCountry.findAll()
        ListPreferenceDialog(
            title = stringResource(R.string.country_code_title),
            entries = countries.map { it.countryName(currentLocale) },
            entryValues = countries.map { it.countryCode },
            selectedValue = countryCode,
            onValueSelected = {
                onSetCountryCode(it)
                showCountryDialog = false
            },
            onDismiss = { showCountryDialog = false }
        )
    }

    if (showLanguageDialog) {
        val languages = supportedLanguages()
        ListPreferenceDialog(
            title = stringResource(R.string.language_title),
            entries = languages.map {
                it.getDisplayName(it).replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(currentLocale) else char.toString()
                }
            },
            entryValues = languages.map { toLanguageTag(it) },
            selectedValue = languageLocale,
            onValueSelected = {
                onSetLanguage(it)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            PreferenceCategory(title = stringResource(R.string.scan_speed_title))
            ListPreference(
                title = stringResource(R.string.scan_speed_title),
                summary = scanSpeed.toString(),
                icon = painterResource(R.drawable.ic_fast_forward),
                onClick = { showScanSpeedDialog = true }
            )

            HorizontalDivider()
            PreferenceCategory(title = stringResource(R.string.sort_by_title))
            ListPreference(
                title = stringResource(R.string.sort_by_title),
                summary = stringArrayResource(R.array.sort_by_array)[sortBy.ordinal],
                icon = painterResource(R.drawable.ic_sort),
                onClick = { showSortByDialog = true }
            )
            ListPreference(
                title = stringResource(R.string.group_by_title),
                summary = stringArrayResource(R.array.group_by_array)[groupBy.ordinal],
                icon = painterResource(R.drawable.group_24px),
                onClick = { showGroupByDialog = true }
            )

            HorizontalDivider()
            PreferenceCategory(title = stringResource(R.string.connection_view_title))
            ListPreference(
                title = stringResource(R.string.connection_view_title),
                summary = stringArrayResource(R.array.connection_view_array)[connectionViewType.ordinal],
                icon = painterResource(R.drawable.view_carousel_24px),
                onClick = { showConnectionViewDialog = true }
            )
            ListPreference(
                title = stringResource(R.string.ap_view_title),
                summary = stringArrayResource(R.array.ap_view_array)[accessPointView.ordinal],
                icon = painterResource(R.drawable.view_carousel_24px),
                onClick = { showApViewDialog = true }
            )

            HorizontalDivider()
            PreferenceCategory(title = stringResource(R.string.graph_maximum_y_title))
            val graphYValues = stringArrayResource(R.array.graph_maximum_y_index_array)
            val graphYEntries = stringArrayResource(R.array.graph_maximum_y_array)
            val graphYIndex =
                graphYValues.indexOf((graphMaximumY / -10).toString()).coerceAtLeast(0)
            ListPreference(
                title = stringResource(R.string.graph_maximum_y_title),
                summary = graphYEntries[graphYIndex],
                onClick = { showGraphYDialog = true }
            )
            ListPreference(
                title = stringResource(R.string.channel_graph_legend_title),
                summary = stringArrayResource(R.array.graph_legend_array)[channelGraphLegend.ordinal],
                icon = painterResource(R.drawable.insert_chart_24px),
                onClick = { showChannelLegendDialog = true }
            )
            ListPreference(
                title = stringResource(R.string.time_graph_legend_title),
                summary = stringArrayResource(R.array.graph_legend_array)[timeGraphLegend.ordinal],
                icon = painterResource(R.drawable.show_chart_24px),
                onClick = { showTimeLegendDialog = true }
            )

            HorizontalDivider()
            PreferenceCategory(title = stringResource(R.string.theme_title))
            ListPreference(
                title = stringResource(R.string.theme_title),
                summary = stringArrayResource(R.array.theme_array)[themeStyle.ordinal],
                icon = painterResource(R.drawable.ic_color_lens),
                onClick = { showThemeDialog = true }
            )
            SwitchPreference(
                title = stringResource(R.string.keep_screen_on_title),
                checked = keepScreenOn,
                onCheckedChange = { onSetKeepScreenOn(it) },
                icon = painterResource(R.drawable.brightness_medium_24px)
            )
            if (showWiFiOffOnExit) {
                SwitchPreference(
                    title = stringResource(R.string.wifi_off_on_exit_title),
                    checked = wiFiOffOnExit,
                    onCheckedChange = { onSetWiFiOffOnExit(it) },
                    icon = painterResource(R.drawable.wifi_off_24px)
                )
            }

            HorizontalDivider()
            PreferenceCategory(title = stringResource(R.string.country_code_title))
            val countryName = WiFiChannelCountry.find(countryCode).countryName(currentLocale)
            ListPreference(
                title = stringResource(R.string.country_code_title),
                summary = countryName,
                onClick = { showCountryDialog = true }
            )
            val languageDisplayName =
                supportedLanguages().find { toLanguageTag(it) == languageLocale }
                    ?.getDisplayName(currentLocale)
                    ?.replaceFirstChar { it.titlecase(currentLocale) } ?: languageLocale
            ListPreference(
                title = stringResource(R.string.language_title),
                summary = languageDisplayName,
                icon = painterResource(R.drawable.language_24px),
                onClick = { showLanguageDialog = true }
            )

            HorizontalDivider()
            PreferenceCategory(title = stringResource(R.string.experimental_title))
            SwitchPreference(
                title = stringResource(R.string.cache_off_title),
                checked = cacheOff,
                onCheckedChange = { onSetCacheOff(it) }
            )

            HorizontalDivider()
            ActionPreference(
                title = stringResource(R.string.reset_title),
                icon = painterResource(R.drawable.ic_reset),
                onClick = onReset
            )
        }
    }
}

@Composable
fun ListPreferenceDialog(
    title: String,
    entries: List<String>,
    entryValues: List<String>,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                entries.forEachIndexed { index, entry ->
                    ListItem(
                        headlineContent = { Text(entry) },
                        leadingContent = {
                            RadioButton(
                                selected = entryValues[index] == selectedValue,
                                onClick = { onValueSelected(entryValues[index]) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onValueSelected(entryValues[index]) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
fun <T : Enum<T>> EnumPreferenceDialog(
    title: String,
    entries: List<String>,
    values: List<T>,
    selectedValue: T,
    onValueSelected: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                entries.forEachIndexed { index, entry ->
                    ListItem(
                        headlineContent = { Text(entry) },
                        leadingContent = {
                            RadioButton(
                                selected = values[index] == selectedValue,
                                onClick = { onValueSelected(values[index]) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onValueSelected(values[index]) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun SettingsScreenLightPreview() {
    AppTheme {
        Surface {
            SettingsContent(
                scanSpeed = 5,
                sortBy = SortBy.STRENGTH,
                groupBy = GroupBy.NONE,
                accessPointView = AccessPointViewType.COMPLETE,
                connectionViewType = ConnectionViewType.COMPACT,
                themeStyle = ThemeStyle.DARK,
                keepScreenOn = true,
                wiFiOffOnExit = false,
                graphMaximumY = -20,
                channelGraphLegend = GraphLegend.HIDE,
                timeGraphLegend = GraphLegend.LEFT,
                countryCode = "US",
                languageLocale = "en",
                cacheOff = false,
                showWiFiOffOnExit = true,
                onSetScanSpeed = {},
                onSetSortBy = {},
                onSetGroupBy = {},
                onSetAccessPointView = {},
                onSetConnectionViewType = {},
                onSetGraphMaximumY = {},
                onSetChannelGraphLegend = {},
                onSetTimeGraphLegend = {},
                onSetThemeStyle = {},
                onSetKeepScreenOn = {},
                onSetWiFiOffOnExit = {},
                onSetCountryCode = {},
                onSetLanguage = {},
                onSetCacheOff = {},
                onReset = {}
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun SettingsScreenDarkPreview() {
    AppTheme {
        Surface {
            SettingsContent(
                scanSpeed = 5,
                sortBy = SortBy.STRENGTH,
                groupBy = GroupBy.NONE,
                accessPointView = AccessPointViewType.COMPLETE,
                connectionViewType = ConnectionViewType.COMPACT,
                themeStyle = ThemeStyle.DARK,
                keepScreenOn = true,
                wiFiOffOnExit = false,
                graphMaximumY = -20,
                channelGraphLegend = GraphLegend.HIDE,
                timeGraphLegend = GraphLegend.LEFT,
                countryCode = "US",
                languageLocale = "en",
                cacheOff = false,
                showWiFiOffOnExit = true,
                onSetScanSpeed = {},
                onSetSortBy = {},
                onSetGroupBy = {},
                onSetAccessPointView = {},
                onSetConnectionViewType = {},
                onSetGraphMaximumY = {},
                onSetChannelGraphLegend = {},
                onSetTimeGraphLegend = {},
                onSetThemeStyle = {},
                onSetKeepScreenOn = {},
                onSetWiFiOffOnExit = {},
                onSetCountryCode = {},
                onSetLanguage = {},
                onSetCacheOff = {},
                onReset = {}
            )
        }
    }
}
