package com.spcrk.app.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------
// 1. 缓动函数（Easing）常量
//    全部为物理直觉曲线，严禁 Linear（唯一例外：按钮点击 150ms 缩放）
// ---------------------------------------------------------------------------

/** 入场弹性，过冲约 15% */
val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

/** 出场收缩，加速离场 */
val EaseInCubic = CubicBezierEasing(0.55f, 0.055f, 0.675f, 0.19f)

/** 列表逐项漂入，快速起速长尾减速 */
val EaseOutQuart = CubicBezierEasing(0.165f, 0.84f, 0.44f, 1f)

/** 开关类对称缓动 */
val EaseInOutQuad = CubicBezierEasing(0.455f, 0.03f, 0.515f, 0.955f)

/** 通用出场/淡入辅助曲线（导航 Tab 用） */
val EaseOutCubic = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)

/** 自定义 Spring：阻尼比 0.85（轻微回弹），刚度 400（中低速率） */
val SparckSpringSpec: SpringSpec<Float> = spring(
    dampingRatio = 0.85f,
    stiffness = 400f
)

// ---------------------------------------------------------------------------
// 动效时长（Int 常量，统一调整入口）
// ---------------------------------------------------------------------------

object AnimationDurations {
    /** 裂变入（页面入场） */
    const val FISSION_ENTER = 450
    /** 聚变出（页面出场） */
    const val FUSION_EXIT = 300
    /** 列表项进场 */
    const val LIST_ENTER = 300
    /** 列表逐项延迟步长 */
    const val LIST_STAGGER_STEP = 20
    /** 列表逐项延迟封顶 */
    const val LIST_STAGGER_CAP = 400
    /** 按钮点击缩放（唯一允许 Linear 语境，实际用 keyframes 弹性） */
    const val BUTTON_CLICK = 150
    /** 开关切换 */
    const val SWITCH_TOGGLE = 200
    /** 呼吸光晕周期 */
    const val BREATHING_PERIOD = 2000
    /** 星芒旋转一圈 */
    const val STAR_ROTATION = 1200
}

/** 动效位移/尺寸常量 */
object AnimationDistances {
    val FISSION_SLIDE = 24.dp
    val FUSION_SLIDE = 12.dp
    val LIST_SLIDE = 16.dp
    /** 涟漪半径超出组件边缘的延伸量 */
    val RIPPLE_OVERSHOOT = 8.dp
    /** 呼吸光晕最大扩散半径 */
    val GLOW_MAX_RADIUS = 20.dp
}

// ---------------------------------------------------------------------------
// 2. 页面转场动画（供 NavHost 使用）
// ---------------------------------------------------------------------------

/**
 * 裂变入：页面如同从点击原点炸开。
 *
 * 组合 fadeIn + slideIn（初始偏移 24dp，方向由点击原点决定）+ scaleIn（初始 0.85），
 * 450ms / EaseOutBack。
 *
 * 注意：此函数**不是** @Composable（供 NavHost 的非 @Composable 转场 lambda 调用），
 * 因此依赖 [LocalDensity]/[LocalConfiguration] 的像素值需由调用方在 @Composable 作用域
 * 预先换算后传入。
 *
 * @param initialOffset 点击原点在屏幕上的像素坐标；null 时默认从下方漂入。
 * @param slidePx 裂变滑动距离（像素）。
 * @param screenWidthPx / screenHeightPx 屏幕尺寸（像素），用于计算炸开方向。
 */
fun fissionEnter(
    initialOffset: Offset? = null,
    slidePx: Float,
    screenWidthPx: Float,
    screenHeightPx: Float
): EnterTransition {
    // 位移方向：屏幕中心指向点击原点，内容从原点一侧向外扩张至中心
    val (dx, dy) = if (initialOffset != null) {
        val screenCenter = Offset(screenWidthPx / 2f, screenHeightPx / 2f)
        val delta = initialOffset - screenCenter
        val length = delta.getDistance()
        if (length > 1f) {
            val dir = delta / length
            (dir.x * slidePx).roundToInt() to (dir.y * slidePx).roundToInt()
        } else {
            0 to slidePx.roundToInt()
        }
    } else {
        0 to slidePx.roundToInt()
    }

    return fadeIn(tween(AnimationDurations.FISSION_ENTER, easing = EaseOutBack)) +
        slideIn(
            animationSpec = tween(AnimationDurations.FISSION_ENTER, easing = EaseOutBack),
            initialOffset = { IntOffset(dx, dy) }
        ) +
        scaleIn(
            initialScale = 0.85f,
            animationSpec = tween(AnimationDurations.FISSION_ENTER, easing = EaseOutBack)
        )
}

