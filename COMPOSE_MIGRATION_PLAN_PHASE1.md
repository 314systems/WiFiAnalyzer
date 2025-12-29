# Jetpack Compose移行計画 - Phase 1

## 📋 概要

このドキュメントは、WiFiAnalyzerアプリをJetpack Compose + Material3へ段階的に移行するためのPhase 1の詳細計画です。

### 移行の目的
- 段階的にJetpack Composeを導入
- AppCompatベースからMaterial3への移行
- 既存機能を維持しながらモダンなUIフレームワークへ更新
- リスクを最小限に抑えた段階的なアプローチ

### Phase 1の対象コンポーネント
1. ✅ **ChannelAvailableFragment** - 最優先（最も簡単）
2. ✅ **WarningView** - 簡単な条件付きUI
3. ✅ **ConnectionView** - リアクティブな更新
4. ✅ **AboutFragment** - Material3 Dialogsのデモ

---

## 🎯 ステップ1: Compose環境のセットアップ

### 1.1 依存関係の追加

**ファイル**: `app/build.gradle`

```gradle
android {
    // 既存の設定...

    buildFeatures {
        viewBinding = true
        compose = true  // 追加
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"  // Kotlin 1.9.22に対応
    }
}

dependencies {
    // 既存の依存関係...

    // Jetpack Compose BOM (Bill of Materials)
    def composeBom = platform('androidx.compose:compose-bom:2024.12.01')
    implementation composeBom
    androidTestImplementation composeBom

    // Compose Core
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.compose.material:material-icons-extended'

    // Compose Integration
    implementation 'androidx.activity:activity-compose:1.9.3'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7'
    implementation 'androidx.lifecycle:lifecycle-runtime-compose:2.8.7'

    // Compose Navigation (後のPhaseで使用)
    implementation 'androidx.navigation:navigation-compose:2.8.5'

    // Debug Tools
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'

    // Android Testing
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
}
```

### 1.2 Kotlinバージョンの確認

**ファイル**: `build.gradle` (プロジェクトルート)

Kotlin 1.9.22以上を使用していることを確認：
```gradle
buildscript {
    ext.kotlin_version = '1.9.22' // または最新の安定版
}
```

### 1.3 ビルド確認

```bash
./gradlew clean build
```

---

## 🎨 ステップ2: Material3テーマシステムの構築

### 2.1 Composeテーマファイルの作成

**新規ファイル**: `app/src/main/kotlin/com/vrem/wifianalyzer/ui/theme/Color.kt`

```kotlin
package com.vrem.wifianalyzer.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Colors
val Primary = Color(0xFF2196F3)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFBBDEFB)
val OnPrimaryContainer = Color(0xFF0D47A1)

val Secondary = Color(0xFF03A9F4)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFB3E5FC)
val OnSecondaryContainer = Color(0xFF01579B)

val Background = Color(0xFFFAFAFA)
val OnBackground = Color(0xFF212121)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF212121)

val Error = Color(0xFFB00020)
val OnError = Color(0xFFFFFFFF)

// Dark Theme Colors
val DarkPrimary = Color(0xFF90CAF9)
val DarkOnPrimary = Color(0xFF0D47A1)
val DarkPrimaryContainer = Color(0xFF1976D2)
val DarkOnPrimaryContainer = Color(0xFFE3F2FD)

val DarkSecondary = Color(0xFF81D4FA)
val DarkOnSecondary = Color(0xFF01579B)
val DarkSecondaryContainer = Color(0xFF0288D1)
val DarkOnSecondaryContainer = Color(0xFFE1F5FE)

val DarkBackground = Color(0xFF121212)
val DarkOnBackground = Color(0xFFE0E0E0)
val DarkSurface = Color(0xFF1E1E1E)
val DarkOnSurface = Color(0xFFE0E0E0)

val DarkError = Color(0xFFCF6679)
val DarkOnError = Color(0xFF000000)

// App-specific colors
val ChannelNumber = Color(0xFF4CAF50)
val Selected = Color(0xFFFF9800)
```

