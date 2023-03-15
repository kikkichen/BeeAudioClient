package com.chen.beeaudio.screen

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chen.beeaudio.R
import com.chen.beeaudio.mock.SingleAlbum
import com.chen.beeaudio.mock.SingleTrackMock
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.audio.AlbumDetail
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.navigation.argument.ShareType
import com.chen.beeaudio.screen.widget.*
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.utils.BlurTransformation
import com.chen.beeaudio.viewmodel.AlbumViewModel
import com.chen.beeaudio.viewmodel.MainViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun AlbumScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    mViewModel: AlbumViewModel = hiltViewModel()
) {
    /* 上下文信息 */
    val context = LocalContext.current
    /* 协程域 */
    val coroutineScope = rememberCoroutineScope()
    /* 当前播放曲目 MusicItem */
    val currentPlayingMusicItem = mainViewModel.currentPlayingMusicItem.collectAsState()

    val collapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState()
    /* 状态顶栏折叠标识 */
    val enabled by remember { mutableStateOf(true) }
    /* 屏幕宽度 */
    val configuration = LocalConfiguration.current
    val currentScreenWidth = configuration.screenWidthDp

    /* 曲目收藏逻逻辑执行窗口弹出标识 */
    var isCollectAction by remember { mutableStateOf(false) }
    /* 当前专辑信息 */
    /* 升级到 Premium 窗口提示标识 */
    var isPremiumAlert by remember { mutableStateOf(false) }

    val albumInfoState = loadCurrentAlbumDetail(viewModel = mViewModel)
    /* 专辑标题信息 */
    val albumTopBarTitle = remember { mutableStateOf("当前专辑") }
    when (albumInfoState.value) {
        is NetAlbumDetailLoadResult.Success -> {
            albumTopBarTitle.value = (albumInfoState.value as NetAlbumDetailLoadResult.Success).albumDetail.album.name ?: "当前专辑 "
        }
        else -> {
            albumTopBarTitle.value = "当前专辑"
        }
    }

    /* 专辑收藏状态 */
    val currentAlbumSubscribeState = mViewModel.isSubscribe.collectAsState()
    /* 当前收藏逻辑选择曲目 */
    val tempCollectTrack : MutableState<Track> = remember { mutableStateOf(SingleTrackMock) }
    /* 曲目收藏逻逻辑执行窗口 */
    if (isCollectAction) {
        CollectToPlaylistDialog(
            context = context,
            targetTrack = tempCollectTrack.value,
            currentUserId = mainViewModel.currentUserId,
            myFavoritePlaylistId = mainViewModel.myFavoritePlaylistId,
            parentWidth = currentScreenWidth,
            onDismissEvent = { isCollectAction = false },
            onUpdateCurrent = {
                coroutineScope.launch(Dispatchers.IO) {
                    mainViewModel.loadCurrentTrackIsMyLike()
                }
            }
        )
    }

    /* 升级到 Premium 会员套餐提示窗口 */
    PremiumRecommendDialog(
        visible = isPremiumAlert,
        navController = navController,
        parentWidth = currentScreenWidth,
        onDismissEvent = { isPremiumAlert = false }
    )

    Box(
        Modifier.fillMaxSize()
    ) {
        CollapsingToolbarScaffold(
            modifier = Modifier.fillMaxSize(),
            state = collapsingToolbarScaffoldState,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            toolbarModifier = Modifier.background(MaterialTheme.colors.surface),
            enabled = enabled,
            toolbar = {
                // Collapsing toolbar collapses its size as small as the that of
                // a smallest child. To make the toolbar collapse to 50dp, we create
                // a dummy Spacer composable.
                // You may replace it with TopAppBar or other preferred composable.
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            color = MaterialTheme.colors.surface
                        )
                )
                when (albumInfoState.value) {
                    is NetAlbumDetailLoadResult.Success -> {
                        val albumInfo =
                            (albumInfoState.value as NetAlbumDetailLoadResult.Success).albumDetail
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(albumInfo.album.picUrl)
                                .transformations(
                                    listOf(
                                        BlurTransformation(scale = 0.5f, radius = 25)
                                    )
                                ).build(),
                            contentDescription = "Current album is '${albumInfo.album.name}'",
                            placeholder = painterResource(id = R.drawable.ic_image_placeholder),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .parallax(0.5f)
                                .height(480.dp)
                                .graphicsLayer {
                                    // change alpha of Image as the toolbar expands
//                            alpha = collapsingToolbarScaffoldState.toolbarState.progress
                                },
                            colorFilter = if (isSystemInDarkTheme()) ColorFilter.tint(
                                Color.Gray.copy(
                                    alpha = .5f
                                ), BlendMode.Darken
                            ) else null
                        )
                        AlbumDetailBlock(
                            album = albumInfo.album,
                            collapsingToolbarScaffoldState = collapsingToolbarScaffoldState,
                            parentHeight = 480,
                            parentWidth = currentScreenWidth
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .height(480.dp)
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colors.surface)
            ) {
                stickyHeader {
                    when (albumInfoState.value) {
                        is NetAlbumDetailLoadResult.Success -> {
                            AlbumColumnStickyHeader(
                                albumDetail = (albumInfoState.value as NetAlbumDetailLoadResult.Success).albumDetail,
                                isSubscribe = currentAlbumSubscribeState.value,
                                onCollectEvent = { mViewModel.changeSubscribeState(context = context, mainViewModel.currentUserId) },
                                onPlayAllEvent = {  }
                            )
                        }
                        else -> {
                            AlbumColumnStickyHeaderLoadingState()
                        }
                    }
                }
                when (albumInfoState.value) {
                    is NetAlbumDetailLoadResult.Success -> {
                        val albumDetail = (albumInfoState.value as NetAlbumDetailLoadResult.Success).albumDetail
                        items(
                            items = albumDetail.songs,
                            key = { item: Track ->
                                item.id
                            }
                        ) { item ->
                            SongTrackShowItemWidget(
                                track = item,
                                currentPlayingTrackId = currentPlayingMusicItem.value?.musicId?.toLong(),
                                onSingleSongPlayEvent = {
                                    if (!item.usable) {
                                        Toast.makeText(context, "十分抱歉！由于各方原因，该曲目现已下架", Toast.LENGTH_SHORT).show()
                                    } else if (item.privilegeSignal == 1) {
                                        if (mainViewModel.currentUserDetailInfo.value.user_type == 1) {
                                            mainViewModel.playTargetAudio(track = item, context = context)
                                        } else {
                                            isPremiumAlert = true
                                        }
                                    } else {
                                        mainViewModel.playTargetAudio(track = item, context = context)
                                    }
                                },
                                onAppendIntoPlayList = { mainViewModel.appendTargetMusic(track = item, context = context) },
                                onSingleSongCollectEvent = {
                                    tempCollectTrack.value = item
                                    isCollectAction = true
                                },
                                onSingleSongShareEvent = {
                                    val shareTypeArgument = ShareType("[share_track]" + Gson().toJson(item))
                                    navController.navigate(route = BlogRoute.SendScreen.route + "?share_item_out=${
                                        Uri.encode(
                                            Gson().toJson(shareTypeArgument))}")
                                }
                            )
                        }
                    }
                    is NetAlbumDetailLoadResult.Loading -> {
                        items(count = 3) {
                            LoadingSongTrackShowItemWidget_1()
                            LoadingSongTrackShowItemWidget_2()
                        }
                    }
                    else -> {
                        item {
                            ErrorDataTipsWidget(
                                text = "该专辑的信息数据加载出了些问题，\n请退出该页重试",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 28.dp)
                            )
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            AlbumDetailHeaderBar(
                albumName = albumTopBarTitle.value,
                collapsingToolbarScaffoldState = collapsingToolbarScaffoldState,
                onBackEvent = { navController.navigateUp() },
                onMoreMenuEvent = {
                    if (albumInfoState.value is NetAlbumDetailLoadResult.Success) {
                        val shareTypeArgument = ShareType("[share_album_detail]" + Gson().toJson((albumInfoState.value as NetAlbumDetailLoadResult.Success).albumDetail))
                        navController.navigate(route = BlogRoute.SendScreen.route + "?share_item_out=${Uri.encode(Gson().toJson(shareTypeArgument))}")
                    }
                },
            )
        }
    }
}

/** 专辑内容页 折叠标题栏
 *  @param  albumName    专辑标题信息字符串
 *  @param  collapsingToolbarScaffoldState  折叠组合布局脚手架状态对象
 *  @param  onBackEvent 返回事件响应
 *  @param  onMoreMenuEvent  更多信息菜单触发书简
 */
@Composable
fun AlbumDetailHeaderBar(
    albumName: String,
    collapsingToolbarScaffoldState : CollapsingToolbarScaffoldState,
    onBackEvent: () -> Unit,
    onMoreMenuEvent: () -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
    ) {
        val (backArrowButton, playListHeader, MoreButton) = createRefs()
        /* 返回 */
        IconButton(
            modifier = Modifier
                .constrainAs(backArrowButton) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
                .background(color = MaterialTheme.colors.surface.copy(alpha = 0f)),
            onClick = { onBackEvent() },
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back Audio Home",
                modifier = Modifier
                    .size(24.dp)
            )
        }

        AnimatedVisibility(
            visible = collapsingToolbarScaffoldState.toolbarState.progress <= .1f,
            enter = fadeIn(animationSpec = tween(durationMillis = 800)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500)),
            modifier = Modifier.constrainAs(playListHeader) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(backArrowButton.end)
                width = Dimension.fillToConstraints
            }
        ) {
            Text(
                text = "《$albumName》",
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        /* 分享歌单 */
        IconButton(
            modifier = Modifier
                .constrainAs(MoreButton) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 12.dp)
                }
                .background(color = MaterialTheme.colors.surface.copy(alpha = 0f)),
            onClick = { onMoreMenuEvent() },
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share This PlayList",
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}

