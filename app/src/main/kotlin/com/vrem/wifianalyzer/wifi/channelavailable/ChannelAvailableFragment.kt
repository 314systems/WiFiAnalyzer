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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.band.WiFiChannelCountry
import com.vrem.wifianalyzer.wifi.model.WiFiWidth

class ChannelAvailableFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    ChannelAvailableContent(state = makeState())
                }
            }
        }
    }

    private fun makeState(): ChannelAvailableState {
        val settings = MainContext.INSTANCE.settings
        val countryCode = settings.countryCode()
        val languageLocale = settings.languageLocale()

        val bands = listOf(
            makeBandState(WiFiBand.GHZ2, listOf(WiFiWidth.MHZ_20, WiFiWidth.MHZ_40), countryCode),
            makeBandState(WiFiBand.GHZ5, listOf(WiFiWidth.MHZ_20, WiFiWidth.MHZ_40, WiFiWidth.MHZ_80, WiFiWidth.MHZ_160), countryCode),
            makeBandState(WiFiBand.GHZ6, listOf(WiFiWidth.MHZ_20, WiFiWidth.MHZ_40, WiFiWidth.MHZ_80, WiFiWidth.MHZ_160, WiFiWidth.MHZ_320), countryCode)
        )

        return ChannelAvailableState(
            countryCode = countryCode,
            countryName = WiFiChannelCountry.find(countryCode).countryName(languageLocale),
            bands = bands.filter { it.widths.isNotEmpty() }
        )
    }

    private fun makeBandState(wiFiBand: WiFiBand, widths: List<WiFiWidth>, countryCode: String): BandState {
        val widthStates = widths.map { wiFiWidth ->
            val channels = wiFiBand.wiFiChannels.availableChannels(wiFiWidth, wiFiBand, countryCode).joinToString(", ")
            WidthState(wiFiWidth.textResource, channels)
        }.filter { it.channels.isNotEmpty() }

        return BandState(
            title = getString(wiFiBand.textResource),
            widths = widthStates
        )
    }
}
