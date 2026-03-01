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
package com.vrem.wifianalyzer.wifi.channelgraph

import android.content.Context
import android.view.View
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.TitleLineGraphSeries
import com.vrem.util.findOne
import com.vrem.wifianalyzer.MainApplication
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.graphutils.GraphDataPoint
import com.vrem.wifianalyzer.wifi.graphutils.GraphLegend
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewBuilder
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewNotifier
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewWrapper
import com.vrem.wifianalyzer.wifi.graphutils.MIN_Y
import com.vrem.wifianalyzer.wifi.graphutils.THICKNESS_INVISIBLE
import com.vrem.wifianalyzer.wifi.graphutils.transparent
import com.vrem.wifianalyzer.wifi.model.SortBy
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.predicate.makeOtherPredicate
import kotlin.enums.EnumEntries

internal fun makeGraphView(
    context: Context,
    graphMaximumY: Int,
    themeStyle: ThemeStyle,
    wiFiBand: WiFiBand,
): GraphView {
    val resources = context.resources
    return GraphViewBuilder(wiFiBand.wiFiChannels.graphChannelCount(), graphMaximumY, themeStyle, true)
        .setLabelFormatter(ChannelAxisLabel(wiFiBand))
        .setVerticalTitle(resources.getString(R.string.graph_axis_y))
        .setHorizontalTitle(resources.getString(R.string.graph_channel_axis_x))
        .build(context, !wiFiBand.ghz2)
}

internal fun makeDefaultSeries(
    frequencyStart: Int,
    frequencyEnd: Int,
): TitleLineGraphSeries<GraphDataPoint> {
    val dataPoints = arrayOf(
        GraphDataPoint(frequencyStart, MIN_Y),
        GraphDataPoint(frequencyEnd, MIN_Y),
    )
    return TitleLineGraphSeries(dataPoints).apply {
        color = transparent.primary.toLong().toInt()
        thickness = THICKNESS_INVISIBLE
    }
}

internal fun makeGraphViewWrapper(
    context: Context,
    wiFiBand: WiFiBand
): GraphViewWrapper {
    val repository = MainApplication.repository
    val themeStyle =
        settingsFind(repository, ThemeStyle.entries, R.string.theme_key, ThemeStyle.DARK)
    val graphMaximumY = getGraphMaximumY(repository)
    val channelGraphLegend = settingsFind(
        repository,
        GraphLegend.entries,
        R.string.channel_graph_legend_key,
        GraphLegend.HIDE
    )

    val graphView = makeGraphView(context, graphMaximumY, themeStyle, wiFiBand)
    val graphViewWrapper = GraphViewWrapper(graphView, channelGraphLegend, themeStyle)

    MainApplication.configuration.size =
        graphViewWrapper.size(graphViewWrapper.calculateGraphType())

    val wiFiChannels = wiFiBand.wiFiChannels.wiFiChannels()
    val minX = wiFiChannels.first().frequency
    val maxX = wiFiChannels.last().frequency

    return graphViewWrapper.apply {
        setViewport(minX, maxX)
        addSeries(makeDefaultSeries(minX, maxX))
    }
}

internal class ChannelGraphView(
    private val context: Context,
    private val wiFiBand: WiFiBand,
    private val dataManager: DataManager = DataManager(),
    private val graphViewWrapper: GraphViewWrapper = makeGraphViewWrapper(context, wiFiBand),
) : GraphViewNotifier {
    override fun update(wiFiData: WiFiData) {
        val repository = MainApplication.repository
        val predicate = makeOtherPredicate(repository)
        val sortBy = settingsFind(repository, SortBy.entries, R.string.sort_by_key, SortBy.STRENGTH)
        val wiFiDetails = wiFiData.wiFiDetails(predicate, sortBy)
        val newSeries = dataManager.newSeries(wiFiDetails)
        val graphMaximumY = getGraphMaximumY(repository)
        val channelGraphLegend = settingsFind(
            repository,
            GraphLegend.entries,
            R.string.channel_graph_legend_key,
            GraphLegend.HIDE
        )

        dataManager.addSeriesData(graphViewWrapper, newSeries, graphMaximumY)

        graphViewWrapper.apply {
            removeSeries(newSeries)
            updateLegend(channelGraphLegend)
            visibility(if (selected()) View.VISIBLE else View.GONE)
        }
    }

    fun selected(): Boolean {
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
