package com.chen.beeaudio

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.mock.SinglePlayListData
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.navigation.AudioHomeRoute
import com.chen.beeaudio.navigation.AuthRoute
import com.chen.beeaudio.navigation.BottomBarRoute
import com.chen.beeaudio.navigation.PersonRoute
import com.chen.beeaudio.screen.widget.HotRecommendPlayListItem
import com.chen.beeaudio.screen.widget.HotRecommendPlayListItemLoadState
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.ui.theme.Cyan200
import com.chen.beeaudio.utils.BlurTransformation
import com.chen.beeaudio.viewmodel.AudioHomeViewModel
import com.chen.beeaudio.viewmodel.HotPlayListDataState
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.TokenAging
import kotlinx.coroutines.flow.catch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalFoundationApi
@Composable
fun AudioHome(
    navController: NavController,
    mViewModel: AudioHomeViewModel = hiltViewModel(),
    mainViewModel: MainViewModel,
    onOpenPlayListPage : (Long) -> Unit,
    onOpenSearchPage : () -> Unit,
) {
    /* 上下文 */
    val context : Context = LocalContext.current

    /* token 数据 */
    val accessToken = mainViewModel.accessToken.collectAsState()
    val refreshToken = mainViewModel.refreshToken.collectAsState()

    LaunchedEffect(key1 = accessToken) {
        /* 若本地协议缓冲区Token内容为空 或无效 */
        if (accessToken.value.isEmpty() || accessToken.value == "") {
            Log.d("_chen", "访问token：${accessToken.value}")
            navController.navigate(
                route = AuthRoute.Login.route
            )
        } else if (mainViewModel.tokenAgingState.value is TokenAging.INVALID) {
            /* 若token时效结束，刷新Token */
            mainViewModel.refreshToken(context = context, refreshToken.value)
        } else if (mainViewModel.tokenAgingState.value is TokenAging.USEFUL) {
            Log.d("_chen","刷新？")
            /* 若token有效, 向协议缓冲区protobuf储存token信息 */
            mainViewModel.currentUserId = (mainViewModel.tokenAgingState.value as TokenAging.USEFUL).userId
            /* 初始化加载所有内容 */
            mainViewModel.mainInit(context = context)
        } else if (mainViewModel.tokenAgingState.value is TokenAging.FINISHED) {
            /* empty */
        } else {
            /* Token存在的情况下查询时效 */
            Log.d("_chen", "查询时效： ${accessToken.value}")
            accessToken.value.let {
                mainViewModel.requestTokenAgingInfo(context = context, token = it) {
                    navController.navigate(route = AuthRoute.Login.route) {
                        popUpTo(BottomBarRoute.AudioHome.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    /* 热门歌单请求结果(含数据) */
    val hotPlayListState = mViewModel.hotPlayListDataState.collectAsState()
    val lazyColumnState = rememberLazyListState()

    /* UserDetail */
    val userDetail = mainViewModel.currentUserDetailInfo.collectAsState()

    Scaffold(
        topBar = {
        },
        content = {
            Box(
                Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (userDetail.value.avatar_url.contains("avatar")) LOCAL_SERVER_URL + userDetail.value.avatar_url else userDetail.value.avatar_url)
                        .crossfade(durationMillis = 500)
                        .transformations(listOf(
                            BlurTransformation(scale = .85f, radius = 10)
                        )).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight(.5f)
                        .align(TopCenter),
                    colorFilter = if (isSystemInDarkTheme())
                        ColorFilter.tint(Color.Black.copy(alpha = .3f), BlendMode.Darken)
                    else ColorFilter.tint(Color.White.copy(alpha = .3f), BlendMode.Lighten)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to MaterialTheme.colors.surface.copy(alpha = .0f),
                                    0.3f to MaterialTheme.colors.surface.copy(alpha = .5f),
                                    0.5f to MaterialTheme.colors.surface.copy(alpha = 1f),
                                    1f to MaterialTheme.colors.surface
                                )
                            )
                        )
                        .align(BottomCenter)
                )
            }
            /* 热门歌单 */
            LazyColumn(
                state = lazyColumnState,
            ) {
                item {
                    Spacer(modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                        .background(color = Color.Transparent)
                    )
                }
                /* 问候标题 */
                item {
                    GreetingsTitle(
                        onOpenHistoryEvent = {
                            navController.navigate(route = PersonRoute.HistoryScreen.route + "?user_id=${mainViewModel.currentUserId}")
                        },
                        onSearchEvent = onOpenSearchPage
                    )
                }
                /* 最近播放 */
                item {
                    RecentPlayListGroup(
                        playListDataState = hotPlayListState,
                        imageLoader = mViewModel.myImageLoader,
                        audioHomeViewModel = mViewModel,
                        onOpenPlayListPage = onOpenPlayListPage
                    )
                }
                item {
                    RecommendTitle(
                        onSearchEvent = onOpenSearchPage
                    )
                }
                when (hotPlayListState.value) {
                    is HotPlayListDataState.Loading -> {
                        for (i in 0 until 5) {
                            item { HotRecommendPlayListItemLoadState() }
                        }
                    }
                    is HotPlayListDataState.Success -> {
                        items(
                            items = (hotPlayListState.value as HotPlayListDataState.Success).collection,
                            key = { playlist ->
                                playlist.id
                            }
                        ) { playlist ->
                            HotRecommendPlayListItem(
                                playListItem = playlist,
                                imageLoader = mViewModel.myImageLoader,
                                onClickEvent = {
                                    onOpenPlayListPage(playlist.id)
                                },
                                onPlayEvent = {
                                    /* 获取歌单播放列表，立即播放 */
                                }
                            )
                        }
                    }
                    else -> {
                        item { ErrorOccurredWidget{
                            mViewModel.getHotPlayListData()
                        } }
                    }
                }
            }
        }
    )
}

/** 问候语标题
 *  @param  text    可选文本内容，该参数为空的情况下，问候语内容依据当前系统时间进行判断
 */
@Composable
fun GreetingsTitle(
    text: String? = null,
    modifier: Modifier = Modifier,
    onOpenHistoryEvent: () -> Unit,
    onSearchEvent : () -> Unit,
) {
    val currentTime = SimpleDateFormat.getTimeInstance().format(Calendar.getInstance().time)
    val greeting : String = text
        ?: when (currentTime.substring(0 until 2).toInt()) {
            in 4..8 -> { "早上好" }
            in 10..11 -> { "上午好" }
            in 13 downTo 12 -> { "中午好" }
            in 14..17 -> { "下午好" }
            else -> { "晚上好" }
        }
    ConstraintLayout(
        modifier = modifier
            .height(58.dp)
            .fillMaxWidth()
            .background(color = Color.Transparent)
    ) {
        val (greetingTitle, historyButton, searchButton) = createRefs()
        Text(
            text = "${greeting}, 朋友",
            style = MaterialTheme.typography.h5.copy(color = MaterialTheme.colors.onSurface.copy(alpha = .7f)),
            modifier = Modifier
                .constrainAs(greetingTitle) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                }
                .padding(horizontal = 14.dp, vertical = 4.dp)
        )
        IconButton(
            onClick = { onOpenHistoryEvent() },
            modifier = Modifier.constrainAs(historyButton) {
                top.linkTo(parent.top)
                end.linkTo(searchButton.start)
                bottom.linkTo(parent.bottom)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_play_history),
                contentDescription = "Play History",
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(
            onClick = { onSearchEvent() },
            modifier = Modifier.constrainAs(searchButton) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Go to Search Page",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/** 最近收听歌单组 （以类似Grid的方式进行排列）
 * @param   playListDataState  热门歌单网络请求结果状态
 * @param   imageLoader coil图片加载器对象
 */
@Composable
fun RecentPlayListGroup(
    playListDataState: State<HotPlayListDataState>,
    imageLoader: ImageLoader,
    audioHomeViewModel: AudioHomeViewModel,
    onOpenPlayListPage : (Long) -> Unit,
) {
    val configuration = LocalConfiguration.current
    /* 单个歌单模块宽度 */
    val singlePlaylistWidth = configuration.screenWidthDp/2 - 10
    Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 16.dp)) {
        Column(
            modifier = Modifier
                .align(alignment = Alignment.Center)
        ) {
            when (playListDataState.value) {
                is HotPlayListDataState.Success -> {
                    val playListInfo = remember { playListDataState.value as HotPlayListDataState.Success }
                    /* 第一行  */
                    Row {
                        RecentPlayListItem(playListInfo.collection[0], imageLoader, singlePlaylistWidth) { onOpenPlayListPage(playListInfo.collection[0].id) }
                        RecentPlayListItem(playListInfo.collection[1], imageLoader, singlePlaylistWidth) { onOpenPlayListPage(playListInfo.collection[1].id) }
                    }
                    /* 第二行 */
                    Row {
                        RecentPlayListItem(playListInfo.collection[2], imageLoader, singlePlaylistWidth) { onOpenPlayListPage(playListInfo.collection[2].id) }
                        RecentPlayListItem(playListInfo.collection[3], imageLoader, singlePlaylistWidth) { onOpenPlayListPage(playListInfo.collection[3].id) }
                    }
                    /* 第三行 */
                    Row {
                        RecentPlayListItem(playListInfo.collection[4], imageLoader, singlePlaylistWidth) { onOpenPlayListPage(playListInfo.collection[4].id) }
                        RecentPlayListItem(playListInfo.collection[5], imageLoader, singlePlaylistWidth) { onOpenPlayListPage(playListInfo.collection[5].id) }
                    }
                }
                is HotPlayListDataState.Error -> {
                    ErrorOccurredWidget{
                        audioHomeViewModel.getHotPlayListData()
                    }
                }
                else -> {
                    Row {
                        RecentPlayListItemLoadingState(singlePlaylistWidth)
                        RecentPlayListItemLoadingState(singlePlaylistWidth)
                    }
                    Row {
                        RecentPlayListItemLoadingState(singlePlaylistWidth)
                        RecentPlayListItemLoadingState(singlePlaylistWidth)
                    }
                    Row {
                        RecentPlayListItemLoadingState(singlePlaylistWidth)
                        RecentPlayListItemLoadingState(singlePlaylistWidth)
                    }
                }
            }
        }
    }
}

/** 首页最近播放
 * @param   playListItem    播放列表歌单单向
 * @param   imageLoader coil 加载器对象
 * @param   itemWidth   单项Card宽度
 * @param   clickEvent  Card点击触发跳转事件
 *
 */
@Composable
fun RecentPlayListItem(
    playListItem : PlayList,
    imageLoader: ImageLoader,
    itemWidth: Int,
    clickEvent: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(82.dp)
            .width(itemWidth.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.03f),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.clickable { clickEvent() }
        ) {
            Surface(
                modifier = Modifier
                    .width(76.dp)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = playListItem.coverImageUrl,
                    contentDescription = "Current Playlist is ${playListItem.name}",
                    contentScale = ContentScale.Crop,
                    imageLoader = imageLoader,
                    placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder),
                )
            }

            Text(
                text = playListItem.name,
                style = MaterialTheme.typography.subtitle2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = .9f)),
                textAlign = TextAlign.Left,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 8.dp, top = 12.dp, end = 10.dp, bottom = 12.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

