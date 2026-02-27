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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiSignal

@Composable
fun AccessPointViewComplete(
    wiFiDetail: WiFiDetail,
    modifier: Modifier = Modifier,
    isChild: Boolean = false,
    isGroup: Boolean = false,
    isExpanded: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isChild) {
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isGroup) {
                    Image(
                        painter = painterResource(
                            id = if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 4.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                }
                SSIDHeader(title = wiFiDetail.wiFiIdentifier.title)
            }

            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SignalIndicator(wiFiDetail = wiFiDetail)
                Spacer(modifier = Modifier.width(12.dp))
                DetailedInfo(wiFiDetail = wiFiDetail)
            }
        }
    }
}

@Composable
private fun SSIDHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun SignalIndicator(
    wiFiDetail: WiFiDetail,
    modifier: Modifier = Modifier
) {
    val signal = wiFiDetail.wiFiSignal
    val color = colorResource(id = signal.strengthColor)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${signal.level} dBm",
            color = color,
            style = MaterialTheme.typography.bodySmall
        )
        Box(modifier = Modifier.size(40.dp)) {
            Image(
                painter = painterResource(id = signal.strength.imageResource),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                colorFilter = ColorFilter.tint(color)
            )
            StandardBadge(signal = signal)
            SecurityBadge(wiFiDetail = wiFiDetail)
        }
    }
}

@Composable
private fun BoxScope.StandardBadge(
    signal: WiFiSignal,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = signal.extra.wiFiStandard.valueResource),
        modifier = modifier.align(Alignment.BottomStart),
        color = MaterialTheme.colorScheme.tertiary,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    )
}

@Composable
private fun BoxScope.SecurityBadge(
    wiFiDetail: WiFiDetail,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = wiFiDetail.wiFiSecurity.security.imageResource),
        contentDescription = null,
        modifier = modifier
            .size(12.dp)
            .align(Alignment.BottomEnd),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
    )
}

@Composable
private fun DetailedInfo(
    wiFiDetail: WiFiDetail,
    modifier: Modifier = Modifier
) {
    val signal = wiFiDetail.wiFiSignal
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChannelLabel(channel = signal.channelDisplay())
            Spacer(modifier = Modifier.width(8.dp))
            FrequencyLabel(frequency = signal.primaryFrequency)
            Spacer(modifier = Modifier.width(8.dp))
            DistanceLabel(distance = signal.distance)
        }

        Row(
            modifier = Modifier.padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FrequencyRangeLabel(signal = signal)
            Spacer(modifier = Modifier.width(8.dp))
            WidthLabel(signal = signal)
            VendorLabel(vendor = wiFiDetail.wiFiAdditional.vendorName)
        }

        CapabilitiesLabel(capabilities = wiFiDetail.wiFiSecurity.securities.joinToString(" ", "[", "]"))
    }
}

@Composable
private fun ChannelLabel(
    channel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.channel_short_name),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = channel,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun FrequencyLabel(
    frequency: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$frequency${WiFiSignal.FREQUENCY_UNITS}",
        modifier = modifier,
        color = MaterialTheme.colorScheme.tertiary,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun DistanceLabel(
    distance: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = distance,
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
    )
}

@Composable
private fun FrequencyRangeLabel(
    signal: WiFiSignal,
    modifier: Modifier = Modifier
) {
    Text(
        text = "${signal.wiFiChannelStart.frequency} - ${signal.wiFiChannelEnd.frequency}",
        modifier = modifier,
        color = MaterialTheme.colorScheme.tertiary,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun WidthLabel(
    signal: WiFiSignal,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = signal.wiFiWidth.textResource),
        modifier = modifier,
        color = MaterialTheme.colorScheme.tertiary,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun VendorLabel(
    vendor: String,
    modifier: Modifier = Modifier
) {
    if (vendor.isNotEmpty()) {
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = vendor,
            modifier = modifier,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CapabilitiesLabel(
    capabilities: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = capabilities,
        modifier = modifier.padding(top = 2.dp),
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
    )
}

@Preview(
    showBackground = true,
    widthDp = 320,
    uiMode = UI_MODE_NIGHT_YES,
    name = "AccessPointViewCompletePreviewDark"
)
@Preview(showBackground = true, widthDp = 320)
@Composable
fun AccessPointViewCompletePreview() {
    AppTheme {
        Surface {
            AccessPointViewComplete(wiFiDetail = WiFiDetail.EMPTY)
        }
    }
}