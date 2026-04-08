package com.beamio.android_ntag

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Standard top bar height and left padding for back button. Align with scan page. */
internal val BACK_BUTTON_TOP_BAR_HEIGHT = 72.dp
internal val BACK_BUTTON_START_PADDING = 8.dp
/** Top padding for back button (from top bar top). Aligns scan page with Charge Amount page. */
internal val BACK_BUTTON_TOP_PADDING = 12.dp
/** Top padding for scan page capsule and Charge/TopUp title. Align title with capsule. */
internal val TOP_BAR_CAPSULE_TITLE_PADDING = 56.dp

/** Nav-back arrow tint: 30% black. Use for all page `ArrowBack` controls (pill bar + text variant). */
internal val BACK_BUTTON_ICON_TINT = Color.Black.copy(alpha = 0.3f)

/** Unified back button style: grey border 0.1dp, shadow, rounded background. Used across the app. */
private val BackButtonModifier = Modifier
    .shadow(4.dp, RoundedCornerShape(24.dp))
    .background(Color(0xFFf3f4f6), RoundedCornerShape(24.dp))
    .border(0.1.dp, Color(0xFF9ca3af), RoundedCornerShape(24.dp))

@Composable
fun BackButtonIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.then(BackButtonModifier)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(24.dp),
            tint = BACK_BUTTON_ICON_TINT
        )
    }
}

/** Back button in standard top bar: independent, fixed height 72dp, 8dp from left. Use as first child. */
@Composable
fun BackButtonTopBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BACK_BUTTON_TOP_BAR_HEIGHT)
    ) {
        BackButtonIcon(
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = BACK_BUTTON_START_PADDING)
        )
    }
}

/** Back button with icon + text (for header rows like TipSelectionScreen, ChargeAmountScreen). */
@Composable
fun BackButtonWithText(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .then(BackButtonModifier)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(24.dp).padding(end = 4.dp),
            tint = BACK_BUTTON_ICON_TINT
        )
        Text("Back", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = BACK_BUTTON_ICON_TINT)
    }
}
