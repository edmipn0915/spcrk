package com.spcrk.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// 共用視覺常量
// ---------------------------------------------------------------------------

/** 圓角：12dp（cornerMedium） */
private val ButtonCorner = RoundedCornerShape(CornerRadiusTokens.s)

/** 圓角：20dp（cornerLarge） */
private val CardCorner = RoundedCornerShape(CornerRadiusTokens.m)

/** 按鈕垂直內距 */
private val ButtonPaddingVertical = 12.dp

/** 按鈕水平內距 */
private val ButtonPaddingHorizontal = 24.dp

/** 輸入框垂直內距 */
private val FieldPaddingVertical = 14.dp

/** 輸入框水平內距 */
private val FieldPaddingHorizontal = 16.dp

/** 亮色模式卡片低陰影：0 1px 4px rgba(0,0,0,0.06) */
private val CardLightShadowColor = Color(0x0F000000)
private const val CardLightShadowElevation = 4

// ---------------------------------------------------------------------------
// 1. TechPrimaryButton
// ---------------------------------------------------------------------------

/**
 * 主要操作按鈕。
 *
 * - 完整 [MaterialTheme.colorScheme.primary] 背景、白色文字
 * - 12dp 圓角、48dp 高、水平 24dp / 垂直 12dp 內距
 * - 按下時縮放至 0.94（[techPressScale]，150ms）並綻放青藍色[techRipple]
 * - 禁用時背景與文字 alpha 降為 0.38f，且封鎖點擊
 *
 * @param text 按鈕文字
 * @param onClick 點擊回呼
 * @param modifier 額外 [Modifier]
 * @param enabled 是否可互動
 */
@Composable
fun TechPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colorScheme = MaterialTheme.colorScheme

    val container = if (enabled) colorScheme.primary else colorScheme.primary.copy(alpha = 0.38f)
    val content = if (enabled) Color.White else Color.White.copy(alpha = 0.38f)

    Box(
        modifier = modifier
            .techPressScale(interactionSource, enabled)
            .defaultMinSize(minHeight = 48.dp)
            .clip(ButtonCorner)
            .background(container, ButtonCorner)
            .techRipple(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource
            )
            .padding(
                horizontal = ButtonPaddingHorizontal,
                vertical = ButtonPaddingVertical
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = content
        )
    }
}

// ---------------------------------------------------------------------------
// 2. TechSecondaryButton
// ---------------------------------------------------------------------------

/**
 * 次要操作按鈕。
 *
 * - 透明背景、1dp [MaterialTheme.colorScheme.primary] 實線邊框、primary 文字
 * - 其餘尺寸、字體、按下縮放、漣漪、禁用行為與 [TechPrimaryButton] 完全相同
 *
 * @param text 按鈕文字
 * @param onClick 點擊回呼
 * @param modifier 額外 [Modifier]
 * @param enabled 是否可互動
 */
@Composable
fun TechSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colorScheme = MaterialTheme.colorScheme

    val borderColor = if (enabled) colorScheme.primary else colorScheme.primary.copy(alpha = 0.38f)
    val content = if (enabled) colorScheme.primary else colorScheme.primary.copy(alpha = 0.38f)

    Box(
        modifier = modifier
            .techPressScale(interactionSource, enabled)
            .defaultMinSize(minHeight = 48.dp)
            .clip(ButtonCorner)
            .background(Color.Transparent, ButtonCorner)
            .border(width = 1.dp, color = borderColor, shape = ButtonCorner)
            .techRipple(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource
            )
            .padding(
                horizontal = ButtonPaddingHorizontal,
                vertical = ButtonPaddingVertical
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = content
        )
    }
}

// ---------------------------------------------------------------------------
// 3. TechTextField
// ---------------------------------------------------------------------------

/**
 * 科技感輸入框。
 *
 * - [MaterialTheme.colorScheme.surface] 背景、12dp 圓角
 * - 水平 16dp / 垂直 14dp 內距、1.5dp 邊框
 * - 失焦邊框為 [MaterialTheme.colorScheme.border]；聚焦時變為 primary 並疊加微弱發光陰影
 * - 使用 [BasicTextField] 重寫，不採用 Material 3 預設 OutlinedTextField 樣式
 *
 * @param value 目前文字
 * @param onValueChange 文字變更回呼
 * @param modifier 額外 [Modifier]
 * @param placeholder 空白時的提示文字
 * @param enabled 是否可互動
 * @param singleLine 是否單行（false 時可依內容增長，最小高度 48dp）
 */
