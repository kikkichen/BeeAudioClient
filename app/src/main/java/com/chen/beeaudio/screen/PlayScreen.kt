package com.chen.beeaudio.screen

import android.content.Context
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chen.beeaudio.R
import com.chen.beeaudio.mock.SingleTrackMock
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.navigation.argument.ShareType
import com.chen.beeaudio.ui.theme.Cyan500
import com.chen.beeaudio.utils.BlurTransformation
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.PlayViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import snow.player.PlayMode
import snow.player.audio.MusicItem
import java.util.concurrent.TimeUnit

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun PlayScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    playViewModel : PlayViewModel = hiltViewModel()
) {
    /* 当前播放曲目 - MediaMetadata */
    val currentMusicItem = mainViewModel.currentPlayingMusicItem.collectAsState()
    /* 请求加载当前曲目信息 */
    playViewModel.currentTrackId = currentMusicItem.value?.musicId?.toLong() ?: SingleTrackMock.id
    playViewModel.loadCurrentTrackDetailInfo()

    val trackState = playViewModel.currentTrack.collectAsState()

    /* 歌单列表播放顺序 */
    val playSequence = remember { mutableStateOf(true) }

    /* 当前播放列表 */
    val playlist by mainViewModel.currentPlaylist.collectAsState()

    /* 当前播放模式 */
    val playMode by mainViewModel.playingMode.collectAsState()

    /* BottomSheet脚手架状态 */
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )
    /* BottomSheet折叠抽屉 内容状态 */
    val sheetContentState = remember { mutableStateOf(SheetContent.PLAYLIST) }

    val coroutineScope = rememberCoroutineScope()

    /* 上下文 */
    val context = LocalContext.current

    AnimatedVisibility(
        visible = currentMusicItem.value != null,
        enter = slideInVertically(
            initialOffsetY = { it }
        ),
        exit = slideOutVertically(
            targetOffsetY = { it }
        )
    ) {
        /* 清空播放列表 弹窗状态 */
        val clearPlaylistDialogState = remember { mutableStateOf(false) }
        ClearPlaylistAlertDialog(
            displayState = clearPlaylistDialogState.value,
            onConfirmEvent = {
                mainViewModel.clearPlayList()   /* 清空播放列表 */
                /* 播放列表抽屉收起 */
                coroutineScope.launch {
                    bottomSheetScaffoldState.bottomSheetState.animateTo(BottomSheetValue.Collapsed)
                }
            },
            onDismissEvent = {
                clearPlaylistDialogState.value = false
            }
        )
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetContent = {
                PlaySheetContent(
                    currentTrackId = currentMusicItem.value?.musicId?.toLong() ?: SingleTrackMock.id,
                    currentTrack = trackState.value,
                    playlist = playlist,
                    context = context,
                    sheetContent = sheetContentState.value,
                    mainViewModel = mainViewModel,
                    playViewModel = playViewModel,
                    onClearPlaylistEvent = {
                        clearPlaylistDialogState.value = true
                    },
                    onSheetCollapseEvent = {
                        /* 播放列表抽屉收起 */
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.animateTo(BottomSheetValue.Collapsed)
                            /* 设置 sheetContentState 为 None 状态，旨在强行变更状态，以刷新列表界面数据 */
                            sheetContentState.value = SheetContent.NONE
                        }
                    }
                )
            },
            sheetPeekHeight = 0.dp,
            sheetElevation = 4.dp,
            drawerScrimColor = Color.Black.copy(alpha = .5f),
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentMusicItem.value?.iconUri)
                        .crossfade(durationMillis = 500)
                        .transformations(listOf(
                            BlurTransformation(scale = 0.9f, radius = 90)
                        )).build(),
                    contentDescription = "Start Listen to ${playViewModel.currentTrack.collectAsState().value.name}",
                    placeholder = painterResource(id = R.drawable.ic_image_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // change alpha of Image as the toolbar expands
//                            alpha = collapsingToolbarScaffoldState.toolbarState.progress
                        },
                    colorFilter = if (isSystemInDarkTheme())
                        ColorFilter.tint(Color.Black.copy(alpha = .6f), BlendMode.Darken)
                    else ColorFilter.tint(Color.White.copy(alpha = .6f), BlendMode.Lighten)
                )
                ControlLayer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Transparent),
                    playSequence = playMode != PlayMode.SHUFFLE,
                    navController = navController,
                    currentTrack = trackState.value,
                    currentMusicItem = currentMusicItem.value ?: MusicItem(),
                    imageLoader = ImageLoader
                        .Builder(LocalContext.current)
                        .crossfade(durationMillis = 500)
                        .build(),
                    context = context,
                    mainViewModel = mainViewModel,
                    onBackEvent = { navController.navigateUp() },
                    onShowPlayListEvent = {
                        coroutineScope.launch {
                            if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                sheetContentState.value = SheetContent.PLAYLIST
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            } else {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            }
                        }
                    },
                    onShowTrackDetailEvent = {
                        coroutineScope.launch {
                            if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                sheetContentState.value = SheetContent.TRACK_DETAIL
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            } else {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            }
                        }
                    },
                    onOpenArtistPage = {
                        playViewModel.openArtistPage(navController)
                    }
                ) {
                    coroutineScope.launch {
                        if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                            sheetContentState.value = SheetContent.SET_TIMING
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        } else {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlLayer(
    modifier: Modifier,
    navController: NavController,
    currentTrack: Track,
    currentMusicItem: MusicItem,
    playSequence: Boolean,
    imageLoader: ImageLoader,
    context: Context,
    mainViewModel: MainViewModel,
    onBackEvent: () -> Unit,
    onShowPlayListEvent: () -> Unit,
    onShowTrackDetailEvent: () -> Unit,
    onOpenArtistPage: () -> Unit,
    onSetTimingEvent: () -> Unit,
) {
    var sliderIsChanging by remember { mutableStateOf(false) }
    var localSliderValue by remember { mutableStateOf(0f) }
    val currentProgress by mainViewModel.currentPlayingProgress.collectAsState()
    val currentDuration by mainViewModel.currentPlayingDuration.collectAsState()
    val currentProgressFormatString by mainViewModel.currentPlayingFormatProgress.collectAsState()
    val currentDurationFormatString by mainViewModel.currentPlayingFormatDuration.collectAsState()
    val sliderProgress by rememberUpdatedState(newValue = (currentProgress.toFloat()/currentDuration.toFloat()))

    /* 以毫秒为单位的曲目播放时长 */
    val currentMilliSecondDuration = currentMusicItem.duration
    /* 曲目加载缓冲状态 */
    val isCacheState by mainViewModel.isCacheState.collectAsState()

    /* 当前曲目的收藏状态 */
    val currentLikeState = mainViewModel.isMyFavoriteTrack.collectAsState()

    /* 状态栏高 */
    val statusBarHeightValue = WindowInsets.systemBars.asPaddingValues()

    ConstraintLayout(
        modifier = modifier
    ) {
        val (topBar, coverImage, trackInfo, seekBar, controlBar ,bottomBar) = createRefs()
        PlayTopBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(top = statusBarHeightValue.calculateTopPadding())
                .constrainAs(topBar) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
                .background(color = Color.Transparent)
                .padding(vertical = 8.dp)
                .padding(top = 8.dp),
            likeConfirm = currentLikeState.value,
            onFoldCurrentEvent = onBackEvent,
            onTouchLikeEvent = {
                mainViewModel.dealWithTrackCollect(context = context)
            },
            onShareAudioEvent = {
                val shareTypeArgument = ShareType("[share_track]" + Gson().toJson(currentTrack))
                navController.navigate(route = BlogRoute.SendScreen.route + "?share_item_out=${
                    Uri.encode(
                        Gson().toJson(shareTypeArgument))}")
            }
        )

        AlbumCoverDisplay(
            modifier = Modifier
                .height(340.dp)
                .width(340.dp)
                .constrainAs(coverImage) {
                    start.linkTo(parent.start)
                    top.linkTo(topBar.bottom, margin = 4.dp)
                    end.linkTo(parent.end)
                    bottom.linkTo(trackInfo.top, margin = (-10).dp)
                },
            picUrl = currentMusicItem.iconUri,
            imageLoader = imageLoader,
        )

        AudioInfoShowBlock(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(trackInfo) {
                    start.linkTo(parent.start)
                    top.linkTo(coverImage.bottom)
                    end.linkTo(parent.end)
                    bottom.linkTo(seekBar.top)
                },
            musicItem = currentMusicItem,
            onOpenArtistPage = { onOpenArtistPage() }
        )

        AudioSeekBar(
            modifier = Modifier
                .constrainAs(seekBar) {
                    start.linkTo(parent.start)
                    top.linkTo(trackInfo.bottom)
                    end.linkTo(parent.end)
                    bottom.linkTo(controlBar.top, margin = (-12).dp)
                }
                .padding(horizontal = 10.dp),
            progress = if (sliderIsChanging || isCacheState) localSliderValue else sliderProgress,
            progressFormatString = currentProgressFormatString,
            durationFormatString = currentDurationFormatString,
            onSliderChange = { newPosition ->
                localSliderValue = newPosition
                sliderIsChanging = true
            },
            onSliderChangeFinished = {
                mainViewModel.seekToTargetPosition((localSliderValue * currentMilliSecondDuration).toInt())
                sliderIsChanging = false
            }
        )

        PlayControllerBlock(
            modifier = Modifier.constrainAs(controlBar) {
                start.linkTo(parent.start)
                top.linkTo(seekBar.bottom, margin = (-24).dp)
                end.linkTo(parent.end)
                bottom.linkTo(bottomBar.top, margin = 12.dp)
            },
            mainViewModel = mainViewModel,
            playSequence = playSequence,
            onSetTimingEvent = { onSetTimingEvent() },
            onPlayControlEvent = { mainViewModel.playOrPauseAction() },
            onPlayPreviousSong = { mainViewModel.playPreviousMusic() },
            onPlayNextSong = { mainViewModel.playNextMusic() }
        )

        ButtonFeatureBar(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(bottomBar) {
                    start.linkTo(parent.start)
                    top.linkTo(controlBar.bottom)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom, margin = 12.dp)
                },
            onOpenPlayListSheet = onShowPlayListEvent,
            onOpenTrackDetailBlock = onShowTrackDetailEvent
        )
    }
}

