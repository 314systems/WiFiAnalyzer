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

import android.view.Menu
import android.view.MenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.forEach
import androidx.core.view.get
import com.google.android.material.navigation.NavigationView
import com.vrem.wifianalyzer.R

class NavigationMenuController(
    navigationMenuControl: NavigationMenuControl,
    val drawerNavigationView: NavigationView = navigationMenuControl.findViewById(R.id.nav_drawer),
) {
    var selectedMenu by mutableStateOf(NavigationMenu.ACCESS_POINTS)
        private set

    fun currentMenuItem(): MenuItem = drawerNavigationView.menu[selectedMenu.ordinal]

    fun currentNavigationMenu(): NavigationMenu = selectedMenu

    fun currentNavigationMenu(navigationMenu: NavigationMenu) {
        selectedMenu = navigationMenu
        setChecked(drawerNavigationView.menu, navigationMenu.idDrawer)
    }

    private fun setChecked(
        menu: Menu,
        id: Int,
    ) {
        if (id != -1) {
            menu.forEach { it.isChecked = false }
            menu.findItem(id)?.let { it.isChecked = true }
        }
    }

    init {
        drawerNavigationView.setNavigationItemSelectedListener(navigationMenuControl)
    }
}
