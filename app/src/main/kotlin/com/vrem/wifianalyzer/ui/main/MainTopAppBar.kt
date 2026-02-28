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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.navigation.NavigationMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    currentMenu: NavigationMenu,
    isScannerRunning: Boolean,
    onNavigationClick: () -> Unit,
    onScannerClick: () -> Unit,
    onFilterClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(currentMenu.title)) },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    painter = painterResource(R.drawable.menu_24px),
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            IconButton(onClick = onScannerClick) {
                val scannerIcon =
                    if (isScannerRunning) R.drawable.pause_24px else R.drawable.play_arrow_24px
                val scannerText =
                    if (isScannerRunning) R.string.scanner_pause else R.string.scanner_play
                Icon(
                    painter = painterResource(scannerIcon),
                    contentDescription = stringResource(scannerText)
                )
            }
            IconButton(onClick = onFilterClick) {
                Icon(
                    painter = painterResource(R.drawable.filter_list_24px),
                    contentDescription = stringResource(R.string.filter_title)
                )
            }
        }
    )
}
