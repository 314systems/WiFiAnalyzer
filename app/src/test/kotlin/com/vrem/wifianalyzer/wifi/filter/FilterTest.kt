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
package com.vrem.wifianalyzer.wifi.filter

import android.content.DialogInterface
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.MainActivity
import com.vrem.wifianalyzer.MainContextHelper
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.RobolectricUtil
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.wifi.filter.Filter.Companion.build
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class FilterTest {
    private val mainActivity: MainActivity = RobolectricUtil.INSTANCE.activity
    private val fixture: Filter = build()

    @Before
    fun setUp() {
        RobolectricUtil.INSTANCE.clearLooper()
    }

    @After
    fun tearDown() {
        mainActivity.currentNavigationMenu(NavigationMenu.ACCESS_POINTS)
        MainContextHelper.INSTANCE.restore()
    }

    @Test
    fun alertDialog() {
        // execute
        val actual = fixture.alertDialog!!
        // validate
        assertThat(actual.isShowing).isFalse
    }

    @Test
    fun show() {
        // execute
        fixture.show()
        // validate
        assertThat(fixture.alertDialog!!.isShowing).isTrue
    }

    @Test
    fun title() {
        // setup
        val expected = mainActivity.resources.getString(R.string.filter_title)
        val shadowAlertDialog = Shadows.shadowOf(fixture.alertDialog!!)
        // execute
        val actual = shadowAlertDialog.title
        // validate
        assertThat(actual.toString()).isEqualTo(expected)
    }

    @Test
    fun positiveButton() {
        // setup
        fixture.show()
        val button = fixture.alertDialog!!.getButton(DialogInterface.BUTTON_POSITIVE)
        val filtersAdapter = MainContextHelper.INSTANCE.filterAdapter
        val mainActivity = MainContextHelper.INSTANCE.mainActivity
        // execute
        button.performClick()
        // validate
        RobolectricUtil.INSTANCE.clearLooper()
        assertThat(fixture.alertDialog.isShowing).isFalse
        verify(filtersAdapter).save()
        verify(mainActivity).update()
    }

    @Test
    fun negativeButton() {
        // setup
        fixture.show()
        val button = fixture.alertDialog!!.getButton(DialogInterface.BUTTON_NEGATIVE)
        val filtersAdapter = MainContextHelper.INSTANCE.filterAdapter
        val mainActivity = MainContextHelper.INSTANCE.mainActivity
        // execute
        button.performClick()
        // validate
        RobolectricUtil.INSTANCE.clearLooper()
        assertThat(fixture.alertDialog.isShowing).isFalse
        verify(filtersAdapter).reset()
        verify(mainActivity).update()
    }

    @Test
    fun neutralButton() {
        // setup
        fixture.show()
        val button = fixture.alertDialog!!.getButton(DialogInterface.BUTTON_NEUTRAL)
        val filtersAdapter = MainContextHelper.INSTANCE.filterAdapter
        val mainActivity = MainContextHelper.INSTANCE.mainActivity
        // execute
        button.performClick()
        // validate
        RobolectricUtil.INSTANCE.clearLooper()
        assertThat(fixture.alertDialog.isShowing).isFalse
        verify(filtersAdapter).reload()
        verify(mainActivity, never()).update()
    }

    @Test
    fun showWhenDialogIsNull() {
        // setup
        val fixture = Filter(null)
        // execute
        fixture.show()
        // validate
        assertThat(fixture.alertDialog).isNull()
    }

    @Test
    fun showWhenAlreadyShowing() {
        // setup
        fixture.show()
        // execute
        fixture.show()
        // validate
        assertThat(fixture.alertDialog!!.isShowing).isTrue
    }

    @Test
    fun buildReturnsNullDialogWhenActivityIsFinishing() {
        // setup
        val mainActivity = MainContextHelper.INSTANCE.mainActivity
        doReturn(true).whenever(mainActivity).isFinishing
        // execute
        val actual = build()
        // validate
        assertThat(actual.alertDialog).isNull()
        verify(mainActivity).isFinishing
    }
}
