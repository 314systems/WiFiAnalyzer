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
package com.vrem.wifianalyzer.about

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrem.wifianalyzer.ui.theme.AppTheme

class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    val viewModel: AboutViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    AboutScreen(
                        uiState = uiState,
                        onWriteReviewClick = { writeReview() }
                    )
                }
            }
        }
    }

    private fun writeReview() {
        val activity = requireActivity()
        val url = "market://details?id=${activity.packageName}"
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        runCatching {
            activity.startActivity(intent)
        }.getOrElse {
            Toast.makeText(activity, it.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
}