**新規ファイル**: `app/src/main/kotlin/com/vrem/wifianalyzer/ui/theme/Theme.kt`

```kotlin
package com.vrem.wifianalyzer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
    onError = OnError
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = DarkError,
    onError = DarkOnError
)

private val BlackColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    background = Color.Black,
    onBackground = DarkOnBackground,
    surface = Color.Black,
    onSurface = DarkOnSurface,
    error = DarkError,
    onError = DarkOnError
)

enum class AppTheme {
    LIGHT, DARK, BLACK, SYSTEM
}

@Composable
fun WiFiAnalyzerTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.BLACK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        appTheme == AppTheme.BLACK -> BlackColorScheme
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**新規ファイル**: `app/src/main/kotlin/com/vrem/wifianalyzer/ui/theme/Type.kt`

```kotlin
package com.vrem.wifianalyzer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

---

## 🚀 ステップ3: ChannelAvailableFragmentのCompose移行

### 3.1 現在の実装分析

**現在のコード** (`ChannelAvailableFragment.kt:44-70`):
- ViewBindingを使用
- 11個のTextViewを手動で更新
- 国コード、国名、チャンネルリストを表示

### 3.2 Composeコンポーネントの作成

**新規ファイル**: `app/src/main/kotlin/com/vrem/wifianalyzer/wifi/channelavailable/ChannelAvailableScreen.kt`

```kotlin
package com.vrem.wifianalyzer.wifi.channelavailable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.band.WiFiChannelCountry
import com.vrem.wifianalyzer.wifi.model.WiFiWidth

data class ChannelAvailableState(
    val countryCode: String,
    val countryName: String,
    val channelsByBandAndWidth: List<ChannelInfo>
)

data class ChannelInfo(
    val band: WiFiBand,
    val width: WiFiWidth,
    val channels: String
)

@Composable
fun ChannelAvailableScreen(
    modifier: Modifier = Modifier
) {
    val state = remember { produceChannelAvailableState() }

    ChannelAvailableContent(
        state = state,
        modifier = modifier
    )
}

@Composable
private fun produceChannelAvailableState(): ChannelAvailableState {
    val settings = MainContext.INSTANCE.settings
    val countryCode = settings.countryCode()
    val languageLocale = settings.languageLocale()
    val countryName = WiFiChannelCountry.find(countryCode).countryName(languageLocale)

    val channelInfoList = listOf(
        Triple(WiFiBand.GHZ2, WiFiWidth.MHZ_20, null),
        Triple(WiFiBand.GHZ2, WiFiWidth.MHZ_40, null),
        Triple(WiFiBand.GHZ5, WiFiWidth.MHZ_20, null),
        Triple(WiFiBand.GHZ5, WiFiWidth.MHZ_40, null),
        Triple(WiFiBand.GHZ5, WiFiWidth.MHZ_80, null),
        Triple(WiFiBand.GHZ5, WiFiWidth.MHZ_160, null),
        Triple(WiFiBand.GHZ6, WiFiWidth.MHZ_20, null),
        Triple(WiFiBand.GHZ6, WiFiWidth.MHZ_40, null),
        Triple(WiFiBand.GHZ6, WiFiWidth.MHZ_80, null),
        Triple(WiFiBand.GHZ6, WiFiWidth.MHZ_160, null),
        Triple(WiFiBand.GHZ6, WiFiWidth.MHZ_320, null),
    ).map { (band, width, _) ->
        ChannelInfo(
            band = band,
            width = width,
            channels = band.wiFiChannels.availableChannels(width, band, countryCode).joinToString(", ")
        )
    }

    return ChannelAvailableState(
        countryCode = countryCode,
        countryName = countryName,
        channelsByBandAndWidth = channelInfoList
    )
}

@Composable
private fun ChannelAvailableContent(
    state: ChannelAvailableState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // WiFi Channels List URL
        Text(
            text = stringResource(R.string.wifi_channels_list_url),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Country Info
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = state.countryCode,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = state.countryName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Channel Lists by Band
        val channelsByBand = state.channelsByBandAndWidth.groupBy { it.band }

        channelsByBand.forEach { (band, channelInfos) ->
            BandSection(
                band = band,
                channelInfos = channelInfos,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun BandSection(
    band: WiFiBand,
    channelInfos: List<ChannelInfo>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Band Title
        Text(
            text = when (band) {
                WiFiBand.GHZ2 -> stringResource(R.string.wifi_band_2ghz)
                WiFiBand.GHZ5 -> stringResource(R.string.wifi_band_5ghz)
                WiFiBand.GHZ6 -> stringResource(R.string.wifi_band_6ghz)
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Width and Channels
        channelInfos.forEach { channelInfo ->
            WidthChannelRow(
                width = channelInfo.width,
                channels = channelInfo.channels,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun WidthChannelRow(
    width: WiFiWidth,
    channels: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = when (width) {
                WiFiWidth.MHZ_20 -> stringResource(R.string.wifi_width_20mhz)
                WiFiWidth.MHZ_40 -> stringResource(R.string.wifi_width_40mhz)
                WiFiWidth.MHZ_80 -> stringResource(R.string.wifi_width_80mhz)
                WiFiWidth.MHZ_160 -> stringResource(R.string.wifi_width_160mhz)
                WiFiWidth.MHZ_320 -> stringResource(R.string.wifi_width_320mhz)
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
        Text(
            text = channels,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChannelAvailableScreenPreview() {
    WiFiAnalyzerTheme {
        ChannelAvailableContent(
            state = ChannelAvailableState(
                countryCode = "US",
                countryName = "United States",
                channelsByBandAndWidth = listOf(
                    ChannelInfo(WiFiBand.GHZ2, WiFiWidth.MHZ_20, "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11"),
                    ChannelInfo(WiFiBand.GHZ2, WiFiWidth.MHZ_40, "3, 11"),
                    ChannelInfo(WiFiBand.GHZ5, WiFiWidth.MHZ_20, "36, 40, 44, 48, 52, 56, 60, 64"),
                )
            )
        )
    }
}
```