/**
 * 聚变出：页面收缩、下沉、聚拢消失。
 *
 * 组合 fadeOut + scaleOut（目标 0.8）+ slideOut（目标偏移 -12dp），
 * 300ms / EaseInCubic。
 *
 * 非 @Composable，像素值由调用方换算后传入。
 *
 * @param exitPx 聚变滑动距离（像素）。
 */
fun fusionExit(exitPx: Float): ExitTransition {
    return fadeOut(tween(AnimationDurations.FUSION_EXIT, easing = EaseInCubic)) +
        scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(AnimationDurations.FUSION_EXIT, easing = EaseInCubic)
        ) +
        slideOut(
            animationSpec = tween(AnimationDurations.FUSION_EXIT, easing = EaseInCubic)
        ) { IntOffset(0, -exitPx.roundToInt()) }
}

// ---------------------------------------------------------------------------
// 3. 列表进场动画（供 LazyColumn items 使用）
// ---------------------------------------------------------------------------

/**
 * 列表项逐层漂入：fadeIn + slideInVertically（初始 16dp）+ scaleIn（初始 0.9），
 * 300ms / EaseOutQuart；延迟 = index * 20ms，封顶 400ms。
 *
 * 典型用法（配合 [StaggeredItem]）：
 * ```
 * itemsIndexed(list) { index, item ->
 *     StaggeredItem(index = index) { ItemRow(item) }
 * }
 * ```
 */
@Composable
fun staggeredItemEnter(index: Int): EnterTransition {
    val delayMillis = min(
        index * AnimationDurations.LIST_STAGGER_STEP,
        AnimationDurations.LIST_STAGGER_CAP
    )
    val slidePx = with(LocalDensity.current) {
        AnimationDistances.LIST_SLIDE.toPx()
    }.roundToInt()
    val floatSpec = tween<Float>(
        durationMillis = AnimationDurations.LIST_ENTER,
        delayMillis = delayMillis,
        easing = EaseOutQuart
    )

    return fadeIn(floatSpec) +
        slideInVertically(
            animationSpec = tween(
                durationMillis = AnimationDurations.LIST_ENTER,
                delayMillis = delayMillis,
                easing = EaseOutQuart
            ),
            initialOffsetY = { slidePx }
        ) +
        scaleIn(initialScale = 0.9f, animationSpec = floatSpec)
}

/**
 * 列表逐项进场容器：首次组合即触发 [staggeredItemEnter]。
 */
@Composable
fun StaggeredItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = staggeredItemEnter(index),
        modifier = modifier
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// 4. 点击涟漪视觉强化
// ---------------------------------------------------------------------------

private val TechRippleDark = Color(0x2600D4FF)   // rgba(0, 212, 255, 0.15)
private val TechRippleLight = Color(0x260088CC)  // rgba(0, 136, 204, 0.15)

/**
 * 科技涟漪：BoundedRipple，涟漪半径按“组件半宽 + 8dp”计算，
 * 视觉上延伸至组件边缘外 8dp。
 *
 * 暗色 #00D4FF@15%，亮色 #0088CC@15%，自动随主题切换。
 *
 * @param interactionSource 可选共享交互源；传入后可与 [techPressScale] 共享同一按压状态。
 */
