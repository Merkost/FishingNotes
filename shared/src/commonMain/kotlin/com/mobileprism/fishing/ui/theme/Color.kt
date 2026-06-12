package com.mobileprism.fishing.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val RedGoogleChrome = Color(0xFFde5246)

// Green base colors
@Deprecated("Use MaterialTheme.colorScheme.primary", ReplaceWith("MaterialTheme.colorScheme.primary"))
val primaryFigmaColor = Color(0xFF43a047)
private val primaryFigmaLightColor = Color(0xFF76d275)
private val primaryFigmaDarkColor = Color(0xFF00701a)
@Deprecated("Use MaterialTheme.colorScheme.secondary", ReplaceWith("MaterialTheme.colorScheme.secondary"))
val secondaryFigmaColor = Color(0xFFff6d00)
private val secondaryFigmaLightColor = Color(0xFFff9e40)
private val secondaryFigmaDarkColor = Color(0xFFc43c00)
private val primaryFigmaTextColor = Color(0xDE000000)
val secondaryFigmaTextColor = Color(0x8A000000)
private val supportFigmaTextColor = Color(0x42000000)
private val surfaceGrayColor = Color(0x12000000)
private val primaryFigmaBackgroundTint = Color(0xFFFFF7E6)
private val backgroundWhiteColor = Color(0xFFFFFFFF)
private val backgroundGreenColor = Color(0x2043A047)
private val surfaceGreenColor = Color(0x8F79B97B)
@Deprecated("Use MaterialTheme.colorScheme.surfaceContainer", ReplaceWith("MaterialTheme.colorScheme.surfaceContainer"))
val cardColor = Color(0xFF8FA590)

// Blue base colors
@Deprecated("Use MaterialTheme.colorScheme.primary", ReplaceWith("MaterialTheme.colorScheme.primary"))
val primaryBlueColor = Color(0xFF2196f3)
private val primaryBlueColorTransparent = Color(0xF22196F3)
@Deprecated("Use MaterialTheme.colorScheme.primary", ReplaceWith("MaterialTheme.colorScheme.primary"))
val primaryBlueLightColorTransparent = Color(0x276EC6FF)
private val primaryBlueLightColor = Color(0xFF6ec6ff)
private val primaryBlueDarkColor = Color(0xFF0069c0)
private val primaryBlueDarkColorTransparent = Color(0xE60069C0)
private val secondaryBlueColor = Color(0xFFff6d00)
private val secondaryBlueLightColor = Color(0xFFff9e40)
private val secondaryBlueLightColorTransparent = Color(0x19FF9E40)
private val secondaryBlueDarkColor = Color(0xFFc43c00)

val secondaryTextColor = Color(0x8A000000)

private val primaryWhiteColor = Color(0xFFFFFFFF)
private val secondaryWhiteColor = Color(0xFFE9E9E9)

// ── Blue Light palette aliases ──────────────────────────────────────────────

private val BlueLightPrimary = primaryBlueColor
private val BlueLightOnPrimary = primaryWhiteColor
private val BlueLightPrimaryContainer = Color(0xFFD3E4FF)
private val BlueLightOnPrimaryContainer = Color(0xFF001C38)
private val BlueLightSecondary = secondaryBlueColor
private val BlueLightOnSecondary = primaryWhiteColor
private val BlueLightSecondaryContainer = Color(0xFFFFDBC8)
private val BlueLightOnSecondaryContainer = Color(0xFF2B1700)
private val BlueLightTertiary = primaryBlueDarkColor
private val BlueLightSurface = Color(0xFFFCFCFF)
private val BlueLightOnSurface = Color(0xFF1B1B1F)
private val BlueLightSurfaceVariant = Color(0xFFE0E2EC)
private val BlueLightOnSurfaceVariant = Color(0xFF44474E)
private val BlueLightOutline = Color(0xFF74777F)
private val BlueLightOutlineVariant = Color(0xFFC4C6D0)
private val BlueLightSurfaceDim = Color(0xFFD9DAE0)
private val BlueLightSurfaceBright = Color(0xFFFCFCFF)
private val BlueLightSurfaceContainerLowest = Color(0xFFFFFFFF)
private val BlueLightSurfaceContainerLow = Color(0xFFF3F4F9)
private val BlueLightSurfaceContainer = Color(0xFFEDEEF3)
private val BlueLightSurfaceContainerHigh = Color(0xFFE7E8ED)
private val BlueLightSurfaceContainerHighest = Color(0xFFE1E2E8)
private val BlueLightInverseSurface = Color(0xFF303034)
private val BlueLightInverseOnSurface = Color(0xFFF1F0F4)
private val BlueLightInversePrimary = Color(0xFF9ECAFF)

