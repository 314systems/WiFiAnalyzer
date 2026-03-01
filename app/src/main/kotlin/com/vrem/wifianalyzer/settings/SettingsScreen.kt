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
import androidx.compose.runtime.Immutable
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

private enum class DialogType {
    NONE, SCAN_SPEED, SORT_BY, GROUP_BY, CONNECTION_VIEW, AP_VIEW, THEME, GRAPH_Y, CHANNEL_LEGEND, TIME_LEGEND, COUNTRY, LANGUAGE
}

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
        actions = SettingsActions(
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
    )
}

@Immutable
data class SettingsActions(
    val onSetScanSpeed: (Int) -> Unit,
    val onSetSortBy: (SortBy) -> Unit,
    val onSetGroupBy: (GroupBy) -> Unit,
    val onSetAccessPointView: (AccessPointViewType) -> Unit,
    val onSetConnectionViewType: (ConnectionViewType) -> Unit,
    val onSetGraphMaximumY: (Int) -> Unit,
    val onSetChannelGraphLegend: (GraphLegend) -> Unit,
    val onSetTimeGraphLegend: (GraphLegend) -> Unit,
    val onSetThemeStyle: (ThemeStyle) -> Unit,
    val onSetKeepScreenOn: (Boolean) -> Unit,
    val onSetWiFiOffOnExit: (Boolean) -> Unit,
    val onSetCountryCode: (String) -> Unit,
    val onSetLanguage: (String) -> Unit,
    val onSetCacheOff: (Boolean) -> Unit,
    val onReset: () -> Unit
)

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
    actions: SettingsActions
) {
    var dialogToShow by remember { mutableStateOf(DialogType.NONE) }
    val currentLocale = remember(languageLocale) { Locale.forLanguageTag(languageLocale) }

    SettingsDialogs(
        dialogType = dialogToShow,
        scanSpeed = scanSpeed,
        sortBy = sortBy,
        groupBy = groupBy,
        connectionViewType = connectionViewType,
        accessPointView = accessPointView,
        themeStyle = themeStyle,
        graphMaximumY = graphMaximumY,
        channelGraphLegend = channelGraphLegend,
        timeGraphLegend = timeGraphLegend,
        countryCode = countryCode,
        languageLocale = languageLocale,
        currentLocale = currentLocale,
        onDismiss = { dialogToShow = DialogType.NONE },
        actions = actions
    )

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(stringResource(R.string.scan_speed_title)) {
                SettingsItem(
                    title = R.string.scan_speed_title,
                    summary = scanSpeed.toString(),
                    icon = R.drawable.ic_fast_forward,
                    onClick = { dialogToShow = DialogType.SCAN_SPEED }
                )
            }

            SettingsSection(stringResource(R.string.sort_by_title)) {
                SettingsItem(
                    title = R.string.sort_by_title,
                    summary = stringArrayResource(R.array.sort_by_array)[sortBy.ordinal],
                    icon = R.drawable.sort_24px,
                    onClick = { dialogToShow = DialogType.SORT_BY }
                )
                SettingsItem(
                    title = R.string.group_by_title,
                    summary = stringArrayResource(R.array.group_by_array)[groupBy.ordinal],
                    icon = R.drawable.group_24px,
                    onClick = { dialogToShow = DialogType.GROUP_BY }
                )
            }

            SettingsSection(stringResource(R.string.connection_view_title)) {
                SettingsItem(
                    title = R.string.connection_view_title,
                    summary = stringArrayResource(R.array.connection_view_array)[connectionViewType.ordinal],
                    icon = R.drawable.view_carousel_24px,
                    onClick = { dialogToShow = DialogType.CONNECTION_VIEW }
                )
                SettingsItem(
                    title = R.string.ap_view_title,
                    summary = stringArrayResource(R.array.ap_view_array)[accessPointView.ordinal],
                    icon = R.drawable.view_carousel_24px,
                    onClick = { dialogToShow = DialogType.AP_VIEW }
                )
            }

            SettingsSection(stringResource(R.string.graph_maximum_y_title)) {
                val graphYEntries = stringArrayResource(R.array.graph_maximum_y_array)
                val graphYIndex = stringArrayResource(R.array.graph_maximum_y_index_array)
                    .indexOf((graphMaximumY / -10).toString()).coerceAtLeast(0)
                SettingsItem(
                    title = R.string.graph_maximum_y_title,
                    summary = graphYEntries[graphYIndex],
                    onClick = { dialogToShow = DialogType.GRAPH_Y }
                )
                SettingsItem(
                    title = R.string.channel_graph_legend_title,
                    summary = stringArrayResource(R.array.graph_legend_array)[channelGraphLegend.ordinal],
                    icon = R.drawable.insert_chart_24px,
                    onClick = { dialogToShow = DialogType.CHANNEL_LEGEND }
                )
                SettingsItem(
                    title = R.string.time_graph_legend_title,
                    summary = stringArrayResource(R.array.graph_legend_array)[timeGraphLegend.ordinal],
                    icon = R.drawable.show_chart_24px,
                    onClick = { dialogToShow = DialogType.TIME_LEGEND }
                )
            }

            SettingsSection(stringResource(R.string.theme_title)) {
                SettingsItem(
                    title = R.string.theme_title,
                    summary = stringArrayResource(R.array.theme_array)[themeStyle.ordinal],
                    icon = R.drawable.ic_color_lens,
                    onClick = { dialogToShow = DialogType.THEME }
                )
                SwitchPreference(
                    title = stringResource(R.string.keep_screen_on_title),
                    checked = keepScreenOn,
                    onCheckedChange = actions.onSetKeepScreenOn,
                    icon = painterResource(R.drawable.brightness_medium_24px)
                )
                if (showWiFiOffOnExit) {
                    SwitchPreference(
                        title = stringResource(R.string.wifi_off_on_exit_title),
                        checked = wiFiOffOnExit,
                        onCheckedChange = actions.onSetWiFiOffOnExit,
                        icon = painterResource(R.drawable.wifi_off_24px)
                    )
                }
            }

            SettingsSection(stringResource(R.string.country_code_title)) {
                val countryName = WiFiChannelCountry.find(countryCode).countryName(currentLocale)
                SettingsItem(
                    title = R.string.country_code_title,
                    summary = countryName,
                    onClick = { dialogToShow = DialogType.COUNTRY }
                )
                val languageDisplayName = supportedLanguages()
                    .find { toLanguageTag(it) == languageLocale }
                    ?.getDisplayName(currentLocale)
                    ?.replaceFirstChar { it.titlecase(currentLocale) } ?: languageLocale
                SettingsItem(
                    title = R.string.language_title,
                    summary = languageDisplayName,
                    icon = R.drawable.language_24px,
                    onClick = { dialogToShow = DialogType.LANGUAGE }
                )
            }

            SettingsSection(stringResource(R.string.experimental_title)) {
                SwitchPreference(
                    title = stringResource(R.string.cache_off_title),
                    checked = cacheOff,
                    onCheckedChange = actions.onSetCacheOff
                )
            }

            HorizontalDivider()
            ActionPreference(
                title = stringResource(R.string.reset_title),
                icon = painterResource(R.drawable.ic_reset),
                onClick = actions.onReset
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    PreferenceCategory(title = title)
    content()
    HorizontalDivider()
}

@Composable
private fun SettingsItem(title: Int, summary: String, icon: Int? = null, onClick: () -> Unit) {
    ListPreference(
        title = stringResource(title),
        summary = summary,
        icon = icon?.let { painterResource(it) },
        onClick = onClick
    )
}

@Composable
private fun SettingsDialogs(
    dialogType: DialogType,
    scanSpeed: Int,
    sortBy: SortBy,
    groupBy: GroupBy,
    connectionViewType: ConnectionViewType,
    accessPointView: AccessPointViewType,
    themeStyle: ThemeStyle,
    graphMaximumY: Int,
    channelGraphLegend: GraphLegend,
    timeGraphLegend: GraphLegend,
    countryCode: String,
    languageLocale: String,
    currentLocale: Locale,
    onDismiss: () -> Unit,
    actions: SettingsActions
) {
    when (dialogType) {
        DialogType.SCAN_SPEED -> ListPreferenceDialog(
            title = stringResource(R.string.scan_speed_title),
            entries = stringArrayResource(R.array.scan_speed_array).toList(),
            entryValues = stringArrayResource(R.array.scan_speed_array).toList(),
            selectedValue = scanSpeed.toString(),
            onValueSelected = { actions.onSetScanSpeed(it.toInt()); onDismiss() },
            onDismiss = onDismiss
        )

        DialogType.SORT_BY -> EnumPreferenceDialog(
            title = stringResource(R.string.sort_by_title),
            entries = stringArrayResource(R.array.sort_by_array).toList(),
            values = SortBy.entries,
            selectedValue = sortBy,
            onValueSelected = { actions.onSetSortBy(it); onDismiss() },
            onDismiss = onDismiss
        )

        DialogType.GROUP_BY -> EnumPreferenceDialog(
            title = stringResource(R.string.group_by_title),
            entries = stringArrayResource(R.array.group_by_array).toList(),
            values = GroupBy.entries,
            selectedValue = groupBy,
            onValueSelected = { actions.onSetGroupBy(it); onDismiss() },
            onDismiss = onDismiss
        )

        DialogType.CONNECTION_VIEW -> EnumPreferenceDialog(
            title = stringResource(R.string.connection_view_title),
            entries = stringArrayResource(R.array.connection_view_array).toList(),
            values = ConnectionViewType.entries,
            selectedValue = connectionViewType,
            onValueSelected = { actions.onSetConnectionViewType(it); onDismiss() },
            onDismiss = onDismiss
        )

        DialogType.AP_VIEW -> EnumPreferenceDialog(
            title = stringResource(R.string.ap_view_title),
            entries = stringArrayResource(R.array.ap_view_array).toList(),
            values = AccessPointViewType.entries,
            selectedValue = accessPointView,
            onValueSelected = { actions.onSetAccessPointView(it); onDismiss() },
            onDismiss = onDismiss
        )

        DialogType.THEME -> EnumPreferenceDialog(
            title = stringResource(R.string.theme_title),
            entries = stringArrayResource(R.array.theme_array).toList(),
            values = ThemeStyle.entries,
            selectedValue = themeStyle,
            onValueSelected = { actions.onSetThemeStyle(it); onDismiss() },
            onDismiss = onDismiss
        )

        DialogType.GRAPH_Y -> ListPreferenceDialog(
            title = stringResource(R.string.graph_maximum_y_title),
            entries = stringArrayResource(R.array.graph_maximum_y_array).toList(),
            entryValues = stringArrayResource(R.array.graph_maximum_y_index_array).toList(),
            selectedValue = (graphMaximumY / -10).toString(),
            onValueSelected = { actions.onSetGraphMaximumY(it.toInt()); onDismiss() },
            onDismiss = onDismiss
        )

        DialogType.CHANNEL_LEGEND -> EnumPreferenceDialog(
            title = stringResource(R.string.channel_graph_legend_title),
            entries = stringArrayResource(R.array.graph_legend_array).toList(),
            values = GraphLegend.entries,
            selectedValue = channelGraphLegend,
            onValueSelected = { actions.onSetChannelGraphLegend(it); onDismiss() },
            onDismiss = onDismiss
        )

        DialogType.TIME_LEGEND -> EnumPreferenceDialog(
            title = stringResource(R.string.time_graph_legend_title),
            entries = stringArrayResource(R.array.graph_legend_array).toList(),
            values = GraphLegend.entries,
            selectedValue = timeGraphLegend,
            onValueSelected = { actions.onSetTimeGraphLegend(it); onDismiss() },
            onDismiss = onDismiss
        )

        DialogType.COUNTRY -> {
            val countries = WiFiChannelCountry.findAll()
            ListPreferenceDialog(
                title = stringResource(R.string.country_code_title),
                entries = countries.map { it.countryName(currentLocale) },
                entryValues = countries.map { it.countryCode },
                selectedValue = countryCode,
                onValueSelected = { actions.onSetCountryCode(it); onDismiss() },
                onDismiss = onDismiss
            )
        }

        DialogType.LANGUAGE -> {
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
                onValueSelected = { actions.onSetLanguage(it); onDismiss() },
                onDismiss = onDismiss
            )
        }

        else -> {}
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
                actions = SettingsActions(
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {})
            )
        }
    }
}
