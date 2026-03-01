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

import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
                AccessPointAlertDialog(
                    wiFiDetail = wiFiDetail,
                    onDismiss = {
                        showDialog = false
                        rootLayout.removeView(composeView)
                    },
                )
            }
        }
        rootLayout.addView(composeView)
    }
}

@Composable
fun AccessPointAlertDialog(
    wiFiDetail: WiFiDetail,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                AccessPointViewPopup(
                    wiFiDetail = wiFiDetail,
                    onClick = null
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun AccessPointAlertDialogPreview() {
    AppTheme(darkTheme = false) {
        AccessPointAlertDialog(
            wiFiDetail = WiFiDetail.EMPTY,
            onDismiss = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun AccessPointAlertDialogDarkPreview() {
    AppTheme(darkTheme = true) {
        AccessPointAlertDialog(
            wiFiDetail = WiFiDetail.EMPTY,
            onDismiss = {},
        )
    }
}