### 3.3 FragmentからComposeへの統合

**更新**: `ChannelAvailableFragment.kt`

```kotlin
package com.vrem.wifianalyzer.wifi.channelavailable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.vrem.wifianalyzer.ui.theme.WiFiAnalyzerTheme

class ChannelAvailableFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            // Fragmentのライフサイクルに合わせてCompositionを破棄
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                WiFiAnalyzerTheme {
                    ChannelAvailableScreen()
                }
            }
        }
    }
}
```

### 3.4 テスト

**新規ファイル**: `app/src/test/kotlin/com/vrem/wifianalyzer/wifi/channelavailable/ChannelAvailableScreenTest.kt`

```kotlin
package com.vrem.wifianalyzer.wifi.channelavailable

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.vrem.wifianalyzer.ui.theme.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.WiFiWidth
import org.junit.Rule
import org.junit.Test

class ChannelAvailableScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun channelAvailableScreenDisplaysCountryInfo() {
        // Given
        val state = ChannelAvailableState(
            countryCode = "US",
            countryName = "United States",
            channelsByBandAndWidth = emptyList()
        )

        // When
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                ChannelAvailableContent(state = state)
            }
        }

        // Then
        composeTestRule.onNodeWithText("US").assertIsDisplayed()
        composeTestRule.onNodeWithText("United States").assertIsDisplayed()
    }

    @Test
    fun channelAvailableScreenDisplaysChannelInfo() {
        // Given
        val state = ChannelAvailableState(
            countryCode = "US",
            countryName = "United States",
            channelsByBandAndWidth = listOf(
                ChannelInfo(WiFiBand.GHZ2, WiFiWidth.MHZ_20, "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11")
            )
        )

        // When
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                ChannelAvailableContent(state = state)
            }
        }

        // Then
        composeTestRule.onNodeWithText("1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11").assertIsDisplayed()
    }
}
```

