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
package com.vrem.wifianalyzer.permission

import android.os.Build
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.RobolectricUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class PermissionDialogFragmentTest {
    private val activity = RobolectricUtil.INSTANCE.activity
    private val fixture = PermissionDialogFragment()

    @Test
    fun show() {
        // execute
        RobolectricUtil.INSTANCE.startFragment(fixture)
        fixture.show(activity.supportFragmentManager, PermissionDialogFragment.TAG)
        RobolectricUtil.INSTANCE.clearLooper()
        // validate
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog).isNotNull()
        assertThat(dialog.isShowing).isTrue()
    }

    @Test
    fun showThrottlingVisible() {
        // execute
        RobolectricUtil.INSTANCE.startFragment(fixture)
        val dialog = fixture.onCreateDialog(null)
        // validate
        assertThat(dialog).isNotNull()
        val view = (dialog as android.app.AlertDialog).findViewById<View>(R.id.throttling)
        assertThat(view?.isVisible).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    fun showThrottlingGone() {
        // execute
        RobolectricUtil.INSTANCE.startFragment(fixture)
        val dialog = fixture.onCreateDialog(null)
        // validate
        assertThat(dialog).isNotNull()
        val view = (dialog as android.app.AlertDialog).findViewById<View>(R.id.throttling)
        assertThat(view?.isGone).isTrue()
    }

    @Test
    fun tag() {
        assertThat(PermissionDialogFragment.TAG).isEqualTo("PermissionDialogFragment")
    }
}
