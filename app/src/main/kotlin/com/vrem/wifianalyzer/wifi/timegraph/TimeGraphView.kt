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
package com.vrem.wifianalyzer.wifi.timegraph

import android.content.Context
import android.view.View
import com.jjoe64.graphview.GraphView
import com.vrem.util.findOne
import com.vrem.wifianalyzer.MainApplication
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.graphutils.GraphLegend
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewBuilder
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewNotifier
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewWrapper
import com.vrem.wifianalyzer.wifi.model.SortBy
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.predicate.makeOtherPredicate
import kotlin.enums.EnumEntries

private const val NUM_X_TIME = 21

internal fun makeGraphView(
    context: Context,
    graphMaximumY: Int,
    themeStyle: ThemeStyle,
): GraphView {
    val resources = context.resources
    return GraphViewBuilder(NUM_X_TIME, graphMaximumY, themeStyle, false)
        .setLabelFormatter(TimeAxisLabel())
        .setVerticalTitle(resources.getString(R.string.graph_axis_y))
        .setHorizontalTitle(resources.getString(R.string.graph_time_axis_x))
        .build(context, false)
}

internal fun makeGraphViewWrapper(context: Context): GraphViewWrapper {
    val repository = MainApplication.repository
    val themeStyle =
        settingsFind(repository, ThemeStyle.entries, R.string.theme_key, ThemeStyle.DARK)
    val graphMaximumY = getGraphMaximumY(repository)
    val timeGraphLegend = settingsFind(
        repository,
        GraphLegend.entries,
        R.string.time_graph_legend_key,
        GraphLegend.LEFT
    )

    val graphView = makeGraphView(context, graphMaximumY, themeStyle)
    val graphViewWrapper = GraphViewWrapper(graphView, timeGraphLegend, themeStyle)

    MainApplication.configuration.size =
        graphViewWrapper.size(graphViewWrapper.calculateGraphType())

    return graphViewWrapper.apply {
        setViewport()
    }
}

internal class TimeGraphView(
    private val context: Context,
    private val wiFiBand: WiFiBand,
    private val dataManager: DataManager = DataManager(),
    private val graphViewWrapper: GraphViewWrapper = makeGraphViewWrapper(context),
) : GraphViewNotifier {
    override fun update(wiFiData: WiFiData) {
        val repository = MainApplication.repository
        val predicate = makeOtherPredicate(repository)
        val sortBy = settingsFind(repository, SortBy.entries, R.string.sort_by_key, SortBy.STRENGTH)
        val wiFiDetails = wiFiData.wiFiDetails(predicate, sortBy)
        val graphMaximumY = getGraphMaximumY(repository)
        val timeGraphLegend = settingsFind(
            repository,
            GraphLegend.entries,
            R.string.time_graph_legend_key,
            GraphLegend.LEFT
        )

        val newSeries = dataManager.addSeriesData(
            graphViewWrapper,
            wiFiDetails,
            graphMaximumY,
        )

        graphViewWrapper.apply {
            removeSeries(newSeries)
            updateLegend(timeGraphLegend)
            visibility(if (selected()) View.VISIBLE else View.GONE)
        }
    }

    private fun selected(): Boolean {
        val wiFiBandValue =
            MainApplication.repository.stringAsInteger(
                R.string.wifi_band_key,
                WiFiBand.GHZ2.ordinal
            )
        val currentWiFiBand = WiFiBand.entries.getOrElse(wiFiBandValue) { WiFiBand.GHZ2 }
        return wiFiBand == currentWiFiBand
    }

    override fun graphView(): GraphView = graphViewWrapper.graphView
}

private fun <T : Enum<T>> settingsFind(
    repository: Repository,
    values: EnumEntries<T>,
    key: Int,
    defaultValue: T,
): T {
    val value = repository.stringAsInteger(key, defaultValue.ordinal)
    return findOne(values, value, defaultValue)
}

private fun getGraphMaximumY(repository: Repository): Int {
    val defaultValue = repository.stringAsInteger(R.string.graph_maximum_y_default, 2)
    return repository.stringAsInteger(R.string.graph_maximum_y_key, defaultValue) * -10
}