/** 播放案件控制布局
 *  @param  modifier    修饰符参数
 *  @param  playSequence    当前播放顺序， true 为按顺序播放， false为随即播放
 *  @param  onPlayControlEvent  变更播放状态的点击事件
 *
 */
@Composable
fun PlayControllerBlock(
    modifier : Modifier,
    playSequence : Boolean,
    mainViewModel: MainViewModel,
    onSetTimingEvent : () -> Unit,
    onPlayControlEvent : () -> Unit,
    onPlayPreviousSong : () -> Unit,
    onPlayNextSong : () -> Unit
) {
    /* 播放状态 */
    val isPlaying by mainViewModel.isPlayingState.collectAsState()

    ConstraintLayout(
        modifier = modifier
    ) {
        val (timingButton, previousButton, playButton, nextButton, sortButton) = createRefs()
        /* 定时按钮 */
        IconButton(
            onClick = onSetTimingEvent,
            modifier = Modifier.constrainAs(timingButton) {
                top.linkTo(parent.top)
                end.linkTo(previousButton.start, margin = 24.dp)
                bottom.linkTo(parent.bottom)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_audio_timing),
                contentDescription = "定时功能",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colors.onSurface
            )
        }
        /* 上一曲 */
        IconButton(
            onClick = { onPlayPreviousSong() },
            modifier = Modifier.constrainAs(previousButton) {
                top.linkTo(parent.top)
                end.linkTo(playButton.start, margin = 24.dp)
                bottom.linkTo(parent.bottom)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_audio_previous),
                contentDescription = "上一曲",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colors.onSurface
            )
        }
        /* 播放 暂停 */
        IconButton(
            onClick = { onPlayControlEvent() },
            modifier = Modifier.constrainAs(playButton) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
        ) {
            Icon(
                painter = painterResource(id = if (isPlaying) R.drawable.ic_audio_pause else R.drawable.ic_audio_play),
                contentDescription = "暂停",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colors.onSurface,
            )
        }
        /* 下一曲 */
        IconButton(
            onClick = { onPlayNextSong() },
            modifier = Modifier.constrainAs(nextButton) {
                start.linkTo(playButton.end, margin = 24.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_audio_next),
                contentDescription = "下一曲",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colors.onSurface
            )
        }
        /* 播放顺序 - 按照顺序播放 */
        Crossfade(
            targetState = playSequence,
            modifier = Modifier.constrainAs(sortButton) {
                start.linkTo(nextButton.end, margin = 24.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        ) { playSequence ->
            IconButton(
                onClick = { mainViewModel.setPlayModel(if (playSequence) PlayMode.SHUFFLE else PlayMode.PLAYLIST_LOOP) },
            ) {
                Icon(
                    painter = painterResource(id = if (playSequence) R.drawable.ic_audio_play_sequential else R.drawable.ic_audio_play_random),
                    contentDescription = if (playSequence) "列表循环" else "随机播放",
                    modifier = Modifier.size(if (playSequence) 32.dp else 30.dp),
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}

/** 播放界面顶栏
 *  @param  likeConfirm 当前曲目的like状态
 *  @param  onFoldCurrentEvent  折叠当前播放页面点击事件
 *  @param  onTouchLikeEvent    点击将当前曲目加入我的喜爱
 *  @param  onShareAudioEvent   点击分析那个当前曲目到社区点击事件
 *
 */
@Composable
fun PlayTopBar(
    modifier : Modifier,
    likeConfirm : Boolean,
    onFoldCurrentEvent : () -> Unit,
    onTouchLikeEvent : (Boolean) -> Unit,
    onShareAudioEvent : () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (foldButton, likeButton, shareButton) = createRefs()
            /* 折叠播放页面按键 */
            IconButton(
                onClick = { onFoldCurrentEvent() },  // 折叠收起当前播放页面
                modifier = Modifier.constrainAs(foldButton) {
                    start.linkTo(parent.start, margin = 12.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_audio_screen_fold),
                    contentDescription = "折叠播放页面",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colors.onSurface
                )
            }
            /* 点赞按钮 */
            IconButton(
                onClick = { onTouchLikeEvent(likeConfirm) },
                modifier = Modifier.constrainAs(likeButton) {
                    top.linkTo(parent.top)
                    end.linkTo(shareButton.start)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                Icon(
                    painter = painterResource(id = if (likeConfirm) R.drawable.ic_audio_like_confirm else R.drawable.ic_audio_like_unconfirm),
                    contentDescription = "将当前歌曲加入我的喜欢",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colors.onSurface
                )
            }
            /* 分享按钮 */
            IconButton(
                onClick = { onShareAudioEvent() },       // 分享当前曲目到社区 点击分享触发时间
                modifier = Modifier.constrainAs(shareButton) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end, margin = 12.dp)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "分享当前歌曲到我的社区",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}

/** 专辑封面显示
 *  @param  modifier    修饰符参数
 *  @param  picUrl      专辑封面图片URL
 *  @param  imageLoader Coil图像加载器
 */
@Composable
fun AlbumCoverDisplay(
    modifier : Modifier,
    picUrl : String,
    imageLoader: ImageLoader
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val coverImg = createRef()
        Card(
            modifier = Modifier
                .height(340.dp)
                .width(340.dp)
                .constrainAs(coverImg) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
            shape = RoundedCornerShape(10.dp),
            elevation = 4.dp
        ) {
            AsyncImage(
                model = picUrl,
                contentDescription = null,
                placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder),
                contentScale = ContentScale.Crop,
                imageLoader = imageLoader
            )
        }
    }
}

/** 曲目名字与创作艺人显示模块
 *  @param  modifier    修饰符参数
 *  @param  musicItem   播放音频条目对象
 *  @param  onOpenArtistPage    打开相关艺人页面
 */
@Composable
fun AudioInfoShowBlock(
    modifier : Modifier,
    musicItem: MusicItem,
    onOpenArtistPage : () -> Unit,
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (trackName, trackArtist) = createRefs()
        Text(
            text = musicItem.title,
            style = MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .constrainAs(trackName) {
                    top.linkTo(parent.top, margin = 10.dp)
                    start.linkTo(parent.start, margin = 26.dp)
                    end.linkTo(parent.end, margin = 26.dp)
                    width = Dimension.fillToConstraints
                }
        )
        Text(
            text = musicItem.artist,
            style = MaterialTheme.typography.body2.copy(
                color = MaterialTheme.colors.onSurface.copy(alpha = .5f),
                fontSize = 12.sp,
                textAlign = TextAlign.Start
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .constrainAs(trackArtist) {
                    top.linkTo(trackName.bottom)
                    start.linkTo(parent.start, margin = 32.dp)
                    end.linkTo(parent.end, margin = 32.dp)
                    width = Dimension.fillToConstraints
                }
                .padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 8.dp)
                .clickable { onOpenArtistPage() }
        )
    }
}

/** 播放进度条
 *
 */
@Composable
fun AudioSeekBar(
    modifier : Modifier,
    progress : Float,
    progressFormatString: String,
    durationFormatString: String,
    onSliderChange : (Float) -> Unit,
    onSliderChangeFinished : () -> Unit,
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (seekbar, currentProgress, trackTime) = createRefs()
        val centerGuideLine = createGuidelineFromBottom(.5f)
        Slider(
            value = progress,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colors.onSurface,
                activeTrackColor = MaterialTheme.colors.onSurface,
                inactiveTrackColor = MaterialTheme.colors.onSurface.copy(alpha = .3f)
            ),
            modifier = Modifier.constrainAs(seekbar) {
                start.linkTo(parent.start, margin = 28.dp)
                top.linkTo(parent.top)
                end.linkTo(parent.end, margin = 28.dp)
                bottom.linkTo(centerGuideLine)
            },
            onValueChange = onSliderChange,
            onValueChangeFinished = onSliderChangeFinished
        )
        Text(
            text = progressFormatString,
            style = MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.onSurface.copy(alpha = .9f),
                fontSize = 14.sp,
                textAlign = TextAlign.Start
            ),
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .constrainAs(currentProgress) {
                    top.linkTo(centerGuideLine, margin = (-4).dp)
                    start.linkTo(parent.start, margin = 8.dp)
                }
                .padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 8.dp)
        )
        Text(
            text = durationFormatString,
            style = MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.onSurface.copy(alpha = .9f),
                fontSize = 14.sp,
                textAlign = TextAlign.End
            ),
            textAlign = TextAlign.Right,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .constrainAs(trackTime) {
                    top.linkTo(centerGuideLine, margin = (-4).dp)
                    end.linkTo(parent.end, margin = 8.dp)
                    width = Dimension.fillToConstraints
                }
                .padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 8.dp)
        )
    }
}