/** 专辑列表吸顶栏 - 统计专辑内曲目信息
 *  @param  albumDetail 专辑详情信息对象
 *  @param  onPlayAllEvent  播放全部曲目触发事件
 */
@Composable
fun AlbumColumnStickyHeader(
    albumDetail: AlbumDetail,
    isSubscribe: Boolean,
    onPlayAllEvent : () -> Unit,
    onCollectEvent : () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        elevation = 1.dp
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (countTitle, subCollectButton) = createRefs()
            Text(
                text = "共 ${albumDetail.songs.size} 首歌曲",
                style = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = .7f),
                    fontSize = 14.sp
                ),
                modifier = Modifier.constrainAs(countTitle) {
                    start.linkTo(parent.start, margin = 12.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                }
            )
            Row(
                modifier = Modifier.constrainAs(subCollectButton){
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 12.dp)
                }
            ) {
                /* 播放全部按钮 */
                IconButton(
                    onClick = { onPlayAllEvent() }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Collect This PlayList",
                        modifier = Modifier.size(25.dp),
                        tint = MaterialTheme.colors.onSurface.copy(.7f)
                    )
                }
                /* 专辑收藏按键 */
                IconButton(
                    onClick = { onCollectEvent() }
                ) {
                    AnimatedVisibility(
                        visible = isSubscribe,
                        enter = fadeIn(tween(500)) + expandVertically(),
                        exit = fadeOut(tween(500)) + shrinkVertically(),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_collect_playlist_confirm),
                            contentDescription = "Cancel Collect This PlayList",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colors.onSurface.copy(.7f)
                        )
                    }
                    AnimatedVisibility(
                        visible = !isSubscribe,
                        enter = fadeIn(tween(500)) + expandVertically(),
                        exit = fadeOut(tween(500)) + shrinkVertically(),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_collect_playlist),
                            contentDescription = "Collect This PlayList",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colors.onSurface.copy(.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumColumnStickyHeaderLoadingState() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.7f at 500
            },
            repeatMode = RepeatMode.Reverse
        )
    )
    Card(
        modifier = Modifier
            .height(50.dp)
            .fillMaxSize()
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val loadingBlock = createRef()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .padding(start = 12.dp, top = 0.dp, end = 30.dp, bottom = 0.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.LightGray.copy(alpha = alpha))
                    .constrainAs(loadingBlock) {
                        start.linkTo(parent.start, margin = 24.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )
        }
    }
}

