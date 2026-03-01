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
package com.vrem.wifianalyzer.wifi.channelrating

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.band.WiFiChannel
import com.vrem.wifianalyzer.wifi.model.ChannelAPCount
import com.vrem.wifianalyzer.wifi.model.Strength
import com.vrem.wifianalyzer.wifi.model.WiFiWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelRatingScreen(
    viewModel: ChannelRatingViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                BestChannelsInfo(
                    wiFiBand = uiState.wiFiBand,
                    bestChannels = uiState.bestChannels,
                    modifier = Modifier.padding(16.dp),
                )

                ChannelRatingTable(
                    ratingItems = uiState.channelRatings,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
fun BestChannelsInfo(
    wiFiBand: WiFiBand,
    bestChannels: List<ChannelAPCount>,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.check_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.channel_rating_best),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val channelsText =
                if (bestChannels.isEmpty()) {
                    if (wiFiBand == WiFiBand.GHZ2) {
                        stringResource(
                            R.string.channel_rating_best_alternative,
                            stringResource(R.string.channel_rating_best_none),
                            stringResource(WiFiBand.GHZ5.textResource),
                        )
                    } else {
                        stringResource(R.string.channel_rating_best_none)
                    }
                } else {
                    bestChannels.map { it.wiFiChannel.channel }.distinct().sorted()
                        .joinToString(", ")
                }

            Text(
                text = channelsText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (bestChannels.isEmpty()) MaterialTheme.colorScheme.error else colorResource(
                    R.color.success
                ),
            )
        }
    }
}

@Composable
fun ChannelRatingTable(
    ratingItems: List<ChannelRatingItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.channel_rating_heading_number),
                modifier = Modifier.weight(0.8f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = stringResource(R.string.channel_rating_heading_rating),
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.channel_rating_heading_count),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.End,
            )
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(ratingItems) { item ->
                ChannelRatingRow(item)
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
fun ChannelRatingRow(
    item: ChannelRatingItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(0.8f)) {
            Text(
                text = item.wiFiChannel.channel.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(item.wiFiWidth.textResource),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        RatingMeter(
            rating = item.rating,
            modifier = Modifier.weight(2f),
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.apCount.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun RatingMeter(
    rating: Strength,
    modifier: Modifier = Modifier,
) {
    val totalSegments = 5
    val activeSegments = rating.ordinal + 1
    val color = colorResource(rating.colorResource)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSegments) { index ->
            val isActive = index < activeSegments
            Box(
                modifier =
                    Modifier
                        .height(8.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isActive) color else MaterialTheme.colorScheme.surfaceVariant,
                        ),
            )
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun ChannelRatingScreenPreview() {
    AppTheme {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {
                BestChannelsInfo(
                    wiFiBand = WiFiBand.GHZ2,
                    bestChannels =
                        listOf(
                            ChannelAPCount(WiFiChannel(1, 2412), WiFiWidth.MHZ_20, 0),
                            ChannelAPCount(WiFiChannel(6, 2437), WiFiWidth.MHZ_20, 1),
                            ChannelAPCount(WiFiChannel(11, 2462), WiFiWidth.MHZ_20, 0),
                        ),
                    modifier = Modifier.padding(16.dp),
                )
                ChannelRatingTable(
                    ratingItems =
                        listOf(
                            ChannelRatingItem(
                                WiFiChannel(1, 2412),
                                0,
                                WiFiWidth.MHZ_20,
                                Strength.FOUR
                            ),
                            ChannelRatingItem(
                                WiFiChannel(6, 2437),
                                2,
                                WiFiWidth.MHZ_20,
                                Strength.TWO
                            ),
                            ChannelRatingItem(
                                WiFiChannel(11, 2462),
                                5,
                                WiFiWidth.MHZ_20,
                                Strength.ZERO
                            ),
                        ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
