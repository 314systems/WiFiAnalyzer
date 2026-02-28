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

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrem.util.readFile
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.AppTheme

data class AboutUiState(
    val packageName: String,
    val versionInfo: String,
    val copyright: String,
    val deviceInfo: String,
    val wiFiThrottlingEnabled: Boolean,
    val is5GHzBandSupported: Boolean,
    val is6GHzBandSupported: Boolean,
)

private data class DialogState(
    @StringRes val titleId: Int,
    @RawRes val resourceId: Int,
    val isSmallFont: Boolean = true
)

@Composable
fun AboutScreen(
    uiState: AboutUiState,
    onWriteReviewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dialogState by remember { mutableStateOf<DialogState?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.activity_horizontal_margin)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.activity_vertical_margin))
        ) {
            AboutHeader(uiState = uiState)

            AboutSectionCard(title = stringResource(R.string.about_description_title)) {
                AboutDeviceInfo(uiState = uiState)
            }

            AboutSectionCard(title = stringResource(R.string.about_license_title)) {
                AboutLinks(
                    onLicenseClick = { dialogState = DialogState(R.string.gpl, R.raw.gpl) },
                    onContributorsClick = {
                        dialogState =
                            DialogState(R.string.about_contributor_title, R.raw.contributors, false)
                    },
                    onWriteReviewClick = onWriteReviewClick,
                )
            }

            AboutSectionCard(title = stringResource(R.string.about_libraries_title)) {
                AboutLibraries(
                    onGraphViewLicenseClick = { dialogState = DialogState(R.string.al, R.raw.al) },
                    onMaterialDesignIconsLicenseClick = {
                        dialogState = DialogState(R.string.al, R.raw.al)
                    },
                )
            }
        }
    }

    dialogState?.let { state ->
        val context = LocalContext.current
        val text = remember(state.resourceId) { readFile(context.resources, state.resourceId) }
        AlertDialog(
            onDismissRequest = { dialogState = null },
            confirmButton = {
                TextButton(onClick = { dialogState = null }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            title = { Text(stringResource(state.titleId)) },
            text = {
                Text(
                    text = text,
                    fontSize = if (state.isSmallFont) 10.sp else 14.sp,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        )
    }
}

@Composable
private fun AboutSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(thickness = 0.5.dp)
            content()
        }
    }
}

@Composable
private fun AboutHeader(
    uiState: AboutUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_app),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(80.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = uiState.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = uiState.versionInfo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.app_company_name),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = uiState.copyright,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun AboutDeviceInfo(
    uiState: AboutUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = uiState.deviceInfo,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
        WiFiStateItem(
            textResId = if (uiState.wiFiThrottlingEnabled) R.string.wifi_throttling_on else R.string.wifi_throttling_off,
            isSuccess = !uiState.wiFiThrottlingEnabled,
            isError = uiState.wiFiThrottlingEnabled,
        )
        listOf(
            R.string.wifi_band_2ghz to true,
            R.string.wifi_band_5ghz to uiState.is5GHzBandSupported,
            R.string.wifi_band_6ghz to uiState.is6GHzBandSupported
        ).forEach { (textResId, isSupported) ->
            WiFiStateItem(
                textResId = textResId,
                isSuccess = isSupported
            )
        }
    }
}

@Composable
private fun WiFiStateItem(
    @StringRes textResId: Int,
    isSuccess: Boolean,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        val color = when {
            isSuccess -> MaterialTheme.colorScheme.primary
            isError -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Icon(
            painter = painterResource(if (isSuccess) R.drawable.check_24px else R.drawable.close_24px),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(textResId),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSuccess || isError) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AboutLinks(
    onLicenseClick: () -> Unit,
    onContributorsClick: () -> Unit,
    onWriteReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ClickableUrlText(stringResource(R.string.app_url))

        Button(
            onClick = onLicenseClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.gpl))
        }
        ClickableUrlText(stringResource(R.string.gpl_url))

        Text(
            text = stringResource(R.string.about_description_text),
            style = MaterialTheme.typography.bodyMedium,
        )

        AboutLinkItem(R.string.about_documentation, R.string.about_documentation_url)
        AboutLinkItem(R.string.about_how_to, R.string.about_how_to_url)
        AboutLinkItem(R.string.about_faq, R.string.about_faq_url)
        AboutLinkItem(R.string.about_privacy_policy, R.string.about_privacy_policy_url)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onContributorsClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.about_contributor_title))
            }
            Button(
                onClick = onWriteReviewClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.about_write_review))
            }
        }
    }
}

@Composable
private fun AboutLinkItem(
    @StringRes labelResId: Int,
    @StringRes urlResId: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(labelResId),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ClickableUrlText(stringResource(urlResId))
    }
}

@Composable
private fun AboutLibraries(
    onGraphViewLicenseClick: () -> Unit,
    onMaterialDesignIconsLicenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LibraryItem(
            nameResId = R.string.about_library_graph_view,
            urlResId = R.string.about_library_graph_view_url,
            onLicenseClick = onGraphViewLicenseClick,
        )
        LibraryItem(
            nameResId = R.string.about_library_material,
            urlResId = R.string.about_library_material_url,
            onLicenseClick = onMaterialDesignIconsLicenseClick,
        )
    }
}

@Composable
private fun LibraryItem(
    @StringRes nameResId: Int,
    @StringRes urlResId: Int,
    onLicenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(nameResId),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Column(
            modifier = Modifier.padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ClickableUrlText(stringResource(urlResId))
            Button(
                onClick = onLicenseClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.al))
            }
            ClickableUrlText(stringResource(R.string.al_url))
        }
    }
}

@Composable
private fun ClickableUrlText(
    url: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    Text(
        text = url,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        modifier = modifier.clickable {
            runCatching { uriHandler.openUri(url) }
        }
    )
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun AboutScreenPreview() {
    val uiState = AboutUiState(
        packageName = "com.vrem.wifianalyzer",
        versionInfo = "1.0.0 (1)",
        copyright = "Copyright (C) 2015 - 2026",
        deviceInfo = "Pixel 7 Pro (Android 14)",
        wiFiThrottlingEnabled = false,
        is5GHzBandSupported = true,
        is6GHzBandSupported = false
    )
    AppTheme {
        AboutScreen(
            uiState = uiState,
            onWriteReviewClick = {}
        )
    }
}
