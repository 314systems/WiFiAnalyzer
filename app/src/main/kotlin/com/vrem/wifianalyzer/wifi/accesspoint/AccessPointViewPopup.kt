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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

@Composable
fun AccessPointViewPopup(
    wiFiDetail: WiFiDetail,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        AccessPointViewComplete(wiFiDetail = wiFiDetail)

        WiFiBandInfo(wiFiDetail = wiFiDetail)
        ChannelRangeInfo(wiFiDetail = wiFiDetail)
        StandardAndRoamingInfo(wiFiDetail = wiFiDetail)
        CapabilitiesInfo(wiFiDetail = wiFiDetail)
        VendorInfo(wiFiDetail = wiFiDetail)
    }
}

@Composable
private fun WiFiBandInfo(
    wiFiDetail: WiFiDetail,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = wiFiDetail.wiFiSignal.wiFiBand.textResource),
        modifier = modifier.padding(top = 8.dp),
        color = MaterialTheme.colorScheme.tertiary,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
    )
}

@Composable
private fun ChannelRangeInfo(
    wiFiDetail: WiFiDetail,
    modifier: Modifier = Modifier
) {
    val signal = wiFiDetail.wiFiSignal
    Row(modifier = modifier.padding(top = 4.dp)) {
        Text(
            text = stringResource(R.string.channel_short_name),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${signal.wiFiChannelStart.channel}",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.channel_from_to),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${signal.wiFiChannelEnd.channel}",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "(${stringResource(id = signal.wiFiWidth.textResource)})",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun StandardAndRoamingInfo(
    wiFiDetail: WiFiDetail,
    modifier: Modifier = Modifier
) {
    val signal = wiFiDetail.wiFiSignal
    Row(modifier = modifier.padding(top = 4.dp)) {
        Text(
            text = stringResource(id = signal.extra.wiFiStandard.fullResource),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        if (signal.extra.is80211mc) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.mc_flag),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        val fastRoaming = signal.extra.fastRoamingDisplay(LocalContext.current)
        if (fastRoaming.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = fastRoaming,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CapabilitiesInfo(
    wiFiDetail: WiFiDetail,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 4.dp)) {
        Text(
            text = wiFiDetail.wiFiSecurity.capabilities,
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
        )
        val securityTypes = wiFiDetail.wiFiSecurity.wiFiSecurityTypesDisplay(LocalContext.current)
        if (securityTypes.isNotBlank()) {
            Text(
                text = securityTypes,
                modifier = Modifier.padding(top = 2.dp),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
            )
        }
    }
}

@Composable
private fun VendorInfo(
    wiFiDetail: WiFiDetail,
    modifier: Modifier = Modifier
) {
    if (wiFiDetail.wiFiAdditional.vendorName.isNotEmpty()) {
        Text(
            text = wiFiDetail.wiFiAdditional.vendorName,
            modifier = modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 320,
    uiMode = UI_MODE_NIGHT_YES,
    name = "AccessPointViewPopupPreviewDark"
)
@Preview(showBackground = true, widthDp = 320)
@Composable
fun AccessPointViewPopupPreview() {
    AppTheme {
        Surface {
            AccessPointViewPopup(
                wiFiDetail = WiFiDetail.EMPTY,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