---

## ⚠️ ステップ4: WarningViewのCompose移行

### 4.1 現在の実装分析

**現在のコード** (`WarningView.kt`):
- 3つの警告状態を管理（throttling, noData, noLocation）
- Visibilityを動的に変更
- MainActivityのViewを直接操作

### 4.2 Composeコンポーネントの作成

**新規ファイル**: `app/src/main/kotlin/com/vrem/wifianalyzer/wifi/accesspoint/WarningComposable.kt`

```kotlin
package com.vrem.wifianalyzer.wifi.accesspoint

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.WiFiAnalyzerTheme

data class WarningState(
    val showThrottling: Boolean = false,
    val showNoData: Boolean = false,
    val showNoLocation: Boolean = false,
    val showThrottlingInNoLocation: Boolean = false
)

@Composable
fun WarningSection(
    state: WarningState,
    modifier: Modifier = Modifier
) {
    if (state.showThrottling || state.showNoData || state.showNoLocation) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.showThrottling) {
                    WarningItem(text = stringResource(R.id.main_wifi_throttling))
                }

                if (state.showNoData) {
                    WarningItem(text = stringResource(R.id.no_data))
                }

                if (state.showNoLocation) {
                    WarningItem(text = stringResource(R.id.no_location))

                    if (state.showThrottlingInNoLocation) {
                        WarningItem(text = stringResource(R.id.throttling))
                    }
                }
            }
        }
    }
}

@Composable
private fun WarningItem(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WarningSectionPreview() {
    WiFiAnalyzerTheme {
        WarningSection(
            state = WarningState(
                showThrottling = true,
                showNoData = true,
                showNoLocation = false
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WarningSectionNoLocationPreview() {
    WiFiAnalyzerTheme {
        WarningSection(
            state = WarningState(
                showNoLocation = true,
                showThrottlingInNoLocation = true
            )
        )
    }
}
```

### 4.3 WarningView更新（段階的移行）

**将来の計画**: MainActivityがComposeに移行するまで、既存のWarningViewを維持し、後で`WarningSection`に完全移行します。

---

## 🔗 ステップ5: ConnectionViewのCompose移行（基本設計）

### 5.1 現在の実装分析

**現在のコード** (`ConnectionView.kt:40-76`):
- `UpdateNotifier`インターフェースを実装
- WiFiDataを受け取り、接続情報を表示
- 動的にViewを作成/更新

### 5.2 基本的なComposeコンポーネント

**新規ファイル**: `app/src/main/kotlin/com/vrem/wifianalyzer/wifi/accesspoint/ConnectionComposable.kt`

```kotlin
package com.vrem.wifianalyzer.wifi.accesspoint

import android.net.wifi.WifiInfo
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.wifi.model.WiFiConnection

data class ConnectionDisplayState(
    val ssid: String,
    val ipAddress: String,
    val linkSpeed: Int,
    val isConnected: Boolean
)

@Composable
fun ConnectionSection(
    state: ConnectionDisplayState?,
    modifier: Modifier = Modifier
) {
    if (state?.isConnected == true) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = state.ssid,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.id.ipAddress),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = state.ipAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (state.linkSpeed != WiFiConnection.LINK_SPEED_INVALID) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.id.linkSpeed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${state.linkSpeed}${WifiInfo.LINK_SPEED_UNITS}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectionSectionPreview() {
    WiFiAnalyzerTheme {
        ConnectionSection(
            state = ConnectionDisplayState(
                ssid = "MyWiFi-5GHz",
                ipAddress = "192.168.1.100",
                linkSpeed = 866,
                isConnected = true
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectionSectionDisconnectedPreview() {
    WiFiAnalyzerTheme {
        ConnectionSection(state = null)
    }
}
```

