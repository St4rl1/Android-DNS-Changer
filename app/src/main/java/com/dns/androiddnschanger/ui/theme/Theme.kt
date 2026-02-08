package com.dns.androiddnschanger.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Tvoje vlastní barevná paleta ---
// hsl(218 66% 2%) → #010406
private val BgDark = Color(0xFF010406)

// hsl(213 43% 5%) → #070C11
private val Bg = Color(0xFF070C11)

// hsl(212 25% 9%) → #111419
private val BgLight = Color(0xFF111419)

// hsl(212 100% 96%) → #EBF4FF
private val TextColor = Color(0xFFEBF4FF)

// hsl(212 22% 71%) → #A9B5C4
private val TextMuted = Color(0xFFA9B5C4)

// hsl(212 14% 40%) → #575E68
private val Highlight = Color(0xFF575E68)

// hsl(212 19% 29%) → #3B4450
private val BorderColor = Color(0xFF3B4450)

// hsl(212 27% 19%) → #242A34
private val BorderMuted = Color(0xFF242A34)

// hsl(212 77% 72%) → #8EC3FD
private val Primary = Color(0xFF8EC3FD)

// hsl(34 60% 63%) → #D5A672
private val Secondary = Color(0xFFD5A672)

// hsl(9 26% 64%) → #B48889
private val Danger = Color(0xFFB48889)

// hsl(52 19% 57%) → #A8A582
private val Warning = Color(0xFFA8A582)

// hsl(146 17% 59%) → #7FA98B
private val Success = Color(0xFF7FA98B)

// hsl(217 28% 65%) → #7B92B3
private val Info = Color(0xFF7B92B3)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Info,
    background = Bg,
    surface = BgLight,
    surfaceVariant = BgLight,
    error = Danger,
    onPrimary = BgDark,
    onSecondary = BgDark,
    onTertiary = BgDark,
    onBackground = TextColor,
    onSurface = TextColor,
    onSurfaceVariant = TextMuted,
    onError = TextColor,
    outline = BorderColor,
    outlineVariant = BorderMuted,
    primaryContainer = Highlight,
    onPrimaryContainer = TextColor,
    secondaryContainer = Secondary.copy(alpha = 0.2f),
    onSecondaryContainer = TextColor,
    surfaceContainer = BgLight,
    surfaceContainerHigh = BgLight,
    surfaceContainerHighest = BgLight,
)

@Composable
fun AndroidDNSChangerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgDark.toArgb()
            window.navigationBarColor = BgDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}