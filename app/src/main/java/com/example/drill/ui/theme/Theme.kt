package com.example.drill.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FacetColorScheme =
        darkColorScheme(
                background = FacetBackground,
                surface = FacetSurface,
                onBackground = FacetTextPrimary,
                onSurface = FacetTextPrimary,
                primary = FacetAccent,
                onPrimary = FacetTextPrimary,
                secondary = FacetAccent,
                onSecondary = FacetTextPrimary,
                outline = FacetDivider
        )

@Composable
fun DrillTheme(content: @Composable () -> Unit) {
    MaterialTheme(
            colorScheme = FacetColorScheme,
            typography = Typography,
            shapes = FacetShapes,
            content = content
    )
}