---

## 📱 ステップ6: AboutFragmentのCompose移行

### 6.1 Composeコンポーネントの作成

**新規ファイル**: `app/src/main/kotlin/com/vrem/wifianalyzer/about/AboutScreen.kt`

```kotlin
package com.vrem.wifianalyzer.about

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import com.vrem.util.packageInfo
import com.vrem.util.readFile
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.ui.theme.WiFiAnalyzerTheme
import java.text.SimpleDateFormat
import java.util.*

data class AboutState(
    val copyright: String,
    val versionInfo: String,
    val packageName: String,
    val device: String,
    val wifiThrottling: Boolean,
    val wifi5GHzSupported: Boolean,
    val wifi6GHzSupported: Boolean
)

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as Activity

    val state = remember {
        produceAboutState(activity)
    }

    var showLicenseDialog by remember { mutableStateOf(false) }
    var showContributorsDialog by remember { mutableStateOf(false) }
    var showApacheLicenseDialog by remember { mutableStateOf(false) }

    AboutContent(
        state = state,
        onLicenseClick = { showLicenseDialog = true },
        onContributorsClick = { showContributorsDialog = true },
        onGraphViewLicenseClick = { showApacheLicenseDialog = true },
        onMaterialDesignIconsLicenseClick = { showApacheLicenseDialog = true },
        onWriteReviewClick = {
            val url = "market://details?id=${activity.packageName}"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            runCatching {
                activity.startActivity(intent)
            }.onFailure {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        },
        modifier = modifier
    )

    if (showLicenseDialog) {
        LicenseDialog(
            title = stringResource(R.string.gpl),
            resourceId = R.raw.gpl,
            onDismiss = { showLicenseDialog = false }
        )
    }

    if (showContributorsDialog) {
        LicenseDialog(
            title = stringResource(R.string.about_contributor_title),
            resourceId = R.raw.contributors,
            isSmallFont = false,
            onDismiss = { showContributorsDialog = false }
        )
    }

    if (showApacheLicenseDialog) {
        LicenseDialog(
            title = stringResource(R.string.al),
            resourceId = R.raw.al,
            onDismiss = { showApacheLicenseDialog = false }
        )
    }
}

@Composable
private fun produceAboutState(activity: Activity): AboutState {
    val context = LocalContext.current
    val mainContext = MainContext.INSTANCE

    val copyright = context.getString(R.string.app_copyright) +
        SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

    val packageInfo = runCatching { activity.packageInfo() }.getOrNull()
    val versionName = packageInfo?.versionName ?: "Unknown"
    val versionCode = packageInfo?.let { PackageInfoCompat.getLongVersionCode(it) } ?: 0

    val configuration = mainContext.configuration
    val versionInfo = "$versionName" +
        (if (configuration.sizeAvailable) "S" else "") +
        (if (configuration.largeScreen) "L" else "") +
        " (${Build.VERSION.RELEASE}-${Build.VERSION.SDK_INT})"

    val device = "${Build.MANUFACTURER} - ${Build.BRAND} - ${Build.MODEL}"

    val wiFiManagerWrapper = mainContext.wiFiManagerWrapper

    return AboutState(
        copyright = copyright,
        versionInfo = "$versionName - $versionCode",
        packageName = activity.packageName,
        device = device,
        wifiThrottling = wiFiManagerWrapper.isScanThrottleEnabled(),
        wifi5GHzSupported = wiFiManagerWrapper.is5GHzBandSupported(),
        wifi6GHzSupported = wiFiManagerWrapper.is6GHzBandSupported()
    )
}

@Composable
private fun AboutContent(
    state: AboutState,
    onLicenseClick: () -> Unit,
    onContributorsClick: () -> Unit,
    onGraphViewLicenseClick: () -> Unit,
    onMaterialDesignIconsLicenseClick: () -> Unit,
    onWriteReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Info Section
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = state.copyright,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = state.versionInfo,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = state.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = state.device,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // WiFi Capabilities Section
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.about_wifi_capabilities),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                WiFiCapabilityRow(
                    label = stringResource(R.string.about_wifi_throttling),
                    enabled = state.wifiThrottling
                )
                WiFiCapabilityRow(
                    label = stringResource(R.string.about_wifi_band_5ghz),
                    enabled = state.wifi5GHzSupported
                )
                WiFiCapabilityRow(
                    label = stringResource(R.string.about_wifi_band_6ghz),
                    enabled = state.wifi6GHzSupported
                )
            }
        }

        // License Section
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.about_license_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ClickableTextRow(
                    text = stringResource(R.string.gpl),
                    onClick = onLicenseClick
                )
                ClickableTextRow(
                    text = stringResource(R.string.about_contributor_title),
                    onClick = onContributorsClick
                )
                ClickableTextRow(
                    text = "GraphView - ${stringResource(R.string.al)}",
                    onClick = onGraphViewLicenseClick
                )
                ClickableTextRow(
                    text = "Material Design Icons - ${stringResource(R.string.al)}",
                    onClick = onMaterialDesignIconsLicenseClick
                )
            }
        }

        // Write Review Button
        Button(
            onClick = onWriteReviewClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.about_write_review))
        }
    }
}

@Composable
private fun WiFiCapabilityRow(
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(
            imageVector = if (enabled) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ClickableTextRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}

@Composable
private fun LicenseDialog(
    title: String,
    resourceId: Int,
    isSmallFont: Boolean = true,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val text = remember {
        readFile(context.resources, resourceId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Text(
                text = text,
                style = if (isSmallFont) {
                    MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.8f)
                } else {
                    MaterialTheme.typography.bodyMedium
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    WiFiAnalyzerTheme {
        AboutContent(
            state = AboutState(
                copyright = "© 2015 - 2025 VREM Software Development",
                versionInfo = "3.2.1 - 321",
                packageName = "com.vrem.wifianalyzer",
                device = "Samsung - Galaxy - S21",
                wifiThrottling = true,
                wifi5GHzSupported = true,
                wifi6GHzSupported = false
            ),
            onLicenseClick = {},
            onContributorsClick = {},
            onGraphViewLicenseClick = {},
            onMaterialDesignIconsLicenseClick = {},
            onWriteReviewClick = {}
        )
    }
}
```

