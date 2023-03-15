package com.chen.beeaudio.screen

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadType
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.chen.beeaudio.mock.SingleTrackMock
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.audio.Artist
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.navigation.AudioHomeRoute
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.navigation.argument.ShareType
import com.chen.beeaudio.screen.widget.*
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.SearchResultViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@Composable
fun SearchResultScreen(
    keyWords : String,
    navController: NavController,
    mainViewModel: MainViewModel,
    mViewModel : SearchResultViewModel = hiltViewModel(),
    onOpenPlayListPage : (Long) -> Unit,
) {
    /* 上下文信息 */
    val context = LocalContext.current
    /* 父级容器宽度 */
    val pageWidth = LocalConfiguration.current.screenWidthDp

    /* 搜索结果数据 */
    val searchSongsForPaging = mViewModel.resultSongs.collectAsLazyPagingItems()
    val searchAlbumsForPaging = mViewModel.resultAlbums.collectAsLazyPagingItems()
    val searchArtistsForPaging = mViewModel.resultArtists.collectAsLazyPagingItems()
    val searchPlayListsForPaging = mViewModel.resultPlayList.collectAsLazyPagingItems()

    /* scaffold脚手架状态 */
    val scaffoldState : ScaffoldState = rememberScaffoldState()

    /* TabRow 顶栏状态 */
    val pagerState = rememberPagerState()

    /* 协程域 */
    val coroutineScope = rememberCoroutineScope()

    /* 曲目收藏逻逻辑执行窗口弹出标识 */
    var isCollectAction by remember { mutableStateOf(false) }

    /* 升级到 Premium 窗口提示标识 */
    var isPremiumAlert by remember { mutableStateOf(false) }

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

    /* TabRow 标题列表 */
    val pagerTitle = listOf("单曲", "专辑", "艺人", "歌单")
    /* 将关键字初始化于ViewModel中 */
    mViewModel.changeCurrentKeyWords(keyWords)
    Scaffold(
        topBar = {
            SearchWidget(
                text = mViewModel.currentKeyWords.collectAsState().value,
                onTextChange = {
                    mViewModel.changeCurrentKeyWords(it)
                },
                onSearchClicked = {
                    if (it == "" || it.isEmpty() || it.isBlank()) {
                        Toast.makeText(context, "\uD83D\uDE15 搜索框没有一点关键字呢", Toast.LENGTH_SHORT).show()
                    } else {
                        mViewModel.apply {
                            when(pagerState.currentPage) {
                                0 -> { loadResultSongs() }
                                1 -> { loadResultAlbums() }
                                2 -> { loadResultArtists() }
                                3 -> { loadResultPlayList() }
                            }
                        }
                    }
                },
                onCloseClicked = { navController.navigateUp() }
            )
        },
        scaffoldState = scaffoldState
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            TabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    PagerTabIndicator(tabPositions = tabPositions, pagerState = pagerState)
                },
                backgroundColor = MaterialTheme.colors.surface
            ) {
                pagerTitle.forEachIndexed { index, title ->
                    PagerTab(
                        pagerState = pagerState,
                        index = index,
                        pageCount = pagerTitle.size,
                        text = title,
                        modifier = Modifier
                            .height(50.dp)
                            .clickable {
                                coroutineScope.launch {
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
                        mViewModel.loadResultSongs()
                        if (searchSongsForPaging.itemCount == 0) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(top = 80.dp)
                            )
                        } else {
                            /* 单曲搜索结果 */
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = MaterialTheme.colors.surface)
                            ) {
                                items(
                                    items = searchSongsForPaging,
                                    key = { item: Track ->
                                        item.id
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
                    1 -> {
                        mViewModel.loadResultAlbums()
                        if (searchAlbumsForPaging.itemCount == 0) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(top = 80.dp)
                            )
                        } else {
                            /* 专辑搜索结果 */
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = MaterialTheme.colors.surface)
                            ) {
                                items(
                                    items = searchAlbumsForPaging,
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
                    2 -> {
                        mViewModel.loadResultArtists()
                        if (searchArtistsForPaging.itemCount == 0) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(top = 80.dp)
                            )
                        } else {
                            /* 艺人搜索结果 */
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = MaterialTheme.colors.surface)
                            ) {
                                items(
                                    items = searchArtistsForPaging,
                                    key = { item: Artist ->
                                        item.id
                                    }
                                ) { artist ->
                                    artist?.let { item ->
                                        ArtistShowItemWidget(
                                            artist = item,
                                            imageLoader = mViewModel.myImageLoader,
                                            onOpenArtistDetailPage = { artistId ->
                                                navController.navigate(
                                                    route = AudioHomeRoute.ArtistScreen.route + "?artist_id=$artistId"
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        mViewModel.loadResultPlayList()
                        if (searchPlayListsForPaging.itemCount == 0) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(top = 80.dp)
                            )
                        } else {
                            /* 歌单搜索结果 */
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = MaterialTheme.colors.surface)
                            ) {
                                items(
                                    items = searchPlayListsForPaging,
                                    key = { item: PlayList ->
                                        item.id
                                    }
                                ) { playlist ->
                                    playlist?.let { item ->
                                        PlayListShowItemWidget(
                                            playList = item,
                                            imageLoader = mViewModel.myImageLoader,
                                            onOpenPlayListDetailPage = onOpenPlayListPage
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

/**
 * PagerTap 指示器
 * @param  percent  指示器占用整个tab宽度的比例
 * @param  height   指示器的高度
 * @param  color    指示器的颜色
 */
@ExperimentalPagerApi
@Composable
fun PagerTabIndicator(
    tabPositions: List<TabPosition>,
    pagerState: PagerState,
    color: Color = if (isSystemInDarkTheme()) MaterialTheme.colors.primary else MaterialTheme.colors.primarySurface,
    @FloatRange(from = 0.0, to = 1.0) percent: Float = 0.4f,
    height: Dp = 4.dp,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val currentPage = minOf(tabPositions.lastIndex, pagerState.currentPage)
        val currentTab = tabPositions[currentPage]
        val previousTab = tabPositions.getOrNull(currentPage - 1)
        val nextTab = tabPositions.getOrNull(currentPage + 1)
        val fraction = pagerState.currentPageOffset

        val indicatorWidth = currentTab.width.toPx() * percent

        val indicatorOffset = if (fraction > 0 && nextTab != null) {
            lerp(currentTab.left, nextTab.left, fraction).toPx()
        } else if (fraction < 0 && previousTab != null) {
            lerp(currentTab.left, previousTab.left, -fraction).toPx()
        } else {
            currentTab.left.toPx()
        }

        /*Log.i(
            "hj",
            "fraction = ${fraction} , indicatorOffset = ${indicatorOffset}"
        )*/
        val canvasHeight = size.height
        drawRoundRect(
            color = color,
            topLeft = Offset(
                indicatorOffset + (currentTab.width.toPx() * (1 - percent) / 2),
                canvasHeight - height.toPx()
            ),
            size = Size(indicatorWidth + indicatorWidth * abs(fraction), height.toPx()),
            cornerRadius = CornerRadius(50f)
        )
    }
}

/**
 * 自定义 PagerTab
 * @param index                     对应第几个tab 从0开始
 * @param pageCount                 page的总个数
 * @param selectedContentColor      tab选中时的颜色
 * @param unselectedContentColor    tab没选中时的颜色
 * @param selectedFontSize          tab选中时的文字大小
 * @param unselectedFontSize        tab没选中时的文字大小
 * @param selectedFontWeight        tab选中时的文字比重
 * @param unselectedFontWeight      tab没选中时的文字比重
 */
@ExperimentalPagerApi
@Composable
fun PagerTab(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    index: Int,
    pageCount: Int,
    text: String,
    selectedContentColor: Color = MaterialTheme.colors.primary,
    unselectedContentColor: Color = MaterialTheme.colors.onSurface,
    selectedFontSize: TextUnit = 18.sp,
    unselectedFontSize: TextUnit = 15.sp,
    selectedFontWeight: FontWeight = FontWeight.Bold,
    unselectedFontWeight: FontWeight = FontWeight.Normal,
) {
    val previousIndex = max(index - 1, 0)
    val nextIndex = min(index + 1, pageCount - 1)
    val currentIndexPlusOffset = pagerState.currentPage + pagerState.currentPageOffset

    val progress =
        if (currentIndexPlusOffset >= previousIndex && currentIndexPlusOffset <= nextIndex) {
            1f - abs(index - currentIndexPlusOffset)
        } else {
            0f
        }

    val fontSize = lerp(unselectedFontSize, selectedFontSize, progress)
    val fontWeight =
        androidx.compose.ui.text.font.lerp(unselectedFontWeight, selectedFontWeight, progress)
    val color =
        androidx.compose.ui.graphics.lerp(unselectedContentColor, selectedContentColor, progress)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = color, fontSize = fontSize, fontWeight = fontWeight)
    }
}