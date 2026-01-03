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

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.settings.ThemeStyle
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class MainActivityThemeTest {

    @After
    fun tearDown() {
        MainContextHelper.INSTANCE.restore()
    }

    @Test
    fun onCreateWithDarkThemeAppliesDarkMode() {
        // setup
        val settings = MainContextHelper.INSTANCE.settings
        whenever(settings.themeStyle()).thenReturn(ThemeStyle.DARK)

        // execute
        Robolectric.buildActivity(MainActivity::class.java).create().get()

        // validate
        verify(settings).themeStyle()
        assertThat(AppCompatDelegate.getDefaultNightMode())
            .isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
    }

    @Test
    fun onCreateWithLightThemeAppliesLightMode() {
        // setup
        val settings = MainContextHelper.INSTANCE.settings
        whenever(settings.themeStyle()).thenReturn(ThemeStyle.LIGHT)

        // execute
        Robolectric.buildActivity(MainActivity::class.java).create().get()

        // validate
        verify(settings).themeStyle()
        assertThat(AppCompatDelegate.getDefaultNightMode())
            .isEqualTo(AppCompatDelegate.MODE_NIGHT_NO)
    }

    @Test
    fun onCreateWithSystemThemeAppliesFollowSystem() {
        // setup
        val settings = MainContextHelper.INSTANCE.settings
        whenever(settings.themeStyle()).thenReturn(ThemeStyle.SYSTEM)

        // execute
        Robolectric.buildActivity(MainActivity::class.java).create().get()

        // validate
        verify(settings).themeStyle()
        assertThat(AppCompatDelegate.getDefaultNightMode())
            .isEqualTo(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    @Test
    fun onCreateWithBlackThemeAppliesDarkMode() {
        // setup
        val settings = MainContextHelper.INSTANCE.settings
        whenever(settings.themeStyle()).thenReturn(ThemeStyle.BLACK)

        // execute
        Robolectric.buildActivity(MainActivity::class.java).create().get()

        // validate
        verify(settings).themeStyle()
        assertThat(AppCompatDelegate.getDefaultNightMode())
            .isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