/** 播放界面底部功能栏
 *  @param  modifier    修饰符参数
 *  @param  onOpenPlayListSheet 打开播放列表抽屉底栏事件
 *  @param  onOpenTrackDetailBlock    显示当前播放音乐音频信息事件
 *
 */
@Composable
fun ButtonFeatureBar(
    modifier : Modifier,
    onOpenPlayListSheet : () -> Unit,
    onOpenTrackDetailBlock : () -> Unit,
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (openPlayListSheetButton, trackInfoButton) = createRefs()
        TextButton(
            onClick = { onOpenPlayListSheet() },
            modifier = Modifier
                .height(52.dp)
                .width(144.dp)
                .constrainAs(openPlayListSheetButton) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .clip(RoundedCornerShape(10.dp))
                .background(color = MaterialTheme.colors.onSurface.copy(alpha = .1f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxSize()
            ) {
                val (text, icon) = createRefs()
                val endGuideLine = createGuidelineFromEnd(.28f)
                Text(
                    text = "播放列表",
                    style = MaterialTheme.typography.body1.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = .5f),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Start
                    ),
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .constrainAs(text) {
                            top.linkTo(parent.top, margin = 4.dp)
                            end.linkTo(endGuideLine)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.fillToConstraints
                        }
                        .padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 8.dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_audio_playlist),
                    contentDescription = "播放列表",
                    modifier = Modifier
                        .size(20.dp)
                        .constrainAs(icon) {
                            top.linkTo(parent.top)
                            start.linkTo(endGuideLine)
                            bottom.linkTo(parent.bottom)
                        },
                    tint = MaterialTheme.colors.onSurface.copy(alpha = .5f)
                )
            }
        }
        /* 当前播放曲目信息 */
        IconButton(
            onClick = { onOpenTrackDetailBlock() },
            modifier = Modifier.constrainAs(trackInfoButton) {
                end.linkTo(parent.end, margin = 26.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_audio_info),
                contentDescription = "下一曲",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colors.onSurface.copy(alpha = .5f)
            )
        }
    }
}

