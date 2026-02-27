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
package com.vrem.wifianalyzer.ui.filter

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrem.util.SPACE_SEPARATOR
import com.vrem.util.specialTrim
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.model.Security
import com.vrem.wifianalyzer.wifi.model.Strength

/**
 * Main Composable for the Filter Popup, migrating from filter_popup.xml.
 */
@Composable
fun FilterPopupContent(
    filtersAdapter: FiltersAdapter,
    isAccessPoints: Boolean,
    modifier: Modifier = Modifier
) {
    var ssid by remember {
        mutableStateOf(
            filtersAdapter.ssidAdapter().selections.joinToString(String.SPACE_SEPARATOR).specialTrim()
        )
    }
    var selectedBands by remember { mutableStateOf(filtersAdapter.wiFiBandAdapter().selections) }
    var selectedStrengths by remember { mutableStateOf(filtersAdapter.strengthAdapter().selections) }
    var selectedSecurities by remember { mutableStateOf(filtersAdapter.securityAdapter().selections) }

    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SSIDFilterSection(
                ssid = ssid,
                onSsidChange = {
                    ssid = it
                    filtersAdapter.ssidAdapter().selections = it.specialTrim().split(String.SPACE_SEPARATOR).toSet()
                }
            )

            if (isAccessPoints) {
                WiFiBandFilterSection(
                    selectedBands = selectedBands,
                    onBandsChange = {
                        selectedBands = it
                        filtersAdapter.wiFiBandAdapter().selections = it
                    }
                )
            }

            StrengthFilterSection(
                selectedStrengths = selectedStrengths,
                onStrengthsChange = {
                    selectedStrengths = it
                    filtersAdapter.strengthAdapter().selections = it
                }
            )

            SecurityFilterSection(
                selectedSecurities = selectedSecurities,
                onSecuritiesChange = {
                    selectedSecurities = it
                    filtersAdapter.securityAdapter().selections = it
                }
            )
        }
    }
}

@Composable
private fun SSIDFilterSection(ssid: String, onSsidChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.filter_ssid_title),
            style = MaterialTheme.typography.titleMedium
        )
        OutlinedTextField(
            value = ssid,
            onValueChange = onSsidChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.filter_ssid_hint)) },
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WiFiBandFilterSection(
    selectedBands: Set<WiFiBand>,
    onBandsChange: (Set<WiFiBand>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.filter_wifi_band_title),
            style = MaterialTheme.typography.titleMedium
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WiFiBand.entries.forEach { band ->
                FilterChip(
                    selected = selectedBands.contains(band),
                    onClick = {
                        val newBands = if (selectedBands.contains(band)) {
                            if (selectedBands.size > 1) selectedBands - band else selectedBands
                        } else {
                            selectedBands + band
                        }
                        onBandsChange(newBands)
                    },
                    label = { Text(stringResource(band.textResource)) }
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun StrengthFilterSection(
    selectedStrengths: Set<Strength>,
    onStrengthsChange: (Set<Strength>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.filter_strength_title),
            style = MaterialTheme.typography.titleMedium
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Strength.entries.forEach { strength ->
                val isSelected = selectedStrengths.contains(strength)
                IconButton(onClick = {
                    val newStrengths = if (isSelected) {
                        if (selectedStrengths.size > 1) selectedStrengths - strength else selectedStrengths
                    } else {
                        selectedStrengths + strength
                    }
                    onStrengthsChange(newStrengths)
                }) {
                    Icon(
                        painter = painterResource(id = strength.imageResource),
                        contentDescription = "Strength Level ${strength.ordinal}",
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        }
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecurityFilterSection(
    selectedSecurities: Set<Security>,
    onSecuritiesChange: (Set<Security>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.filter_security_title),
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Security.entries.forEach { security ->
                val isSelected = selectedSecurities.contains(security)
                val securityNameResId = when (security) {
                    Security.NONE -> R.string.security_none
                    Security.WPS -> R.string.security_wps
                    Security.WEP -> R.string.security_wep
                    Security.WPA -> R.string.security_wpa
                    Security.WPA2 -> R.string.security_wpa2
                    Security.WPA3 -> R.string.security_wpa3
                }

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newSecurities = if (isSelected) {
                            if (selectedSecurities.size > 1) selectedSecurities - security else selectedSecurities
                        } else {
                            selectedSecurities + security
                        }
                        onSecuritiesChange(newSecurities)
                    },
                    label = {
                        Text(
                            text = stringResource(securityNameResId),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterPopupContentPreview() {
    AppTheme {
        // Preview might need a mock adapter or similar if we want to see it.
        // For now, it's enough to check the layout with some dummy data if possible,
        // but FiltersAdapter is not easily mocked here without more setup.
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun FilterPopupContentDarkPreview() {
    AppTheme {
    }
}