/** 加载状态的PlayList Item
 * @param   itemWidth   单项Card宽度
 */
@Composable
fun RecentPlayListItemLoadingState(
    itemWidth: Int,
) {
    /* 循环动画变换状态 */
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
            .height(82.dp)
            .width(itemWidth.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.03f),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.clickable {  }
        ) {
            Surface(
                modifier = Modifier
                    .width(76.dp)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder,
                    contentDescription = "Current Playlist Info is Loading",
                    contentScale = ContentScale.Crop,
                )
            }

            ConstraintLayout(
                modifier = Modifier.fillMaxSize()
            ) {
                val (block1, spacer, block2) = createRefs()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .padding(horizontal = 6.dp)
                        .constrainAs(block1) {
                            top.linkTo(parent.top, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(spacer.top)
                            width = Dimension.fillToConstraints
                        }
                        .background(Color.LightGray.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier
                    .width(8.dp)
                    .constrainAs(spacer) {
                        top.linkTo(block1.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(block2.top)
                    })
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .padding(horizontal = 6.dp)
                        .constrainAs(block2) {
                            top.linkTo(spacer.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom, margin = 12.dp)
                            width = Dimension.fillToConstraints
                        }
                        .background(Color.LightGray.copy(alpha = alpha))
                )
            }
        }
    }
}

