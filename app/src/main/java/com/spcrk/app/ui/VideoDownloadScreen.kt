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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spcrk.app.model.DownloadState
import com.spcrk.app.ui.l10n.appStrings
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
    val s = appStrings()
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
                        text = s.videoDownloadTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = s.back,
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
                text = s.pasteVideoLinkHint,
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
                    placeholder = s.urlPlaceholder,
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
                        contentDescription = s.paste,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TechPrimaryButton(
                text = when {
                    uiState.isDownloading -> s.cancelDownload
                    uiState.videoInfo != null -> {
                        val q = uiState.selectedQuality
                        s.downloadAction + if (q != null) " (${q.resolution ?: q.label})" else ""
                    }
                    else -> s.parseVideo
                },
                onClick = {
                    when {
                        uiState.isDownloading -> viewModel.cancelDownload()
                        uiState.videoInfo != null -> viewModel.downloadVideo()
                        else -> viewModel.parseVideo()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = when {
                    uiState.isDownloading -> true
                    uiState.downloadState == DownloadState.Parsing -> false
                    else -> uiState.url.isNotEmpty()
                }
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
                            text = s.parsingVideo,
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
                                text = String.format(s.sourceFormat, info.platform),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // 畫質選擇（多於一檔才顯示）
                            if (info.qualities.size > 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = s.videoQuality,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    info.qualities.forEach { quality ->
                                        val isSelected = uiState.selectedQuality?.url == quality.url
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.selectQuality(quality) },
                                            enabled = !uiState.isDownloading,
                                            label = {
                                                Text(
                                                    text = quality.resolution ?: quality.label,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        )
                                    }
                                }
                            }

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
                                        text = s.downloadComplete,
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
                                        text = "📂 " + s.openFile,
                                        onClick = {
                                            val path = uiState.completedFilePath ?: return@TechSecondaryButton
                                            openVideoFile(context, path, s.fileNotFound, s.noVideoPlayer)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    TechPrimaryButton(
                                        text = "📤 " + s.share,
                                        onClick = {
                                            val path = uiState.completedFilePath ?: return@TechPrimaryButton
                                            shareVideoFile(context, path, info.title, s.fileNotFound, s.shareVideo, s.shareFailedFormat)
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
                        text = s.supportedPlatforms,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = s.supportedPlatformsList,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

private fun openVideoFile(context: android.content.Context, filePath: String, fileNotFoundText: String, noPlayerText: String) {
    try {
        // content:// URI（Android 10+ MediaStore）不需要 FileProvider，也不能用 File.exists 檢查
        val uri = if (com.spcrk.app.downloader.PlaybackUri.isContentUri(filePath)) {
            Uri.parse(filePath)
        } else {
            val file = File(filePath)
            if (!file.exists()) {
                android.widget.Toast.makeText(context, fileNotFoundText, android.widget.Toast.LENGTH_SHORT).show()
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
        android.widget.Toast.makeText(context, noPlayerText, android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun shareVideoFile(
    context: android.content.Context,
    filePath: String,
    title: String,
    fileNotFoundText: String,
    shareVideoText: String,
    shareFailedFormat: String
) {
    try {
        // content:// URI 可直接分享，不必 FileProvider 轉換
        val uri = if (com.spcrk.app.downloader.PlaybackUri.isContentUri(filePath)) {
            Uri.parse(filePath)
        } else {
            val file = File(filePath)
            if (!file.exists()) {
                android.widget.Toast.makeText(context, fileNotFoundText, android.widget.Toast.LENGTH_SHORT).show()
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
        context.startActivity(Intent.createChooser(intent, shareVideoText).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, String.format(shareFailedFormat, e.message ?: ""), android.widget.Toast.LENGTH_SHORT).show()
    }
}