/* 当前专辑页面 - 当前专辑详细信息请求结果状态 */
sealed class NetAlbumDetailLoadResult<T> {
    object Loading : NetAlbumDetailLoadResult<AlbumDetail>()
    object Error : NetAlbumDetailLoadResult<AlbumDetail>()
    data class Success(val albumDetail: AlbumDetail) : NetAlbumDetailLoadResult<AlbumDetail>()
}

/** 使用produceState将当前歌单信息数据Flow转换为State
 *  @param  viewModel   当前PlayListScreen 的ViewModel
 */
@Composable
fun loadCurrentAlbumDetail(
    viewModel: AlbumViewModel
) : State<NetAlbumDetailLoadResult<AlbumDetail>> {
    return produceState(initialValue = NetAlbumDetailLoadResult.Loading as NetAlbumDetailLoadResult<AlbumDetail>, viewModel) {
        var currentAlbum : AlbumDetail? = null
        viewModel.currentAlbumDetailFlow()
            .catch {
                value = NetAlbumDetailLoadResult.Error
            }
            .collect {
                currentAlbum = it
            }
        value = if (currentAlbum == null) {
            NetAlbumDetailLoadResult.Error
        } else {
            NetAlbumDetailLoadResult.Success(currentAlbum!!)
        }
    }
}

