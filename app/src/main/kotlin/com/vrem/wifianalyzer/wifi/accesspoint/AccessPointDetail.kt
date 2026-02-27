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
package com.vrem.wifianalyzer.wifi.accesspoint

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.model.WiFiAdditional
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiSignal

class AccessPointDetail {
    fun makeView(
        convertView: View?,
        parent: ViewGroup?,
        wiFiDetail: WiFiDetail,
        child: Boolean = false,
        @LayoutRes layout: Int =
            MainContext.INSTANCE.settings
                .accessPointView()
                .layout,
    ): View {
        val view = convertView ?: MainContext.INSTANCE.layoutInflater.inflate(layout, parent, false)
        setViewCompact(view, wiFiDetail, child)
        setViewExtra(view, wiFiDetail)
        setViewVendorShort(view, wiFiDetail.wiFiAdditional)
        return view
    }

    private fun setViewCompact(
        view: View,
        wiFiDetail: WiFiDetail,
        child: Boolean,
    ) = view.findViewById<TextView>(R.id.ssid)?.let {
        it.text = wiFiDetail.wiFiIdentifier.title
        val wiFiSignal = wiFiDetail.wiFiSignal
        view.findViewById<TextView>(R.id.channel).text = wiFiSignal.channelDisplay()
        view.findViewById<TextView>(R.id.primaryFrequency).text =
            "${wiFiSignal.primaryFrequency}${WiFiSignal.FREQUENCY_UNITS}"
        view.findViewById<TextView>(R.id.distance).text = wiFiSignal.distance
        view.findViewById<View>(R.id.tab).visibility = if (child) View.VISIBLE else View.GONE
        setSecurityImage(view, wiFiDetail)
        setLevelText(view, wiFiSignal)
    }

    private fun setSecurityImage(
        view: View,
        wiFiDetail: WiFiDetail,
    ) = view.findViewById<ImageView>(R.id.securityImage)?.let {
        val security = wiFiDetail.wiFiSecurity.security
        it.tag = security.imageResource
        it.setImageResource(security.imageResource)
    }

    private fun setViewExtra(
        view: View,
        wiFiDetail: WiFiDetail,
    ) = view.findViewById<TextView>(R.id.channel_frequency_range)?.let {
        val wiFiSignal = wiFiDetail.wiFiSignal
        setLevelImage(view, wiFiSignal)
        setWiFiStandardImage(view, wiFiSignal)
        setWiFiWidth(view, wiFiSignal)
        it.text = "${wiFiSignal.wiFiChannelStart.frequency} - ${wiFiSignal.wiFiChannelEnd.frequency}"
        view.findViewById<TextView>(R.id.capabilities).text =
            wiFiDetail.wiFiSecurity.securities
                .toList()
                .joinToString(" ", "[", "]")
    }

    private fun setWiFiWidth(
        view: View,
        wiFiSignal: WiFiSignal,
    ) = view.findViewById<TextView>(R.id.width)?.let {
        it.text = ContextCompat.getString(view.context, wiFiSignal.wiFiWidth.textResource)
    }

    private fun setWiFiStandardImage(
        view: View,
        wiFiSignal: WiFiSignal,
    ) = view.findViewById<TextView>(R.id.wiFiStandardValue)?.let {
        it.text = ContextCompat.getString(view.context, wiFiSignal.extra.wiFiStandard.valueResource)
    }

    private fun setLevelText(
        view: View,
        wiFiSignal: WiFiSignal,
    ) = view.findViewById<TextView>(R.id.level)?.let {
        it.text = "${wiFiSignal.level}dBm"
        it.setTextColor(ContextCompat.getColor(view.context, wiFiSignal.strengthColor))
    }

    private fun setLevelImage(
        view: View,
        wiFiSignal: WiFiSignal,
    ) = view.findViewById<ImageView>(R.id.levelImage)?.let {
        val strength = wiFiSignal.strength
        it.tag = strength.imageResource
        it.setImageResource(strength.imageResource)
        it.setColorFilter(ContextCompat.getColor(view.context, wiFiSignal.strengthColor))
    }

    private fun setViewVendorShort(
        view: View,
        wiFiAdditional: WiFiAdditional,
    ) = view.findViewById<TextView>(R.id.vendorShort)?.let {
        if (wiFiAdditional.vendorName.isBlank()) {
            it.visibility = View.GONE
        } else {
            it.visibility = View.VISIBLE
            it.text = wiFiAdditional.vendorName
        }
    }
}