// ── Blue Dark palette aliases ───────────────────────────────────────────────

private val BlueDarkPrimary = Color(0xFF9ECAFF)
private val BlueDarkOnPrimary = Color(0xFF003258)
private val BlueDarkPrimaryContainer = Color(0xFF004880)
private val BlueDarkOnPrimaryContainer = Color(0xFFD3E4FF)
private val BlueDarkSecondary = Color(0xFFFFB781)
private val BlueDarkOnSecondary = Color(0xFF4A2800)
private val BlueDarkSecondaryContainer = Color(0xFF6A3B00)
private val BlueDarkOnSecondaryContainer = Color(0xFFFFDBC8)
private val BlueDarkTertiary = primaryBlueColor
private val BlueDarkSurface = Color(0xFF111318)
private val BlueDarkOnSurface = Color(0xFFE2E2E9)
private val BlueDarkSurfaceVariant = Color(0xFF44474E)
private val BlueDarkOnSurfaceVariant = Color(0xFFC4C6D0)
private val BlueDarkOutline = Color(0xFF8E9099)
private val BlueDarkOutlineVariant = Color(0xFF44474E)
private val BlueDarkSurfaceDim = Color(0xFF111318)
private val BlueDarkSurfaceBright = Color(0xFF37393E)
private val BlueDarkSurfaceContainerLowest = Color(0xFF0C0E13)
private val BlueDarkSurfaceContainerLow = Color(0xFF191C20)
private val BlueDarkSurfaceContainer = Color(0xFF1D2024)
private val BlueDarkSurfaceContainerHigh = Color(0xFF282A2F)
private val BlueDarkSurfaceContainerHighest = Color(0xFF33353A)
private val BlueDarkInverseSurface = Color(0xFFE2E2E9)
private val BlueDarkInverseOnSurface = Color(0xFF2E3036)
private val BlueDarkInversePrimary = primaryBlueDarkColor

// ── Green Light palette aliases ─────────────────────────────────────────────

private val GreenLightPrimary = primaryFigmaColor
private val GreenLightOnPrimary = primaryWhiteColor
private val GreenLightPrimaryContainer = Color(0xFFC8E6C9)
private val GreenLightOnPrimaryContainer = Color(0xFF002106)
private val GreenLightSecondary = secondaryFigmaColor
private val GreenLightOnSecondary = primaryWhiteColor
private val GreenLightSecondaryContainer = Color(0xFFFFDBC8)
private val GreenLightOnSecondaryContainer = Color(0xFF2B1700)
private val GreenLightTertiary = primaryFigmaDarkColor
private val GreenLightSurface = Color(0xFFF9FCF6)
private val GreenLightOnSurface = Color(0xFF1A1C18)
private val GreenLightSurfaceVariant = Color(0xFFDEE5D9)
private val GreenLightOnSurfaceVariant = Color(0xFF424940)
private val GreenLightOutline = Color(0xFF72796F)
private val GreenLightOutlineVariant = Color(0xFFC2C9BD)
private val GreenLightSurfaceDim = Color(0xFFD8DCD2)
private val GreenLightSurfaceBright = Color(0xFFF9FCF6)
private val GreenLightSurfaceContainerLowest = Color(0xFFFFFFFF)
private val GreenLightSurfaceContainerLow = Color(0xFFF2F5EF)
private val GreenLightSurfaceContainer = Color(0xFFECF0EA)
private val GreenLightSurfaceContainerHigh = Color(0xFFE7EAE4)
private val GreenLightSurfaceContainerHighest = Color(0xFFE1E4DE)
private val GreenLightInverseSurface = Color(0xFF2F312C)
private val GreenLightInverseOnSurface = Color(0xFFF0F1EC)
private val GreenLightInversePrimary = primaryFigmaLightColor

// ── Green Dark palette aliases ──────────────────────────────────────────────

