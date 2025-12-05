package com.apptolast.customlogin.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Color scheme for authentication screens.
 */
@Immutable
data class AuthColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val background: Color,
    val onBackground: Color,
    val error: Color,
    val onError: Color,
    val outline: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val inputBorderFocused: Color,
    val link: Color,
    val divider: Color,
    val success: Color
) {
    companion object {
        /**
         * Create AuthColors from Material3 ColorScheme.
         */
        @Composable
        fun fromMaterialTheme(): AuthColors {
            val colors = MaterialTheme.colorScheme
            return AuthColors(
                primary = colors.primary,
                onPrimary = colors.onPrimary,
                secondary = colors.secondary,
                onSecondary = colors.onSecondary,
                surface = colors.surface,
                onSurface = colors.onSurface,
                surfaceVariant = colors.surfaceVariant,
                onSurfaceVariant = colors.onSurfaceVariant,
                background = colors.background,
                onBackground = colors.onBackground,
                error = colors.error,
                onError = colors.onError,
                outline = colors.outline,
                inputBackground = colors.surfaceVariant,
                inputBorder = colors.outline,
                inputBorderFocused = colors.primary,
                link = colors.primary,
                divider = colors.outlineVariant,
                success = Color(0xFF4CAF50)
            )
        }

        /**
         * Default light theme colors.
         */
        val Light = AuthColors(
            primary = Color(0xFF0D47A1),
            onPrimary = Color.White,
            secondary = Color(0xFF1976D2),
            onSecondary = Color.White,
            surface = Color.White,
            onSurface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFFF5F5F5),
            onSurfaceVariant = Color(0xFF49454F),
            background = Color.White,
            onBackground = Color(0xFF1C1B1F),
            error = Color(0xFFB3261E),
            onError = Color.White,
            outline = Color(0xFF79747E),
            inputBackground = Color(0xFFF5F5F5),
            inputBorder = Color(0xFFE0E0E0),
            inputBorderFocused = Color(0xFF0D47A1),
            link = Color(0xFF1976D2),
            divider = Color(0xFFE0E0E0),
            success = Color(0xFF4CAF50)
        )

        /**
         * Default dark theme colors.
         */
        val Dark = AuthColors(
            primary = Color(0xFF90CAF9),
            onPrimary = Color(0xFF0D47A1),
            secondary = Color(0xFF64B5F6),
            onSecondary = Color(0xFF0D47A1),
            surface = Color(0xFF1C1B1F),
            onSurface = Color(0xFFE6E1E5),
            surfaceVariant = Color(0xFF2D2D2D),
            onSurfaceVariant = Color(0xFFCAC4D0),
            background = Color(0xFF1C1B1F),
            onBackground = Color(0xFFE6E1E5),
            error = Color(0xFFF2B8B5),
            onError = Color(0xFF601410),
            outline = Color(0xFF938F99),
            inputBackground = Color(0xFF2D2D2D),
            inputBorder = Color(0xFF49454F),
            inputBorderFocused = Color(0xFF90CAF9),
            link = Color(0xFF90CAF9),
            divider = Color(0xFF49454F),
            success = Color(0xFF81C784)
        )
    }
}

/**
 * Typography for authentication screens.
 */
@Immutable
data class AuthTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle
) {
    companion object {
        /**
         * Create AuthTypography from Material3 Typography.
         */
        @Composable
        fun fromMaterialTheme(): AuthTypography {
            val typography = MaterialTheme.typography
            return AuthTypography(
                displayLarge = typography.displayLarge,
                displayMedium = typography.displayMedium,
                displaySmall = typography.displaySmall,
                headlineLarge = typography.headlineLarge,
                headlineMedium = typography.headlineMedium,
                headlineSmall = typography.headlineSmall,
                titleLarge = typography.titleLarge,
                titleMedium = typography.titleMedium,
                titleSmall = typography.titleSmall,
                bodyLarge = typography.bodyLarge,
                bodyMedium = typography.bodyMedium,
                bodySmall = typography.bodySmall,
                labelLarge = typography.labelLarge,
                labelMedium = typography.labelMedium,
                labelSmall = typography.labelSmall
            )
        }

        /**
         * Default typography.
         */
        val Default = AuthTypography(
            displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Normal, lineHeight = 64.sp),
            displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.Normal, lineHeight = 52.sp),
            displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Normal, lineHeight = 44.sp),
            headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold, lineHeight = 40.sp),
            headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 36.sp),
            headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),
            titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, lineHeight = 28.sp),
            titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
            titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
            bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
            bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
            bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),
            labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
            labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
            labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp)
        )
    }
}

/**
 * Shapes for authentication screens.
 */
@Immutable
data class AuthShapes(
    val button: Shape,
    val buttonSmall: Shape,
    val input: Shape,
    val card: Shape,
    val container: Shape,
    val avatar: Shape
) {
    companion object {
        val Default = AuthShapes(
            button = RoundedCornerShape(12.dp),
            buttonSmall = RoundedCornerShape(8.dp),
            input = RoundedCornerShape(12.dp),
            card = RoundedCornerShape(16.dp),
            container = RoundedCornerShape(24.dp),
            avatar = RoundedCornerShape(50)
        )
    }
}

/**
 * Spacing values for authentication screens.
 */
@Immutable
data class AuthSpacing(
    val xs: Int = 4,
    val sm: Int = 8,
    val md: Int = 16,
    val lg: Int = 24,
    val xl: Int = 32,
    val xxl: Int = 48
) {
    companion object {
        val Default = AuthSpacing()
    }
}

/**
 * Complete theme for authentication screens.
 */
@Immutable
data class AuthTheme(
    val colors: AuthColors,
    val typography: AuthTypography,
    val shapes: AuthShapes,
    val spacing: AuthSpacing
) {
    companion object {
        /**
         * Create theme from MaterialTheme.
         */
        @Composable
        fun fromMaterialTheme(): AuthTheme = AuthTheme(
            colors = AuthColors.fromMaterialTheme(),
            typography = AuthTypography.fromMaterialTheme(),
            shapes = AuthShapes.Default,
            spacing = AuthSpacing.Default
        )

        /**
         * Default light theme.
         */
        val Light = AuthTheme(
            colors = AuthColors.Light,
            typography = AuthTypography.Default,
            shapes = AuthShapes.Default,
            spacing = AuthSpacing.Default
        )

        /**
         * Default dark theme.
         */
        val Dark = AuthTheme(
            colors = AuthColors.Dark,
            typography = AuthTypography.Default,
            shapes = AuthShapes.Default,
            spacing = AuthSpacing.Default
        )
    }
}

/**
 * CompositionLocal for accessing AuthTheme in composables.
 */
val LocalAuthTheme = staticCompositionLocalOf { AuthTheme.Light }

/**
 * Provider composable for AuthTheme.
 */
@Composable
fun AuthThemeProvider(
    theme: AuthTheme = AuthTheme.fromMaterialTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAuthTheme provides theme) {
        content()
    }
}

/**
 * Convenience object to access current AuthTheme.
 */
object AuthThemeDefaults {
    val theme: AuthTheme
        @Composable
        get() = LocalAuthTheme.current

    val colors: AuthColors
        @Composable
        get() = LocalAuthTheme.current.colors

    val typography: AuthTypography
        @Composable
        get() = LocalAuthTheme.current.typography

    val shapes: AuthShapes
        @Composable
        get() = LocalAuthTheme.current.shapes

    val spacing: AuthSpacing
        @Composable
        get() = LocalAuthTheme.current.spacing
}
