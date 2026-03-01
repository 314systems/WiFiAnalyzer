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
package com.vrem.wifianalyzer.wifi.channelavailable

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.vrem.util.defaultCountryCode
import com.vrem.wifianalyzer.MainApplication
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.band.WiFiChannelCountry
import com.vrem.wifianalyzer.wifi.model.WiFiWidth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

class ChannelAvailableViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)
    private val _uiState = MutableStateFlow(ChannelAvailableState())
    val uiState: StateFlow<ChannelAvailableState> = _uiState.asStateFlow()

    init {
        update()
    }

    fun update() {
        val countryCode = repository.string(R.string.country_code_key, defaultCountryCode())
        val wiFiChannelCountry = WiFiChannelCountry.find(countryCode)
        val currentLocale = Locale.getDefault()

        val bands = WiFiBand.entries
            .filter { it.available(MainApplication.wiFiManagerWrapper) }
            .map { band ->
                BandState(
                    title = getApplication<Application>().resources.getString(band.textResource),
                    widths = WiFiWidth.entries.mapNotNull { width ->
                        val channels = band.wiFiChannels.availableChannels(width, band, countryCode)
                        if (channels.isNotEmpty()) {
                            WidthState(
                                widthTitleRes = width.textResource,
                                channels = channels.joinToString(", ")
                            )
                        } else {
                            null
                        }
                    }
                )
            }

        _uiState.update {
            it.copy(
                countryCode = countryCode,
                countryName = wiFiChannelCountry.countryName(currentLocale),
                bands = bands
            )
        }
    }
}
