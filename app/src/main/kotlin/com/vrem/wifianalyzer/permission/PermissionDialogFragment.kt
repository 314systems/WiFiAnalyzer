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

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.vrem.annotation.OpenClass
import com.vrem.util.buildMinVersionP
import com.vrem.wifianalyzer.R

@OpenClass
class PermissionDialogFragment : DialogFragment() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) activity?.finish()
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.info_permission, null)
        view.findViewById<View>(R.id.throttling)?.visibility = if (buildMinVersionP()) View.VISIBLE else View.GONE
        return AlertDialog
            .Builder(requireContext())
            .setView(view)
            .setTitle(R.string.app_full_name)
            .setIcon(R.drawable.ic_app)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                requestPermissionLauncher.launch(ApplicationPermission.PERMISSION)
            }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                activity?.finish()
            }.create()
    }

    companion object {
        internal const val TAG = "PermissionDialogFragment"
    }
}
