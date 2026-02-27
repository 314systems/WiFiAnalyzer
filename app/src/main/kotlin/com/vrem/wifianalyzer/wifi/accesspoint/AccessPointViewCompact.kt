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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.AppTheme

/**
 * Data class representing the UI state for an Access Point in compact view.
 */
data class AccessPointViewData(
    val ssid: String,
    val level: String,
    val channel: String,
    val primaryFrequency: String,
    val distanceText: String,
    val isGrouped: Boolean,
    val security: String,
    val showGroupIndicator: Boolean
)

@Composable
fun AccessPointViewCompact(
    data: AccessPointViewData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AccessPointRow(
        onClick = onClick,
        modifier = modifier,
        isChild = data.isGrouped
    ) {
        SSIDHeader(
            ssid = data.ssid,
            isExpanded = if (data.showGroupIndicator) false else null
        )

        Spacer(modifier = Modifier.height(4.dp))

        AccessPointDetails(
            data = data,
            modifier = Modifier.padding(start = if (data.showGroupIndicator) 24.dp else 0.dp)
        )
    }
}

@Composable
private fun AccessPointDetails(
    data: AccessPointViewData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = data.level,
            modifier = Modifier.padding(end = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        ChannelLabel(
            channel = data.channel,
            modifier = Modifier.padding(end = 8.dp),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        FrequencyLabel(
            frequency = data.primaryFrequency,
            modifier = Modifier.padding(end = 8.dp),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        if (data.security.isNotEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.lock_24px),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        DistanceLabel(
            distance = data.distanceText,
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

private val PreviewGroupedData = AccessPointViewData(
    ssid = "GROUP SSID (0a:0a:0a:0a:0a:0a)",
    level = "-70dBm",
    channel = "36(40)",
    primaryFrequency = "5180MHz",
    distanceText = "~5.0m",
    isGrouped = true,
    security = "WPA3",
    showGroupIndicator = true
)

private val PreviewUngroupedData = AccessPointViewData(
    ssid = "SINGLE SSID (b:b:b:b:b:b)",
    level = "-40dBm",
    channel = "1",
    primaryFrequency = "2412MHz",
    distanceText = "~1.2m",
    isGrouped = false,
    security = "WPA2",
    showGroupIndicator = false
)

@Preview(showBackground = true, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun AccessPointViewCompactGroupedPreview() {
    AppTheme {
        Surface {
            AccessPointViewCompact(data = PreviewGroupedData, onClick = {})
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun AccessPointViewCompactUngroupedPreview() {
    AppTheme {
        Surface {
            AccessPointViewCompact(data = PreviewUngroupedData, onClick = {})
        }
    }
}
