package com.chen.beeaudio.screen

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.mock.SinglePlayListData
import com.chen.beeaudio.mock.SingleTrackMock
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.navigation.PersonRoute
import com.chen.beeaudio.navigation.argument.ShareType
import com.chen.beeaudio.screen.widget.*
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.ui.theme.BlueGrey700
import com.chen.beeaudio.ui.theme.Cyan300
import com.chen.beeaudio.utils.BlurTransformation
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.PlayListViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import me.onebone.toolbar.*
import java.util.*

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalToolbarApi
@Composable
fun PlayListScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    mViewModel : PlayListViewModel = hiltViewModel(),
    onPlayAllEvent: () -> Unit,
) {
    /* 上下文信息 */
    val context = LocalContext.current
    /* 父级容器宽度 */
    val pageWidth = LocalConfiguration.current.screenWidthDp
    /* 协程域 */
    val coroutineScope = rememberCoroutineScope()

    val collapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState()

    /* 状态顶栏折叠标识 */
    val enabled by remember { mutableStateOf(true) }
    /* 曲目收藏逻逻辑执行窗口弹出标识 */
    var isCollectAction by remember { mutableStateOf(false) }

    /* 升级到 Premium 窗口提示标识 */
    var isPremiumAlert by remember { mutableStateOf(false) }

    /* 屏幕宽度 */
    val configuration = LocalConfiguration.current
    val currentScreenWidth = configuration.screenWidthDp
    /* 当前歌单信息状态 */
    val playListInfoState = loadCurrentPlayListDetail(viewModel = mViewModel)
    /* 歌单标题信息状态 */
    val playListTitle = remember { mutableStateOf("Title") }
    when (playListInfoState.value) {
        is NetDetailLoadResult.Success -> {
            playListTitle.value = (playListInfoState.value as NetDetailLoadResult.Success).playList.name
        }
        else -> {
            playListTitle.value = "当前歌单"
        }
    }
    /* 当前歌单是否为我创建的自建歌单状态 */
    val isMyCreatedState = mViewModel.isMyCreated.collectAsState()

    /* 加载歌曲数据 */
    val tracksForPaging = mViewModel.currentPlayListTracks.collectAsLazyPagingItems()

    /* 歌单收藏状态 */
    val currentPlaylistSubscribeState = mViewModel.isSubscribe.collectAsState()

    /* 当前收藏逻辑选择曲目 */
    val tempCollectTrack : MutableState<Track> = remember { mutableStateOf(SingleTrackMock) }
    /* 曲目收藏逻逻辑执行窗口 */
    if (isCollectAction) {
        CollectToPlaylistDialog(
            context = context,
            targetTrack = tempCollectTrack.value,
            currentUserId = mainViewModel.currentUserId,
            myFavoritePlaylistId = mainViewModel.myFavoritePlaylistId,
            parentWidth = pageWidth,
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
        parentWidth = pageWidth,
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
                when (playListInfoState.value) {
                    is NetDetailLoadResult.Success -> {
                        val playListInfo = (playListInfoState.value as NetDetailLoadResult.Success).playList
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(if (playListInfo.coverImageUrl.contains("playlist")) {
                                    LOCAL_SERVER_URL + playListInfo.coverImageUrl
                                } else {
                                    playListInfo.coverImageUrl
                                })
                                .transformations(listOf(
                                    BlurTransformation(scale = 0.5f, radius = 25)
                                )).build(),
                            contentDescription = "Start Listen to ${playListInfo.description}",
                            placeholder = painterResource(id = R.drawable.ic_image_placeholder),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .parallax(0.5f)
                                .height(324.dp)
                                .graphicsLayer {
                                    // change alpha of Image as the toolbar expands
//                            alpha = collapsingToolbarScaffoldState.toolbarState.progress
                                },
                            colorFilter = if (isSystemInDarkTheme()) ColorFilter.tint(Color.Gray.copy(alpha = .5f), BlendMode.Darken) else null
                        )
                        PlayListDetail(
                            playListInfo = playListInfo,
                            isSubscribe = currentPlaylistSubscribeState.value,
                            collapsingToolbarScaffoldState = collapsingToolbarScaffoldState,
                            parentHeight = 324,
                            parentWidth = currentScreenWidth,
                            onCollectEvent = {
                                mViewModel.changeSubscribeState(context, mainViewModel.currentUserId)
                            }
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .height(324.dp)
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
                    when (playListInfoState.value) {
                        is NetDetailLoadResult.Success -> {
                            PlayListColumnStickyHeader(
                                playListInfo = (playListInfoState.value as NetDetailLoadResult.Success).playList,
                                isSubscribe = currentPlaylistSubscribeState.value,
                                collapsingToolbarScaffoldState = collapsingToolbarScaffoldState,
                                onCollectEvent = { mViewModel.changeSubscribeState(context = context, currentUserId = mainViewModel.currentUserId) }
                            )
                        } else -> {
                            /* 未获得正确加载的歌单 歌曲列表吸顶栏 */
                            PlayListColumnStickyHeader(
                                playListInfo = SinglePlayListData,
                                isSubscribe = false,
                                collapsingToolbarScaffoldState = collapsingToolbarScaffoldState
                            ) {  }
                        }
                    }
                }
                items(
                    items = tracksForPaging,
                    key = { track ->
                        track.id
                    }
                ) { track ->
                    track?.let { item ->
                        SongTrackShowItemWidget(
                            track = item,
                            currentPlayingTrackId = mainViewModel.currentPlayingMusicItem.value?.musicId?.toLong(),
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
                                navController.navigate(route = BlogRoute.SendScreen.route + "?share_item_out=${Uri.encode(Gson().toJson(shareTypeArgument))}")
                            }
                        )
                    }
                }
                if (tracksForPaging.loadState.refresh is LoadState.Loading) {
                    item {
                        for (i in 0 until 3) {
                            LoadingSongTrackShowItemWidget_1()
                            LoadingSongTrackShowItemWidget_2()
                        }
                    }
                } else if (tracksForPaging.loadState.refresh is LoadState.Error) {
//                    item {
//                        ErrorDataTipsWidget(
//                            text = "数据获取出错，请退出该页重试",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 40.dp)
//                        )
//                    }
                    item {
                        EmptyDataBlock(
                            tipText = "这个歌单，空空如也 ～"
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(color = Color.Transparent)
        ) {
            PlayListHeaderBar(
                isMyCreated = isMyCreatedState.value,
                playListName = playListTitle.value,
                collapsingToolbarScaffoldState = collapsingToolbarScaffoldState,
                onBackEvent = { navController.navigateUp() },
                onPlayAllSongs = {
                    if (playListInfoState.value is NetDetailLoadResult.Success) {
                        try {

                            val trackIds = (playListInfoState.value as NetDetailLoadResult.Success).playList.trackIds?.map { it.id }
                            if (trackIds!!.isNotEmpty()) {
                                if (mainViewModel.startPlayListPlaying(trackIds = trackIds)) {
                                    Toast.makeText(context, "歌单播放准备中...", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "这个歌单当前正在播放哦", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "这个歌单，空空如也～", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e : Throwable) {
                            if (e is NullPointerException) {
                                Toast.makeText(context, "这个歌单，空空如也～", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "未知错误(15)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                onSharePlayList = {
                    if (playListInfoState.value is NetDetailLoadResult.Success) {
                        val shareTypeArgument = ShareType("[share_playlist]" + Gson().toJson((playListInfoState.value as NetDetailLoadResult.Success).playList))
                        navController.navigate(route = BlogRoute.SendScreen.route + "?share_item_out=${Uri.encode(Gson().toJson(shareTypeArgument))}")
                    }
                },
                onEditPlaylist = {
                    /* 在当前歌单ID有效的情况下打开歌单编辑页面 */
                    if (playListInfoState.value is NetDetailLoadResult.Success) {
                        val playlistId = (playListInfoState.value as NetDetailLoadResult.Success).playList.id
                        navController.navigate(route = PersonRoute.EditPlayListScreen.route + "?playlistId=${playlistId}")
                    }
                }
            )
        }
    }
}

/* 当前歌单页面 - 当前歌单详细信息请求结果状态 */
sealed class NetDetailLoadResult<T> {
    object Loading : NetDetailLoadResult<PlayList>()
    object Error : NetDetailLoadResult<PlayList>()
    data class Success(val playList: PlayList) : NetDetailLoadResult<PlayList>()
}

/** 使用produceState将当前歌单信息数据Flow转换为State
 *  @param  viewModel   当前PlayListScreen 的ViewModel
 */
@Composable
fun loadCurrentPlayListDetail(
    viewModel: PlayListViewModel
) : State<NetDetailLoadResult<PlayList>> {
    return produceState(initialValue = NetDetailLoadResult.Loading as NetDetailLoadResult<PlayList>, viewModel) {
        var currentPlaylist : PlayList? = null
        viewModel.currentPlayListDetailFlow()
            .catch {
                value = NetDetailLoadResult.Error
            }
            .collect {
                currentPlaylist = it
            }
        value = if (currentPlaylist == null) {
            NetDetailLoadResult.Error
        } else {
            NetDetailLoadResult.Success(currentPlaylist!!)
        }
    }
}

/** 歌单内容页 折叠标题栏
 *  @param  playListName    歌单标题信息字符串
 *  @param  collapsingToolbarScaffoldState  折叠组合布局脚手架状态对象
 *  @param  onBackEvent 返回事件响应
 *  @param  onPlayAllSongs  将所有歌曲加入播放队列
 *  @param  onSharePlayList 分享该歌单
 */
@Composable
fun PlayListHeaderBar(
    isMyCreated: Boolean,
    playListName: String,
    collapsingToolbarScaffoldState : CollapsingToolbarScaffoldState,
    onBackEvent: () -> Unit,
    onPlayAllSongs : () -> Unit,
    onSharePlayList: () -> Unit,
    onEditPlaylist: () -> Unit,
) {
    /* 更多信息菜单弹出 状态变量 */
    val menuExpanded = remember { mutableStateOf(false) }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
            .statusBarsPadding()
    ) {
        val (backArrowButton, playListHeader, playAllButton, MoreButton) = createRefs()
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
                text = playListName,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Left,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        /* 播放全部 */
        IconButton(
            modifier = Modifier
                .constrainAs(playAllButton) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(MoreButton.start)
                }
                .background(color = MaterialTheme.colors.surface.copy(alpha = 0f)),
            onClick = { onPlayAllSongs() },
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play All Song",
                modifier = Modifier
                    .size(24.dp)
            )
        }
        if (isMyCreated) {
            IconButton(
                modifier = Modifier
                    .constrainAs(MoreButton) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 12.dp)
                    }
                    .background(color = MaterialTheme.colors.surface.copy(alpha = 0f)),
                onClick = { menuExpanded.value = true },    // 点击展开更多菜单
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More function for the playlist",
                    modifier = Modifier
                        .size(24.dp)
                )
                /* 更多菜单 */
                DropdownMenu(
                    expanded = menuExpanded.value,
                    onDismissRequest = { menuExpanded.value = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onEditPlaylist()
                            menuExpanded.value = false
                        }
                    ) {
                        Text(text = "编辑歌单信息")
                    }
                    DropdownMenuItem(
                        onClick = {
                            onSharePlayList()
                            menuExpanded.value = false
                        }
                    ) {
                        Text(text = "分享当前歌单")
                    }
                }
            }
        } else {
            /* 分享歌单 */
            IconButton(
                modifier = Modifier
                    .constrainAs(MoreButton) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 12.dp)
                    }
                    .background(color = MaterialTheme.colors.surface.copy(alpha = 0f)),
                onClick = { onSharePlayList() }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "share the playlist",
                    modifier = Modifier
                        .size(20.dp)
                )
            }
        }
    }
}

