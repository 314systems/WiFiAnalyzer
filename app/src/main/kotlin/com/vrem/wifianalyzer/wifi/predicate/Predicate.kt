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
package com.vrem.wifianalyzer.wifi.predicate

import com.vrem.util.findSet
import com.vrem.util.ordinals
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.SSID
import com.vrem.wifianalyzer.wifi.model.Security
import com.vrem.wifianalyzer.wifi.model.Strength
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import kotlin.enums.EnumEntries

internal typealias Predicate = (wiFiDetail: WiFiDetail) -> Boolean
internal typealias ToPredicate<T> = (T) -> Predicate

internal val truePredicate: Predicate = { true }
internal val falsePredicate: Predicate = { false }

internal fun List<Predicate>.anyPredicate(): Predicate =
    { wiFiDetail -> this.any { predicate -> predicate(wiFiDetail) } }

internal fun List<Predicate>.allPredicate(): Predicate =
    { wiFiDetail -> this.all { predicate -> predicate(wiFiDetail) } }

fun WiFiBand.predicate(): Predicate = { wiFiDetail -> wiFiDetail.wiFiSignal.wiFiBand == this }

internal fun Strength.predicate(): Predicate = { wiFiDetail -> wiFiDetail.wiFiSignal.strength == this }

internal fun SSID.predicate(): Predicate = { wiFiDetail -> wiFiDetail.wiFiIdentifier.ssid.contains(this) }

internal fun Security.predicate(): Predicate = { wiFiDetail -> wiFiDetail.wiFiSecurity.securities.contains(this) }

private fun Set<SSID>.ssidPredicate(): Predicate =
    if (this.isEmpty()) {
        truePredicate
    } else {
        this.map { it.predicate() }.anyPredicate()
    }

internal fun <T : Enum<T>> makePredicate(
    values: EnumEntries<T>,
    filter: Set<T>,
    toPredicate: ToPredicate<T>,
): Predicate =
    if (filter.size >= values.size) {
        truePredicate
    } else {
        filter.map { toPredicate(it) }.anyPredicate()
    }

private fun predicates(
    repository: Repository,
    wiFiBands: Set<WiFiBand>,
): List<Predicate> =
    listOf(
        repository.stringSet(R.string.filter_ssid_key, setOf()).ssidPredicate(),
        makePredicate(WiFiBand.entries, wiFiBands) { wiFiBand -> wiFiBand.predicate() },
        makePredicate(
            Strength.entries,
            settingsFindSet(
                repository,
                Strength.entries,
                R.string.filter_strength_key,
                Strength.FOUR
            )
        ) { strength -> strength.predicate() },
        makePredicate(
            Security.entries,
            settingsFindSet(
                repository,
                Security.entries,
                R.string.filter_security_key,
                Security.NONE
            )
        ) { security -> security.predicate() },
    )

fun makeAccessPointsPredicate(repository: Repository): Predicate =
    predicates(
        repository,
        settingsFindSet(repository, WiFiBand.entries, R.string.filter_wifi_band_key, WiFiBand.GHZ2)
    ).allPredicate()

fun makeOtherPredicate(repository: Repository): Predicate {
    val wiFiBandValue = repository.stringAsInteger(R.string.wifi_band_key, WiFiBand.GHZ2.ordinal)
    val wiFiBand = WiFiBand.entries.getOrElse(wiFiBandValue) { WiFiBand.GHZ2 }
    return predicates(repository, setOf(wiFiBand)).allPredicate()
}

private fun <T : Enum<T>> settingsFindSet(
    repository: Repository,
    values: EnumEntries<T>,
    key: Int,
    defaultValue: T,
): Set<T> {
    val ordinalDefault = ordinals(values)
    val ordinalSaved = repository.stringSet(key, ordinalDefault)
    return findSet(values, ordinalSaved, defaultValue)
}
