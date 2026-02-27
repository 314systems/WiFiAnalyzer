package com.vrem.wifianalyzer.permission

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vrem.wifianalyzer.MainContext

@Composable
fun PermissionHandler(
    onPermissionGranted: () -> Unit,
    onTerminateApp: () -> Unit
) {
    val context = LocalContext.current
    val permissionService = MainContext.INSTANCE.permissionService
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
                launcher.launch(ApplicationPermission.PERMISSION)
            },
            onTerminateApp = onTerminateApp
        )
    }
}