fun Modifier.techRipple(
    onClick: () -> Unit,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val isDark = isSystemInDarkTheme()
    val rippleColor = if (isDark) TechRippleDark else TechRippleLight
    val source = interactionSource ?: remember { MutableInteractionSource() }

    var measuredSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    // 半径 = max(宽, 高) / 2 + 8dp，未测得尺寸前交给系统默认
    val radius = if (measuredSize == IntSize.Zero) {
        Dp.Unspecified
    } else {
        with(density) {
            max(measuredSize.width, measuredSize.height).toDp() / 2 +
                AnimationDistances.RIPPLE_OVERSHOOT
        }
    }

    this
        .onSizeChanged { measuredSize = it }
        .clickable(
            interactionSource = source,
            indication = rememberRipple(
                bounded = true,
                radius = radius,
                color = rippleColor
            ),
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * 点击缩放反馈：按下时缩放到 [pressedScale]（默认 0.94），松开回弹至 1.0，
 * 时长 150ms（[AnimationDurations.BUTTON_CLICK]）。
 *
 * 与 [techRipple] 共享同一 [interactionSource] 时，缩放与涟漪同步触发。
 */
fun Modifier.techPressScale(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.94f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (enabled && isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = AnimationDurations.BUTTON_CLICK),
        label = "techPressScale"
    )
    this.scale(scale)
}

// ---------------------------------------------------------------------------
// 5. 呼吸光晕（首页能量核心指示器）
// ---------------------------------------------------------------------------

/**
 * 呼吸光晕：透明度在 [minAlpha, maxAlpha] 间往复，周期 2000ms；
 * 阴影半径随呼吸同步扩张（0 → 20dp），形成光晕扩散。
 *
 * 注：animateFloatAsState 本身不支持无限循环，
 * 等效采用 rememberInfiniteTransition.animateFloat（等效替代方案）。
 */
@Composable
fun BreathingGlow(
    modifier: Modifier = Modifier,
    color: Color,
    minAlpha: Float = 0.3f,
    maxAlpha: Float = 1.0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathingGlow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = AnimationDurations.BREATHING_PERIOD / 2,
                easing = EaseInOutQuad
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingAlpha"
    )

    Box(
        modifier = modifier
            .size(16.dp) // 兜底尺寸，调用方显式指定 size 时自动让位
            .shadow(
                elevation = AnimationDistances.GLOW_MAX_RADIUS * alpha,
                shape = CircleShape,
                ambientColor = color,
                spotColor = color
            )
            .background(color.copy(alpha = alpha * 0.35f), CircleShape)
    )
}

// ---------------------------------------------------------------------------
// 6. 加载星芒（通用 Loading 指示器）
// ---------------------------------------------------------------------------

/**
 * 四芒星加载指示器：围绕 Y 轴 3D 旋转（graphicsLayer rotationY），
 * 1200ms/圈；同时脉冲缩放 0.8 ↔ 1.2。
 *
 * 旋转采用 keyframes 分段 EaseInOutQuad 前进，避免 Linear 的机械匀速感。
 */
@Composable
fun StarLoadingIndicator(
    modifier: Modifier = Modifier,
    starSize: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "starLoading")

    val starRotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = AnimationDurations.STAR_ROTATION,
                easing = EaseInOutQuad
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "starRotationY"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = AnimationDurations.STAR_ROTATION / 2,
                easing = EaseInOutQuad
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starPulse"
    )

    Box(
        modifier = modifier.size(starSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = starRotationY
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            drawPath(fourPointStarPath(size), color = color)
        }
    }
}

/** 四芒星路径：外顶点 N/E/S/W，内凹控制点构成星芒 */
private fun DrawScope.fourPointStarPath(size: Size): Path {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outer = min(cx, cy)
    val inner = outer / 4f
    return Path().apply {
        moveTo(cx, cy - outer)
        quadraticBezierTo(cx + inner, cy - inner, cx + outer, cy)
        quadraticBezierTo(cx + inner, cy + inner, cx, cy + outer)
        quadraticBezierTo(cx - inner, cy + inner, cx - outer, cy)
        quadraticBezierTo(cx - inner, cy - inner, cx, cy - outer)
        close()
    }
}

// ---------------------------------------------------------------------------
// 导航 Tab/详情页转场（AppNavigation 使用）
// ---------------------------------------------------------------------------

object NavigationTransitions {
    const val TAB_FADE_IN_DURATION = 220
    const val TAB_FADE_OUT_DURATION = 180
    const val DETAIL_SLIDE_DURATION = 280
    const val DETAIL_POP_SLIDE_DURATION = 260

    val tabFadeIn = tween<Float>(TAB_FADE_IN_DURATION, easing = EaseOutCubic)

    val tabFadeOut = tween<Float>(TAB_FADE_OUT_DURATION, easing = EaseInCubic)

    val detailSlideIn = tween<IntOffset>(DETAIL_SLIDE_DURATION, easing = EaseInOutQuad)

    val detailFadeIn = tween<Float>(TAB_FADE_IN_DURATION, easing = EaseOutCubic)

    val detailSlideOut = tween<IntOffset>(DETAIL_POP_SLIDE_DURATION, easing = EaseInOutQuad)

    val detailFadeOut = tween<Float>(TAB_FADE_OUT_DURATION, easing = EaseInCubic)
}
