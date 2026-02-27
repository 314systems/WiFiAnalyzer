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
import android.view.ViewGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

class AccessPointPopup {
    fun show(
        view: View,
        wiFiDetail: WiFiDetail = WiFiDetail.EMPTY,
    ) {
        val rootLayout = view.rootView as? ViewGroup ?: return
        val composeView = ComposeView(view.context)
        composeView.setContent {
            var showDialog by remember { mutableStateOf(true) }
            if (showDialog) {
                AppTheme {
                    AccessPointAlertDialog(
                        wiFiDetail = wiFiDetail,
                        onDismiss = {
                            showDialog = false
                            rootLayout.removeView(composeView)
                        },
                    )
                }
            }
        }
        rootLayout.addView(composeView)
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

@Composable
fun AccessPointAlertDialog(
    wiFiDetail: WiFiDetail,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        text = {
            AccessPointViewPopup(wiFiDetail = wiFiDetail)
        },
    )
}

@Preview(
    showBackground = true,
    widthDp = 320,
    uiMode = UI_MODE_NIGHT_YES,
    name = "AccessPointAlertDialogPreviewDark"
)
@Preview(showBackground = true)
@Composable
fun AccessPointAlertDialogPreview() {
    AppTheme {
        AccessPointAlertDialog(
            wiFiDetail = WiFiDetail.EMPTY,
            onDismiss = {},
        )
    }
}