@Composable
fun TechTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isDark = isSystemInDarkTheme()
    val glow = if (isDark) DarkShadowElevation.glow else LightShadowElevation.glow

    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.border.copy(alpha = 0.4f)
            isFocused -> colorScheme.primary
            else -> colorScheme.border
        },
        animationSpec = tween(durationMillis = AnimationDurations.SWITCH_TOGGLE),
        label = "fieldBorder"
    )

    val fieldShape = RoundedCornerShape(CornerRadiusTokens.s)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = if (enabled) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        cursorBrush = SolidColor(colorScheme.primary),
        modifier = modifier
            .heightIn(min = 48.dp)
            .then(
                if (isFocused && enabled) {
                    Modifier.shadow(
                        elevation = glow.blur,
                        shape = fieldShape,
                        ambientColor = glow.color,
                        spotColor = glow.color
                    )
                } else {
                    Modifier
                }
            )
            .clip(fieldShape)
            .background(colorScheme.surface, fieldShape)
            .border(width = 1.5.dp, color = borderColor, shape = fieldShape)
            .padding(horizontal = FieldPaddingHorizontal, vertical = FieldPaddingVertical),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.TopStart) {
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                innerTextField()
            }
        }
    )
}

// ---------------------------------------------------------------------------
// 4. TechSwitch
// ---------------------------------------------------------------------------

/** 關閉態軌道色（暗 / 亮） */
private val SwitchTrackOffDark = Color(0xFF3A4460)
private val SwitchTrackOffLight = Color(0xFFC0C8D8)

/**
 * 科技感開關。
 *
 * - 軌道 36dp × 18dp，圓角 999dp
 * - 滑塊 16dp 純白、帶微陰影
 * - 關閉態：暗色 #3A4460 / 亮色 #C0C8D8；開啟態：[MaterialTheme.colorScheme.primary]
 * - 切換動畫 200ms，使用 [EaseInOutQuad]
 *
 * @param checked 是否開啟
 * @param onCheckedChange 切換回呼
 * @param modifier 額外 [Modifier]
 * @param enabled 是否可互動
 */
@Composable
fun TechSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val trackOff = if (isDark) SwitchTrackOffDark else SwitchTrackOffLight

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 18.dp else 0.dp,
        animationSpec = tween(
            durationMillis = AnimationDurations.SWITCH_TOGGLE,
            easing = EaseInOutQuad
        ),
        label = "switchThumb"
    )
    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> (if (checked) colorScheme.primary else trackOff).copy(alpha = 0.4f)
            checked -> colorScheme.primary
            else -> trackOff
        },
        animationSpec = tween(
            durationMillis = AnimationDurations.SWITCH_TOGGLE,
            easing = EaseInOutQuad
        ),
        label = "switchTrack"
    )

    val toggle: () -> Unit = { if (enabled) onCheckedChange(!checked) }

    Box(
        modifier = modifier
            .size(width = 36.dp, height = 18.dp)
            .clip(CircleShape)
            .background(trackColor, CircleShape)
            .techRipple(onClick = toggle, enabled = enabled),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = 1.dp + thumbOffset)
                .size(16.dp)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .background(Color.White, CircleShape)
        )
    }
}

// ---------------------------------------------------------------------------
// 5. TechCard
// ---------------------------------------------------------------------------

/**
 * 通用卡片容器。
 *
 * - [MaterialTheme.colorScheme.surface] 背景、20dp 圓角、16dp 內距
 * - 暗色：1dp [MaterialTheme.colorScheme.border] 邊框
 * - 亮色：無邊框，改加低陰影（0 1px 4px rgba(0,0,0,0.06)）
 *
 * @param modifier 額外 [Modifier]
 * @param content 卡片內容
 */
@Composable
fun TechCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    val cardModifier = if (isDark) {
        modifier
            .clip(CardCorner)
            .background(colorScheme.surface, CardCorner)
            .border(width = 1.dp, color = colorScheme.border, shape = CardCorner)
    } else {
        modifier
            .shadow(
                elevation = CardLightShadowElevation.dp,
                shape = CardCorner,
                ambientColor = CardLightShadowColor,
                spotColor = CardLightShadowColor
            )
            .clip(CardCorner)
            .background(colorScheme.surface, CardCorner)
    }

    Box(
        modifier = cardModifier.padding(SpacingTokens.screenEdge) // 16dp
    ) {
        content()
    }
}