### 6.2 AboutFragment更新

**更新**: `AboutFragment.kt`

```kotlin
package com.vrem.wifianalyzer.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.vrem.wifianalyzer.ui.theme.WiFiAnalyzerTheme

class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                WiFiAnalyzerTheme {
                    AboutScreen()
                }
            }
        }
    }
}
```

---

## ✅ ステップ7: テストと検証

### 7.1 ユニットテストの実行

```bash
./gradlew testDebugUnitTest
```

### 7.2 UIテストの実行

```bash
./gradlew connectedDebugAndroidTest
```

### 7.3 手動テスト項目

- [ ] ChannelAvailableFragment表示確認
- [ ] 国コード変更時の再描画確認
- [ ] AboutFragmentの表示確認
- [ ] Dialogの動作確認
- [ ] テーマ切り替え（Light/Dark/Black/System）確認
- [ ] 画面回転時の状態保持確認

---

## 📝 ステップ8: ドキュメントと移行記録

### 8.1 移行記録の作成

**新規ファイル**: `docs/compose_migration_log.md`

```markdown
# Compose Migration Log

## Phase 1 - Completed Components

### ChannelAvailableFragment
- **Status**: ✅ Migrated
- **Date**: [実装日]
- **Files**:
  - `ChannelAvailableScreen.kt` (新規)
  - `ChannelAvailableFragment.kt` (更新: Compose統合)
- **Notes**:
  - ViewBindingから完全にComposeへ移行
  - Previewを追加してデザイン確認を容易化

### AboutFragment
- **Status**: ✅ Migrated
- **Date**: [実装日]
- **Files**:
  - `AboutScreen.kt` (新規)
  - `AboutFragment.kt` (更新: Compose統合)
- **Notes**:
  - AlertDialog → Material3 AlertDialog
  - クリックリスナー → Compose onClick

### WarningView
- **Status**: 🚧 Partial (Component created, integration pending)
- **Date**: [実装日]
- **Files**:
  - `WarningComposable.kt` (新規)
- **Notes**:
  - MainActivityのCompose移行時に完全統合予定

### ConnectionView
- **Status**: 🚧 Partial (Component created, integration pending)
- **Date**: [実装日]
- **Files**:
  - `ConnectionComposable.kt` (新規)
- **Notes**:
  - MainActivityのCompose移行時に完全統合予定
```

