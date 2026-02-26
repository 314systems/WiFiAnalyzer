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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.vrem.wifianalyzer.R

data class AboutUiState(
    val packageName: String,
    val versionInfo: String,
    val copyright: String,
    val deviceInfo: String,
    val wiFiThrottlingEnabled: Boolean,
    val is5GHzBandSupported: Boolean,
    val is6GHzBandSupported: Boolean,
)

@Composable
fun AboutScreen(
    uiState: AboutUiState,
    onLicenseClick: () -> Unit,
    onContributorsClick: () -> Unit,
    onGraphViewLicenseClick: () -> Unit,
    onMaterialDesignIconsLicenseClick: () -> Unit,
    onWriteReviewClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.activity_vertical_margin))
            .verticalScroll(rememberScrollState()),
    ) {
        AboutHeader(uiState = uiState)

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.activity_vertical_margin)))

        AboutDeviceInfo(uiState = uiState)

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.activity_vertical_margin)))

        AboutLinks(
            onLicenseClick = onLicenseClick,
            onContributorsClick = onContributorsClick,
            onWriteReviewClick = onWriteReviewClick,
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.activity_vertical_margin)))

        AboutLibraries(
            onGraphViewLicenseClick = onGraphViewLicenseClick,
            onMaterialDesignIconsLicenseClick = onMaterialDesignIconsLicenseClick,
        )
    }
}

@Composable
private fun AboutHeader(uiState: AboutUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_app),
            contentDescription = stringResource(R.string.app_full_name),
            modifier = Modifier.size(64.dp),
        )
        Column(
            modifier = Modifier.padding(start = dimensionResource(R.dimen.activity_vertical_margin)),
        ) {
            Text(
                text = stringResource(R.string.app_full_name),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = uiState.packageName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = uiState.versionInfo,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.app_company_name),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = uiState.copyright,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AboutDeviceInfo(uiState: AboutUiState) {
    Column {
        Text(
            text = uiState.deviceInfo,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        WiFiStateItem(
            textResId = if (uiState.wiFiThrottlingEnabled) R.string.wifi_throttling_on else R.string.wifi_throttling_off,
            iconResId = if (uiState.wiFiThrottlingEnabled) R.drawable.ic_close else R.drawable.ic_check,
            iconColor = if (uiState.wiFiThrottlingEnabled) MaterialTheme.colorScheme.error else Color(0xFF4CAF50), // success color
        )
        WiFiStateItem(
            textResId = R.string.wifi_band_2ghz,
            iconResId = R.drawable.ic_check,
            iconColor = Color(0xFF4CAF50), // success color
        )
        WiFiStateItem(
            textResId = R.string.wifi_band_5ghz,
            iconResId = if (uiState.is5GHzBandSupported) R.drawable.ic_check else R.drawable.ic_close,
            iconColor = if (uiState.is5GHzBandSupported) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
        )
        WiFiStateItem(
            textResId = R.string.wifi_band_6ghz,
            iconResId = if (uiState.is6GHzBandSupported) R.drawable.ic_check else R.drawable.ic_close,
            iconColor = if (uiState.is6GHzBandSupported) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun WiFiStateItem(
    @StringRes textResId: Int,
    @DrawableRes iconResId: Int,
    iconColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp),
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = stringResource(textResId),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = dimensionResource(R.dimen.activity_horizontal_margin)),
        )
    }
}

@Composable
private fun AboutLinks(
    onLicenseClick: () -> Unit,
    onContributorsClick: () -> Unit,
    onWriteReviewClick: () -> Unit,
) {
    Column {
        ClickableUrlText(stringResource(R.string.app_url))

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.activity_vertical_margin)))
        Text(
            text = stringResource(R.string.about_license_title),
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Button(
            onClick = onLicenseClick,
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Text(text = stringResource(R.string.gpl))
        }
        ClickableUrlText(stringResource(R.string.gpl_url))

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.activity_vertical_margin)))
        Text(
            text = stringResource(R.string.about_description_title),
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.about_description_text),
            color = MaterialTheme.colorScheme.onSurface,
        )

        AboutLinkItem(R.string.about_documentation, R.string.about_documentation_url)
        AboutLinkItem(R.string.about_how_to, R.string.about_how_to_url)
        AboutLinkItem(R.string.about_faq, R.string.about_faq_url)
        AboutLinkItem(R.string.about_privacy_policy, R.string.about_privacy_policy_url)

        Button(
            onClick = onContributorsClick,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.activity_vertical_margin)),
        ) {
            Text(text = stringResource(R.string.about_contributor_title))
        }

        Button(
            onClick = onWriteReviewClick,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.activity_vertical_margin)),
        ) {
            Text(text = stringResource(R.string.about_write_review))
        }
    }
}

@Composable
private fun AboutLinkItem(@StringRes labelResId: Int, @StringRes urlResId: Int) {
    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.activity_vertical_margin)))
    Text(
        text = stringResource(labelResId),
        color = MaterialTheme.colorScheme.onSurface,
    )
    ClickableUrlText(stringResource(urlResId))
}

@Composable
private fun AboutLibraries(
    onGraphViewLicenseClick: () -> Unit,
    onMaterialDesignIconsLicenseClick: () -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.about_libraries_title),
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface,
        )
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
) {
    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.activity_vertical_margin)))
    Text(
        text = stringResource(nameResId),
        color = MaterialTheme.colorScheme.onSurface,
    )
    Column(modifier = Modifier.padding(start = dimensionResource(R.dimen.activity_horizontal_margin))) {
        ClickableUrlText(stringResource(urlResId))
        Button(
            onClick = onLicenseClick,
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Text(text = stringResource(R.string.al))
        }
        ClickableUrlText(stringResource(R.string.al_url))
    }
}

@Composable
private fun ClickableUrlText(url: String) {
    val uriHandler = LocalUriHandler.current
    Text(
        text = url,
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable {
            runCatching { uriHandler.openUri(url) }
        }
    )
}
