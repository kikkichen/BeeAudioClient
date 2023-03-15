package com.chen.beeaudio.screen.widget

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.chen.beeaudio.mock.SingleTrackMock
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.ui.theme.Cyan500
import com.chen.beeaudio.ui.theme.shimmerEffect
import java.util.concurrent.TimeUnit

/** 歌曲音频条目组件
 * @param   track   单曲信息
 * @param   currentPlayingTrackId    当前播放歌曲ID
 * @param   onSingleSongPlayEvent     点击单曲播放事件
 * @param   onAppendIntoPlayList    将曲目加入到播放列表队尾
 * @param   onSingleSongCollectEvent    点击单曲收藏事件
 * @param   onSingleSongShareEvent      点击单曲分享事件
 *
 */
@SuppressLint("UnrememberedMutableState")
@Composable
fun SongTrackShowItemWidget(
    track: Track,
    modifier: Modifier = Modifier,
    currentPlayingTrackId: Long?,
    onSingleSongPlayEvent : () -> Unit,
    onAppendIntoPlayList: () -> Unit,
    onSingleSongCollectEvent : () -> Unit,
    onSingleSongShareEvent : () -> Unit,
) {
    /* 更多信息菜单弹出 状态变量 */
    val menuExpanded = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onSingleSongPlayEvent() }
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (playSignal ,trackName, premiumLogo, trackArtistAndAlbumInfo, playTime, MoreItem) = createRefs()
            val bottomGuideLine = createGuidelineFromBottom(0.4f)
            /* 匹配当前播放状态 */
            TrackItemPlaySignal(
                /* 若当前单曲项目为播放中的曲目， 则点亮播放标识 */
                currentPlayState = mutableStateOf(currentPlayingTrackId == track.id),
                modifier = Modifier
                    .constrainAs(playSignal) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        bottom.linkTo(parent.bottom)
                    }
                    .width(22.dp)
                    .fillMaxHeight()
            )
            /* 音频名字 */
            Text(
                text = track.name,
                style = MaterialTheme.typography.subtitle1.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = if (track.usable) 1f else .3f)      /* 音频可可用性判断， 结果通过颜色深重反馈到界面上 */
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(trackName) {
                    start.linkTo(playSignal.end, margin = 8.dp)
                    bottom.linkTo(bottomGuideLine, margin = 8.dp)
                    width = Dimension.fillToConstraints
                }
            )
            AnimatedVisibility(
                visible = track.privilegeSignal!! >= 1,
                modifier = Modifier.constrainAs(premiumLogo) {
                    start.linkTo(trackName.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(bottomGuideLine)
                }
            ) {
                PremiumLogo(Modifier.padding(start = 6.dp, top = 4.dp, end= 2.dp, bottom = 6.dp ))
            }
            /* 音频艺人与专辑信息 */
            val artists : String = track.ar.map { it.name }.toString()
            Text(
                text = "${artists.replace("[","").replace("]", "")} - ${track.al.name ?: "空"}",
                style = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = if (track.usable) .5f else .3f),
                    fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(trackArtistAndAlbumInfo) {
                    start.linkTo(playSignal.end, margin = 8.dp)
                    top.linkTo(bottomGuideLine)
                    end.linkTo(MoreItem.start)
                    width = Dimension.fillToConstraints
                }
            )
            /* 音频时长 */
            Text(
                text = "${TimeUnit.MILLISECONDS.toMinutes(track.dt)}:${String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(track.dt)%60)}",
                style = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    fontSize = 16.sp
                ),
                modifier = Modifier.constrainAs(playTime) {
                    end.linkTo(MoreItem.start, margin = 4.dp)
                    bottom.linkTo(bottomGuideLine, margin = 8.dp)
                }
            )

            /* 更多选项菜单按钮 */
            IconButton(
                onClick = { menuExpanded.value = true },
                modifier = Modifier.constrainAs(MoreItem) {
                    top.linkTo(parent.top, margin = 6.dp)
                    end.linkTo(parent.end, margin = 2.dp)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More"
                )
                /* 弹出菜单 */
                DropdownMenu(
                    expanded = menuExpanded.value,
                    onDismissRequest = { menuExpanded.value = false }
                ) {
                    DropdownMenuItem(onClick = {
                        onSingleSongPlayEvent()
                        menuExpanded.value = false
                    }) {
                        Text(text = "立即播放")
                    }
                    DropdownMenuItem(onClick = {
                        onAppendIntoPlayList()
                        menuExpanded.value = false
                    }) {
                        Text(text = "加入播放列表")
                    }
                    DropdownMenuItem(onClick = {
                        onSingleSongCollectEvent()
                        menuExpanded.value = false
                    }) {
                        Text(text = "收藏到歌单")
                    }
                    DropdownMenuItem(onClick = {
                        onSingleSongShareEvent()
                        menuExpanded.value = false
                    }) {
                        Text(text = "分享")
                    }
                }
            }
        }
    }
}