/** 错误发生覆盖组件
 *  @param  onRetryRequestDataEvent 点击重新请求数据事件
 */
@Composable
fun ErrorOccurredWidget(
    onRetryRequestDataEvent: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(224.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onRetryRequestDataEvent() },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(width = 1.dp, MaterialTheme.colors.onSurface.copy(alpha = .3f)),
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val errorIcon = createRef()
            Column(
                modifier = Modifier.constrainAs(errorIcon) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_error_occurred),
                    contentDescription = "Sorry, Here is a Error",
                    tint = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .size(32.dp)
                        .align(CenterHorizontally),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "点击重新加载数据...",
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = .6f)
                )
            }
        }
    }
}


/** 组件 “热门歌单” 标题
 *
 */
@Composable
fun RecommendTitle(
    modifier: Modifier = Modifier,
    onSearchEvent: () -> Unit
) {
    ConstraintLayout(
        modifier = modifier
            .height(58.dp)
            .fillMaxWidth()
            .background(color = Color.Transparent)
    ) {
        val (hotTitle, searchButton) = createRefs()
        Text(
            text = "热门歌单",
            style = MaterialTheme.typography.h5.copy(color = MaterialTheme.colors.onSurface.copy(alpha = .7f)),
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(hotTitle) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
        IconButton(
            onClick = { onSearchEvent() },
            modifier = Modifier.constrainAs(searchButton) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Go to Search Page",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/** 预览最近歌单 - 单项(Card)
 *
 */
@Composable
@Preview
fun PreviewRecentPlayListItem() {
    BeeAudioTheme {
        Box {
            RecentPlayListItem(
                SinglePlayListData,
                imageLoader = ImageLoader.Builder(LocalContext.current).crossfade(true).build(),
                itemWidth = 210,
                clickEvent = { /**/ }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewHotRecommendPlayListItem() {
    BeeAudioTheme {
        HotRecommendPlayListItem(
            playListItem = SinglePlayListData,
            imageLoader = ImageLoader.Builder(LocalContext.current).crossfade(true).build(),
            onClickEvent = {  },
            onPlayEvent = {  }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewErrorOccurredWidget() {
    BeeAudioTheme {
        ErrorOccurredWidget(
            onRetryRequestDataEvent = {  }
        )
    }
}

//@Composable
//@Preview(showBackground = true)
//fun PreviewRecentPlayListItemLoadingState() {
//    BeeAudioTheme {
//        RecentPlayListItemLoadingState(184)
//    }
//}
//
//@Composable
//@Preview(showBackground = true)
//fun PreviewHotRecommendPlayListItemLoadState() {
//    BeeAudioTheme {
//        HotRecommendPlayListItemLoadState()
//    }
//}