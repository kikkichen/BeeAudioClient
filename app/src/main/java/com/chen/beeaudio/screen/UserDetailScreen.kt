package com.chen.beeaudio.screen

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.mock.RequestBlogMock
import com.chen.beeaudio.mock.RequestUserDetailMock
import com.chen.beeaudio.mock.UserCountMock
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.audio.Premium
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.model.blog.SimpleUserCount
import com.chen.beeaudio.navigation.AudioHomeRoute
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.screen.widget.BlogItemForRequestBlog
import com.chen.beeaudio.screen.widget.ErrorDataTipsWidget
import com.chen.beeaudio.screen.widget.PlayListShowItemWidget
import com.chen.beeaudio.screen.widget.PremiumLogoForUser
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.ui.theme.shimmerEffect
import com.chen.beeaudio.utils.BlurTransformation
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.UserDetailViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun UserDetailScreen(
    userId: Long,
    navController: NavController,
    userDetailViewModel: UserDetailViewModel = hiltViewModel(),
    mainViewModel: MainViewModel,
) {
    /* 上下文 */
    val context : Context = LocalContext.current

    val collapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState()

    /* 状态顶栏折叠标识 */
    val enabled by remember { mutableStateOf(true) }

    /* 屏幕宽度 */
    val configuration = LocalConfiguration.current
    val currentScreenWidth = configuration.screenWidthDp

    /* 当前用户详细信息状态 */
    val userDetailState = loadCurrentUserDetail(userId = userId, viewModel = userDetailViewModel)
    /* 用户粉丝、关注数量 */
    val userCountState = userDetailViewModel.requestUserCount.collectAsState()
    /* Premium会员状态 */
    val isPremiumState = userDetailViewModel.isPremiumTag.collectAsState()
    /* 我 与当前用户页用户的关系状态 */
    val weRelativeState = userDetailViewModel.weRelative.collectAsState()
    /* 当前用户自建歌单列表 */
    val userCreatedPlaylists = userDetailViewModel.playlists.collectAsState()

    /* 加载所需数据 */
    userDetailViewModel.loadUserCount(userId)
    userDetailViewModel.loadIsPremiumTag(userId)
    userDetailViewModel.loadRelative(mainViewModel.currentUserId, userId)

    /* 分页博文请求结果 */
    val blogsForPaging = userDetailViewModel.blogs.collectAsLazyPagingItems()
    /* 分页原创博文请求结果 */
    val originalBlogsForPaging = userDetailViewModel.originalBlogs.collectAsLazyPagingItems()

    val pagerTitle = listOf("历史动态", "原创博文", "ta的歌单")

    /* SnackBar状态 */
    val snackBarState = remember { SnackbarHostState() }
    /* 协程域 */
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CollapsingToolbarScaffold(
            modifier = Modifier.fillMaxSize(),
            state = collapsingToolbarScaffoldState,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            toolbarModifier = Modifier.background(MaterialTheme.colors.surface),
            toolbar = {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            color = Color.Transparent
                        )
                        .statusBarsPadding()
                )
                when (userDetailState.value) {
                    is NetUserDetailLoadResult.Success -> {
                        val userDetail = (userDetailState.value as NetUserDetailLoadResult.Success).userDetail
                        UserDetailBlock(
                            userDetail = userDetail,
                            collapsingToolbarScaffoldState = collapsingToolbarScaffoldState,
                            parentHeight = 326,
                            parentWidth = currentScreenWidth,
                            mainViewModel = mainViewModel,
                            imageLoader = userDetailViewModel.myImageLoader,
                            relative = weRelativeState.value,
                            isPremium = isPremiumState.value,
                            userCountData = userCountState.value,
                            onFollowButtonEvent = {
                                userDetailViewModel.dealWithFollowAction(mainViewModel.currentUserId, userId, snackBarState)
                            },
                            onOpenFollowUserScreen = { userid, username ->
                                navController.navigate(
                                    route = BlogRoute.FollowUserScreen.route + "?uid=$userid&username=$username"
                                )
                            },
                            onOpenFansUserScreen = { userid, username ->
                                navController.navigate(
                                    route = BlogRoute.FansUserScreen.route + "?uid=$userid&username=$username"
                                )
                            }
                        )
                    }
                    is NetUserDetailLoadResult.Error -> {
                        UserDetailBlockErrorState(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(326.dp)
                                .background(color = Color.Transparent)
                                .statusBarsPadding()
                        )
                    }
                    is NetUserDetailLoadResult.Loading -> {
                        UserDetailBlockLoadingState()
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
                    when (page) {
                        0 -> {
                            userDetailViewModel.loadUserBlogs(userId)
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = MaterialTheme.colors.onSurface.copy(
                                            alpha = .1f
                                        )
                                    )
                            ) {
                                when (blogsForPaging.loadState.refresh) {
                                    is LoadState.NotLoading -> {
                                        if (blogsForPaging.itemCount == 0) {
                                            item {
                                                ConstraintLayout(modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(120.dp)
                                                ) {
                                                    val block = createRef()
                                                    Column(
                                                        modifier = Modifier.constrainAs(block) {
                                                            start.linkTo(parent.start)
                                                            top.linkTo(parent.top)
                                                            end.linkTo(parent.end)
                                                            bottom.linkTo(parent.bottom)
                                                        }
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.ic_blog_empty),
                                                            contentDescription = "Not any blogs",
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = "对方暂时没有发布任何的动态哦！",
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Light,
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            items(
                                                items = blogsForPaging,
                                                key = { blog ->
                                                    blog.Id
                                                }
                                            ) { blog ->
                                                BlogItemForRequestBlog(
                                                    navController = navController,
                                                    blogData = blog ?: RequestBlogMock,
                                                    onPlayEvent = { track ->
                                                        mainViewModel.playTargetAudio(track = track, context = context)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    is LoadState.Loading -> {
                                        item {
                                            ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                                                val circle = createRef()
                                                CircularProgressIndicator(
                                                    modifier = Modifier
                                                        .padding(top = 120.dp)
                                                        .constrainAs(circle) {
                                                            start.linkTo(parent.start)
                                                            end.linkTo(parent.end)
                                                        }
                                                )
                                            }
                                        }
                                    }
                                    is LoadState.Error -> {
                                        item { ErrorDataTipsWidget(
                                            text = "错误导致没有加载出任何动态",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                        ) }
                                    }
                                }
                            }
                        }
                        1 -> {
                            userDetailViewModel.loadUserOriginalBlogs(userId)
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = MaterialTheme.colors.onSurface.copy(
                                            alpha = .1f
                                        )
                                    )
                            ) {
                                when (blogsForPaging.loadState.refresh) {
                                    is LoadState.NotLoading -> {
                                        if (originalBlogsForPaging.itemCount == 0) {
                                            item {
                                                ConstraintLayout(modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(120.dp)
                                                ) {
                                                    val block = createRef()
                                                    Column(
                                                        modifier = Modifier.constrainAs(block) {
                                                            start.linkTo(parent.start)
                                                            top.linkTo(parent.top)
                                                            end.linkTo(parent.end)
                                                            bottom.linkTo(parent.bottom)
                                                        }
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.ic_blog_empty),
                                                            contentDescription = "Not any original blogs",
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = "对方暂时没有发布任何的原创动态哦！",
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Light,
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            items(
                                                items = originalBlogsForPaging,
                                                key = { blog ->
                                                    blog.Id
                                                }
                                            ) { blog ->
                                                BlogItemForRequestBlog(
                                                    navController = navController,
                                                    blogData = blog ?: RequestBlogMock,
                                                    onPlayEvent = { track ->
                                                        mainViewModel.playTargetAudio(track = track, context = context)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    is LoadState.Loading -> {
                                        item {
                                            ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                                                val circle = createRef()
                                                CircularProgressIndicator(
                                                    modifier = Modifier
                                                        .padding(top = 80.dp)
                                                        .constrainAs(circle) {
                                                            start.linkTo(parent.start)
                                                            end.linkTo(parent.end)
                                                        }
                                                )
                                            }
                                        }
                                    }
                                    is LoadState.Error -> {
                                        item { ErrorDataTipsWidget(
                                            text = "错误导致没有加载出任何动态",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(86.dp)
                                        ) }
                                    }
                                }
                            }
                        }
                        2 -> {
                            userDetailViewModel.loadUserCreatedPlaylist(userId)
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = MaterialTheme.colors.onSurface.copy(
                                            alpha = .1f
                                        )
                                    )
                            ) {
                                when (userCreatedPlaylists.value) {
                                    is NetUserCreatedPlaylistResult.Loading -> {
                                        item {
                                            LoadingPlaylistBlock()
                                        }
                                    }
                                    is NetUserCreatedPlaylistResult.Error -> {
                                        item {
                                            ErrorDataTipsWidget(
                                                text = "ta歌单加载出现了一些错误,\n 点击可以重试 ~",
                                                onClickEvent = { userDetailViewModel.loadUserCreatedPlaylist(userId) }
                                            )
                                        }
                                    }
                                    is NetUserCreatedPlaylistResult.Success -> {
                                        val playlists = (userCreatedPlaylists.value as NetUserCreatedPlaylistResult.Success).playlists
                                        if (playlists.isNotEmpty()) {
                                            items(
                                                items = playlists,
                                                key = { item -> item.id }
                                            ) { playlist ->
                                                Card {
                                                    PlayListShowItemWidget(
                                                        playList = playlist,
                                                        imageLoader = userDetailViewModel.myImageLoader,
                                                        onOpenPlayListDetailPage = {
                                                            navController.navigate(
                                                                route = AudioHomeRoute.PlayListScreen.route + "/$it"
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        } else {
                                            item {
                                                EmptyDataBlock(tipText = "对方没有设置可以公开的歌单哦～")
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
                .height(80.dp)
                .background(color = Color.Transparent)
                .statusBarsPadding()
        ) {
            when (userDetailState.value) {
                is NetUserDetailLoadResult.Success -> {
                    UserDetailHeaderBar(
                        userDetail = (userDetailState.value as NetUserDetailLoadResult.Success).userDetail,
                        collapsingToolbarScaffoldState = collapsingToolbarScaffoldState,
                        onBackEvent = { navController.navigateUp() },
                        onMoreEvent = {  }
                    )
                } else -> {
                    UserDetailHeaderBar(
                        userDetail = RequestUserDetailMock,
                        collapsingToolbarScaffoldState = collapsingToolbarScaffoldState,
                        onBackEvent = { navController.navigateUp() },
                        onMoreEvent = {  }
                    )
                }
            }
        }
        SnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            hostState = snackBarState
        )
    }
}

/* 用户详细页面 - 用户详细信息请求结果状态 */
sealed class NetUserDetailLoadResult<T> {
    object Loading : NetUserDetailLoadResult<RequestUserDetail>()
    object Error : NetUserDetailLoadResult<RequestUserDetail>()
    data class Success(val userDetail: RequestUserDetail) : NetUserDetailLoadResult<RequestUserDetail>()
}

/* 用户详细页面 - 自建歌单列表信息加载结果 */
sealed class NetUserCreatedPlaylistResult<T> {
    object Loading : NetUserCreatedPlaylistResult<List<PlayList>>()
    object Error : NetUserCreatedPlaylistResult<List<PlayList>>()
    data class Success(val playlists : List<PlayList>) : NetUserCreatedPlaylistResult<List<PlayList>>()
}

/** 使用produceState将当前用户详细信息数据Flow转换为State
 *  @param  userId  当前页面哟你农户ID
 *  @param  viewModel   当前UserDetailScreen 的ViewModel
 */
@Composable
fun loadCurrentUserDetail(
    userId : Long,
    viewModel : UserDetailViewModel
) : State<NetUserDetailLoadResult<RequestUserDetail>> {
    return produceState(initialValue = NetUserDetailLoadResult.Loading as NetUserDetailLoadResult<RequestUserDetail>, userId, viewModel) {
        var currentUserDetail : RequestUserDetail? = null
        viewModel.currentUserDetailFlow(userId = userId)
            .catch {
                value = NetUserDetailLoadResult.Error
            }
            .collect {
                currentUserDetail = it
            }
        value = if (currentUserDetail == null) {
            NetUserDetailLoadResult.Error
        } else {
            NetUserDetailLoadResult.Success(currentUserDetail!!)
        }
    }
}

@Composable
fun UserDetailBlock(
    userDetail : RequestUserDetail,
    collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState,
    parentHeight: Int,
    parentWidth: Int,
    mainViewModel: MainViewModel,
    imageLoader : ImageLoader,
    isPremium: Premium,
    relative: Int,
    userCountData: SimpleUserCount,
    onFollowButtonEvent: () -> Unit,
    onOpenFollowUserScreen : (Long, String) -> Unit,
    onOpenFansUserScreen : (Long, String) -> Unit,
) {

    Box(
        modifier = Modifier
            .height(parentHeight.dp)
            .width(parentWidth.dp)
            .background(color = MaterialTheme.colors.surface)
            .graphicsLayer {
                translationY -= 2.toFloat() * parentHeight * (1 - collapsingToolbarScaffoldState.toolbarState.progress)
                alpha = collapsingToolbarScaffoldState.toolbarState.progress
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(if (userDetail.avatar_url.contains("avatar")) LOCAL_SERVER_URL + userDetail.avatar_url else userDetail.avatar_url)
                .transformations(listOf(
                    BlurTransformation(scale = .98f, radius = 10)
                )).build(),
            contentDescription = "${userDetail.name}'s Person Page.",
            placeholder = painterResource(id = R.drawable.ic_image_placeholder),
            contentScale = ContentScale.Crop,
            imageLoader = imageLoader,
            modifier = Modifier
                .height(parentHeight.dp)
                .graphicsLayer {
                    // change alpha of Image as the toolbar expands
//                            alpha = collapsingToolbarScaffoldState.toolbarState.progress
                },
            colorFilter = if (isSystemInDarkTheme()) ColorFilter.tint(Color.Gray.copy(alpha = .5f), BlendMode.Darken) else null
        )
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent)
        ) {
            val (bottomCover, avatar, nameText, descriptionText, userCount ,premiumLogo, followOnButton) = createRefs()
            Box(
                modifier = Modifier
                    .height((parentHeight / 2).dp)
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colors.surface)
                    .constrainAs(bottomCover) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            )
            Surface(
                modifier = Modifier
                    .size(86.dp)
                    .constrainAs(avatar) {
                        start.linkTo(parent.start, margin = 24.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .padding(3.dp),
                shape = CircleShape,
                border = BorderStroke(3.dp, MaterialTheme.colors.surface),
            ) {
                AsyncImage(
                    model = if (userDetail.avatar_url.contains("avatar")) LOCAL_SERVER_URL + userDetail.avatar_url else userDetail.avatar_url,
                    contentDescription = "${userDetail.name}'s avatar",
                    placeholder = painterResource(id = R.drawable.personnel)
                )
            }
            AnimatedVisibility(
                visible = (userDetail.uid!=mainViewModel.currentUserId),
                modifier = Modifier.constrainAs(followOnButton) {
                    end.linkTo(parent.end, margin = 30.dp)
                    bottom.linkTo(nameText.top)
                }
            ) {
                if (relative == 1 || relative == 3) {
                    TextButton(
                        onClick = { onFollowButtonEvent() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .padding(horizontal = 12.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colors.primary)
                    ) {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cancel_focus_user),
                                contentDescription = "取消关注",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "取消关注")
                        }
                    }
                } else {
                    Button(
                        onClick = { onFollowButtonEvent() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .padding(horizontal = 12.dp)
                    ) {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_focus_user),
                                contentDescription = "关注",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colors.surface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "关注")
                        }
                    }
                }
            }
            Text(
                text = userDetail.name,
                fontStyle = MaterialTheme.typography.h2.fontStyle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .constrainAs(nameText) {
                        start.linkTo(parent.start, margin = 20.dp)
                        top.linkTo(avatar.bottom, margin = 12.dp)
                    }
            )
            AnimatedVisibility(
                visible = isPremium.card_id.isNotEmpty(),
                modifier = Modifier.constrainAs(premiumLogo) {
                    start.linkTo(nameText.end, margin = 8.dp)
                    top.linkTo(nameText.top)
                    bottom.linkTo(nameText.bottom)
                }
            ) {
                PremiumLogoForUser()
            }
            ShowFollowsAndFansBlock(
                modifier = Modifier.constrainAs(userCount) {
                    start.linkTo(parent.start, margin = 36.dp)
                    top.linkTo(nameText.bottom, margin = 4.dp)
                },
                userCount = userCountData,
                onLookFollowListEvent = { onOpenFollowUserScreen(userDetail.uid, userDetail.name) },
                onLookFanListEvent = { onOpenFansUserScreen(userDetail.uid, userDetail.name) }
            )
            Text(
                text = userDetail.description,
                fontSize = 12.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colors.onSurface.copy(.6f),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp)
                    .constrainAs(descriptionText) {
                        start.linkTo(userCount.end, margin = 20.dp)
                        top.linkTo(nameText.bottom)
                        end.linkTo(parent.end, margin = 10.dp)
                        width = Dimension.fillToConstraints
                    }
            )
        }
    }
}

/** 用户个人内容页 折叠标题栏
 *  @param  userDetail    用户详细信息
 *  @param  collapsingToolbarScaffoldState  折叠组合布局脚手架状态对象
 *  @param  onBackEvent 返回事件响应
 *  @param  onMoreEvent 更多操作点击事件
 */
@Composable
fun UserDetailHeaderBar(
    userDetail: RequestUserDetail,
    collapsingToolbarScaffoldState : CollapsingToolbarScaffoldState,
    onBackEvent: () -> Unit,
    onMoreEvent: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
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
                text = userDetail.name,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        /* 更多选项 */
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
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Action For this User.",
                modifier = Modifier
                    .size(22.dp)
            )
        }
    }
}

@Composable
fun ShowFollowsAndFansBlock(
    modifier: Modifier = Modifier,
    userCount : SimpleUserCount,
    onLookFanListEvent : () -> Unit,
    onLookFollowListEvent : () -> Unit
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (fans, follows, divide) = createRefs()
        val centerGuideLine = createGuidelineFromStart(.5f)
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(48.dp)
                .constrainAs(divide) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top, margin = 8.dp)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom, margin = 8.dp)
                }
                .background(color = MaterialTheme.colors.onSurface.copy(alpha = .3f))
        )
        Column(
            modifier = Modifier
                .constrainAs(fans) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(centerGuideLine, margin = 18.dp)
                    bottom.linkTo(parent.bottom)
                }
                .clickable { onLookFanListEvent() },
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "粉丝",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = userCount.fans.toString(),
                style = MaterialTheme.typography.subtitle1.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
            )
        }
        Column(
            modifier = Modifier
                .constrainAs(follows) {
                    start.linkTo(centerGuideLine, margin = 18.dp)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .clickable { onLookFollowListEvent() },
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "关注",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = userCount.follows.toString(),
                style = MaterialTheme.typography.subtitle1.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/* 用户详情模块的加载样式 */
@Composable
fun UserDetailBlockLoadingState() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(326.dp)
            .background(color = Color.Transparent)
            .statusBarsPadding()
    ) {
        val startGuideLine = createGuidelineFromStart(.32f)
        val (avatar, numberCount, nameTitle, descBox) = createRefs()
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(color = Color.Transparent)
                .shimmerEffect()
                .constrainAs(avatar) {
                    start.linkTo(parent.start, margin = 18.dp)
                    top.linkTo(parent.top)
                    end.linkTo(startGuideLine)
                    bottom.linkTo(parent.bottom)
                }
        )
        ConstraintLayout(
            modifier = Modifier.constrainAs(numberCount) {
                start.linkTo(parent.start, margin = 36.dp)
                top.linkTo(avatar.bottom)
                end.linkTo(startGuideLine)
                bottom.linkTo(parent.bottom)
            }
        ) {
            val (fans, follows, divide) = createRefs()
            val centerGuideLine = createGuidelineFromStart(.5f)
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .constrainAs(divide) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top, margin = 8.dp)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom, margin = 8.dp)
                    }
                    .background(color = MaterialTheme.colors.onSurface.copy(alpha = .3f))
            )
            Column(
                modifier = Modifier
                    .constrainAs(fans) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(centerGuideLine, margin = 18.dp)
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                Box(modifier = Modifier
                    .width(40.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier
                    .width(36.dp)
                    .height(36.dp)
                    .shimmerEffect()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Column(
                modifier = Modifier
                    .constrainAs(follows) {
                        start.linkTo(centerGuideLine, margin = 18.dp)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                Box(modifier = Modifier
                    .width(40.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier
                    .width(36.dp)
                    .height(36.dp)
                    .shimmerEffect()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth(.38f)
                .constrainAs(nameTitle) {
                    start.linkTo(startGuideLine, margin = 20.dp)
                    top.linkTo(parent.top, margin = 36.dp)
                    bottom.linkTo(parent.bottom)
                }
                .clip(CircleShape)
                .shimmerEffect()
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .constrainAs(descBox) {
                    start.linkTo(startGuideLine, margin = 20.dp)
                    top.linkTo(nameTitle.bottom, margin = 10.dp)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
        ) {
            repeat((0..3).count()) {
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(.95f)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(.6f)
                    .clip(CircleShape)
                    .shimmerEffect()
            )
        }
    }
}

/* 错误的用户信息布局错误状态 */
@Composable
fun UserDetailBlockErrorState(
    modifier: Modifier
) {
    ErrorDataTipsWidget(
        text = "用户信息加载出了些错误\n请推出该页重试",
        modifier = modifier
    )
}

@Composable
@Preview(showBackground = true)
fun PreviewShowFollowsAndFansBlock() {
    BeeAudioTheme {
        ShowFollowsAndFansBlock(
            modifier = Modifier.padding(12.dp),
            userCount = UserCountMock,
            onLookFanListEvent = {  },
            onLookFollowListEvent = {  }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewUserDetailBlockLoadingState() {
    UserDetailBlockLoadingState()
}