/** 曲目 Track 信息展示板块
 *  @param  modifier    修饰符参数
 *  @param  playViewModel   当前播放页ViewModel
 *
 */
@Composable
fun ShowTrackDetailBlock(
    modifier : Modifier = Modifier,
    track : Track,
    playViewModel: PlayViewModel
) {

    val trackFileState = playViewModel.currentTrackFile.collectAsState()
    Column(
        modifier = modifier,
    ) {
        ShowTrackDetailItem(keyTitle = "曲名", valueContent = track.name)
        ShowTrackDetailItem(keyTitle = "艺人", valueContent = track.ar.map { it.name }.toString().replace("[", "").replace("]",""))
        ShowTrackDetailItem(keyTitle = "专辑", valueContent = "《${track.al.name}》")
        ShowTrackDetailItem(keyTitle = "时长", valueContent = "${TimeUnit.MILLISECONDS.toMinutes(track.dt)}:${String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(track.dt)%60)}")
        ShowTrackDetailItem(keyTitle = "文件大小", valueContent = trackFileState.value.size.toString())
        ShowTrackDetailItem(keyTitle = "可用性", valueContent = track.usable.toString())
        ShowTrackDetailItem(keyTitle = "品质", valueContent = "标准")
        ShowTrackDetailItem(keyTitle = "编码", valueContent = trackFileState.value.encodeType)
        ShowTrackDetailItem(keyTitle = "码率", valueContent = "44100 KHz")
        ShowTrackDetailItem(keyTitle = "权限", valueContent = if (track.privilegeSignal == 1) "Premium" else "Common")
        ShowTrackDetailItem(keyTitle = "来源", valueContent = track.source.toString())
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** track 音频 信息条目
 *  @param  keyTitle    键标题
 *  @param  valueContent    值内容
 */
@Composable
fun ShowTrackDetailItem(
    keyTitle : String,
    valueContent : String
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        val (key, value) = createRefs()
        val startGuideLine = createGuidelineFromStart(0.3f)
        Text(
            text = keyTitle,
            style = MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            ),
            modifier = Modifier.constrainAs(key) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                end.linkTo(startGuideLine, margin = 12.dp)
                bottom.linkTo(parent.bottom)
            }
        )
        Text(
            text = valueContent,
            style = MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            ),
            modifier = Modifier.constrainAs(value) {
                start.linkTo(startGuideLine, margin = 12.dp)
                top.linkTo(parent.top)
                end.linkTo(parent.end, margin = 10.dp)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
            }
        )
    }
}

