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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jjoe64.graphview.GraphView
import com.vrem.wifianalyzer.MainApplication
import com.vrem.wifianalyzer.wifi.band.WiFiBand

@Composable
fun ChannelGraphContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scannerService = MainApplication.scannerService
    val repository = MainApplication.repository

    val wiFiBandValue = repository.stringAsInteger(
        com.vrem.wifianalyzer.R.string.wifi_band_key,
        WiFiBand.GHZ2.ordinal
    )
    val wiFiBand = WiFiBand.entries.getOrElse(wiFiBandValue) { WiFiBand.GHZ2 }

    val graphViews = remember {
        WiFiBand.entries.associateWith { band -> ChannelGraphView(context, band) }
    }

    val currentGraphView = graphViews[wiFiBand]

    DisposableEffect(currentGraphView) {
        currentGraphView?.let {
            scannerService.register(it)
            it.update(scannerService.wiFiData())
        }
        onDispose {
            currentGraphView?.let { scannerService.unregister(it) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        currentGraphView?.let { viewNotifier ->
            AndroidView<GraphView>(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    viewNotifier.graphView()
                },
                update = {
                    // Update logic if needed
                }
            )
        }
    }
}
