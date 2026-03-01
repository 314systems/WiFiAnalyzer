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

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.WiFiAnalyzerApplication
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.wifi.band.WiFiBand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    currentMenu: NavigationMenu,
    isScannerRunning: Boolean,
    isFilterActive: Boolean,
    currentWiFiBand: WiFiBand,
    onNavigationClick: () -> Unit,
    onScannerClick: () -> Unit,
    onFilterClick: () -> Unit,
    onWiFiBandClick: (WiFiBand) -> Unit,
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
            if (currentMenu.wiFiBandVisible) {
                WiFiBandSelector(
                    currentWiFiBand = currentWiFiBand,
                    onWiFiBandClick = onWiFiBandClick
                )
            }
            if (currentMenu.filterVisible) {
                IconButton(onClick = onFilterClick) {
                    val filterIconColor = if (isFilterActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Unspecified
                    }
                    Icon(
                        painter = painterResource(R.drawable.filter_list_24px),
                        contentDescription = stringResource(R.string.filter_title),
                        tint = filterIconColor
                    )
                }
            }
            if (currentMenu.scannerVisible) {
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
            }
        }
    )
}

@Composable
private fun WiFiBandSelector(
    currentWiFiBand: WiFiBand,
    onWiFiBandClick: (WiFiBand) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as WiFiAnalyzerApplication
    var expanded by remember { mutableStateOf(false) }

    TextButton(onClick = { expanded = true }) {
        Text(
            text = stringResource(currentWiFiBand.textResource).replace(' ', '\n'),
            style = MaterialTheme.typography.labelLarge
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        WiFiBand.entries.forEach { band ->
            if (band.available(app)) {
                DropdownMenuItem(
                    text = { Text(stringResource(band.textResource)) },
                    onClick = {
                        onWiFiBandClick(band)
                        expanded = false
                    }
                )
            }
        }
    }
}
