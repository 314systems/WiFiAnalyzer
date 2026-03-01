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

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import com.vrem.util.defaultCountryCode
import com.vrem.util.findOne
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.WiFiAnalyzerApplication
import com.vrem.wifianalyzer.settings.Repository
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
import kotlin.enums.EnumEntries

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

class ChannelRatingViewModel(application: Application) :
    AndroidViewModel(application),
    UpdateNotifier {
    private val app = application as WiFiAnalyzerApplication
    private val repository = Repository(application)
    private val channelRating = ChannelRating()
    private val _uiState = MutableStateFlow(ChannelRatingUiState())
    val uiState: StateFlow<ChannelRatingUiState> = _uiState.asStateFlow()

    init {
        if (app.isScannerServiceInitialized) {
            app.scannerService.register(this)
            update(app.scannerService.wiFiData())
        }
    }

    override fun onCleared() {
        if (app.isScannerServiceInitialized) {
            app.scannerService.unregister(this)
        }
        super.onCleared()
    }

    override fun update(wiFiData: WiFiData) {
        val wiFiBand = readWiFiBand()
        val countryCode = repository.string(R.string.country_code_key, defaultCountryCode())
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
        if (app.isScannerServiceInitialized) {
            _uiState.update { it.copy(isRefreshing = true) }
            app.scannerService.update()
        }
    }

    private fun readWiFiBand(): WiFiBand =
        settingsFind(WiFiBand.entries, R.string.wifi_band_key, WiFiBand.GHZ2)

    private fun <T : Enum<T>> settingsFind(
        values: EnumEntries<T>,
        key: Int,
        defaultValue: T,
    ): T {
        val value = repository.stringAsInteger(key, defaultValue.ordinal)
        return findOne(values, value, defaultValue)
    }
}
