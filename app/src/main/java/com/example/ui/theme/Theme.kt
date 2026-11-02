package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Deep black for all text reading
val TextBlack = Color(0xFF000000)
val TextDark = Color(0xFF111827)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFF3B0764),
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Amber80,
    onSecondary = Color(0xFF78350F),
    background = LightBlueBackground,
    surface = Color.White,
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Amber40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF000000),
    background = LightBlueBackground,
    surface = Color.White,
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE0F2FE),
    onSurfaceVariant = Color(0xFF000000)
)

@Composable
fun RTailoringTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
