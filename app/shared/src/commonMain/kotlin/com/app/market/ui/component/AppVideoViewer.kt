package com.app.market.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.app.market.domain.model.market.AppVideo
import com.app.market.domain.model.market.ScreenshotOrientation
import com.app.market.resources.Res
import com.app.market.resources.video_close
import com.app.market.resources.video_load_failed
import com.app.market.resources.video_mute
import com.app.market.resources.video_pause
import com.app.market.resources.video_play
import com.app.market.resources.video_unmute
import com.app.market.ui.util.animatedRect
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val VIDEO_VIEWER_ANIM_MS = 400
private const val VIDEO_VIEWER_CORNER_DP = 16f

/** 播放进度条使用的取值区间：0..1000。 */
private const val VIDEO_PROGRESS_RANGE = 1000f

@Composable
fun AppVideoViewer(
    video: AppVideo,
    cardBounds: Rect?,
    onClosed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playerState = rememberVideoPlayerState()
    LaunchedEffect(video.url) { playerState.openUri(video.url) }

    val progress = remember { Animatable(0f) }
    var browsing by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(VIDEO_VIEWER_ANIM_MS))
        browsing = true
    }
    val animationScope = rememberCoroutineScope()
    val dismiss: () -> Unit = {
        if (!closing) {
            closing = true
            browsing = false
            playerState.pause()
            animationScope.launch {
                progress.animateTo(0f, tween(VIDEO_VIEWER_ANIM_MS))
                onClosed()
            }
        }
    }

    val backState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = !closing,
        onBackCompleted = dismiss,
    )

    var viewerOrigin by remember { mutableStateOf(Offset.Zero) }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { viewerOrigin = it.positionInRoot() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress.value }
                .background(Color.Black),
        )

        if (browsing) {
            VideoPlaybackLayer(
                playerState = playerState,
                onClose = dismiss,
            )
        } else {
            val ratio = if (video.orientation == ScreenshotOrientation.LANDSCAPE) 16f / 9f else 9f / 16f
            val containerWidth = constraints.maxWidth.toFloat()
            val containerHeight = constraints.maxHeight.toFloat()
            val fitWidth = minOf(containerWidth, containerHeight * ratio)
            val fitRect = Rect(
                Offset((containerWidth - fitWidth) / 2f, (containerHeight - fitWidth / ratio) / 2f),
                Size(fitWidth, fitWidth / ratio),
            )
            val cardRect = cardBounds?.translate(-viewerOrigin) ?: fitRect
            val cornerDp by remember(progress) {
                derivedStateOf { (VIDEO_VIEWER_CORNER_DP * (1f - progress.value)).roundToInt() }
            }
            AppAsyncImage(
                url = video.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    // animatedRect 必须在裁剪之前，否则裁剪区停在原点
                    .animatedRect { lerp(cardRect, fitRect, progress.value) }
                    .clip(RoundedCornerShape(cornerDp.dp)),
            )
        }
    }
}

@Composable
private fun VideoPlaybackLayer(
    playerState: VideoPlayerState,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(playerState) {
                detectTapGestures {
                    if (playerState.isPlaying) playerState.pause() else playerState.play()
                }
            },
    ) {
        VideoPlayerSurface(
            playerState = playerState,
            modifier = Modifier.fillMaxSize(),
        )

        if (playerState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp),
                color = Color.White,
                strokeWidth = 3.dp,
            )
        }

        if (playerState.error != null) {
            Text(
                text = stringResource(Res.string.video_load_failed),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
            )
        }

        // 关闭按钮：深色圆形底衬，保证在任意画面上都能看清
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.45f),
            contentColor = Color.White,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(Res.string.video_close),
                    tint = Color.White,
                )
            }
        }

        VideoControlBar(
            playerState = playerState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** 底部播放控制浮层：播放 / 暂停、进度、时间、静音。 */
@Composable
private fun VideoControlBar(
    playerState: VideoPlayerState,
    modifier: Modifier = Modifier,
) {
    val playing = playerState.isPlaying
    var volumeBeforeMute by remember { mutableFloatStateOf(playerState.volume) }
    val muted = playerState.volume == 0f
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.Black.copy(alpha = 0.55f),
        contentColor = Color.White,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 控制条自身吞掉点击，避免冒泡到播放 / 暂停层
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (playing) playerState.pause() else playerState.play() }) {
                Icon(
                    imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(
                        if (playing) Res.string.video_pause else Res.string.video_play
                    ),
                    tint = Color.White,
                )
            }
            Text(
                text = playerState.positionText,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
            )
            VideoProgressBar(
                progress = { playerState.sliderPos },
                onSeek = playerState::seekStart,
                onSeekFinished = playerState::seekFinished,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )
            Text(
                text = playerState.durationText,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
            )
            IconButton(
                onClick = {
                    if (muted) {
                        playerState.volume = volumeBeforeMute.takeIf { it > 0f } ?: 1f
                    } else {
                        volumeBeforeMute = playerState.volume
                        playerState.volume = 0f
                    }
                },
            ) {
                Icon(
                    imageVector = if (muted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    contentDescription = stringResource(
                        if (muted) Res.string.video_unmute else Res.string.video_mute
                    ),
                    tint = Color.White,
                )
            }
        }
    }
}

/** 进度条：M3 Slider，拖动时持续 seek，松手结束。 */
@Composable
private fun VideoProgressBar(
    progress: () -> Float,
    onSeek: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Slider(
        value = (progress() / VIDEO_PROGRESS_RANGE).coerceIn(0f, 1f),
        onValueChange = { onSeek(it * VIDEO_PROGRESS_RANGE) },
        onValueChangeFinished = onSeekFinished,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White,
            inactiveTrackColor = Color.White.copy(alpha = 0.3f),
        ),
    )
}
