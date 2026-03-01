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

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.AppTheme

@Composable
fun PermissionHandler(
    onPermissionGranted: () -> Unit,
    onTerminateApp: () -> Unit
) {
    val context = LocalContext.current
    val permissionService = remember { PermissionService(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionGranted by remember { mutableStateOf(permissionService.permissionGranted()) }
    var showDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionGranted = true
            onPermissionGranted()
        } else {
            onTerminateApp()
        }
    }

    LaunchedEffect(Unit) {
        if (permissionGranted) {
            onPermissionGranted()
        } else {
            showDialog = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (permissionGranted && !permissionService.locationEnabled()) {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showDialog && !permissionGranted) {
        PermissionDialog(
            onConfirm = {
                showDialog = false
                launcher.launch(PermissionService.PERMISSION)
            },
            onTerminateApp = onTerminateApp
        )
    }
}

@Composable
private fun PermissionDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onTerminateApp: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val faqUrl = stringResource(id = R.string.no_data_url)

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onTerminateApp,
        title = {
            Text(text = stringResource(id = R.string.app_name))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ThrottlingInfo()
                LocationInfo()
                FaqInfo(faqUrl = faqUrl) { uriHandler.openUri(faqUrl) }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onTerminateApp) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun ThrottlingInfo(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Text(
            text = stringResource(id = R.string.throttling_msg),
            fontWeight = FontWeight.Bold,
            modifier = modifier
        )
    }
}

@Composable
private fun LocationInfo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.permission_msg)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location_on),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(text = stringResource(id = R.string.location_msg))
        }
    }
}

@Composable
private fun FaqInfo(
    faqUrl: String,
    modifier: Modifier = Modifier,
    onFaqClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.no_data_msg),
            textAlign = TextAlign.Center,
        )
        TextButton(
            onClick = onFaqClick
        ) {
            Text(
                text = faqUrl,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionDialogPreview() {
    AppTheme {
        PermissionDialog(
            onConfirm = {},
            onTerminateApp = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DarkPermissionDialogPreview() {
    AppTheme {
        PermissionDialog(
            onConfirm = {},
            onTerminateApp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MaterialPermissionDialogPreview() {
    MaterialTheme {
        PermissionDialog(
            onConfirm = {},
            onTerminateApp = {}
        )
    }
}
