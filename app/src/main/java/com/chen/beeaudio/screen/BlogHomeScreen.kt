package com.chen.beeaudio.screen

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.chen.beeaudio.R
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.screen.widget.BlogItem
import com.chen.beeaudio.ui.theme.LightBlue200
import com.chen.beeaudio.ui.theme.LightBlue300
import com.chen.beeaudio.viewmodel.BlogHomeViewModel
import com.chen.beeaudio.viewmodel.MainViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@ExperimentalMaterialApi
@ExperimentalPagingApi
@ExperimentalFoundationApi
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun BlogHomeScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    viewModel: BlogHomeViewModel = hiltViewModel()
) {
    /* 上下文 */
    val context : Context = LocalContext.current

    val lazyListState = rememberLazyListState()

    val collapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState()

    val coroutineScope = rememberCoroutineScope()

    /* 状态顶栏折叠标识 */
    val enabled by remember { mutableStateOf(true) }

    Scaffold(
        content = {
            CollapsingToolbarScaffold(
                modifier = Modifier.fillMaxSize(),
                state = collapsingToolbarScaffoldState,
                scrollStrategy = ScrollStrategy.EnterAlways,
                toolbarModifier = Modifier.background(MaterialTheme.colors.surface),
                enabled = enabled,
                toolbar = {
                    /* 最低高度 */
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.dp)
                            .background(Color.Transparent)
                    )
                    /* 顶栏本体 */
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .background(Color.Transparent)
                            .padding(horizontal = 20.dp)
                            .graphicsLayer {
                                translationY -= 96.toFloat() * 2 * (1 - collapsingToolbarScaffoldState.toolbarState.progress)
                            }
                            .statusBarsPadding(),
                    ) {
                        val (title, starIcon, notificationButton, searchButton) = createRefs()
                        Text(
                            text = "动态",
                            style = TextStyle(
                                fontSize = MaterialTheme.typography.h6.fontSize,
                                color = MaterialTheme.colors.onSurface
                            ),
                            modifier = Modifier.constrainAs(title) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            }
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_hoshi_sora),
                            contentDescription = null,
                            modifier = Modifier
                                .constrainAs(starIcon) {
                                    start.linkTo(parent.start)
                                    top.linkTo(parent.top)
                                    end.linkTo(parent.end)
                                    bottom.linkTo(parent.bottom)
                                }
                                .size(36.dp)
                                .clickable { coroutineScope.launch { lazyListState.animateScrollToItem(0) } },
                            tint = LightBlue300
                        )
                        IconButton(
                            onClick = {
                                /* TODO */
                            },
                            modifier = Modifier.constrainAs(notificationButton) {
                                top.linkTo(parent.top)
                                end.linkTo(searchButton.start)
                                bottom.linkTo(parent.bottom)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "通知"
                            )
                        }
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    route = BlogRoute.BlogSearch.route
                                )
                            },
                            modifier = Modifier.constrainAs(searchButton) {
                                top.linkTo(parent.top)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "进入博文动态搜索页"
                            )
                        }
                    }
                }
            ) {
                RefreshCardListScreen(
                    navController = navController,
                    lazyListState = lazyListState,
                    viewModel = viewModel,
                    onPlayEvent = { track ->
                        mainViewModel.playTargetAudio(track = track, context = context)
                    }
                )
            }
//                BlogHomeTopBar(navController = navController, lazyListState = lazyListState)
        },
        floatingActionButton = {
            HomeFloatingActionButton (
                extended = lazyListState.isScrollingUp(),
                onClick = {
                    navController.navigate(
                        /* 打开博文动态编辑页 */
                        route = BlogRoute.SendScreen.route
                    )
                }
            )
        }
    )
}

/**
 *  悬浮按钮
 *  @param  extended    展开状态
 *  @param  onClick     按钮触发时间 - 进入“发布动态页”
 */
@Composable
private fun HomeFloatingActionButton(
    extended: Boolean,
    onClick: () -> Unit
) {
    FloatingActionButton(
        backgroundColor = LightBlue200,
        onClick = onClick,
        modifier = Modifier.padding(bottom = 50.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = Color.Black
            )
            // 此处不使用 AnimatedVisibility 会影响 浮动按钮的动画效果
            AnimatedVisibility(visible = extended) {
                Text(
                    text = "写动态",
                    color = Color.Black.copy(alpha = .8f),
                    modifier = Modifier
                        .padding(start = 8.dp, top = 3.dp)
                )
            }
        }
    }
}

/**
 *  刷新滚动列表承载
 *  @param  navController   导航控制器
 *  @param  lazyListState   LazyColumn状态
 *  @param  viewModel       viewModel
 */
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalPagingApi
@Composable
fun RefreshCardListScreen(
    navController: NavController,
    lazyListState: LazyListState,
    viewModel: BlogHomeViewModel,
    onPlayEvent : (Track) -> Unit,
) {
    val collectAsLazyPagingDataItems = viewModel.loadBlogList.collectAsLazyPagingItems()

    SwipeRefreshList(
        lazyListState = lazyListState,
        collectAsLazyPagingItems = collectAsLazyPagingDataItems,
    ) {
        items(
            items = collectAsLazyPagingDataItems,
            key = { it.Id }
        ) { data ->
            BlogItem(
                navController = navController,
                blogData = data!!,
                onPlayEvent = onPlayEvent
            )
        }
    }
}