/** 展示于Header上的歌单信息布局
 * @param   album    专辑信息对象
 * @param   collapsingToolbarScaffoldState  折叠组合布局脚手架状态对象
 * @param   parentHeight    父布局高度
 * @param   parentWidth     父布局宽度
 *
 */
@Composable
fun AlbumDetailBlock(
    album : Album,
    collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState,
    parentHeight: Int,
    parentWidth: Int
) {
    Box(
        modifier = Modifier
            .width(parentWidth.dp)
            .heightIn(min = parentHeight.dp, max = 480.dp)
            .padding(vertical = 16.dp, horizontal = 20.dp)
            .graphicsLayer {
                translationY -= 3.toFloat() * parentHeight * (1 - collapsingToolbarScaffoldState.toolbarState.progress)
            }
            .statusBarsPadding()
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (coverImg, albumName, artistName, textDescription) = createRefs()
            Text(
                text = album.name,
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(albumName) {
                        top.linkTo(parent.top, margin = 10.dp)
                        start.linkTo(parent.start, margin = 26.dp)
                        end.linkTo(parent.end, margin = 26.dp)
                        width = Dimension.fillToConstraints
                    }
            )
            Text(
                text = album.artist.name,
                style = MaterialTheme.typography.body2.copy(
                    color = Color.White.copy(alpha = .5f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(artistName) {
                        top.linkTo(albumName.bottom)
                        start.linkTo(parent.start, margin = 32.dp)
                        end.linkTo(parent.end, margin = 32.dp)
                        width = Dimension.fillToConstraints
                    }
                    .padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 8.dp)
            )
            Card(
                modifier = Modifier
                    .height(240.dp)
                    .width(240.dp)
                    .constrainAs(coverImg) {
                        start.linkTo(parent.start)
                        top.linkTo(artistName.bottom)
                        end.linkTo(parent.end)
                    },
                shape = RoundedCornerShape(10.dp),
                elevation = 4.dp
            ) {
                AsyncImage(
                    model = album.picUrl,
                    contentDescription = album.name,
                    placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder),
                    contentScale = ContentScale.Crop,
                )
            }
            Text(
                text = album.description,
                style = MaterialTheme.typography.body2.copy(
                    color = Color.White.copy(alpha = .4f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Start
                ),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(textDescription) {
                        start.linkTo(parent.start)
                        top.linkTo(coverImg.bottom, 16.dp)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom, margin = 14.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    }
                    .padding(start = 8.dp)
                    .scrollable(
                        rememberScrollState(),
                        Orientation.Vertical,
                        true
                    )
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewAlbumDetailBlock() {
    BeeAudioTheme {
        AlbumDetailBlock(
            album = SingleAlbum,
            collapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState(),
            parentWidth = 420,
            parentHeight = 300
        )
    }
}