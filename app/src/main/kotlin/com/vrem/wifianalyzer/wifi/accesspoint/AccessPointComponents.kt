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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.channel

@Composable
fun AccessPointRow(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isChild: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val rowContent = @Composable {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isChild) {
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            content = rowContent
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            content = rowContent
        )
    }
}

@Composable
fun SSIDHeader(
    ssid: String,
    modifier: Modifier = Modifier,
    isExpanded: Boolean? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        isExpanded?.let {
            Icon(
                painter = painterResource(
                    id = if (it) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                ),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = ssid,
            style = textStyle.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ChannelLabel(
    channel: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.channel_short_name),
            color = MaterialTheme.colorScheme.channel,
            style = textStyle.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = channel,
            color = MaterialTheme.colorScheme.primary,
            style = textStyle.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun FrequencyLabel(
    frequency: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall
) {
    Text(
        text = frequency,
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondary,
        style = textStyle
    )
}

@Composable
fun DistanceLabel(
    distance: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall
) {
    Text(
        text = distance,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface,
        style = textStyle.copy(fontWeight = FontWeight.Bold)
    )
}