/** 定时关闭任务 抽屉内容界面
 *  @param  modifier    修饰符参数
 *  @param  mainViewModel   MainViewModel 视图模型
 */
@Composable
fun TimingSheetContent(
    modifier: Modifier,
    mainViewModel: MainViewModel
) {
    val tags = arrayListOf("无定时" ,"10分钟", "15分钟", "30分钟", "45分钟", "1小时", "2小时")
    val selectedTag = remember { mutableStateOf("无定时") }

    Column(
        modifier = modifier
    ) {
        tags.forEach {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = it == selectedTag.value,
                    onClick = {
                        selectedTag.value = it
                        when (it) {
                            "无定时" -> {
                                mainViewModel.setCancelPlaySleepTime()
                            }
                            "10分钟" -> {
                                mainViewModel.setStartPlaySleepTime((10*60000).toLong())
                            }
                            "15分钟" -> {
                                mainViewModel.setStartPlaySleepTime((15*60000).toLong())
                            }
                            "30分钟" -> {
                                mainViewModel.setStartPlaySleepTime((30*60000).toLong())
                            }
                            "45分钟" -> {
                                mainViewModel.setStartPlaySleepTime((45*60000).toLong())
                            }
                            "1小时" -> {
                                mainViewModel.setStartPlaySleepTime((60*60000).toLong())
                            }
                            "2小时" -> {
                                mainViewModel.setStartPlaySleepTime((120*60000).toLong())
                            }
                        }
                    })
                Text(text = it)
            }
        }
    }
}

