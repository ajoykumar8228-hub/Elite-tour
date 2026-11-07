package Com.Elite.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkNavyBg,
    primaryContainer = CardNavyBg,
    onPrimaryContainer = PureWhite,
    secondary = NeonOrange,
    onSecondary = DarkNavyBg,
    secondaryContainer = CardNavyBorder,
    onSecondaryContainer = PureWhite,
    tertiary = GoldYellow,
    background = DarkNavyBg,
    onBackground = PureWhite,
    surface = CardNavyBg,
    onSurface = PureWhite,
    surfaceVariant = CardNavyBorder,
    onSurfaceVariant = PureWhite,
    error = CrimsonRed,
    onError = PureWhite
)

@Composable
fun EliteTournamentTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