private val GreenDarkPrimary = primaryFigmaLightColor
private val GreenDarkOnPrimary = Color(0xFF00390A)
private val GreenDarkPrimaryContainer = Color(0xFF005313)
private val GreenDarkOnPrimaryContainer = Color(0xFFC8E6C9)
private val GreenDarkSecondary = Color(0xFFFFB781)
private val GreenDarkOnSecondary = Color(0xFF4A2800)
private val GreenDarkSecondaryContainer = Color(0xFF6A3B00)
private val GreenDarkOnSecondaryContainer = Color(0xFFFFDBC8)
private val GreenDarkTertiary = primaryFigmaColor
private val GreenDarkSurface = Color(0xFF101410)
private val GreenDarkOnSurface = Color(0xFFE0E4DB)
private val GreenDarkSurfaceVariant = Color(0xFF424940)
private val GreenDarkOnSurfaceVariant = Color(0xFFC2C9BD)
private val GreenDarkOutline = Color(0xFF8C9388)
private val GreenDarkOutlineVariant = Color(0xFF424940)
private val GreenDarkSurfaceDim = Color(0xFF101410)
private val GreenDarkSurfaceBright = Color(0xFF363A33)
private val GreenDarkSurfaceContainerLowest = Color(0xFF0B0F0B)
private val GreenDarkSurfaceContainerLow = Color(0xFF181C17)
private val GreenDarkSurfaceContainer = Color(0xFF1C201B)
private val GreenDarkSurfaceContainerHigh = Color(0xFF272B25)
private val GreenDarkSurfaceContainerHighest = Color(0xFF323630)
private val GreenDarkInverseSurface = Color(0xFFE0E4DB)
private val GreenDarkInverseOnSurface = Color(0xFF2D312B)
private val GreenDarkInversePrimary = primaryFigmaDarkColor

// ── M3 Color Schemes ────────────────────────────────────────────────────────

