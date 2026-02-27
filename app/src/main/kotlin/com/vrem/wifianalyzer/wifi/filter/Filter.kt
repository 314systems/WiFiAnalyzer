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
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.ui.filter.FilterPopupContent
import com.vrem.wifianalyzer.ui.theme.AppTheme

class Filter(
    val alertDialog: AlertDialog?,
) {
    fun show() {
        if (alertDialog != null && !alertDialog.isShowing) {
            alertDialog.show()
        }
    }

    private class Close : DialogInterface.OnClickListener {
        override fun onClick(
            dialog: DialogInterface,
            which: Int,
        ) {
            dialog.dismiss()
            MainContext.INSTANCE.filtersAdapter.reload()
        }
    }

    private class Apply : DialogInterface.OnClickListener {
        override fun onClick(
            dialog: DialogInterface,
            which: Int,
        ) {
            dialog.dismiss()
            MainContext.INSTANCE.filtersAdapter.save()
            MainContext.INSTANCE.mainActivity.update()
        }
    }

    private class Reset : DialogInterface.OnClickListener {
        override fun onClick(
            dialog: DialogInterface,
            which: Int,
        ) {
            dialog.dismiss()
            MainContext.INSTANCE.filtersAdapter.reset()
            MainContext.INSTANCE.mainActivity.update()
        }
    }

    companion object {
        fun build(): Filter = Filter(buildAlertDialog())

        private fun buildAlertDialog(): AlertDialog? {
            val mainActivity = MainContext.INSTANCE.mainActivity
            if (mainActivity.isFinishing) {
                return null
            }
            val composeView = ComposeView(mainActivity).apply {
                setContent {
                    AppTheme {
                        FilterPopupContent(
                            filtersAdapter = MainContext.INSTANCE.filtersAdapter,
                            isAccessPoints = mainActivity.currentNavigationMenu() == NavigationMenu.ACCESS_POINTS
                        )
                    }
                }
            }
            return AlertDialog
                .Builder(mainActivity)
                .setView(composeView)
                .setTitle(R.string.filter_title)
                .setIcon(R.drawable.ic_filter_list)
                .setNegativeButton(R.string.filter_reset, Reset())
                .setNeutralButton(R.string.filter_close, Close())
                .setPositiveButton(R.string.filter_apply, Apply())
                .create()
        }
    }
}
