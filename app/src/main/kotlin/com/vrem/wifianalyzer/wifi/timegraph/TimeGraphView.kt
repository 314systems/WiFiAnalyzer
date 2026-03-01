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
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.WiFiAnalyzerApplication
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewBuilder
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewNotifier
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewWrapper
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.predicate.Predicate
import com.vrem.wifianalyzer.wifi.predicate.makeOtherPredicate

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

internal fun makeGraphViewWrapper(app: WiFiAnalyzerApplication): GraphViewWrapper {
    val settings = app.settings
    val themeStyle = settings.themeStyle()
    val graphView = makeGraphView(app, settings.graphMaximumY(), themeStyle)
    val graphViewWrapper = GraphViewWrapper(graphView, settings.timeGraphLegend(), themeStyle)

    app.configuration.size = graphViewWrapper.size(graphViewWrapper.calculateGraphType())

    return graphViewWrapper.apply {
        setViewport()
    }
}

internal class TimeGraphView(
    private val app: WiFiAnalyzerApplication,
    private val wiFiBand: WiFiBand,
    private val dataManager: DataManager = DataManager(),
    private val graphViewWrapper: GraphViewWrapper = makeGraphViewWrapper(app),
) : GraphViewNotifier {
    override fun update(wiFiData: WiFiData) {
        val settings = app.settings
        val predicate = predicate(settings)
        val wiFiDetails = wiFiData.wiFiDetails(predicate, settings.sortBy())

        val newSeries = dataManager.addSeriesData(
            graphViewWrapper,
            wiFiDetails,
            settings.graphMaximumY(),
        )

        graphViewWrapper.apply {
            removeSeries(newSeries)
            updateLegend(settings.timeGraphLegend())
            visibility(if (selected()) View.VISIBLE else View.GONE)
        }
    }

    fun predicate(settings: Settings): Predicate = makeOtherPredicate(settings)

    private fun selected(): Boolean = wiFiBand == app.settings.wiFiBand()

    override fun graphView(): GraphView = graphViewWrapper.graphView
}
