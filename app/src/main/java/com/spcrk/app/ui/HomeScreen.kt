package com.spcrk.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spcrk.app.ui.theme.BreathingGlow
import com.spcrk.app.ui.theme.StaggeredItem
import com.spcrk.app.ui.theme.TechCard
import com.spcrk.app.ui.theme.glass
import com.spcrk.app.ui.theme.techRipple
import kotlin.random.Random

/**
 * 首页
 *
 * 视觉结构：
 * 1. 顶部能量核心区域（120dp，毛玻璃背景 + 居中 Sparck 图标 + 呼吸光晕 + 胶囊状态文字）
 * 2. 下方 LazyColumn 列出功能卡片，每张卡片用 [StaggeredItem] 错峰入场
 * 3. 点击卡片时上报点击坐标（相对根布局）供 AppNavigation 用于 fissionEnter
 */
@Composable
fun HomeScreen(
    onNavigateToVideoDownload: (Offset) -> Unit,
    onNavigateToHistory: (Offset) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .starryBackground() // 星穹背景繪製：纯色基底 + 极淡网格 + 随机星点
    ) {
        EnergyCoreHeader()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val features = listOf(
                FeatureItem(
                    title = "视频下载",
                    description = "下载在线视频到本地",
                    iconEmoji = "📹",
                    onClick = onNavigateToVideoDownload
                ),
                FeatureItem(
                    title = "历史记录",
                    description = "查看下载历史和浏览记录",
                    iconEmoji = "🕒",
                    onClick = onNavigateToHistory
                )
            )

            itemsIndexed(items = features, key = { _, item -> item.title }) { index, item ->
                StaggeredItem(index = index) {
                    FeatureCardItem(
                        title = item.title,
                        description = item.description,
                        iconEmoji = item.iconEmoji,
                        onClick = item.onClick
                    )
                }
            }
        }
    }
}

private data class FeatureItem(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val onClick: (Offset) -> Unit
)

/**
 * 顶部能量核心指示器：120dp 固定高度，毛玻璃背景，中央 Sparck 图标 + 呼吸光晕 + 胶囊状态文字。
 */
