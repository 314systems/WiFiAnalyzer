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

import androidx.lifecycle.ViewModel
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.SIZE_MAX
import com.vrem.wifianalyzer.SIZE_MIN
import com.vrem.wifianalyzer.wifi.graphutils.TYPE1
import com.vrem.wifianalyzer.wifi.graphutils.TYPE2
import com.vrem.wifianalyzer.wifi.graphutils.TYPE3
import com.vrem.wifianalyzer.wifi.model.GroupBy
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.predicate.makeAccessPointsPredicate
import com.vrem.wifianalyzer.wifi.scanner.UpdateNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class AccessPointsViewModel : ViewModel(), UpdateNotifier {
    private val _wiFiDetails = MutableStateFlow<List<WiFiDetail>>(emptyList())
    val wiFiDetails: StateFlow<List<WiFiDetail>> = _wiFiDetails.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _expandedGroups = MutableStateFlow<Set<String>>(emptySet())
    val expandedGroups: StateFlow<Set<String>> = _expandedGroups.asStateFlow()

    private var groupBy: GroupBy = GroupBy.NONE

    init {
        MainContext.INSTANCE.scannerService.register(this)
        update(MainContext.INSTANCE.scannerService.wiFiData())
    }

    override fun onCleared() {
        MainContext.INSTANCE.scannerService.unregister(this)
        super.onCleared()
    }

    override fun update(wiFiData: WiFiData) {
        MainContext.INSTANCE.configuration.size = type(calculateChildType())

        val settings = MainContext.INSTANCE.settings
        val predicate = makeAccessPointsPredicate(settings)
        val currentGroupBy = settings.groupBy()

        if (currentGroupBy != groupBy) {
            _expandedGroups.value = emptySet()
            groupBy = currentGroupBy
        }

        val details = wiFiData.wiFiDetails(predicate, settings.sortBy(), settings.groupBy())
        _wiFiDetails.value = details

        _isRefreshing.value = false
    }

    fun refresh() {
        _isRefreshing.value = true
        MainContext.INSTANCE.scannerService.update()
    }

    fun toggleGroup(wiFiDetail: WiFiDetail) {
        val group = groupBy.group(wiFiDetail)
        val currentExpanded = _expandedGroups.value.toMutableSet()
        if (currentExpanded.contains(group)) {
            currentExpanded.remove(group)
        } else {
            currentExpanded.add(group)
        }
        _expandedGroups.value = currentExpanded
    }

    private fun calculateChildType(): Int =
        runCatching {
            with(MessageDigest.getInstance("MD5")) {
                update(
                    MainContext.INSTANCE.mainActivity.packageName
                        .toByteArray(),
                )
                val digest: ByteArray = digest()
                digest.contentHashCode()
            }
        }.getOrDefault(TYPE1)

    private fun type(value: Int): Int = if (value == TYPE1 || value == TYPE2 || value == TYPE3) SIZE_MAX else SIZE_MIN
}