---

## 🎯 次のステップ（Phase 2以降）

### Phase 2の準備
1. **SettingsFragment**のCompose移行計画
2. **VendorFragment**のLazyColumn実装設計
3. **Navigation Compose**の導入検討

### Phase 3以降
1. **AccessPointsFragment**のExpandableList実装
2. **ChannelRatingFragment**の複雑なリスト実装
3. **Graph系Fragment**の代替ライブラリ調査（Vico、Compose Charts等）

---

## 📚 参考リソース

### 公式ドキュメント
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3 in Compose](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Compose Migration Guide](https://developer.android.com/jetpack/compose/migrate)
- [Interoperability APIs](https://developer.android.com/jetpack/compose/migrate/interoperability-apis)

### ベストプラクティス
- [State in Compose](https://developer.android.com/jetpack/compose/state)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Side-effects in Compose](https://developer.android.com/jetpack/compose/side-effects)

---

## 🔄 バージョン管理

### Git Workflow

```bash
# Feature Branchの作成
git checkout -b compose/phase1-migration

# 各コンポーネントごとにコミット
git add app/src/main/kotlin/com/vrem/wifianalyzer/ui/theme/
git commit -m "Add Compose theme system (Material3)"

git add app/src/main/kotlin/com/vrem/wifianalyzer/wifi/channelavailable/ChannelAvailableScreen.kt
git commit -m "Migrate ChannelAvailableFragment to Compose"

# Phase 1完了時にマージ
git checkout main
git merge compose/phase1-migration
```

---

## ⚠️ 注意事項とリスク

### リスク管理

1. **後方互換性**
   - minSdkVersion 24をサポート
   - Compose BOMで依存関係を統一

2. **パフォーマンス**
   - Composeのリコンポジション最適化
   - remember/derivedStateOfの適切な使用

3. **テスト**
   - 既存のRobolectricテストとの共存
   - Compose UIテストの段階的な追加

4. **チーム教育**
   - Composeの学習曲線
   - ベストプラクティスの共有

### トラブルシューティング

#### ビルドエラー
```bash
# Gradleキャッシュのクリア
./gradlew clean
rm -rf ~/.gradle/caches/
./gradlew build
```

#### Composeプレビューが表示されない
- Android Studio Hedgehog以降を使用
- `@Preview`アノテーションの確認
- Invalidate Caches and Restart

---

## 📊 成功指標

### Phase 1完了の定義
- [ ] 4つのコンポーネントすべてがCompose化
- [ ] 既存のユニットテストがすべて通過
- [ ] 新規ComposeUIテストの追加
- [ ] テーマシステムの動作確認（4テーマ）
- [ ] パフォーマンス劣化なし
- [ ] ドキュメント完成

---

**最終更新日**: 2025-12-29
**担当**: [Your Name]
**レビュアー**: [Reviewer Name]
