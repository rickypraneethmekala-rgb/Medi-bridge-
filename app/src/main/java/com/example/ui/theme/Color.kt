package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// MediBridge+ Premium Healthcare Design System: Centralized Color Palette
// ============================================================================

// --- Core Brand Colors ---
// Primary: Deep Navy / Midnight Blue (Trust, Clinical Authority, Elegance)
val MediNavy = Color(0xFF0A192F)
val MediNavyDark = Color(0xFF060F1E)
val MediNavyLight = Color(0xFF132A4A)
val MediNavyContainer = Color(0xFFE2E8F0)
val MediOnNavy = Color(0xFFFFFFFF)

// Secondary: Premium Healthcare Teal / Emerald (Health, Life, Care)
val MediTeal = Color(0xFF00A896)
val MediTealDark = Color(0xFF007A6D)
val MediTealLight = Color(0xFFE0F5F3)
val MediTealContainer = Color(0xFFCCF0EC)
val MediOnTeal = Color(0xFFFFFFFF)

// Accent: Vibrant Cyan / Ice Mint (Modern Technology, Precision, Clarity)
val MediCyan = Color(0xFF00B4D8)
val MediCyanLight = Color(0xFFE0F7FA)
val MediCyanBright = Color(0xFF00E5FF)

// --- Backgrounds & Neutral Surfaces (Clean Crisp White Theme) ---
val MediBackgroundLight = Color(0xFFFFFFFF)      // Pure crisp white background
val MediSurfaceLight = Color(0xFFFFFFFF)         // Crisp pure white card surface
val MediSurfaceVariantLight = Color(0xFFF8FAFC)  // Subtle clean light container
val MediBorderLight = Color(0xFFE2E8F0)          // Subtle card border
val MediDividerLight = Color(0xFFF1F5F9)         // Hairline separator

// --- Typography Colors (Light Mode) ---
val MediTextPrimary = Color(0xFF0F172A)          // High-contrast deep navy/slate
val MediTextSecondary = Color(0xFF475569)        // Muted clinical slate
val MediTextTertiary = Color(0xFF94A3B8)         // Soft caption slate
val MediTextDisabled = Color(0xFFCBD5E1)         // Disabled text

// --- Status & Functional Colors ---
// Confirmed / Success
val MediSuccess = Color(0xFF10B981)
val MediSuccessBg = Color(0xFFECFDF5)
val MediOnSuccess = Color(0xFFFFFFFF)
val MediSuccessText = Color(0xFF047857)

// --- Centralized Input Field Color Tokens (High Accessibility & Contrast) ---
val MediInputBackgroundLight = Color(0xFFFFFFFF)         // Clean light input background
val MediInputBackgroundUnfocusedLight = Color(0xFFF8FAFC)// Subtle unfocused surface
val MediInputTextLight = Color(0xFF0F172A)               // Deep navy / near-black high-contrast text
val MediInputPlaceholderLight = Color(0xFF64748B)        // Medium gray readable placeholder
val MediInputBorderLight = Color(0xFFCBD5E1)             // Light gray unfocused border
val MediInputBorderFocusedLight = MediTeal               // MediBridge teal focused border
val MediInputCursorLight = MediTeal                      // MediBridge teal cursor

val MediInputBackgroundDark = Color(0xFF14263E)          // Dark navy input background
val MediInputBackgroundUnfocusedDark = Color(0xFF0D1B2E) // Darker unfocused input surface
val MediInputTextDark = Color(0xFFF8FAFC)                // Crisp white high-contrast text
val MediInputPlaceholderDark = Color(0xFF94A3B8)         // Light muted gray placeholder
val MediInputBorderDark = Color(0xFF334155)              // Muted slate unfocused border
val MediInputBorderFocusedDark = MediTeal                // MediBridge teal focused border
val MediInputCursorDark = MediCyanBright                 // Bright teal cursor

// Waiting / Warning
val MediWarning = Color(0xFFF59E0B)
val MediWarningBg = Color(0xFFFFFBEB)
val MediOnWarning = Color(0xFFFFFFFF)
val MediWarningText = Color(0xFFB45309)

