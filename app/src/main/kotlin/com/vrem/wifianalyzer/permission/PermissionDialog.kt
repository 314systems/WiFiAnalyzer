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

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.view.View
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.databinding.InfoPermissionBinding

class PermissionDialog(
    private val activity: Activity,
) {
    fun show(requestPermission: () -> Unit): View {
        val binding = InfoPermissionBinding.inflate(activity.layoutInflater)
        binding.infoThrottling.throttling.visibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) View.VISIBLE else View.GONE

        AlertDialog
            .Builder(activity)
            .setView(binding.root)
            .setTitle(R.string.app_full_name)
            .setIcon(R.drawable.ic_app)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                requestPermission()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                activity.finish()
            }
            .show()

        return binding.root
    }
}
