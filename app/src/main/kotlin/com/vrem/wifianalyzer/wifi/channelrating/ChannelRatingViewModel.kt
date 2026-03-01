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
package com.vrem.wifianalyzer.wifi.channelrating

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.band.WiFiChannel
import com.vrem.wifianalyzer.wifi.model.ChannelAPCount
import com.vrem.wifianalyzer.wifi.model.ChannelRating
import com.vrem.wifianalyzer.wifi.model.SortBy
import com.vrem.wifianalyzer.wifi.model.Strength
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiWidth
import com.vrem.wifianalyzer.wifi.predicate.predicate
import com.vrem.wifianalyzer.wifi.scanner.UpdateNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class ChannelRatingUiState(
    val wiFiBand: WiFiBand = WiFiBand.GHZ2,
    val bestChannels: List<ChannelAPCount> = emptyList(),
    val channelRatings: List<ChannelRatingItem> = emptyList(),
    val isRefreshing: Boolean = false,
)

@Immutable
data class ChannelRatingItem(
    val wiFiChannel: WiFiChannel,
    val apCount: Int,
    val wiFiWidth: WiFiWidth,
    val rating: Strength,
)

class ChannelRatingViewModel :
    ViewModel(),
    UpdateNotifier {
    private val channelRating = ChannelRating()
    private val _uiState = MutableStateFlow(ChannelRatingUiState())
    val uiState: StateFlow<ChannelRatingUiState> = _uiState.asStateFlow()

    init {
        MainContext.INSTANCE.scannerService.register(this)
        update(MainContext.INSTANCE.scannerService.wiFiData())
    }

    override fun onCleared() {
        MainContext.INSTANCE.scannerService.unregister(this)
        super.onCleared()
    }

    override fun update(wiFiData: WiFiData) {
        val settings = MainContext.INSTANCE.settings
        val wiFiBand = settings.wiFiBand()
        val countryCode = settings.countryCode()
        val wiFiChannels = wiFiBand.wiFiChannels.availableChannels(wiFiBand, countryCode)
        val predicate = wiFiBand.predicate()
        val wiFiDetails = wiFiData.wiFiDetails(predicate, SortBy.STRENGTH)

        channelRating.wiFiDetails(wiFiDetails)
        val bestChannels = channelRating.bestChannels(wiFiBand, wiFiChannels)

        val ratingItems =
            wiFiChannels.map { channel ->
                ChannelRatingItem(
                    wiFiChannel = channel,
                    apCount = channelRating.count(channel),
                    wiFiWidth = wiFiBand.wiFiChannels.wiFiWidthByChannel(channel.channel),
                    rating = Strength.reverse(channelRating.strength(channel)),
                )
            }

        _uiState.update {
            it.copy(
                wiFiBand = wiFiBand,
                bestChannels = bestChannels,
                channelRatings = ratingItems,
                isRefreshing = false,
            )
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        MainContext.INSTANCE.scannerService.update()
    }
}
