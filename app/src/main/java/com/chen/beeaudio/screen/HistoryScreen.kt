package com.chen.beeaudio.screen

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.chen.beeaudio.R
import com.chen.beeaudio.mock.SingleTrackMock
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.navigation.argument.ShareType
import com.chen.beeaudio.screen.widget.CollectToPlaylistDialog
import com.chen.beeaudio.screen.widget.PremiumRecommendDialog
import com.chen.beeaudio.screen.widget.SongTrackShowItemWidget
import com.chen.beeaudio.viewmodel.HistoryViewModel
import com.chen.beeaudio.viewmodel.MainViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 历史播放记录 页面
 *
 */
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun HistoryScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    /* 父级容器宽度 */
    val pageWidth = LocalConfiguration.current.screenWidthDp
    /* 协程域 */
    val coroutineScope = rememberCoroutineScope()

    /* 历史播放记录分页数据 */
    val historyPagingData = historyViewModel.historyDataCollections.collectAsLazyPagingItems()

    /* 下拉刷新组件状态 */
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    /* 懒加载列表状态 */
    val lazyListState = rememberLazyListState()

    /* 清空播放历史记录 提示弹窗状态 */
    val clearHistoryDialogState = remember { mutableStateOf(false) }

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

    ClearHistoryDialog(
        isClearAlert = clearHistoryDialogState,
        onConfirmEvent = {
            historyViewModel.clearMyPlayHistory(
                context = context,
                onAfterClear = {
                    navController.navigateUp()
                }
            )
        }
    )

    Scaffold(
        topBar = {
            HistoryTopAppBar(
                onBackUpEvent = { navController.navigateUp() },
                onClearEvent = { clearHistoryDialogState.value = true }
            )
        }
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                historyViewModel.loadHistoryData()
            },
            modifier = Modifier
                .background(color = Color.Transparent)
                .padding(paddingValues = it)
        ) {
            swipeRefreshState.isRefreshing =
                historyPagingData.loadState.refresh is LoadState.Loading

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                /* 历史记录曲目子项 */
                items(
                    items = historyPagingData,
                    key = { data ->
                        data.songInfo.id
                    }
                ) { metaHistoryData ->
                    metaHistoryData?.let { item ->
                        val song = item.songInfo
                        SongTrackShowItemWidget(
                            track = song,
                            modifier = Modifier.animateItemPlacement(),
                            currentPlayingTrackId = mainViewModel.currentPlayingMusicItem.value?.musicId?.toLong(),
                            onSingleSongPlayEvent = {
                                if (!song.usable) {
                                    Toast.makeText(context, "十分抱歉！由于各方原因，该曲目现已下架", Toast.LENGTH_SHORT).show()
                                } else if (song.privilegeSignal == 1) {
                                    if (mainViewModel.currentUserDetailInfo.value.user_type == 1) {
                                        mainViewModel.playTargetAudio(track = song, context = context)
                                    } else {
                                        isPremiumAlert = true
                                    }
                                } else {
                                    mainViewModel.playTargetAudio(track = song, context = context)
                                }
                            },
                            onAppendIntoPlayList = { mainViewModel.appendTargetMusic(track = song, context = context) },
                            onSingleSongCollectEvent = {
                                tempCollectTrack.value = item.songInfo
                                isCollectAction = true
                            },
                            onSingleSongShareEvent = {
                                val shareTypeArgument = ShareType("[share_track]" + Gson().toJson(song))
                                navController.navigate(route = BlogRoute.SendScreen.route + "?share_item_out=${
                                    Uri.encode(
                                        Gson().toJson(shareTypeArgument))}")
                            }
                        )
                    }
                }

                historyPagingData.apply {
                    when {
                        loadState.append is LoadState.Loading -> {
                            item {
                                LoadingItem()
                            }
                        }
                        loadState.append is LoadState.Error -> {
                            item {
                                NoMoreDataBlock()
                            }
                        }
                        loadState.append == LoadState.NotLoading(endOfPaginationReached = true) -> {
                            item {
                                NoMoreDataBlock()
                            }
                        }
                        loadState.refresh is LoadState.Error -> {
                            if (historyPagingData.itemCount <= 0) {
                                // 刷新的时候， 如果itemCount小于0, 第一次加载异常
                                item {
                                    NoMoreDataBlock()
                                }
                            } else {
                                item {
                                    NoMoreDataBlock()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 历史播放记录顶栏
 *  @param  onBackUpEvent   返回事件
 *  @param  onClearEvent    清空历史记录事件
 */
@Composable
fun HistoryTopAppBar(
    onBackUpEvent : () -> Unit,
    onClearEvent : () -> Unit 
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
            .statusBarsPadding(),
        backgroundColor = MaterialTheme.colors.surface,
        title = { Text(text = "播放历史") },
        navigationIcon = {
            IconButton(
                onClick = { onBackUpEvent() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回上一页",
                    modifier = Modifier
                        .size(28.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = { onClearEvent() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clear_history),
                    contentDescription = "Clear Play History Data",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

/**
 *  清空列表对话框
 *  @param  isClearAlert    警告对话窗口打开状态
 *  @param  onConfirmEvent  确认按钮事件
 */
@Composable
fun ClearHistoryDialog(
    isClearAlert: MutableState<Boolean>,
    onConfirmEvent: () -> Unit
) {
    if (isClearAlert.value) {
        AlertDialog(
            title = { Text(text = "清空播放历史") },
            text = { Text(text = "确定要清空您的历史播放记录吗？") },
            onDismissRequest = { isClearAlert.value = false },
            dismissButton = {
                TextButton(
                    onClick = {
                        isClearAlert.value = false
                    }
                ) {
                    Text("取消")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmEvent()
                        isClearAlert.value = false
                    }
                ) {
                    Text("确认")
                }
            },
        )
    }
}

@Composable
fun NoMoreDataBlock() {
    Text(
        text = "找不到更多历史数据了哦～",
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp)
    )
}