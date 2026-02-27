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
package com.vrem.wifianalyzer.wifi.accesspoint

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

class AccessPointPopup {
    fun show(
        view: View,
        wiFiDetail: WiFiDetail,
    ): AlertDialog {
        val composeView =
            ComposeView(view.context).apply {
                setContent {
                    AppTheme {
                        AccessPointViewPopup(wiFiDetail = wiFiDetail)
                    }
                }
            }

        val alertDialog = AlertDialog
                .Builder(view.context)
                .setView(composeView)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.cancel()
                }.create()
        alertDialog.show()
        return alertDialog
    }

    fun attach(
        view: View,
        wiFiDetail: WiFiDetail,
    ) {
        view.setOnClickListener {
            runCatching { show(view, wiFiDetail) }
        }
    }
}
