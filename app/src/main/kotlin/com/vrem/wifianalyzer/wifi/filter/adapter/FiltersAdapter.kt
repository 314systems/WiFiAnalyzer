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
package com.vrem.wifianalyzer.wifi.filter.adapter

import com.vrem.util.findSet
import com.vrem.util.ordinals
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.Security
import com.vrem.wifianalyzer.wifi.model.Strength
import java.io.Serializable
import kotlin.enums.EnumEntries

class FiltersAdapter(
    private val repository: Repository,
) {
    private var ssidAdapter: SSIDAdapter = SSIDAdapter(findSSIDs())
    private var wiFiBandAdapter: WiFiBandAdapter = WiFiBandAdapter(findWiFiBands())
    private var strengthAdapter: StrengthAdapter = StrengthAdapter(findStrengths())
    private var securityAdapter: SecurityAdapter = SecurityAdapter(findSecurities())

    fun reload() {
        ssidAdapter = SSIDAdapter(findSSIDs())
        wiFiBandAdapter = WiFiBandAdapter(findWiFiBands())
        strengthAdapter = StrengthAdapter(findStrengths())
        securityAdapter = SecurityAdapter(findSecurities())
    }

    fun reset(isAccessPoints: Boolean) =
        filterAdapters(isAccessPoints).forEach {
            it.reset()
            it.save(repository)
        }

    fun save(isAccessPoints: Boolean) =
        filterAdapters(isAccessPoints).forEach { it.save(repository) }

    fun ssidAdapter(): SSIDAdapter = ssidAdapter

    fun wiFiBandAdapter(): WiFiBandAdapter = wiFiBandAdapter

    fun strengthAdapter(): StrengthAdapter = strengthAdapter

    fun securityAdapter(): SecurityAdapter = securityAdapter

    internal fun isActive(isAccessPoints: Boolean): Boolean =
        filterAdapters(isAccessPoints).any { it.isActive() }

    internal fun filterAdapters(accessPoints: Boolean): List<BasicFilterAdapter<out Serializable>> =
        if (accessPoints) {
            listOf(ssidAdapter, strengthAdapter, securityAdapter, wiFiBandAdapter)
        } else {
            listOf(ssidAdapter, strengthAdapter, securityAdapter)
        }

    private fun findSSIDs(): Set<String> = repository.stringSet(R.string.filter_ssid_key, setOf())

    private fun findWiFiBands(): Set<WiFiBand> =
        settingsFindSet(WiFiBand.entries, R.string.filter_wifi_band_key, WiFiBand.GHZ2)

    private fun findStrengths(): Set<Strength> =
        settingsFindSet(Strength.entries, R.string.filter_strength_key, Strength.FOUR)

    private fun findSecurities(): Set<Security> =
        settingsFindSet(Security.entries, R.string.filter_security_key, Security.NONE)

    private fun <T : Enum<T>> settingsFindSet(
        values: EnumEntries<T>,
        key: Int,
        defaultValue: T,
    ): Set<T> {
        val ordinalDefault = ordinals(values)
        val ordinalSaved = repository.stringSet(key, ordinalDefault)
        return findSet(values, ordinalSaved, defaultValue)
    }
}
