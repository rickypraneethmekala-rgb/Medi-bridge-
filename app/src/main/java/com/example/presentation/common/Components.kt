package com.example.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.UserRole
import com.example.ui.theme.*

// ============================================================================
// MediBridge+ Standardized UI Component Library
// Consistent styling, elevation, typography, and accessibility across all screens
// ============================================================================

/**
 * Standardized MediBridge+ Primary Action Button (Teal / Primary)
 */
@Composable
fun MediPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    testTag: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(MediButtonHeight.standard)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(MediCornerRadius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = MediTeal,
            contentColor = MediOnTeal,
            disabledContainerColor = MediBorderLight,
            disabledContentColor = MediTextTertiary
        ),
        contentPadding = PaddingValues(horizontal = MediSpacing.lg, vertical = MediSpacing.sm)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(MediIconSize.sm)
            )
            Spacer(modifier = Modifier.width(MediSpacing.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Standardized MediBridge+ Outlined Secondary Button
 */
@Composable
fun MediSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    testTag: String = ""
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(MediButtonHeight.standard)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(MediCornerRadius.md),
        border = BorderStroke(1.dp, if (enabled) MediTeal else MediBorderLight),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MediTeal,
            disabledContentColor = MediTextTertiary
        ),
        contentPadding = PaddingValues(horizontal = MediSpacing.lg, vertical = MediSpacing.sm)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(MediIconSize.sm)
            )
            Spacer(modifier = Modifier.width(MediSpacing.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Standardized MediBridge+ Destructive Action Button (Red)
 */
@Composable
fun MediDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    testTag: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(MediButtonHeight.standard)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(MediCornerRadius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = MediError,
            contentColor = MediOnError
        ),
        contentPadding = PaddingValues(horizontal = MediSpacing.lg, vertical = MediSpacing.sm)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(MediIconSize.sm)
            )
            Spacer(modifier = Modifier.width(MediSpacing.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Standardized MediBridge+ Card Container
 * Consistent corner radius, 1dp subtle border, pure white container, comfortable padding
 */
@Composable
fun MediCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    cornerRadius: Dp = MediCornerRadius.lg,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = MediElevation.subtle)
    ) {
        Column(
            modifier = Modifier.padding(MediSpacing.lg),
            content = content
        )
    }
}

/**
 * Standardized Status Badge (Confirmed, Waiting, Your Turn, Cancelled, Delivered, Local, etc.)
 */
