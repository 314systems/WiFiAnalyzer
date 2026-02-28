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
package com.vrem.wifianalyzer

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.vrem.util.SPACE_SEPARATOR
import com.vrem.util.createContext
import com.vrem.util.specialTrim
import com.vrem.wifianalyzer.databinding.MainActivityBinding
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.navigation.NavigationMenuControl
import com.vrem.wifianalyzer.navigation.NavigationMenuController
import com.vrem.wifianalyzer.navigation.options.OptionMenu
import com.vrem.wifianalyzer.permission.PermissionHandler
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.ui.filter.FilterDialog
import com.vrem.wifianalyzer.ui.main.MainBottomNavigation
import com.vrem.wifianalyzer.ui.main.MainTopAppBar
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionView
import com.vrem.wifianalyzer.wifi.scanner.ScannerService

class MainActivity :
    AppCompatActivity(),
    NavigationMenuControl,
    OnSharedPreferenceChangeListener {
    internal lateinit var drawerNavigation: DrawerNavigation
    internal lateinit var mainReload: MainReload
    internal lateinit var navigationMenuController: NavigationMenuController
    internal lateinit var optionMenu: OptionMenu
    internal lateinit var connectionView: ConnectionView
    private lateinit var binding: MainActivityBinding

    var showFilterDialog by mutableStateOf(false)
    var isScannerRunning by mutableStateOf(false)

    override fun attachBaseContext(newBase: Context) =
        super.attachBaseContext(newBase.createContext(Settings(Repository(newBase)).languageLocale()))

    override fun onCreate(savedInstanceState: Bundle?) {
        val mainContext = MainContext.INSTANCE
        mainContext.initialize(this, largeScreen)

        val settings = mainContext.settings
        settings.initializeDefaultValues()
        settings.themeStyle().setTheme(this)

        mainReload = MainReload(settings)

        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.permissionComposeView.apply {
            setContent {
                AppTheme {
                    PermissionHandler(
                        onPermissionGranted = { update() },
                        onTerminateApp = { finish() }
                    )

                    if (showFilterDialog) {
                        FilterDialog(
                            filtersAdapter = MainContext.INSTANCE.filtersAdapter,
                            isAccessPoints = currentNavigationMenu() == NavigationMenu.ACCESS_POINTS,
                            onApply = { ssid, bands, strengths, securities ->
                                with(MainContext.INSTANCE.filtersAdapter) {
                                    ssidAdapter().selections = ssid.specialTrim().split(String.SPACE_SEPARATOR).toSet()
                                    wiFiBandAdapter().selections = bands
                                    strengthAdapter().selections = strengths
                                    securityAdapter().selections = securities
                                    save()
                                }
                                update()
                                showFilterDialog = false
                            },
                            onReset = {
                                MainContext.INSTANCE.filtersAdapter.reset()
                                update()
                                showFilterDialog = false
                            },
                            onClose = {
                                MainContext.INSTANCE.filtersAdapter.reload()
                                showFilterDialog = false
                            }
                        )
                    }
                }
            }
        }

        settings.registerOnSharedPreferenceChangeListener(this)
        optionMenu = OptionMenu()

        keepScreenOn()

        drawerNavigation = DrawerNavigation(this)
        drawerNavigation.create()

        navigationMenuController = NavigationMenuController(this)
        navigationMenuController.currentNavigationMenu(settings.selectedMenu())

        binding.mainContent.toolbarComposeView.setContent {
            AppTheme {
                MainTopAppBar(
                    currentMenu = navigationMenuController.selectedMenu,
                    isScannerRunning = isScannerRunning,
                    onNavigationClick = { drawerNavigation.toggle() },
                    onScannerClick = {
                        MainContext.INSTANCE.scannerService.toggle()
                        update()
                    },
                    onFilterClick = { showFilterDialog = true }
                )
            }
        }

        binding.mainContent.navBottomComposeView.setContent {
            AppTheme {
                MainBottomNavigation(
                    selectedMenu = navigationMenuController.selectedMenu,
                    onMenuSelected = { menu ->
                        val menuItem =
                            navigationMenuController.drawerNavigationView.menu.findItem(menu.idDrawer)
                        if (menuItem != null) {
                            onNavigationItemSelected(menuItem)
                        }
                    }
                )
            }
        }

        onNavigationItemSelected(currentMenuItem())

        connectionView = ConnectionView(this)

        onBackPressedDispatcher.addCallback(this, MainActivityBackPressed(this))
    }

    public override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerNavigation.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerNavigation.onConfigurationChanged(newConfig)
    }

    private val largeScreen: Boolean
        get() {
            val configuration = resources.configuration
            val screenLayoutSize = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
            return screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?,
    ) {
        val mainContext = MainContext.INSTANCE
        if (mainReload.shouldReload(mainContext.settings)) {
            MainContext.INSTANCE.scannerService.stop()
            recreate()
        } else {
            keepScreenOn()
            update()
        }
    }

    fun update() {
        val scannerService = MainContext.INSTANCE.scannerService
        scannerService.update()
        isScannerRunning = scannerService.running()
        updateActionBar()
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        closeDrawer()
        val currentNavigationMenu = NavigationMenu.find(menuItem.itemId)
        currentNavigationMenu.activateNavigationMenu(this)
        return true
    }

    fun closeDrawer(): Boolean {
        val drawer = binding.drawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
            return true
        }
        return false
    }

    public override fun onPause() {
        val scannerService: ScannerService = MainContext.INSTANCE.scannerService
        scannerService.pause()
        scannerService.unregister(connectionView)
        update()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        val scannerService: ScannerService = MainContext.INSTANCE.scannerService
        if (MainContext.INSTANCE.permissionService.permissionGranted()) {
            scannerService.resume()
        }
        update()
        scannerService.register(connectionView)
    }

    public override fun onStop() {
        MainContext.INSTANCE.scannerService.stop()
        update()
        super.onStop()
    }

    public override fun onStart() {
        super.onStart()
        update()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        optionMenu.create(this, menu)
        updateActionBar()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionMenu.select(item)
        updateActionBar()
        return true
    }

    fun updateActionBar() = currentNavigationMenu().activateOptions(this)

    override fun currentMenuItem(): MenuItem = navigationMenuController.currentMenuItem()

    override fun currentNavigationMenu(): NavigationMenu = navigationMenuController.currentNavigationMenu()

    override fun currentNavigationMenu(navigationMenu: NavigationMenu) {
        navigationMenuController.currentNavigationMenu(navigationMenu)
        MainContext.INSTANCE.settings.saveSelectedMenu(navigationMenu)
    }

    override fun navigationView(): NavigationView = navigationMenuController.drawerNavigationView

    fun mainConnectionVisibility(visibility: Int) {
        binding.mainContent.mainConnectionComposeView.visibility = visibility
    }
}
