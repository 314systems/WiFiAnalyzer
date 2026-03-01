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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.vrem.util.findOne
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.SIZE_MAX
import com.vrem.wifianalyzer.SIZE_MIN
import com.vrem.wifianalyzer.WiFiAnalyzerApplication
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.wifi.graphutils.TYPE1
import com.vrem.wifianalyzer.wifi.graphutils.TYPE2
import com.vrem.wifianalyzer.wifi.graphutils.TYPE3
import com.vrem.wifianalyzer.wifi.model.GroupBy
import com.vrem.wifianalyzer.wifi.model.SortBy
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.predicate.makeAccessPointsPredicate
import com.vrem.wifianalyzer.wifi.scanner.UpdateNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import kotlin.enums.EnumEntries

class AccessPointsViewModel(application: Application) : AndroidViewModel(application),
    UpdateNotifier {
    private val app = application as WiFiAnalyzerApplication
    private val repository = Repository(application)
    private val _wiFiDetails = MutableStateFlow<List<WiFiDetail>>(emptyList())
    val wiFiDetails: StateFlow<List<WiFiDetail>> = _wiFiDetails.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _expandedGroups = MutableStateFlow<Set<String>>(emptySet())
    val expandedGroups: StateFlow<Set<String>> = _expandedGroups.asStateFlow()

    private var groupBy: GroupBy = GroupBy.NONE

    private val _accessPointView = MutableStateFlow(readAccessPointView())
    val accessPointView: StateFlow<AccessPointViewType> = _accessPointView.asStateFlow()

    private val _groupByState = MutableStateFlow(readGroupBy())
    val groupByState: StateFlow<GroupBy> = _groupByState.asStateFlow()

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
        app.configuration.size = type(calculateChildType())

        val predicate = makeAccessPointsPredicate(repository)
        val currentGroupBy = readGroupBy()
        val currentSortBy = readSortBy()

        if (currentGroupBy != groupBy) {
            _expandedGroups.value = emptySet()
            groupBy = currentGroupBy
            _groupByState.value = currentGroupBy
        }
        _accessPointView.value = readAccessPointView()

        val details = wiFiData.wiFiDetails(predicate, currentSortBy, currentGroupBy)
        _wiFiDetails.value = details

        _isRefreshing.value = false
    }

    fun refresh() {
        if (app.isScannerServiceInitialized) {
            _isRefreshing.value = true
            app.scannerService.update()
        }
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
                update(app.packageName.toByteArray())
                val digest: ByteArray = digest()
                digest.contentHashCode()
            }
        }.getOrDefault(TYPE1)

    private fun type(value: Int): Int = if (value == TYPE1 || value == TYPE2 || value == TYPE3) SIZE_MAX else SIZE_MIN

    private fun readGroupBy(): GroupBy =
        settingsFind(GroupBy.entries, R.string.group_by_key, GroupBy.NONE)

    private fun readSortBy(): SortBy =
        settingsFind(SortBy.entries, R.string.sort_by_key, SortBy.STRENGTH)

    private fun readAccessPointView(): AccessPointViewType =
        settingsFind(
            AccessPointViewType.entries,
            R.string.ap_view_key,
            AccessPointViewType.COMPLETE
        )

    private fun <T : Enum<T>> settingsFind(
        values: EnumEntries<T>,
        key: Int,
        defaultValue: T,
    ): T {
        val value = repository.stringAsInteger(key, defaultValue.ordinal)
        return findOne(values, value, defaultValue)
    }
}