val BlueLightColorScheme = lightColorScheme(
    primary = BlueLightPrimary,
    onPrimary = BlueLightOnPrimary,
    primaryContainer = BlueLightPrimaryContainer,
    onPrimaryContainer = BlueLightOnPrimaryContainer,
    secondary = BlueLightSecondary,
    onSecondary = BlueLightOnSecondary,
    secondaryContainer = BlueLightSecondaryContainer,
    onSecondaryContainer = BlueLightOnSecondaryContainer,
    tertiary = BlueLightTertiary,
    onTertiary = primaryWhiteColor,
    surface = BlueLightSurface,
    onSurface = BlueLightOnSurface,
    surfaceVariant = BlueLightSurfaceVariant,
    onSurfaceVariant = BlueLightOnSurfaceVariant,
    outline = BlueLightOutline,
    outlineVariant = BlueLightOutlineVariant,
    surfaceDim = BlueLightSurfaceDim,
    surfaceBright = BlueLightSurfaceBright,
    surfaceContainerLowest = BlueLightSurfaceContainerLowest,
    surfaceContainerLow = BlueLightSurfaceContainerLow,
    surfaceContainer = BlueLightSurfaceContainer,
    surfaceContainerHigh = BlueLightSurfaceContainerHigh,
    surfaceContainerHighest = BlueLightSurfaceContainerHighest,
    inverseSurface = BlueLightInverseSurface,
    inverseOnSurface = BlueLightInverseOnSurface,
    inversePrimary = BlueLightInversePrimary,
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

val BlueDarkColorScheme = darkColorScheme(
    primary = BlueDarkPrimary,
    onPrimary = BlueDarkOnPrimary,
    primaryContainer = BlueDarkPrimaryContainer,
    onPrimaryContainer = BlueDarkOnPrimaryContainer,
    secondary = BlueDarkSecondary,
    onSecondary = BlueDarkOnSecondary,
    secondaryContainer = BlueDarkSecondaryContainer,
    onSecondaryContainer = BlueDarkOnSecondaryContainer,
    tertiary = BlueDarkTertiary,
    onTertiary = primaryWhiteColor,
    surface = BlueDarkSurface,
    onSurface = BlueDarkOnSurface,
    surfaceVariant = BlueDarkSurfaceVariant,
    onSurfaceVariant = BlueDarkOnSurfaceVariant,
    outline = BlueDarkOutline,
    outlineVariant = BlueDarkOutlineVariant,
    surfaceDim = BlueDarkSurfaceDim,
    surfaceBright = BlueDarkSurfaceBright,
    surfaceContainerLowest = BlueDarkSurfaceContainerLowest,
    surfaceContainerLow = BlueDarkSurfaceContainerLow,
    surfaceContainer = BlueDarkSurfaceContainer,
    surfaceContainerHigh = BlueDarkSurfaceContainerHigh,
    surfaceContainerHighest = BlueDarkSurfaceContainerHighest,
    inverseSurface = BlueDarkInverseSurface,
    inverseOnSurface = BlueDarkInverseOnSurface,
    inversePrimary = BlueDarkInversePrimary,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

val GreenLightColorScheme = lightColorScheme(
    primary = GreenLightPrimary,
    onPrimary = GreenLightOnPrimary,
    primaryContainer = GreenLightPrimaryContainer,
    onPrimaryContainer = GreenLightOnPrimaryContainer,
    secondary = GreenLightSecondary,
    onSecondary = GreenLightOnSecondary,
    secondaryContainer = GreenLightSecondaryContainer,
    onSecondaryContainer = GreenLightOnSecondaryContainer,
    tertiary = GreenLightTertiary,
    onTertiary = primaryWhiteColor,
    surface = GreenLightSurface,
    onSurface = GreenLightOnSurface,
    surfaceVariant = GreenLightSurfaceVariant,
    onSurfaceVariant = GreenLightOnSurfaceVariant,
    outline = GreenLightOutline,
    outlineVariant = GreenLightOutlineVariant,
    surfaceDim = GreenLightSurfaceDim,
    surfaceBright = GreenLightSurfaceBright,
    surfaceContainerLowest = GreenLightSurfaceContainerLowest,
    surfaceContainerLow = GreenLightSurfaceContainerLow,
    surfaceContainer = GreenLightSurfaceContainer,
    surfaceContainerHigh = GreenLightSurfaceContainerHigh,
    surfaceContainerHighest = GreenLightSurfaceContainerHighest,
    inverseSurface = GreenLightInverseSurface,
    inverseOnSurface = GreenLightInverseOnSurface,
    inversePrimary = GreenLightInversePrimary,
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

val GreenDarkColorScheme = darkColorScheme(
    primary = GreenDarkPrimary,
    onPrimary = GreenDarkOnPrimary,
    primaryContainer = GreenDarkPrimaryContainer,
    onPrimaryContainer = GreenDarkOnPrimaryContainer,
    secondary = GreenDarkSecondary,
    onSecondary = GreenDarkOnSecondary,
    secondaryContainer = GreenDarkSecondaryContainer,
    onSecondaryContainer = GreenDarkOnSecondaryContainer,
    tertiary = GreenDarkTertiary,
    onTertiary = primaryWhiteColor,
    surface = GreenDarkSurface,
    onSurface = GreenDarkOnSurface,
    surfaceVariant = GreenDarkSurfaceVariant,
    onSurfaceVariant = GreenDarkOnSurfaceVariant,
    outline = GreenDarkOutline,
    outlineVariant = GreenDarkOutlineVariant,
    surfaceDim = GreenDarkSurfaceDim,
    surfaceBright = GreenDarkSurfaceBright,
    surfaceContainerLowest = GreenDarkSurfaceContainerLowest,
    surfaceContainerLow = GreenDarkSurfaceContainerLow,
    surfaceContainer = GreenDarkSurfaceContainer,
    surfaceContainerHigh = GreenDarkSurfaceContainerHigh,
    surfaceContainerHighest = GreenDarkSurfaceContainerHighest,
    inverseSurface = GreenDarkInverseSurface,
    inverseOnSurface = GreenDarkInverseOnSurface,
    inversePrimary = GreenDarkInversePrimary,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

val InitColorScheme = BlueLightColorScheme

object BrandGradients {
    fun primaryVertical(scheme: ColorScheme): Brush = Brush.verticalGradient(
        listOf(scheme.primary, scheme.tertiary)
    )

    fun primaryDiagonal(scheme: ColorScheme): Brush = Brush.linearGradient(
        listOf(scheme.primary, scheme.tertiary)
    )

    fun surfaceVertical(scheme: ColorScheme): Brush = Brush.verticalGradient(
        listOf(scheme.surface, scheme.surfaceContainerHighest)
    )
}