/** 播放页折叠抽屉内容枚举类
 *
 */
enum class SheetContent {
    NONE,           // 无状态
    PLAYLIST,       // 播放列表
    TRACK_DETAIL,   // 曲目信息
    SET_TIMING      // 定时关闭
}

/** 播放列表
 *  @param  currentTrackId  当前播放曲目ID
 *  @param  context         上下文对象参数
 *  @param  sheetContent    折叠底栏上下文信息
 *  @param  mainViewModel   MainViewModel 对象
 *  @param  playViewModel   PlayViewModel 对象
 */
@ExperimentalFoundationApi
@Composable
fun PlaySheetContent(
    currentTrackId: Long,
    currentTrack: Track,
    playlist : MutableList<MusicItem>,
    context : Context,
    sheetContent: SheetContent,
    mainViewModel: MainViewModel,
    playViewModel: PlayViewModel,
    onClearPlaylistEvent: () -> Unit,
    onSheetCollapseEvent: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(380.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(color = Color.Transparent),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            stickyHeader {
                ConstraintLayout(
                    modifier = Modifier
                        .height(58.dp)
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colors.surface),
                ) {
                    val (sheetTitle, clearIcon) = createRefs()
                    /* 抽屉栏标题 */
                    Text(
                        text = when(sheetContent){
                            SheetContent.PLAYLIST -> "播放列表"
                            SheetContent.SET_TIMING -> "定时关闭"
                            else -> "更多信息"
                        },
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.constrainAs(sheetTitle){
                            start.linkTo(parent.start, margin = 20.dp)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                    )
                    /* 清空播放列表 */
                    if (sheetContent == SheetContent.PLAYLIST) {
                        IconButton(
                            onClick = onClearPlaylistEvent,
                            modifier = Modifier.constrainAs(clearIcon) {
                                top.linkTo(parent.top)
                                end.linkTo(parent.end, margin = 12.dp)
                                bottom.linkTo(parent.bottom)
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_clear_playlist),
                                contentDescription = "清空播放列表",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colors.onSurface.copy(alpha = .5f)
                            )
                        }
                    }
                }
            }
            when (sheetContent) {
                SheetContent.PLAYLIST -> {
                    items(
                        items = playlist,
                        key = { item: MusicItem ->
                            item.musicId
                        }
                    ) { musicItem ->
                        PlayListSheetItem(
                            musicItem = musicItem,
                            currentTrackId = currentTrackId,
                            onClickEvent = {
                                val position = playlist.indexOf(musicItem)
                                mainViewModel.playTargetPositionMusic(position)
                            },
                            onMoveToNextEvent = {
                                mainViewModel.moveToNextPlayMusic(it, context)
                                onSheetCollapseEvent()
                            },
                            onRemoveItemEvent = {
                                mainViewModel.removeTargetMusic(it, context)
                                onSheetCollapseEvent()
                            }
                        )
                    }
                }
                SheetContent.TRACK_DETAIL -> {
                    item {
                        ShowTrackDetailBlock(
                            modifier = Modifier.scrollable(rememberScrollState(), Orientation.Vertical),
                            track = currentTrack,
                            playViewModel = playViewModel
                        )
                    }
                }
                SheetContent.SET_TIMING -> {
                    item {
                        TimingSheetContent(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            mainViewModel = mainViewModel
                        )
                    }
                }
            }
        }
    }
}