/**
 *  滑动列表LazyColumn内容
 *  @param  lazyListState   LazyColumn状态
 *  @param  collectAsLazyPagingItems    Paging响应数据状态体
 *  @param  listContent     LazyColumn列表内容
 */
@Composable
fun <T: Any> SwipeRefreshList(
    lazyListState: LazyListState,
    collectAsLazyPagingItems : LazyPagingItems<T>,
    listContent : LazyListScope.() -> Unit
) {
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    SwipeRefresh(
        state = rememberSwipeRefreshState,
        onRefresh = { collectAsLazyPagingItems.refresh() },
        modifier = Modifier
            .background(color = Color.Transparent)
    ) {

        rememberSwipeRefreshState.isRefreshing =
            collectAsLazyPagingItems.loadState.refresh is LoadState.Loading

        // 定义一个协程作用域用来跳转到列表顶部
        val coroutineScope = rememberCoroutineScope()

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            // 具体的列表内容， 从父节点参数传入
            listContent()

            collectAsLazyPagingItems.apply {
                when {
                    loadState.append is LoadState.Loading -> {
                        // 加载更多， 底部loading
                        item {
                            LoadingItem()
                        }
                    }

                    loadState.append is LoadState.Error -> {
                        // 加载更多异常
                        item {
                            ErrorMoreRetryItem {
                                collectAsLazyPagingItems.retry()
                            }
                        }
                    }

                    loadState.append == LoadState.NotLoading(endOfPaginationReached = true) -> {
                        // 已经没有更多数据了
                        item {
                            NoMoreDataFindItem(onClick = {
                                // 点击事件 跳到列表顶部
                                coroutineScope.launch {
                                    lazyListState.animateScrollToItem(0)
                                }
                            })
                        }
                    }

                    loadState.refresh is LoadState.Error -> {
                        if (collectAsLazyPagingItems.itemCount <= 0) {
                            // 刷新的时候， 如果itemCount小于0, 第一次加载异常
                            item {
                                ErrorContent {
                                    collectAsLazyPagingItems.retry()
                                }
                            }
                        } else {
                            item {
                                ErrorMoreRetryItem {
                                    collectAsLazyPagingItems.retry()
                                }
                            }
                        }
                    }

                    loadState.refresh is LoadState.Loading -> {
                        // 第一次加载 且正在加载中
//                        if (collectAsLazyPagingItems.itemCount == 0) {
//
//                        }
                    }
                }
            }
        }
    }
}

/**
 *   底部加载更多失败处理
 */
@Composable
fun ErrorMoreRetryItem(retry: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextButton(
            onClick = { retry() },
            modifier = Modifier
                .padding(20.dp)
                .width(80.dp)
                .height(30.dp),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(3.dp),
            colors = ButtonDefaults.textButtonColors(backgroundColor = Color(0xFF90A4AE)),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(text = "请重试", color = Color(0xFF546E7A))
        }
    }
}

/**
 *   页面加载失败处理
 */
@Composable
fun ErrorContent(retry: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 100.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.padding(top = 80.dp),
            painter = painterResource(id = com.chen.beeaudio.R.drawable.ic_baseline_error_24),
            contentDescription = null)
        Text(text = "请求失败，请检查网络", modifier = Modifier.padding(8.dp))
        TextButton(
            onClick = { retry() },
            modifier = Modifier
                .padding(20.dp)
                .width(80.dp)
                .height(30.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(5.dp),
            colors = ButtonDefaults.textButtonColors(backgroundColor = Color(0xFF90A4AE)),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(text = "重试", color = Color(0xFF455A64))
        }
    }
}

/**
 *   底部加载更多
 */
@Composable
fun LoadingItem() {
    Row(
        modifier = Modifier
            .height(34.dp)
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Color(0xFF546E7A),
            strokeWidth = 2.dp
        )
        Text(
            text = "加载中",
            color = Color(0xFF546E7A),
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 20.dp),
            fontSize = 18.sp
        )
    }
}

/**
 *   没有更多数据了
 */
@Composable
fun NoMoreDataFindItem(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextButton(
            onClick = {onClick()},
            modifier = Modifier
                .padding(20.dp)
                .width(80.dp)
                .height(30.dp),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(3.dp),
            colors = ButtonDefaults.textButtonColors(backgroundColor = Color(0xFF90A4AE)),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(text = "已经没有更多数据啦 ～～ Click to top", color = Color(0xFF90A4AE))
        }
    }
}

/**
 *  上滑判断
 */
@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}