/** 播放音频条目 头部播放状态
 *
 */
@Composable
fun TrackItemPlaySignal(
    currentPlayState : State<Boolean>,
    modifier: Modifier
) {
    Box(
        modifier = modifier
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = currentPlayState.value,
                enter = fadeIn(tween(100)),
                exit = fadeOut(tween(durationMillis = 100))
            ) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(color = Cyan500)  // 若处于播放状态则点亮
                )
            }
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (block1, block2, block3) = createRefs()
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(2.dp)
                        .constrainAs(block1) {
                            top.linkTo(parent.top, margin = 20.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(block2.top)
                        }
                        .background(color = MaterialTheme.colors.onSurface.copy(alpha = .2f))
                        .clip(RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(2.dp)
                        .constrainAs(block2) {
                            top.linkTo(block1.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(block3.top)
                        }
                        .background(color = MaterialTheme.colors.onSurface.copy(alpha = .2f))
                        .clip(RoundedCornerShape(2.dp)),
                )
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(2.dp)
                        .constrainAs(block3) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(block2.bottom)
                            bottom.linkTo(parent.bottom, margin = 20.dp)
                        }
                        .background(color = MaterialTheme.colors.onSurface.copy(alpha = .2f))
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        }

    }
}

/** 加载状态的曲目条目组件 1
 *
 */
@Composable
fun LoadingSongTrackShowItemWidget_1() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val showBlock = createRef()
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 20.dp)
                .constrainAs(showBlock) {
                    start.linkTo(parent.start, margin = 20.dp)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end, margin = 12.dp)
                    bottom.linkTo(parent.bottom)
                },
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier
                    .width(142.dp)
                    .height(20.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
                    .weight(4f)
                )
                Spacer(modifier = Modifier.weight(3f))
                Box(modifier = Modifier
                    .width(46.dp)
                    .height(20.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
                    .weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier
                    .width(138.dp)
                    .height(15.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
                )
            }
        }
    }
}

/** 加载状态的曲目条目组件 1
 *
 */
@Composable
fun LoadingSongTrackShowItemWidget_2() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val showBlock = createRef()
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 20.dp)
                .constrainAs(showBlock) {
                    start.linkTo(parent.start, margin = 20.dp)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end, margin = 12.dp)
                    bottom.linkTo(parent.bottom)
                },
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier
                    .width(142.dp)
                    .height(20.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
                    .weight(2f)
                )
                Spacer(modifier = Modifier.weight(5f))
                Box(modifier = Modifier
                    .width(46.dp)
                    .height(20.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
                    .weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier
                    .width(248.dp)
                    .height(15.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
                )
            }
        }
    }
}

/**
 *
 */
@Composable
@Preview(showBackground = true)
fun PreviewSongTrackShowItemWidget() {
    BeeAudioTheme {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        SongTrackShowItemWidget(
            track = SingleTrackMock,
            currentPlayingTrackId = null,
            onSingleSongPlayEvent = {  },
            onAppendIntoPlayList = {  },
            onSingleSongCollectEvent = {  },
        ) { }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewLoadingSongTrackShowItemWidget_1() {
    LoadingSongTrackShowItemWidget_1()
}

@Composable
@Preview(showBackground = true)
fun PreviewLoadingSongTrackShowItemWidget_2() {
    LoadingSongTrackShowItemWidget_2()
}