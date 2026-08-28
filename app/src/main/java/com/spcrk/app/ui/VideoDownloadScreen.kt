package com.spcrk.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spcrk.app.model.DownloadState
import com.spcrk.app.ui.theme.LocalSuccessColor
import com.spcrk.app.ui.theme.TechCard
import com.spcrk.app.ui.theme.TechPrimaryButton
import com.spcrk.app.ui.theme.TechSecondaryButton
import com.spcrk.app.ui.theme.TechTextField
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun VideoDownloadScreen(
    onBackClick: () -> Unit,
    viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "视频下载",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "粘贴视频链接即可下载",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TechTextField(
                    value = uiState.url,
                    onValueChange = viewModel::updateUrl,
                    modifier = Modifier.weight(1f),
                    placeholder = "粘贴视频链接...",
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        clipboardManager.getText()?.text?.let { pastedText ->
                            if (pastedText.isNotEmpty()) {
                                viewModel.updateUrl(pastedText)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "粘贴",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TechPrimaryButton(
                text = if (uiState.isDownloading) "取消下载" else "⬇ 下载视频",
                onClick = {
                    if (uiState.isDownloading) {
                        viewModel.cancelDownload()
                    } else {
                        viewModel.startDownload()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.url.isNotEmpty() || uiState.isDownloading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Show parsing status
            if (uiState.downloadState is DownloadState.Parsing) {
                TechCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "正在解析视频...",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.videoInfo != null,
                enter = fadeIn(tween(220)) + slideInVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 6 }
                ),
                exit = fadeOut(tween(160))
            ) {
                uiState.videoInfo?.let { info ->
                    TechCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = info.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "来源: ${info.platform}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val animatedProgress by animateFloatAsState(
                                targetValue = uiState.downloadProgress.coerceIn(0f, 1f),
                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                label = "downloadProgress"
                            )

                            if (uiState.downloadProgress > 0f) {
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = animatedProgress,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${(animatedProgress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (uiState.downloadSpeed.isNotEmpty()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "⚡ ",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = uiState.downloadSpeed,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                if (uiState.totalSize.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = uiState.downloadedSize,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = uiState.totalSize,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // 下载完成后显示分享/打开按钮
                            if (uiState.completedFilePath != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = LocalSuccessColor.current,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "下载完成",
                                        color = LocalSuccessColor.current,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TechSecondaryButton(
                                        text = "📂 打开",
                                        onClick = {
                                            val path = uiState.completedFilePath ?: return@TechSecondaryButton
                                            openVideoFile(context, path)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    TechPrimaryButton(
                                        text = "📤 分享",
                                        onClick = {
                                            val path = uiState.completedFilePath ?: return@TechPrimaryButton
                                            shareVideoFile(context, path, info.title)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn(tween(220)) + slideInVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 6 }
                ),
                exit = fadeOut(tween(160))
            ) {
                uiState.errorMessage?.let { error ->
                    TechCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "⚠ $error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            TechCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "支持平台",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• B站 (bilibili.com)\n• YouTube\n• 抖音\n• 快手\n• 优酷\n• 更多平台持续添加...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

private fun openVideoFile(context: android.content.Context, filePath: String) {
    try {
        // content:// URI（Android 10+ MediaStore）不需要 FileProvider，也不能用 File.exists 檢查
        val uri = if (com.spcrk.app.downloader.PlaybackUri.isContentUri(filePath)) {
            Uri.parse(filePath)
        } else {
            val file = File(filePath)
            if (!file.exists()) {
                android.widget.Toast.makeText(context, "文件不存在", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            val authority = context.packageName + ".provider"
            FileProvider.getUriForFile(context, authority, file)
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "未找到可用的视频播放器", android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun shareVideoFile(context: android.content.Context, filePath: String, title: String) {
    try {
        // content:// URI 可直接分享，不必 FileProvider 轉換
        val uri = if (com.spcrk.app.downloader.PlaybackUri.isContentUri(filePath)) {
            Uri.parse(filePath)
        } else {
            val file = File(filePath)
            if (!file.exists()) {
                android.widget.Toast.makeText(context, "文件不存在", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            val authority = context.packageName + ".provider"
            FileProvider.getUriForFile(context, authority, file)
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享视频").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "分享失败: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}
