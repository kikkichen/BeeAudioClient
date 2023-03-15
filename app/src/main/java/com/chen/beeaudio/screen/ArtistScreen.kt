package com.chen.beeaudio.screen

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chen.beeaudio.R
import com.chen.beeaudio.mock.SingleArtistMock
import com.chen.beeaudio.mock.SingleTrackMock
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.audio.Artist
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.navigation.AudioHomeRoute
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.navigation.argument.ShareType
import com.chen.beeaudio.screen.widget.*
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.ui.theme.BlueGrey700
import com.chen.beeaudio.utils.BlurTransformation
import com.chen.beeaudio.viewmodel.ArtistViewModel
import com.chen.beeaudio.viewmodel.MainViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@Composable
fun ArtistScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    mViewModel: ArtistViewModel
) {
    /* 上下文信息 */
    val context = LocalContext.current
    /* 父级容器宽度 */
    val pageWidth = LocalConfiguration.current.screenWidthDp
    /* 协程域 */
    val coroutineScope = rememberCoroutineScope()

    val pagerTitle = listOf("单曲", "专辑")
    /* 艺人作品请求 结果数据 */
    val tracksForPaging = mViewModel.currentArtistTracks.collectAsLazyPagingItems()
    val albumsForPaging = mViewModel.currentArtistAlbums.collectAsLazyPagingItems()

    val collapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState()

    /* 状态顶栏折叠标识 */
    val enabled by remember { mutableStateOf(true) }
    /* 曲目收藏逻逻辑执行窗口弹出标识 */
    var isCollectAction by remember { mutableStateOf(false) }

    /* 屏幕宽度 */
    val configuration = LocalConfiguration.current
    val currentScreenWidth = configuration.screenWidthDp

    /* 升级到 Premium 窗口提示标识 */
    var isPremiumAlert by remember { mutableStateOf(false) }

    /* 当前艺人信息状态 */
    val artistInfoState = loadCurrentArtistDetail(viewModel = mViewModel)
    /* 艺人标题信息状态 */
    val artistTitle = remember { mutableStateOf("群星") }
    when (artistInfoState.value) {
        is NetArtistDetailResult.Success -> {
            artistTitle.value = (artistInfoState.value as NetArtistDetailResult.Success).artist.name
        }
        else -> {
            artistTitle.value = "Title"
        }
    }

    /* 歌单收藏状态 */
    val currentArtistSubscribeState = mViewModel.isSubscribe.collectAsState()
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
                        .height(72.dp)
                        .background(
                            color = MaterialTheme.colors.surface
                        )
                )
                when (artistInfoState.value) {
                    is NetArtistDetailResult.Success -> {
                        val artistInfo = (artistInfoState.value as NetArtistDetailResult.Success).artist
                        AsyncImage(
                            /* 自下到上的渐变效果 */
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(artistInfo.cover)
                                .build(),
                            contentDescription = "Start Listen to ${artistInfo.briefDesc}",
                            placeholder = painterResource(id = R.drawable.ic_image_placeholder),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .parallax(0.5f)
                                .height(420.dp)
                                .graphicsLayer {
                                    // change alpha of Image as the toolbar expands
//                                    alpha = collapsingToolbarScaffoldState.toolbarState.progress
                                },
                            colorFilter = if (isSystemInDarkTheme()) ColorFilter.tint(Color.Gray.copy(alpha = .5f), BlendMode.Darken,) else null
                        )
                        ArtistDetailBlock(
                            artist = artistInfo,
                            isSubscribe = currentArtistSubscribeState.value,
                            collapsingToolbarScaffoldState = collapsingToolbarScaffoldState,
                            parentHeight = 420,
                            parentWidth = currentScreenWidth,
                            onCollectEvent = { mViewModel.changeSubscribeState(context = context, mainViewModel.currentUserId) }
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .height(420.dp)
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
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                /* TabRow 顶栏 */
                val pagerState = rememberPagerState()
                TabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { tabPositions ->
                        PagerTabIndicator(tabPositions = tabPositions, pagerState = pagerState)
                    },
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    val scope: CoroutineScope = rememberCoroutineScope()
                    pagerTitle.forEachIndexed { index, title ->
                        PagerTab(
                            pagerState = pagerState,
                            index = index,
                            pageCount = pagerTitle.size,
                            text = title,
                            modifier = Modifier
                                .height(50.dp)
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }
                HorizontalPager(
                    count = pagerTitle.size,
                    state = pagerState
                ) { page ->
                    when(page) {
                        0 -> {
                            /* 单曲搜索结果 */
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = MaterialTheme.colors.surface)
                            ) {
                                when (tracksForPaging.loadState.refresh) {
                                    is LoadState.Loading -> {
                                        items(count = 5) {
                                            Column {
                                                LoadingSongTrackShowItemWidget_1()
                                                LoadingSongTrackShowItemWidget_2()
                                            }
                                        }
                                    }
                                    is LoadState.Error -> {
                                        item {
                                            ErrorDataTipsWidget(
                                                text = "该艺人的曲目作品数据加载出了些问题，\n请退出该页重试",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 28.dp)
                                            )
                                        }
                                    }
                                    else -> {
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
                                                                mViewModel.loadSelectedTrackDetail(context = context, track = item) { track ->
                                                                    mainViewModel.playTargetAudio(track = track, context = context)
                                                                }
                                                            } else {
                                                                isPremiumAlert = true
                                                            }
                                                        } else {
                                                            mViewModel.loadSelectedTrackDetail(context = context, track = item) { track ->
                                                                mainViewModel.playTargetAudio(track = track, context = context)
                                                            }
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
                                    }
                                }
                            }
                        }
                        1 -> {
                            /* 专辑搜索结果 */
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = MaterialTheme.colors.surface)
                            ) {
                                when (albumsForPaging.loadState.refresh) {
                                    is LoadState.Loading -> {
                                        items(count = 5) {
                                            Column {
                                                LoadingAlbumShowItemWidget_1()
                                                LoadingAlbumShowItemWidget_2()
                                            }
                                        }
                                    }
                                    is LoadState.Error -> {
                                        item {
                                            ErrorDataTipsWidget(
                                                text = "该艺人的专辑列表数据加载出了些问题，\n请退出该页重试",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 28.dp)
                                            )
                                        }
                                    }
                                    else -> {
                                        items(
                                            items = albumsForPaging,
                                            key = { item: Album ->
                                                item.id
                                            }
                                        ) { album ->
                                            album?.let { item ->
                                                AlbumShowItemWidget(
                                                    album = item,
                                                    imageLoader = mViewModel.myImageLoader,
                                                    onOpenAlbumDetailPage = { albumId ->
                                                        navController.navigate(
                                                            route = AudioHomeRoute.AlbumScreen.route + "?album_id=$albumId"
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color.Transparent)
            .statusBarsPadding()
    ) {
        ArtistHeaderBar(
            artistName = artistTitle.value,
            collapsingToolbarScaffoldState = collapsingToolbarScaffoldState,
            onBackEvent = { navController.navigateUp() },
            onMoreEvent = {
                if (artistInfoState.value is NetArtistDetailResult.Success) {
                    val artist = (artistInfoState.value as NetArtistDetailResult.Success).artist
                    val shareTypeArgument = ShareType("[share_artist]" + Gson().toJson(artist))
                    navController.navigate(route = BlogRoute.SendScreen.route + "?share_item_out=${
                        Uri.encode(
                            Gson().toJson(shareTypeArgument))}")
                }
            }
        )
    }
}

/* 当前艺人页面 - 当前艺人详细信息请求结果状态 */
sealed class NetArtistDetailResult<T> {
    object Loading : NetArtistDetailResult<Artist>()
    object Error : NetArtistDetailResult<Artist>()
    data class Success(val artist: Artist) : NetArtistDetailResult<Artist>()
}

/** 使用produceState将当前歌单信息数据Flow转换为State
 *  @param  viewModel   当前PlayListScreen 的ViewModel
 */
@Composable
fun loadCurrentArtistDetail(
    viewModel: ArtistViewModel
) : State<NetArtistDetailResult<Artist>> {
    return produceState(initialValue = NetArtistDetailResult.Loading as NetArtistDetailResult<Artist>, viewModel) {
        var currentArtist : Artist? = null
        viewModel.currentArtistDetailFlow()
            .catch {
                value = NetArtistDetailResult.Error
            }
            .collect {
                currentArtist = it
            }
        value = if (currentArtist == null) {
            NetArtistDetailResult.Error
        } else {
            NetArtistDetailResult.Success(currentArtist!!)
        }
    }
}

/** 歌单内容页 折叠标题栏
 *  @param  artistName    艺人艺名字符串
 *  @param  collapsingToolbarScaffoldState  折叠组合布局脚手架状态对象
 *  @param  onBackEvent 返回事件响应
 *  @param  onMoreEvent 更多操作点击事件
 */
@Composable
fun ArtistHeaderBar(
    artistName: String,
    collapsingToolbarScaffoldState : CollapsingToolbarScaffoldState,
    onBackEvent: () -> Unit,
    onMoreEvent: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
    ) {
        val (backArrowButton, artistHeader, moreItem) = createRefs()
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
            modifier = Modifier.constrainAs(artistHeader) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(backArrowButton.end)
                end.linkTo(moreItem.start)
                width = Dimension.fillToConstraints
            }
        ) {
            Text(
                text = artistName,
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
                .constrainAs(moreItem) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 12.dp)
                }
                .background(color = MaterialTheme.colors.surface.copy(alpha = 0f)),
            onClick = { onMoreEvent() },
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share This PlayList",
                modifier = Modifier
                    .size(20.dp)
            )
        }
    }
}

