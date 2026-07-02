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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MareDarkPrimary,
    onPrimary = MareDarkOnPrimary,
    primaryContainer = MareDarkPrimaryContainer,
    onPrimaryContainer = MareDarkOnPrimaryContainer,
    secondary = MareDarkSecondary,
    onSecondary = MareDarkOnSecondary,
    secondaryContainer = MareDarkSecondaryContainer,
    onSecondaryContainer = MareDarkOnSecondaryContainer,
    background = MareDarkBackground,
    onBackground = MareDarkOnBackground,
    surface = MareDarkSurface,
    onSurface = MareDarkOnSurface,
    surfaceVariant = MareDarkSurfaceVariant,
    onSurfaceVariant = MareDarkOnSurfaceVariant,
    outline = MareDarkOutline,
    outlineVariant = MareDarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = MarePrimary,
    onPrimary = MareOnPrimary,
    primaryContainer = MarePrimaryContainer,
    onPrimaryContainer = MareOnPrimaryContainer,
    secondary = MareSecondary,
    onSecondary = MareOnSecondary,
    secondaryContainer = MareSecondaryContainer,
    onSecondaryContainer = MareOnSecondaryContainer,
    tertiary = MareTertiary,
    onTertiary = MareOnTertiary,
    tertiaryContainer = MareTertiaryContainer,
    onTertiaryContainer = MareOnTertiaryContainer,
    background = MareBackground,
    onBackground = MareOnBackground,
    surface = MareSurface,
    onSurface = MareOnSurface,
    surfaceVariant = MareSurfaceVariant,
    onSurfaceVariant = MareOnSurfaceVariant,
    outline = MareOutline,
    outlineVariant = MareOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    customPrimary: Color? = null,
    customBackground: Color? = null,
    content: @Composable () -> Unit,
) {
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val colorScheme = if (customPrimary != null || customBackground != null) {
        val prim = customPrimary ?: baseScheme.primary
        val bg = customBackground ?: baseScheme.background
        val onPrim = if (prim.luminance() > 0.5f) Color(0xFF103630) else Color.White
        
        if (darkTheme) {
            baseScheme.copy(
                primary = prim,
                onPrimary = onPrim,
                primaryContainer = prim.copy(alpha = 0.2f),
                onPrimaryContainer = prim,
                background = bg,
                surface = bg,
                surfaceVariant = bg
            )
        } else {
            baseScheme.copy(
                primary = prim,
                onPrimary = onPrim,
                primaryContainer = prim.copy(alpha = 0.15f),
                onPrimaryContainer = prim,
                background = bg,
                surface = bg,
                surfaceVariant = bg
            )
        }
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