/** 播放列表条目子项
 *  @param  musicItem   播放曲目信息对象
 *  @param  currentTrackId  当前播放曲目ID
 */
@Composable
fun PlayListSheetItem(
    musicItem: MusicItem,
    currentTrackId: Long,
    onClickEvent: (MusicItem) -> Unit,
    onMoveToNextEvent: (MusicItem) -> Unit,
    onRemoveItemEvent: (MusicItem) -> Unit,
) {
    val archive = SwipeAction(
        icon = painterResource(id = R.drawable.ic_set_next_play),
        background = Color.Green,
        isUndo = true,
        onSwipe = {
            onMoveToNextEvent(musicItem)
        }
    )
    val snooze = SwipeAction(
        icon = painterResource(id = R.drawable.ic_remove_music_item),
        background = Color.Red,
        isUndo = true,
        onSwipe = {
            onRemoveItemEvent(musicItem)
        }
    )

    SwipeableActionsBox(
        startActions = listOf(archive),
        endActions = listOf(snooze)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.surface)
                .clickable { onClickEvent(musicItem) }  /* 点击播放 */
        ) {
            val (currentPlaySignal, song, artist, divideLine) = createRefs()
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(color = if (currentTrackId == musicItem.musicId.toLong()) Cyan500 else Color.Transparent)  // 若处于播放状态则点亮
                    .constrainAs(currentPlaySignal) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )
            Text(
                text = "《${musicItem.title}》",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(song) {
                    start.linkTo(currentPlaySignal.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                }
            )
            Text(
                text = "- ${musicItem.artist}",
                style = MaterialTheme.typography.subtitle1.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    fontSize = 14.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(artist) {
                    start.linkTo(song.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                }
            )
            Divider(
                modifier = Modifier.constrainAs(divideLine) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
            )
        }
    }
}

/** 清空播放列表警告弹窗
 *  @param  displayState    弹窗警告显示状态
 *  @param  onConfirmEvent  确认清空播放列表事件
 *  @param  onDismissEvent  取消事件
 */
@Composable
fun ClearPlaylistAlertDialog(
    displayState: Boolean,
    onConfirmEvent: () -> Unit,
    onDismissEvent: () -> Unit,
) {
    AnimatedVisibility(
        visible = displayState,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AlertDialog(
            title = { Text(text = "清空播放列表") },
            text = { Text(text = "您确定要清空播放列表吗") },
            dismissButton = {
                TextButton(onClick = { onDismissEvent() }) {
                    Text(text = "取消")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirmEvent()
                    onDismissEvent()
                }) {
                    Text(text = "确认")
                }
            },
            onDismissRequest = { /*TODO*/ }
        )
    }
}