/** 艺人信息布局模块
 *  @param  artist  艺人信息对象
 *  @param   isSubscribe     收藏订阅表示
 *  @param  collapsingToolbarScaffoldState  折叠组合布局脚手架状态对象
 *  @param  parentHeight    父布局高度
 *  @param  parentWidth     父布局宽度
 *  @param  onCollectEvent  收藏操作事件
 */
@Composable
fun ArtistDetailBlock(
    artist: Artist,
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
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = .3f),
                        Color.Transparent,
                        Color.Black.copy(alpha = .4f),
                        Color.Black.copy(alpha = .8f),
                        Color.Black
                    ),
                )
            )
            .padding(vertical = 24.dp, horizontal = 24.dp)
            .graphicsLayer {
                translationY -= 2.toFloat() * parentHeight * (1 - collapsingToolbarScaffoldState.toolbarState.progress)
                alpha = collapsingToolbarScaffoldState.toolbarState.progress
            }
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (artistName, descriptionText, subscribeData) = createRefs()
            Text(
                text = artist.name,
                style = MaterialTheme.typography.h3.copy(
                    color = Color.White
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(artistName) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(subscribeData.top)
                        width = Dimension.fillToConstraints
                    }
            )
            if (!isSubscribe) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                        .constrainAs(subscribeData) {
                            start.linkTo(parent.start)
                            bottom.linkTo(descriptionText.top, margin = 6.dp)
                        }
                        .clickable { onCollectEvent() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_collect_playlist),
                        contentDescription = "Collect This PlayList",
                        modifier = Modifier.size(16.dp),
                        tint = if (isSystemInDarkTheme()) Color.Black else MaterialTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "加入收藏 ",
                        style = MaterialTheme.typography.body1.copy(
                            color = if (isSystemInDarkTheme()) Color.Black else MaterialTheme.colors.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Transparent)
                        .border(BorderStroke(2.dp, Color.White))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .constrainAs(subscribeData) {
                            start.linkTo(parent.start)
                            bottom.linkTo(descriptionText.top, margin = 6.dp)
                        }
                        .clickable { onCollectEvent() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_collect_playlist_confirm),
                        contentDescription = "Cancel Collect This PlayList",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "在收藏中 ",
                        style = MaterialTheme.typography.body1.copy(
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Text(
                text = artist.briefDesc,
                style = MaterialTheme.typography.body2.copy(
                    color = Color.White.copy(alpha = .5f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start
                ),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(descriptionText) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end, margin = 10.dp)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewArtistDetailBlock() {
    BeeAudioTheme {
        ArtistDetailBlock(
            artist = SingleArtistMock,
            isSubscribe = false,
            collapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState(),
            parentWidth = LocalConfiguration.current.screenWidthDp,
            parentHeight = 420,
        ) {}
    }
}