/** 展示于Header上的歌单信息布局
 * @param   playListInfo    歌单信息对象
 * @param   isSubscribe     收藏订阅表示
 * @param   collapsingToolbarScaffoldState  折叠组合布局脚手架状态对象
 * @param   parentHeight    父布局高度
 * @param   parentWidth     父布局宽度
 * @param   onCollectEvent  收藏操作事件
 *
 */
@Composable
fun PlayListDetail(
    playListInfo: PlayList,
    isSubscribe: Boolean,
    collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState,
    parentHeight: Int,
    parentWidth: Int,
    onCollectEvent: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(parentHeight.dp)
            .width(parentWidth.dp)
            .padding(vertical = 24.dp, horizontal = 24.dp)
            .graphicsLayer {
                translationY -= 3.toFloat() * parentHeight * (1 - collapsingToolbarScaffoldState.toolbarState.progress)
            }
            .statusBarsPadding()
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (coverImg, textTitle, textCreator, textDescription, collectButton) = createRefs()
            val startGuideline = createGuidelineFromStart(.4f)
            Card(
                modifier = Modifier
                    .height(126.dp)
                    .width(126.dp)
                    .constrainAs(coverImg) {
                        top.linkTo(parent.top, margin = 28.dp)
                        start.linkTo(parent.start)
                        end.linkTo(startGuideline)
                    },
                shape = RoundedCornerShape(10.dp),
                elevation = 2.dp
            ) {
                AsyncImage(
                    model = if (playListInfo.coverImageUrl.contains("playlist")) {
                        LOCAL_SERVER_URL + playListInfo.coverImageUrl
                    } else {
                        playListInfo.coverImageUrl
                    },
                    contentDescription = playListInfo.description,
                    placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder),
                    contentScale = ContentScale.Crop,

                )
            }
            Text(
                text = playListInfo.name,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(textTitle) {
                        top.linkTo(parent.top, margin = 8.dp)
                        start.linkTo(startGuideline)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .padding(start = 6.dp, top = 18.dp, end = 6.dp, bottom = 2.dp)
            )
            Row(
                modifier = Modifier
                    .constrainAs(textCreator) {
                        top.linkTo(textTitle.bottom)
                        start.linkTo(startGuideline)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = if (playListInfo.creator.avatarUrl.contains("avatar")) LOCAL_SERVER_URL + playListInfo.creator.avatarUrl else playListInfo.creator.avatarUrl,
                    contentDescription = "Creator avatar",
                    placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = playListInfo.creator.nickName,
                    style = MaterialTheme.typography.body2.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = .7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Start,
                        textDecoration = TextDecoration.Underline
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = playListInfo.description,
                style = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = .4f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Start
                ),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(textDescription) {
                        start.linkTo(startGuideline)
                        top.linkTo(textCreator.bottom)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom, margin = 44.dp)
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

            if (!isSubscribe) {
                TextButton(
                    onClick = { onCollectEvent() },
                    modifier = Modifier
                        .constrainAs(collectButton) {
                            start.linkTo(startGuideline)
                            bottom.linkTo(parent.bottom, margin = 4.dp)
                        }
                        .clip(CircleShape)
                        .padding(horizontal = 6.dp),
                    colors = if (isSystemInDarkTheme()) ButtonDefaults.buttonColors(backgroundColor = Color.White) else ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_collect_playlist),
                            contentDescription = "Collect This PlayList",
                            modifier = Modifier.size(20.dp),
                            tint = if (isSystemInDarkTheme()) Color.Black else MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "加入收藏",
                            style = MaterialTheme.typography.body1.copy(
                                color = if (isSystemInDarkTheme()) Color.Black else MaterialTheme.colors.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                TextButton(
                    onClick = { onCollectEvent() },
                    modifier = Modifier
                        .constrainAs(collectButton) {
                            start.linkTo(startGuideline)
                            bottom.linkTo(parent.bottom, margin = 4.dp)
                        }
                        .clip(CircleShape)
                        .padding(horizontal = 6.dp),
                    border = BorderStroke(2.dp, if (isSystemInDarkTheme()) Color.White else BlueGrey700)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_collect_playlist_confirm),
                            contentDescription = "Cancel Collect This PlayList",
                            modifier = Modifier.size(20.dp),
                            tint = if (isSystemInDarkTheme()) Color.White else BlueGrey700
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "在收藏中",
                            style = MaterialTheme.typography.body1.copy(
                                color = if (isSystemInDarkTheme()) Color.White else BlueGrey700,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

/** 歌单列表吸顶栏目
 *  @param  playListInfo    歌单信息对象
 *  @param  collapsingToolbarScaffoldState  折叠顶栏框架状态对象
 *  @param  onCollectEvent  点击收藏事件
 *
 */
@Composable
fun PlayListColumnStickyHeader(
    playListInfo: PlayList,
    isSubscribe: Boolean,
    collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState,
    onCollectEvent: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        elevation = 1.dp
    ) {
        /* 副栏收藏按钮显示并可用状态 */
        val collectButtonEnable = rememberUpdatedState(newValue = collapsingToolbarScaffoldState.toolbarState.progress <= 0.1)

        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (countTitle, subCollectButton) = createRefs()
            Text(
                text = "共 ${playListInfo.trackIds?.size} 首歌曲",
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
            if (isSubscribe) {
                IconButton(
                    modifier = Modifier.constrainAs(subCollectButton){
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 12.dp)
                    },
                    enabled = collectButtonEnable.value,
                    onClick = { onCollectEvent() }
                ) {
                    AnimatedVisibility(
                        visible = collectButtonEnable.value,
                        enter = fadeIn(tween(500)) + expandVertically(),
                        exit = fadeOut(tween(500)) + shrinkVertically(),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_collect_playlist_confirm),
                            contentDescription = "Collect This PlayList",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colors.onSurface.copy(.7f)
                        )
                    }
                }
            } else {
                IconButton(
                    modifier = Modifier.constrainAs(subCollectButton){
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 12.dp)
                    },
                    enabled = collectButtonEnable.value,
                    onClick = { onCollectEvent() }
                ) {
                    AnimatedVisibility(
                        visible = collectButtonEnable.value,
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
@Preview(showBackground = true)
fun PreviewPlayListDetail() {
    BeeAudioTheme {
        val configuration = LocalConfiguration.current
        /* 屏幕宽度 */
        val currentScreenWidth = configuration.screenWidthDp
        PlayListDetail(
            playListInfo = SinglePlayListData,
            isSubscribe = false,
            collapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState(),
            parentHeight = 300,
            parentWidth = currentScreenWidth
        ) {  }
    }
}