// Action / Your Turn (Teal Accent)
val MediActionTeal = Color(0xFF00A896)
val MediActionTealBg = Color(0xFFF0FDFA)
val MediActionTealText = Color(0xFF006D62)

// Cancelled / Error / Destructive
val MediError = Color(0xFFEF4444)
val MediErrorBg = Color(0xFFFEF2F2)
val MediOnError = Color(0xFFFFFFFF)
val MediErrorText = Color(0xFFB91C1C)

// Emergency Alert Red (Urgent 24x7)
val MediEmergencyRed = Color(0xFFDC2626)
val MediEmergencyBg = Color(0xFFFEF2F2)
val MediEmergencyText = Color(0xFFFFFFFF)

// --- Screen-Specific Subtle Accents ---
val AccentHome = Color(0xFF00A896)        // Home: MediBridge Navy + Teal
val AccentAppt = Color(0xFF2563EB)        // Appt: Calm Royal Blue
val AccentApptBg = Color(0xFFEFF6FF)
val AccentMedi = Color(0xFF059669)        // Medi: Emerald Health Green
val AccentMediBg = Color(0xFFECFDF5)
val AccentAi = Color(0xFF6366F1)          // AI: Modern Indigo / Cyber Cyan
val AccentAiBg = Color(0xFFEEF2FF)
val AccentDocuments = Color(0xFF4F46E5)   // Documents: Structured Deep Indigo
val AccentDocumentsBg = Color(0xFFEEF2FF)
val AccentDocs = AccentDocuments
val AccentDocsBg = AccentDocumentsBg
val AccentHealth = Color(0xFF10B981)      // Health: Vitality Emerald
val AccentHealthBg = Color(0xFFECFDF5)
val AccentSettings = Color(0xFF64748B)    // Settings: Neutral Refined Slate
val AccentSettingsBg = Color(0xFFF8FAFC)

// --- Dark Mode Surface Palette ---
val MediBackgroundDark = Color(0xFF070E1A)     // Deep midnight canvas
val MediSurfaceDark = Color(0xFF0D1B2E)        // Rich dark navy card surface
val MediSurfaceVariantDark = Color(0xFF14263E) // Dark slate container
val MediBorderDark = Color(0xFF1E3553)         // Dark card border
val MediTextPrimaryDark = Color(0xFFF8FAFC)    // Crisp white text
val MediTextSecondaryDark = Color(0xFF94A3B8)  // Cool gray secondary text
val MediTextTertiaryDark = Color(0xFF64748B)   // Dim gray tertiary text

// Legacy aliases for backwards compatibility
val TealPrimary = MediTeal
val OnTealPrimary = MediOnTeal
val TealPrimaryContainer = MediTealContainer
val OnTealPrimaryContainer = Color(0xFF00201C)
val SecondaryNavy = MediNavy
val OnSecondaryNavy = MediOnNavy
val SecondaryContainer = MediNavyContainer
val OnSecondaryContainer = MediTextPrimary
val TertiaryBlue = MediCyan
val OnTertiaryBlue = Color(0xFFFFFFFF)
val TertiaryContainer = MediCyanLight
val OnTertiaryContainer = Color(0xFF001E31)
val ErrorRed = MediError
val OnErrorRed = MediOnError
val ErrorContainer = MediErrorBg
val OnErrorContainer = MediErrorText
val BackgroundLight = MediBackgroundLight
val OnBackgroundLight = MediTextPrimary
val SurfaceLight = MediSurfaceLight
val OnSurfaceLight = MediTextPrimary
val SurfaceVariantLight = MediSurfaceVariantLight
val OnSurfaceVariantLight = MediTextSecondary
val OutlineLight = MediBorderLight
val EmergencyBannerRed = MediEmergencyRed
val EmergencyBannerText = MediEmergencyText
val SeniorContrastPrimary = Color(0xFF004D40)
val SeniorContrastBackground = Color(0xFFFFFFFF)
val SeniorContrastText = Color(0xFF000000)
val SeniorContrastBorder = Color(0xFF004D40)