@Composable
fun MediStatusBadge(
    statusText: String,
    statusType: MediStatusType = MediStatusType.NEUTRAL,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (statusType) {
        MediStatusType.CONFIRMED, MediStatusType.SUCCESS -> MediSuccessBg to MediSuccessText
        MediStatusType.WAITING, MediStatusType.WARNING -> MediWarningBg to MediWarningText
        MediStatusType.YOUR_TURN, MediStatusType.ACTIVE, MediStatusType.PRIMARY -> MediActionTealBg to MediActionTealText
        MediStatusType.CANCELLED, MediStatusType.ERROR -> MediErrorBg to MediErrorText
        MediStatusType.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        MediStatusType.INFO -> AccentApptBg to AccentAppt
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(MediCornerRadius.pill),
        modifier = modifier
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

enum class MediStatusType {
    CONFIRMED,
    SUCCESS,
    WAITING,
    WARNING,
    YOUR_TURN,
    ACTIVE,
    PRIMARY,
    CANCELLED,
    ERROR,
    NEUTRAL,
    INFO
}

/**
 * Standardized Section Header with Accent Bar
 */
@Composable
fun MediSectionHeader(
    title: String,
    subtitle: String? = null,
    accentColor: Color = MediTeal,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MediSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(MediCornerRadius.xs))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(MediSpacing.sm))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (!actionText.isNullOrEmpty() && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = MediSpacing.sm, vertical = MediSpacing.xs)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Standardized User Role Chip
 */
@Composable
fun UserRoleChip(role: UserRole) {
    Surface(
        color = MediTealLight,
        shape = RoundedCornerShape(MediCornerRadius.pill),
        border = BorderStroke(1.dp, MediTeal.copy(alpha = 0.3f))
    ) {
        Text(
            text = role.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MediTealDark,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Standardized Emergency Quick Access Banner
 */
@Composable
fun EmergencyQuickAccessBanner(
    onEmergencyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MediEmergencyRed,
        shape = RoundedCornerShape(MediCornerRadius.md),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MediSpacing.lg, vertical = MediSpacing.xs)
            .testTag("emergency_banner"),
        shadowElevation = MediElevation.subtle
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = MediSpacing.lg, vertical = MediSpacing.md)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(MediCornerRadius.sm))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Emergency",
                        tint = Color.White,
                        modifier = Modifier.size(MediIconSize.md)
                    )
                }
                Spacer(modifier = Modifier.width(MediSpacing.md))
                Column {
                    Text(
                        text = "Emergency 24x7 / Ambulance",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Dial 108 or locate nearest ER hospital",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(MediSpacing.sm))
            FilledTonalButton(
                onClick = onEmergencyClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color.White,
                    contentColor = MediEmergencyRed
                ),
                shape = RoundedCornerShape(MediCornerRadius.sm),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_emergency_action")
            ) {
                Text(
                    text = "Emergency",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Standardized Medical Disclaimer Banner
 */
@Composable
fun MedicalDisclaimerBanner(
    modifier: Modifier = Modifier,
    customText: String = "AI-generated information is for educational purposes only. Follow your doctor's or pharmacist's instructions."
) {
    Surface(
        color = AccentAiBg,
        shape = RoundedCornerShape(MediCornerRadius.md),
        border = BorderStroke(1.dp, AccentAi.copy(alpha = 0.2f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MediSpacing.lg, vertical = MediSpacing.sm)
            .testTag("medical_disclaimer_banner")
    ) {
        Row(
            modifier = Modifier.padding(MediSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Medical Disclaimer",
                tint = AccentAi,
                modifier = Modifier.size(MediIconSize.sm)
            )
            Spacer(modifier = Modifier.width(MediSpacing.sm))
            Text(
                text = customText,
                style = MaterialTheme.typography.bodySmall,
                color = MediTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Standardized API Not Configured Card
 */
@Composable
fun ApiUnavailableCard(
    serviceName: String,
    endpointName: String,
    onConfigureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MediCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(MediSpacing.lg)
            .testTag("api_unavailable_card"),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(MediCornerRadius.md))
                    .background(MediTealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "API Not Connected",
                    modifier = Modifier.size(MediIconSize.lg),
                    tint = MediTeal
                )
            }
            Spacer(modifier = Modifier.height(MediSpacing.md))
            Text(
                text = "$serviceName API Not Configured",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(MediSpacing.xs))
            Text(
                text = "Live endpoints for $endpointName are not provided. Please set your real backend endpoint in Settings to fetch live clinical data.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(MediSpacing.lg))
            MediPrimaryButton(
                text = "Configure Endpoint",
                onClick = onConfigureClick,
                icon = Icons.Default.Settings,
                testTag = "btn_configure_endpoint",
                modifier = Modifier.widthIn(max = 240.dp)
            )
        }
    }
}

// ============================================================================
// Standardized Input Field Styling & Components (Theme & High Contrast)
// ============================================================================

/**
 * Standardized MediBridge+ TextFieldColors providing high contrast on clean white interface.
 */
@Composable
fun mediTextFieldColors(
    containerColor: Color = MediInputBackgroundLight,
    unfocusedContainerColor: Color = MediInputBackgroundUnfocusedLight,
    focusedTextColor: Color = MediInputTextLight,
    unfocusedTextColor: Color = MediInputTextLight,
    disabledTextColor: Color = MediTextDisabled,
    errorTextColor: Color = MediInputTextLight,
    focusedBorderColor: Color = MediInputBorderFocusedLight,
    unfocusedBorderColor: Color = MediInputBorderLight,
    disabledBorderColor: Color = Color(0xFFE2E8F0),
    errorBorderColor: Color = MediError,
    cursorColor: Color = MediInputCursorLight,
    errorCursorColor: Color = MediError,
    focusedPlaceholderColor: Color = MediInputPlaceholderLight,
    unfocusedPlaceholderColor: Color = MediInputPlaceholderLight,
    disabledPlaceholderColor: Color = MediTextDisabled,
    errorPlaceholderColor: Color = MediInputPlaceholderLight,
    focusedLeadingIconColor: Color = MediTeal,
    unfocusedLeadingIconColor: Color = MediInputPlaceholderLight,
    focusedTrailingIconColor: Color = MediTeal,
    unfocusedTrailingIconColor: Color = MediInputPlaceholderLight,
    focusedLabelColor: Color = MediTeal,
    unfocusedLabelColor: Color = MediTextSecondary,
    errorLabelColor: Color = MediError
): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = focusedTextColor,
    unfocusedTextColor = unfocusedTextColor,
    disabledTextColor = disabledTextColor,
    errorTextColor = errorTextColor,
    focusedContainerColor = containerColor,
    unfocusedContainerColor = unfocusedContainerColor,
    disabledContainerColor = Color(0xFFF1F5F9),
    errorContainerColor = Color(0xFFFEF2F2),
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = unfocusedBorderColor,
    disabledBorderColor = disabledBorderColor,
    errorBorderColor = errorBorderColor,
    cursorColor = cursorColor,
    errorCursorColor = errorCursorColor,
    selectionColors = androidx.compose.foundation.text.selection.TextSelectionColors(
        handleColor = MediTeal,
        backgroundColor = MediTeal.copy(alpha = 0.35f)
    ),
    focusedPlaceholderColor = focusedPlaceholderColor,
    unfocusedPlaceholderColor = unfocusedPlaceholderColor,
    disabledPlaceholderColor = disabledPlaceholderColor,
    errorPlaceholderColor = errorPlaceholderColor,
    focusedLeadingIconColor = focusedLeadingIconColor,
    unfocusedLeadingIconColor = unfocusedLeadingIconColor,
    focusedTrailingIconColor = focusedTrailingIconColor,
    unfocusedTrailingIconColor = unfocusedTrailingIconColor,
    focusedLabelColor = focusedLabelColor,
    unfocusedLabelColor = unfocusedLabelColor,
    errorLabelColor = errorLabelColor
)

/**
 * Standardized MediBridge+ Outlined Text Input Field
 */
@Composable
fun MediOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(MediCornerRadius.md),
    colors: TextFieldColors = mediTextFieldColors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        shape = shape,
        colors = colors
    )
}

