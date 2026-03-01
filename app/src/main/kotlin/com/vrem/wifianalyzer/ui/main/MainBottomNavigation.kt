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
package com.vrem.wifianalyzer.ui.main

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vrem.wifianalyzer.navigation.MAIN_NAVIGATION
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.ui.theme.AppTheme

@Composable
fun MainBottomNavigation(
    selectedMenu: NavigationMenu,
    onMenuSelected: (NavigationMenu) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier) {
        MAIN_NAVIGATION.forEach { menu ->
            NavigationBarItem(
                selected = selectedMenu == menu,
                onClick = {
                    onMenuSelected(menu)
                },
                icon = {
                    Icon(
                        painter = painterResource(id = menu.icon),
                        contentDescription = stringResource(id = menu.title)
                    )
                },
                label = {
                    Text(text = stringResource(id = menu.title))
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainBottomNavigationPreview() {
    AppTheme {
        MainBottomNavigation(
            selectedMenu = NavigationMenu.ACCESS_POINTS,
            onMenuSelected = {}
        )
    }
}
