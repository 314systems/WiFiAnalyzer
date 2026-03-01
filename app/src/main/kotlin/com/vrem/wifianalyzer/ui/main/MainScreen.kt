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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrem.wifianalyzer.WiFiAnalyzerApplication
import com.vrem.wifianalyzer.about.AboutScreen
import com.vrem.wifianalyzer.about.AboutViewModel
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.settings.SettingsScreen
import com.vrem.wifianalyzer.settings.SettingsViewModel
import com.vrem.wifianalyzer.vendor.VendorView
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointAlertDialog
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointsContent
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.channelavailable.ChannelAvailableContent
import com.vrem.wifianalyzer.wifi.channelavailable.ChannelAvailableViewModel
import com.vrem.wifianalyzer.wifi.channelgraph.ChannelGraphContent
import com.vrem.wifianalyzer.wifi.channelrating.ChannelRatingScreen
import com.vrem.wifianalyzer.wifi.channelrating.ChannelRatingViewModel
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.timegraph.TimeGraphContent
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    currentMenu: NavigationMenu,
    isScannerRunning: Boolean,
    isFilterActive: Boolean,
    currentWiFiBand: WiFiBand,
    onMenuSelected: (NavigationMenu) -> Unit,
    onToggleScanner: () -> Unit,
    onFilterClick: () -> Unit,
    onWiFiBandClick: (WiFiBand) -> Unit,
    onBackPressed: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            onBackPressed()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                MainDrawerContent(
                    selectedMenu = currentMenu,
                    onMenuSelected = { menu ->
                        onMenuSelected(menu)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                MainTopAppBar(
                    currentMenu = currentMenu,
                    isScannerRunning = isScannerRunning,
                    isFilterActive = isFilterActive,
                    currentWiFiBand = currentWiFiBand,
                    onNavigationClick = { scope.launch { drawerState.open() } },
                    onScannerClick = onToggleScanner,
                    onFilterClick = onFilterClick,
                    onWiFiBandClick = onWiFiBandClick
                )
            },
            bottomBar = {
                if (currentMenu.showBottomBar) {
                    MainBottomNavigation(
                        selectedMenu = currentMenu,
                        onMenuSelected = onMenuSelected
                    )
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                val connectionViewModel: MainConnectionViewModel = viewModel()
                if (currentMenu.registered()) {
                    val connectionState by connectionViewModel.state.collectAsStateWithLifecycle()
                    val selectedWiFiDetail by connectionViewModel.selectedWiFiDetail.collectAsStateWithLifecycle()

                    MainConnection(
                        state = connectionState,
                        onConnectionClick = { connectionViewModel.onSelectedWiFiDetail(it) }
                    )

                    selectedWiFiDetail?.let {
                        AccessPointAlertDialog(
                            wiFiDetail = it,
                            onDismiss = { connectionViewModel.onDismissPopup() }
                        )
                    }
                }
                MainContentArea(
                    currentMenu = currentMenu,
                    onShowPopup = { connectionViewModel.onSelectedWiFiDetail(it) }
                )
            }
        }
    }
}

@Composable
private fun MainContentArea(
    currentMenu: NavigationMenu,
    onShowPopup: (WiFiDetail) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as WiFiAnalyzerApplication

    when (currentMenu) {
        NavigationMenu.ACCESS_POINTS -> AccessPointsContent(onShowPopup = onShowPopup)
        NavigationMenu.CHANNEL_GRAPH -> ChannelGraphContent()
        NavigationMenu.TIME_GRAPH -> TimeGraphContent()
        NavigationMenu.CHANNEL_RATING -> ChannelRatingScreen(viewModel = viewModel<ChannelRatingViewModel>())
        NavigationMenu.CHANNEL_AVAILABLE -> {
            val channelAvailableViewModel = viewModel<ChannelAvailableViewModel>()
            val uiState by channelAvailableViewModel.uiState.collectAsStateWithLifecycle()
            ChannelAvailableContent(state = uiState)
        }

        NavigationMenu.VENDORS -> VendorView(vendorService = app.vendorService)
        NavigationMenu.SETTINGS -> SettingsScreen(viewModel = viewModel<SettingsViewModel>())
        NavigationMenu.ABOUT -> {
            val aboutViewModel = viewModel<AboutViewModel>()
            val uiState by aboutViewModel.uiState.collectAsStateWithLifecycle()
            AboutScreen(uiState = uiState, onWriteReviewClick = {})
        }

        else -> AccessPointsContent(onShowPopup = onShowPopup)
    }
}