@Composable
private fun EnergyCoreHeader() {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            // 卡片霧光：先绘制底部垂直渐层（primary 10% → 透明），再叠毛玻璃
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Transparent,
                        0.6f to Color.Transparent,
                        1.0f to colorScheme.primary.copy(alpha = 0.10f)
                    )
                )
            )
            .glass()
    ) {
        // 中央内容：图标 + 呼吸光晕 + 胶囊文字
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                BreathingGlow(
                    modifier = Modifier.size(80.dp),
                    color = colorScheme.primary,
                    minAlpha = 0.3f,
                    maxAlpha = 1.0f
                )
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = "Sparck 灵愿",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 胶囊状状态文字
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "本地引擎 · 运行中",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 功能卡片：TechCard 容器 + 内部点击上报坐标
 *
 * 点击时以卡片中心在根布局中的坐标（含卡片自身偏移，修正了「根原点 + 局部坐标」
 * 漏加卡片偏移的问题）作为炸开原点，供 AppNavigation 的 fissionEnter 使用。
 */
@Composable
private fun FeatureCardItem(
    title: String,
    description: String,
    iconEmoji: String,
    onClick: (Offset) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    // 卡片在根布局中的位置与尺寸，用于把点击换算为根布局坐标
    var cardOrigin by remember { mutableStateOf(Offset.Zero) }
    var cardSize by remember { mutableStateOf(IntSize.Zero) }

    TechCard(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                cardOrigin = coords.positionInRoot()
                cardSize = coords.size
            }
    ) {
        Box {
            // 卡片霧光：底部垂直渐层（primary 10% → 透明），位于卡片内容底层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.55f to Color.Transparent,
                                1.0f to colorScheme.primary.copy(alpha = 0.10f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .techRipple(
                        onClick = {
                            // 以卡片中心作为裂变炸开原点
                            onClick(
                                cardOrigin + Offset(
                                    x = cardSize.width / 2f,
                                    y = cardSize.height / 2f
                                )
                            )
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconEmoji,
                    fontSize = 24.sp,
                    color = colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 星穹背景繪製：纯 Compose Canvas，无外部图片资源
// ---------------------------------------------------------------------------

/** 星点数据：位置归一化（0..1），半径 1~3dp，alpha 0.1~0.3 */
private data class StarPoint(
    val x: Float,
    val y: Float,
    val radiusDp: Float,
    val alpha: Float
)

/** 星穹网格：节点（归一化坐标）与相邻连接索引 */
private data class StarGrid(
    val nodes: List<Offset>,
    val connections: List<Pair<Int, Int>>
)

/** 用固定种子生成不规则几何网格节点（4×6，带随机抖动） */
private fun generateStarGrid(cols: Int = 4, rows: Int = 6): StarGrid {
    val random = Random(0x5EED)
    val nodes = List(cols * rows) { i ->
        val row = i / cols
        val col = i % cols
        Offset(
            x = ((col + 0.5f) / cols + (random.nextFloat() - 0.5f) * 0.12f).coerceIn(0.04f, 0.96f),
            y = ((row + 0.5f) / rows + (random.nextFloat() - 0.5f) * 0.12f).coerceIn(0.04f, 0.96f)
        )
    }
    val connections = buildList {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val idx = row * cols + col
                if (col < cols - 1) add(idx to idx + 1)      // 横向连线
                if (row < rows - 1) add(idx to idx + cols)   // 纵向连线
            }
        }
    }
    return StarGrid(nodes, connections)
}

/** 用固定种子生成 40 个星点，均匀散布于画面 */
private fun generateStarPoints(count: Int = 40): List<StarPoint> {
    val random = Random(0x57A2)
    return List(count) {
        StarPoint(
            x = random.nextFloat(),
            y = random.nextFloat(),
            radiusDp = 1.2f + random.nextFloat() * 2.3f, // 1.2..3.5dp
            alpha = 0.25f + random.nextFloat() * 0.3f    // 0.25..0.55
        )
    }
}

/** 边缘渐隐系数：距任一边缘 <15% 时线性衰减到 0 */
private fun edgeFade(nx: Float, ny: Float): Float {
    val distToEdge = minOf(nx, 1f - nx, ny, 1f - ny)
    return (distToEdge / 0.15f).coerceIn(0f, 1f)
}

/**
 * 星穹背景：纯色基底 + 极淡几何网格 + 随机星点。
 *
 * 颜色取自 [MaterialTheme.colorScheme]（暗 #0B0E14 / 亮 #F4F6FA，网格星点随主题自动
 * 切换为亮白/暗黑），点位用固定种子 remember 缓存，重组时不重新随机。
 */
@Composable
private fun Modifier.starryBackground(): Modifier = composed {
    val colorScheme = MaterialTheme.colorScheme
    val baseColor = colorScheme.background
    val starColor = colorScheme.onBackground
    // 固定种子缓存点位，重组时不再重新随机  // 星穹背景繪製
    val grid = remember { generateStarGrid() }
    val stars = remember { generateStarPoints() }

    this.drawBehind {
        // 1. 纯色基底
        drawRect(color = baseColor)

        // 2. 星穹网格：横纵相邻节点连线，边缘区域渐隐（alpha 0.10 基底，越靠边越淡）
        val lineWidthPx = 0.7.dp.toPx()
        grid.connections.forEach { (fromIdx, toIdx) ->
            val a = grid.nodes[fromIdx]
            val b = grid.nodes[toIdx]
            val fade = edgeFade((a.x + b.x) / 2f, (a.y + b.y) / 2f)
            if (fade > 0.01f) {
                drawLine(
                    color = starColor.copy(alpha = 0.10f * fade),
                    start = Offset(a.x * size.width, a.y * size.height),
                    end = Offset(b.x * size.width, b.y * size.height),
                    strokeWidth = lineWidthPx
                )
            }
        }

        // 3. 星点：半径 1~3dp，alpha 0.1~0.3
        stars.forEach { star ->
            drawCircle(
                color = starColor.copy(alpha = star.alpha),
                radius = star.radiusDp.dp.toPx(),
                center = Offset(star.x * size.width, star.y * size.height)
            )
        }
    }
}
