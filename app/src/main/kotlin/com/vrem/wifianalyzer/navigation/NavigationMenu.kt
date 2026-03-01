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
package com.vrem.wifianalyzer.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.vrem.wifianalyzer.R

val MAIN_NAVIGATION =
    listOf(
        NavigationMenu.ACCESS_POINTS,
        NavigationMenu.CHANNEL_RATING,
        NavigationMenu.CHANNEL_GRAPH,
        NavigationMenu.TIME_GRAPH,
    )

enum class NavigationMenu(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val showBottomBar: Boolean = false,
    private val isRegistered: Boolean = false,
    val scannerVisible: Boolean = false,
    val filterVisible: Boolean = false,
    val wiFiBandVisible: Boolean = false,
) {
    ACCESS_POINTS(
        R.string.action_access_points,
        R.drawable.wifi_24px,
        showBottomBar = true,
        isRegistered = true,
        scannerVisible = true,
        filterVisible = true,
        wiFiBandVisible = true,
    ),
    CHANNEL_RATING(
        R.string.action_channel_rating,
        R.drawable.wifi_tethering_24px,
        showBottomBar = true,
        isRegistered = true,
        scannerVisible = true,
        filterVisible = false,
        wiFiBandVisible = true,
    ),
    CHANNEL_GRAPH(
        R.string.action_channel_graph,
        R.drawable.insert_chart_24px,
        showBottomBar = true,
        isRegistered = true,
        scannerVisible = true,
        filterVisible = true,
        wiFiBandVisible = true,
    ),
    TIME_GRAPH(
        R.string.action_time_graph,
        R.drawable.show_chart_24px,
        showBottomBar = true,
        isRegistered = true,
        scannerVisible = true,
        filterVisible = true,
        wiFiBandVisible = true,
    ),
    EXPORT(
        R.string.action_export,
        R.drawable.ic_app, // Need a proper icon if used in drawer
    ),
    CHANNEL_AVAILABLE(
        R.string.action_channel_available,
        R.drawable.ic_app,
    ),
    VENDORS(
        R.string.action_vendors,
        R.drawable.ic_app,
    ),
    SETTINGS(
        R.string.action_settings,
        R.drawable.ic_app,
    ),
    ABOUT(
        R.string.action_about,
        R.drawable.ic_app,
    ),
    ;

    fun wiFiBandSwitchable(): Boolean = wiFiBandVisible

    fun registered(): Boolean = isRegistered
}
