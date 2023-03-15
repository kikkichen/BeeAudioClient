package com.chen.beeaudio.screen.widget

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.ImageLoader
import coil.compose.AsyncImage
import com.chen.beeaudio.R
import com.chen.beeaudio.ui.theme.BlueGrey400
import com.chen.beeaudio.ui.theme.Red300
import com.chen.beeaudio.viewmodel.MainViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/** 音乐媒体播放控制底栏
 *  @param  mainViewModel   MainViewModel 视图模型
 *  @param  onOpenPlayPage  打开播放页面的点击事件
 */
@Composable
fun PlayerBarWidget(
    mainViewModel: MainViewModel,
    onOpenPlayPage: () -> Unit,
) {
    /* 当前播放曲目 */
    val playingMusicItem = mainViewModel.currentPlayingMusicItem.collectAsState()
    /* 播放进度条 */
    val playingProgress = mainViewModel.currentPlayingProgress.collectAsState()
    /* 当期曲目持续播放时长 */
    val playingDuration = mainViewModel.currentPlayingDuration.collectAsState()

    /* 当前播放器播放状态 */
    val isPlaying = mainViewModel.isPlayingState.collectAsState()

    /* 当前曲目的收藏状态 */
    val currentLikeState = mainViewModel.isMyFavoriteTrack.collectAsState()

    /* 上下文 */
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color = BlueGrey400),
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onOpenPlayPage() }
        ) {
            val (coverImage, title, likeIcon, playIcon, progressBar) = createRefs()
            AsyncImage(
                model = playingMusicItem.value?.iconUri,
                contentDescription = null,
                imageLoader = ImageLoader.Builder(LocalContext.current).crossfade(durationMillis = 500).build(),
                placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder ),
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .constrainAs(coverImage) {
                        start.linkTo(parent.start, margin = 8.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )
            Column(
                modifier = Modifier
                    .constrainAs(title) {
                        start.linkTo(coverImage.end, margin = 12.dp)
                        top.linkTo(parent.top, margin = 4.dp)
                        bottom.linkTo(parent.bottom, margin = 4.dp)
                        end.linkTo(playIcon.start)
                        width = Dimension.fillToConstraints
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onOpenPlayPage() }
                        )
                    }
                    .swipeToController(
                        onPrevious = { mainViewModel.playPreviousMusic() },
                        onNextEvent = { mainViewModel.playNextMusic() }
                    )
            ) {
                Text(
                    text = playingMusicItem.value?.title ?: "闲来无事，来点音乐",
                    style = MaterialTheme.typography.subtitle2.copy(
                        color = Color.White
                    ),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                /* 创作艺人信息 */
                val artists : String = playingMusicItem.value?.artist ?: "佚名"
                Text(
                    text = artists,
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            /* 喜欢按钮 */
            IconButton(
                onClick = { mainViewModel.dealWithTrackCollect(context) },
                modifier = Modifier.constrainAs(likeIcon) {
                    top.linkTo(parent.top)
                    end.linkTo(playIcon.start, margin = (-4).dp)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                Icon(
                    painter = painterResource(id = if (currentLikeState.value) R.drawable.ic_audio_like_confirm else R.drawable.ic_audio_like_unconfirm),
                    contentDescription = "Like",
                    modifier = Modifier.size(26.dp),
                    tint = if (currentLikeState.value) Red300 else Color.White
                )
            }
            /* 播放按钮 */
            IconButton(
                onClick = { mainViewModel.playOrPauseAction() },
                modifier = Modifier.constrainAs(playIcon) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end, margin = 8.dp)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                Icon(
                    painter = painterResource(id = if (isPlaying.value) R.drawable.ic_audio_pause else R.drawable.ic_audio_play),
                    contentDescription = "More",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
            AnimatedVisibility(
                visible = playingMusicItem.value != null,
                modifier = Modifier.constrainAs(progressBar) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                }
            ) {
                LinearProgressIndicator(
                    progress = if (playingDuration.value != 0 || playingProgress.value != 0){
                        playingProgress.value.toFloat() / playingDuration.value.toFloat()
                    }
                    else
                        Float.MIN_VALUE,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colors.primary.copy(alpha = .5f),
                    backgroundColor = Color.Transparent
                )
            }
        }
    }
}

/**
 *  修饰符元素扩展函数 为该元素添加水平滑动行为
 *  @param  onNextEvent 播放下一曲目事件
 *  @param  onPrevious  播放上一曲目事件
 */
private fun Modifier.swipeToController(
    onNextEvent: () -> Unit,
    onPrevious: () -> Unit,
): Modifier = composed{
    /* 该元素水平偏移量的 动画 数据 */
    val offsetX = remember { Animatable(0f) }
    pointerInput(Unit) {
        /* 通过计算该触发动画的初始位置获得衰减参数 */
        val decay = splineBasedDecay<Float>(this)
        /* 启动协程来使用花旗函数处理触发事件与东湖 */
        coroutineScope {
            while (true) {
                /* 等待一个触发事件 */
                val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                /* 打断将要进行的动画 */
                offsetX.stop()
                /* 为拖动事件记录触发的速度 */
                val velocityTracker = VelocityTracker()
                /* 等待拖动事件 */
                awaitPointerEventScope {
                    horizontalDrag(pointerId) { change ->
                        /* 记录偏移后位置 */
                        val horizontalDragOffset = offsetX.value + change.positionChange().x
                        launch {
                            /* 当元素正在被拖动时 应用其偏移量，更新它的实时位置 */
                            offsetX.snapTo(horizontalDragOffset)
                        }
                        /* 记录拖动的速度 */
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        /* 消费手势事件， 不需要处理额外的事件 */
                        if (change.positionChange() != Offset.Zero) change.consume()
                    }
                }
                /* 拖动手势结束时， 计算惯性的速度 */
                val velocity = velocityTracker.calculateVelocity().x
                /* 计算元素由于拖动后惯性影响造成的偏移量，得到目标偏移位置 */
                val targetOffset = decay.calculateTargetValue(offsetX.value, velocity)
                /* 为该元素设置拖动触发时的边界值 */
                offsetX.updateBounds(
                    lowerBound = (-size.width) / 2.toFloat(),
                    upperBound = (size.width) / 2.toFloat()
                )
                launch {
                    if (targetOffset.absoluteValue <= size.width / 2) {
                        /* 初速度未达到其边界值， 该元素滑回原处 */
                        offsetX.animateTo(targetValue = 0f, initialVelocity = velocity)
                    } else {
                        /* 达到初速度，动画启动，惯性参数应用其衰减 */
                        offsetX.snapTo(targetValue = 0f)
                        /* 响应滑动触发的事件 */
                        if (targetOffset > 0) {
                            onPrevious()
                        } else {
                            onNextEvent()
                        }
                    }
                }
            }
        }
    }
        /* 为该元素应用水平偏移量 */
        .offset {
            IntOffset(offsetX.value.roundToInt(), 0)
        }
        .alpha((offsetX.value.absoluteValue) * (-1) / (223) + 1f)
}