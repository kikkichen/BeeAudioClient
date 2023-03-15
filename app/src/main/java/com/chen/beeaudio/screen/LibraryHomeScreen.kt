package com.chen.beeaudio.screen

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.airbnb.lottie.compose.*
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.model.localmodel.Subscribe
import com.chen.beeaudio.navigation.AudioHomeRoute
import com.chen.beeaudio.navigation.AuthRoute
import com.chen.beeaudio.navigation.BottomBarRoute
import com.chen.beeaudio.navigation.PersonRoute
import com.chen.beeaudio.screen.widget.AccountControlDialog
import com.chen.beeaudio.ui.theme.Green400
import com.chen.beeaudio.ui.theme.LightBlueA400
import com.chen.beeaudio.viewmodel.LibraryViewModel
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.SubscribeDataState
import com.chen.beeaudio.viewmodel.TokenAging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun LibraryHomeScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    libraryViewModel : LibraryViewModel = hiltViewModel()
) {
    /* 上下文 */
    val context : Context = LocalContext.current
    /* 协程域 */
    val coroutineScope = rememberCoroutineScope()
    /* 当前登陆用户信息 */
    val userInfo = mainViewModel.currentUserDetailInfo.collectAsState()
    /* 用户 Premium 会员状态 */
    val premiumState = mainViewModel.premiumUserInfo.collectAsState()
    /* 搜索展开标识 */
    val searchExpandedSignal = remember { mutableStateOf(false) }
    /* 屏幕宽度 */
    val screenWidth = LocalConfiguration.current.screenWidthDp

    /* 搜索关键字 */
    val (searchKeyword, changeSearchKeyword) = remember { mutableStateOf("") }
    /* Chip 列表选择标识 */
    val selectChipGroupState : MutableState<SubscribeFilterChip> = remember { mutableStateOf(SubscribeFilterChip.All) }
    /* 订阅条目展示状态 */
    val (subscribeItemSortState, subscribeItemSortStateChange) = remember { mutableStateOf(false) }
    /* 行列转换状态 */
    val (showItemModuleState, showItemModuleStateChange) = remember { mutableStateOf(false) }

    /* 用户操作窗口显示状态 */
    val userControlWindowShowState = remember { mutableStateOf(false) }

    /* 上传新的用户头像 图片Uri状态 */
    val avatarUploadUriState = libraryViewModel.preUploadAvatarImageUri.collectAsState()
    /* 图片选择器 */
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) {
        if (it.toString().isNotEmpty()) {
            libraryViewModel.preUploadAvatarImageUri.value = it.toString()
            /* 执行头像上传逻辑 */
            libraryViewModel.uploadPlaylistCoverImage(
                context = context,
                currentUserId = mainViewModel.currentUserId,
                finishedEvent = {
                    Toast.makeText(context, "头像上传成功！", Toast.LENGTH_SHORT).show()
                    libraryViewModel.preUploadAvatarImageUri.value = ""
                    userControlWindowShowState.value = false
                    coroutineScope.launch(Dispatchers.IO) {
                        mainViewModel.loadCurrentUserDetail()
                    }
                }
            )
        }
    }

    /* 搜索结果加载状态 */
    val searchDataState = libraryViewModel.searchResultList.collectAsState()
    /* 订阅项目加载状态 */
    val subscribeDataState = libraryViewModel.subscribeDataList.collectAsState()

    /* 更新 MainViewModel 中的 Premium 信息数据 */
    LaunchedEffect(Unit) {
        mainViewModel.loadIsPremiumTag(userId = mainViewModel.currentUserId)
    }

    /* 用户设置操作窗口 */
    AccountControlDialog(
        visible = userControlWindowShowState.value,
        navController = navController,
        userInfo = userInfo.value,
        parentWidth = screenWidth,
        uploadAvatarEvent = {
            photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        logoutAccountEvent = {
            /* 退出登陆 */
            mainViewModel.clearToken()
            mainViewModel.clearSubscribeAndTrackDatabase()
            navController.navigate(route = AuthRoute.Login.route) {
                popUpTo(BottomBarRoute.AudioHome.route) {
                    inclusive = true
                }
            }
            mainViewModel.tokenAgingState.value = TokenAging.NONE
        },
        dismissEvent = { userControlWindowShowState.value = false }
    )

    Scaffold(
        topBar = {
            LibraryTopAppBar(
                userDetail = userInfo.value,
                isPremiumUser = premiumState.value.uid != 0.toLong(),
                searchKeyword = searchKeyword,
                isSearchBarExpand = searchExpandedSignal.value,
                selectChipState = selectChipGroupState.value,
                onAvatarClickEvent = {
                    userControlWindowShowState.value = true
                },
                onSearchActionEvent = { keyword ->
                    /* Chip 选择项目重置为“全部” */
                    selectChipGroupState.value = SubscribeFilterChip.All
                    /* 将输入字符赋值 */
                    changeSearchKeyword(keyword)
                    if (keyword.isEmpty() || keyword.isBlank()) {
                        /* 展示全部 */
                        libraryViewModel.replaceSearchResultList()
                    } else {
                        /* 搜索关键字结果执行 */
                        libraryViewModel.loadSubscribeDataByKeyword(searchKeyword)
                    }
                },
                onSearchDismissEvent = {
                    searchExpandedSignal.value = !searchExpandedSignal.value
                    libraryViewModel.loadSubscribeData(subscribeSignal = SubscribeFilterChip.All)
                },
                onSelectChipEvent = {
                    selectChipGroupState.value = it
                    libraryViewModel.loadSubscribeData(it)
                },
                onOpenPremiumPage = {
                    if (premiumState.value.card_id.isNotEmpty()) {
                        navController.navigate(route = PersonRoute.PremiumDetailScreen.route)
                    } else {
                        navController.navigate(route = PersonRoute.GuidePremiumScreen.route)
                    }
                },
                onOpenCreatePlaylist = {
                    /* 创建我的歌单 */
                    val createSignal : Long = 0
                    navController.navigate(route = PersonRoute.EditPlayListScreen.route + "?playlistId=$createSignal")
                }
            )
        }
    ) {
        /* 内容展示 */
        if (searchExpandedSignal.value) {
            Crossfade(
                targetState = searchDataState,
                modifier = Modifier.padding(paddingValues = it)
            ) { searchDataState ->
                when (searchDataState.value) {
                    is SubscribeDataState.None -> { /* empty */ }
                    is SubscribeDataState.Success -> {
                        val data = (searchDataState.value as SubscribeDataState.Success).data
                        /* 展示搜索结果 */
                        LazyColumn {
                            items(
                                items = data,
                                key = { item -> item.itemId }
                            ) { subscribeData ->
                                SubscribeContextItem(
                                    subscribe = subscribeData,
                                    myFavoritePlaylistId = mainViewModel.myFavoritePlaylistId,
                                    onBindToTop = {
                                        if (subscribeData.itemId == mainViewModel.myFavoritePlaylistId) {
                                            Toast.makeText(context, "默认「喜爱」歌单无法取消置顶哦", Toast.LENGTH_SHORT).show()
                                        } else {
                                            libraryViewModel.changeSubscribeDataTopState(subscribeData, mainViewModel.myFavoritePlaylistId)
                                            libraryViewModel.loadSubscribeData(selectChipGroupState.value)
                                            libraryViewModel.syncSubscribeDataToServer(context, mainViewModel.currentUserId)
                                        }
                                    },
                                    onOpenItemPage = { navigationToSubscribePage(subscribeData, navController) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                    is SubscribeDataState.EmptyData -> {
                        EmptyDataBlock(tipText = "貌似你还没有相关的订阅内容？")
                    }
                }
            }

        } else {
            Crossfade(
                targetState = subscribeDataState,
                modifier = Modifier.padding(paddingValues = it)
            ) { subscribeDataState ->
                when (subscribeDataState.value) {
                    is SubscribeDataState.None -> { /* empty */ }
                    is SubscribeDataState.Success -> {
                        val data = (subscribeDataState.value as SubscribeDataState.Success).data
                        if (!showItemModuleState) { /* 展示 Chip 表示内容 */
                            LazyColumn(modifier = Modifier.padding(paddingValues = it)) {
                                item {
                                    SubscribeSortBar(
                                        sortState = subscribeItemSortState,
                                        showState = false,
                                        changeSortStateEvent = { subscribeItemSortStateChange(!subscribeItemSortState) },
                                        changeShowStateEvent = { showItemModuleStateChange(true) }
                                    )
                                }
                                items(
                                    items = data.sortedByDescending { item -> if (subscribeItemSortState) item.type else item.weight },
                                    key = { item -> item.itemId }
                                ) { subscribeData ->
                                    /* 访问权重排序展示 */
                                    SubscribeContextItem(
                                        subscribe = subscribeData,
                                        myFavoritePlaylistId = mainViewModel.myFavoritePlaylistId,
                                        onBindToTop = {
                                            if (subscribeData.itemId == mainViewModel.myFavoritePlaylistId) {
                                                Toast.makeText(context, "默认「喜爱」歌单无法取消置顶哦", Toast.LENGTH_SHORT).show()
                                            } else {
                                                libraryViewModel.changeSubscribeDataTopState(subscribeData, mainViewModel.myFavoritePlaylistId)
                                                libraryViewModel.loadSubscribeData(selectChipGroupState.value)
                                                libraryViewModel.syncSubscribeDataToServer(context, mainViewModel.currentUserId)
                                            }
                                        },
                                        onOpenItemPage = { navigationToSubscribePage(subscribeData, navController) }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        } else {
                            /* 封面大图形式 */
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                userScrollEnabled = true,
                                modifier = Modifier.padding(paddingValues = it)
                            ) {
                                item(
                                    span = { GridItemSpan(2) }
                                ) {
                                    SubscribeSortBar(
                                        sortState = subscribeItemSortState,
                                        showState = true,
                                        changeSortStateEvent = {
                                            subscribeItemSortStateChange(!subscribeItemSortState)
                                            libraryViewModel.loadSubscribeData(selectChipGroupState.value)
                                        },
                                        changeShowStateEvent = { showItemModuleStateChange(false) }
                                    )
                                }
                                items(
                                    items = data.sortedByDescending { item -> if (subscribeItemSortState) item.type else item.weight },
                                    key = { item -> item.itemId }
                                ) { subscribeData ->
                                    /* 访问权重排序展示 */
                                    SubscribeContextBlock(
                                        parentWidth = screenWidth,
                                        subscribe = subscribeData,
                                        myFavoritePlaylistId = mainViewModel.myFavoritePlaylistId,
                                        onBindToTop = {
                                            if (subscribeData.itemId == mainViewModel.myFavoritePlaylistId) {
                                                Toast.makeText(context, "默认「喜爱」歌单无法取消置顶哦", Toast.LENGTH_SHORT).show()
                                            } else {
                                                libraryViewModel.changeSubscribeDataTopState(subscribeData, mainViewModel.myFavoritePlaylistId)
                                                libraryViewModel.loadSubscribeData(selectChipGroupState.value)
                                                libraryViewModel.syncSubscribeDataToServer(context, mainViewModel.currentUserId)
                                            }
                                        },
                                        onOpenItemPage = { navigationToSubscribePage(subscribeData, navController) }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }
                    is SubscribeDataState.EmptyData -> {
                        EmptyDataBlock(tipText = "貌似你还没有相关的订阅内容？")
                    }
                }
            }
        }
    }

}

/** “我的库” 顶栏
 *
 */
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun LibraryTopAppBar(
    modifier: Modifier = Modifier,
    userDetail: RequestUserDetail,
    isPremiumUser: Boolean,
    isSearchBarExpand: Boolean,
    searchKeyword: String,
    selectChipState: SubscribeFilterChip,
    onAvatarClickEvent: () -> Unit,
    onSearchActionEvent: (String) -> Unit,
    onSearchDismissEvent: () -> Unit,
    onSelectChipEvent: (SubscribeFilterChip) -> Unit,
    onOpenPremiumPage: () -> Unit,
    onOpenCreatePlaylist: () -> Unit,
) {

    TopAppBar(
        modifier = modifier
            .background(color = Color.Transparent)
            .height(158.dp)
            .statusBarsPadding(),
        backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AppBarTitleRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 2.dp),
                userDetail = userDetail,
                isPremiumUser = isPremiumUser,
                searchKeyword = searchKeyword,
                isSearchBarExpand = isSearchBarExpand,
                onAvatarClickEvent = onAvatarClickEvent,
                onSearchActionEvent = onSearchActionEvent,
                onSearchDismissEvent = onSearchDismissEvent,
                onOpenPremiumPage = onOpenPremiumPage,
                onOpenCreatePlaylist = onOpenCreatePlaylist
            )
            /* Chip Row */
            AnimatedVisibility(
                visible = !isSearchBarExpand,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                TopBarChipRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .padding(start = 12.dp, bottom = 4.dp)
                        .animateContentSize(),
                    currentSelected = selectChipState,
                    onSelectChipEvent = onSelectChipEvent
                )
            }
            AnimatedVisibility(
                visible = isSearchBarExpand,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .animateContentSize()
                ) {
                    SubscribeSearchBar(
                        searchKeyword = searchKeyword,
                        onSearchActionEvent = onSearchActionEvent,
                        onSearchDismissEvent = onSearchDismissEvent,
                    )
                }
            }
        }
    }
}

/** 顶栏功能入口面板
 *
 */
@Composable
fun AppBarTitleRow(
    modifier: Modifier,
    userDetail: RequestUserDetail,
    searchKeyword: String,
    isPremiumUser: Boolean,
    isSearchBarExpand: Boolean,
    onAvatarClickEvent: () -> Unit,
    onSearchActionEvent: (String) -> Unit,
    onSearchDismissEvent: () -> Unit,
    onOpenPremiumPage: () -> Unit,
    onOpenCreatePlaylist: () -> Unit,
) {
    val compositeResult : LottieCompositionResult = rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("lottie/icon_premium_animation.json")
    )
    val progressAnimation by animateLottieCompositionAsState(
        compositeResult.value,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
        speed = 1.0f
    )
    ConstraintLayout(
        modifier = modifier
    ) {
        val (userPanel, buttonGroup) = createRefs()
        /* 用户头像与本页标题 */
        Row(
            modifier = Modifier
                .offset(y = 4.dp)
                .wrapContentHeight()
                .constrainAs(userPanel) {
                    start.linkTo(parent.start, margin = 20.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .background(color = Color.Transparent),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (userDetail.avatar_url.contains("avatar")) LOCAL_SERVER_URL + userDetail.avatar_url else userDetail.avatar_url,
                contentDescription = userDetail.name,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder),
                modifier = Modifier
                    .clip(CircleShape)
                    .size(36.dp)
                    .clickable {
                        onAvatarClickEvent()
                    }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "我的库",
                fontStyle = MaterialTheme.typography.h2.fontStyle,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
            )
        }
        /* Action Button Group */
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .constrainAs(buttonGroup) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end, margin = 12.dp)
                    bottom.linkTo(parent.bottom)
                }
                .background(color = Color.Transparent)
                .animateContentSize()
        ) {
            if (!isPremiumUser) {
                IconButton(onClick = { onOpenPremiumPage() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_premium_unconfirm_logo)  ,
                        contentDescription = "Premium Enter",
                        modifier = Modifier.size( 28.dp),
                        tint = MaterialTheme.colors.onSurface.copy(alpha = .8f)
                    )
                }
            } else {
                IconButton(onClick = { onOpenPremiumPage() }) {
                    LottieAnimation(
                        composition = compositeResult.value,
                        progress = progressAnimation,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            IconButton(onClick = { onOpenCreatePlaylist() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_create_playlist)  ,
                    contentDescription = "Premium Enter",
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colors.onSurface
                )
            }
            /* 搜索按键 */
            IconButton(
                onClick = {
                    if (isSearchBarExpand) onSearchActionEvent(searchKeyword) else onSearchDismissEvent()
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search_my_playlist),
                    contentDescription = "Search My Subscribe Item",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}

/** 订阅项目搜索栏
 *  @param  searchKeyword   关键字字段
 *  @param  onSearchActionEvent 搜索执行事件
 *  @param  onSearchDismissEvent    搜索框滚比事件
 */
@Composable
fun SubscribeSearchBar(
    searchKeyword: String,
    onSearchActionEvent: (String) -> Unit,
    onSearchDismissEvent: () -> Unit,
) {
    BasicTextField(
        value = searchKeyword,
        onValueChange = { onSearchActionEvent(it) },
        cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                /* 搜索事件触发 */
                onSearchActionEvent(searchKeyword)
            }
        ),
        textStyle = MaterialTheme.typography.body1.copy(
            color = MaterialTheme.colors.onSurface
        ),
        decorationBox = { innerTextField ->
            ConstraintLayout(
                modifier = Modifier.fillMaxSize()
            ) {
                val (prefix, trailing, textContent) = createRefs()
                IconButton(
                    modifier = Modifier
                        .alpha(alpha = ContentAlpha.medium)
                        .constrainAs(prefix) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                    ,
                    onClick = {
                        onSearchActionEvent(searchKeyword)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
                Box(
                    modifier = Modifier.constrainAs(textContent) {
                        start.linkTo(prefix.end)
                        top.linkTo(parent.top)
                        end.linkTo(trailing.start)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    },
                ) {
                    if (searchKeyword.isEmpty()) Text(
                        "搜索我的订阅内容",
                        style = LocalTextStyle.current.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                            fontSize = 16.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    innerTextField()
                }
                IconButton(
                    modifier = Modifier
                        .alpha(alpha = ContentAlpha.medium)
                        .constrainAs(trailing) {
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }
                    ,
                    onClick = {
                        onSearchActionEvent("")
                        onSearchDismissEvent()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Icon",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            }
        }
    )
}

/** 订阅项目条目展示形式
 *  @param  subscribe   订阅项目
 *  @param  myFavoritePlaylistId    我的默认喜爱歌单ID
 *  @param  onBindToTop 置顶事件
 *  @param  onOpenItemPage  打开订阅条目内容页
 */
@Composable
fun SubscribeContextItem(
    subscribe: Subscribe,
    myFavoritePlaylistId : Long,
    onBindToTop: (Subscribe) -> Unit,
    onOpenItemPage: (Subscribe) -> Unit,
) {
    val archive = SwipeAction(
        icon = painterResource(id = R.drawable.ic_bind_to_top),
        background = Green400,
        isUndo = true,
        onSwipe = {
            onBindToTop(subscribe)
        },
        weight = .6
    )
    SwipeableActionsBox(
        startActions = listOf(archive)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .height(86.dp)
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.surface)
                .clickable { onOpenItemPage(subscribe) }
        ) {
            val (cover, title, subText) = createRefs()
            val centerGuideLine = createGuidelineFromTop(.5f)
            Surface(
                modifier = Modifier
                    .size(76.dp)
                    .constrainAs(cover) {
                        start.linkTo(parent.start, margin = 18.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                AsyncImage(
                    model = if (subscribe.coverImgUrl.contains("playlist")) LOCAL_SERVER_URL + subscribe.coverImgUrl else subscribe.coverImgUrl,
                    contentDescription = "cover",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_alnum_subscribe),
                    modifier = Modifier.clip(
                        when (subscribe.type) {
                            10 -> { RoundedCornerShape(16.dp) }
                            100 -> { CircleShape }
                            else -> { RoundedCornerShape(0.dp) }
                        }
                    )
                )
            }
            Text(
                text = subscribe.title,
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(title) {
                    start.linkTo(cover.end, margin = 12.dp)
                    bottom.linkTo(centerGuideLine, margin = 2.dp)
                    end.linkTo(parent.end, margin = 4.dp)
                    width = Dimension.fillToConstraints
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight()
                    .constrainAs(subText) {
                        start.linkTo(cover.end, margin = 12.dp)
                        end.linkTo(parent.end, margin = 4.dp)
                        width = Dimension.fillToConstraints
                    }
                    .offset(y = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (subscribe.isTop || (subscribe.itemId == myFavoritePlaylistId && subscribe.type == 1000)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bind_to_top),
                        contentDescription = "置顶",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp),
                        tint = Green400
                    )
                }
                when(subscribe.type) {
                    100 -> {
                        Text(
                            text = "艺人",
                            fontSize = 15.sp,
                            color = MaterialTheme.colors.onSurface.copy(.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    10 -> {
                        Text(
                            text =  "专辑 • " + subscribe.creator,
                            fontSize = 15.sp,
                            color = MaterialTheme.colors.onSurface.copy(.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    else -> {
                        Text(
                            text =  "歌单 • " + subscribe.creator,
                            fontSize = 15.sp,
                            color = MaterialTheme.colors.onSurface.copy(.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/** 订阅项目封面大图展示形式
 *
 */
@Composable
fun SubscribeContextBlock(
    parentWidth: Int,
    subscribe: Subscribe,
    myFavoritePlaylistId: Long,
    onBindToTop: (Subscribe) -> Unit,
    onOpenItemPage: (Subscribe) -> Unit,
) {
    /* 菜单展开状态 */
    val dropMenuExpandState = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width((parentWidth / 2).dp)
            .wrapContentHeight()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onOpenItemPage(subscribe) },
                    onLongPress = { dropMenuExpandState.value = true }
                )
            }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size((parentWidth / 2 - 18).dp)
                .padding(horizontal = 4.dp)
                .padding(top = 4.dp, bottom = 3.dp),
            shape = when (subscribe.type) {
                10 -> {
                    RoundedCornerShape(16.dp)
                }
                100 -> {
                    CircleShape
                }
                else -> {
                    RoundedCornerShape(0.dp)
                }
            }
        ) {
            AsyncImage(
                model = if (subscribe.coverImgUrl.contains("playlist")) LOCAL_SERVER_URL + subscribe.coverImgUrl else subscribe.coverImgUrl,
                contentDescription = "cover",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_alnum_subscribe),
            )
        }
        Text(
            text = subscribe.title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .padding(start = 6.dp)
        )
        DropdownMenu(
            expanded = dropMenuExpandState.value,
            onDismissRequest = { dropMenuExpandState.value = !dropMenuExpandState.value },
            modifier = Modifier.wrapContentHeight()
        ) {
            DropdownMenuItem(onClick = { onBindToTop(subscribe) }) {
                if (subscribe.isTop) {
                    Text(text = "取消置顶")
                } else {
                    Text(text = "置顶")
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 4.dp)
                .padding(start = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (subscribe.isTop || (subscribe.itemId == myFavoritePlaylistId && subscribe.type == 1000)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bind_to_top),
                    contentDescription = "置顶",
                    modifier = Modifier
                        .size(22.dp)
                        .padding(end = 4.dp),
                    tint = Green400
                )
            }
            when(subscribe.type) {
                100 -> {
                    Text(
                        text = "艺人",
                        color = MaterialTheme.colors.onSurface.copy(.6f),
                        fontWeight = FontWeight.Light,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                10 -> {
                    Text(
                        text =  "专辑 • " + subscribe.creator,
                        color = MaterialTheme.colors.onSurface.copy(.6f),
                        fontWeight = FontWeight.Light,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                else -> {
                    Text(
                        text =  "歌单 • " + subscribe.creator,
                        color = MaterialTheme.colors.onSurface.copy(.6f),
                        fontWeight = FontWeight.Light,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Chip Row
 *
 */
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun TopBarChipRow(
    modifier: Modifier = Modifier,
    currentSelected: SubscribeFilterChip,
    onSelectChipEvent: (SubscribeFilterChip) -> Unit,
) {
    val allStateList = listOf(
        SubscribeFilterChip.All,
        SubscribeFilterChip.PlayList,
        SubscribeFilterChip.Album,
        SubscribeFilterChip.Artist
    )

    val playListStateList = listOf(
        SubscribeFilterChip.Clear,
        SubscribeFilterChip.PlayList,
        SubscribeFilterChip.PlayListOfMyCreated
    )

    LazyRow(
        modifier = modifier
    ) {
        when (currentSelected) {
            is SubscribeFilterChip.All -> {
                /* 当前Chip选择器处于 全部 显示的状态 */
                items(
                    items = allStateList,
                    key = { it.logo },
                ) { chip ->
                    FilterChip(
                        selected = chip == currentSelected,
                        onClick = { onSelectChipEvent(chip) },
                        leadingIcon = {
                              Icon(
                                  painter = painterResource(id = chip.logo),
                                  contentDescription = chip.title,
                                  modifier = Modifier
                                      .size(24.dp)
                                      .padding(start = 3.dp)
                              )
                        },
                        colors = ChipDefaults.filterChipColors(selectedBackgroundColor = MaterialTheme.colors.primary),
                        modifier = Modifier
                            .background(Color.Transparent)
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                            .animateItemPlacement()
                    ) {
                        Text(text = chip.title)
                    }
                }
            }
            is SubscribeFilterChip.PlayList -> {
                items(
                    items = playListStateList,
                    key = { it.logo }
                ) { chip ->
                    FilterChip(
                        selected = chip == currentSelected,
                        onClick = {
                            if (chip is SubscribeFilterChip.Clear) {
                                onSelectChipEvent(SubscribeFilterChip.All)  // 此时选中清除按键， 则 状态回归ALL
                            } else {
                                onSelectChipEvent(chip)
                            }
                        },
                        leadingIcon = {
                            if (chip != SubscribeFilterChip.Clear) {
                                Icon(
                                    painter = painterResource(id = chip.logo),
                                    contentDescription = chip.title,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 3.dp)
                                )
                            } else { /* empty */ }
                        },
                        colors = ChipDefaults.filterChipColors(selectedBackgroundColor = MaterialTheme.colors.primary),
                        modifier = Modifier
                            .background(Color.Transparent)
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                            .animateItemPlacement()
                    ) {
                        if (chip is SubscribeFilterChip.Clear) {
                            Icon(painter = painterResource(id = SubscribeFilterChip.Clear.logo), contentDescription = "Clear Selected", modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = chip.title)
                        }
                    }
                }
            }
            else -> {
                item {
                    FilterChip(
                        selected = false,
                        onClick = { onSelectChipEvent(SubscribeFilterChip.All) },
                        colors = ChipDefaults.filterChipColors(selectedBackgroundColor = MaterialTheme.colors.primary),
                        modifier = Modifier
                            .background(Color.Transparent)
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                            .animateItemPlacement(),
                    ) { Icon(painter = painterResource(id = SubscribeFilterChip.Clear.logo), contentDescription = "Clear Selected", modifier = Modifier.size(24.dp)) }
                }
                item {
                    FilterChip(
                        selected = true,
                        onClick = { onSelectChipEvent(currentSelected) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = currentSelected.logo),
                                contentDescription = currentSelected.title,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(start = 3.dp)
                            )
                        },
                        colors = ChipDefaults.filterChipColors(selectedBackgroundColor = MaterialTheme.colors.primary),
                        modifier = Modifier
                            .background(Color.Transparent)
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                            .animateItemPlacement()
                    ) {
                        Text(text = currentSelected.title)
                    }
                }
            }
        }
    }
}

/** 控制展示条目显示形式的控制栏
 *  @param  sortState   订阅项目排序状态
 *  @param  showState   订阅项目展示形式状态
 *  @param  changeSortStateEvent    变更订阅项目排序状态事件
 *  @param  changeShowStateEvent    变更订阅项目展示形式状态事件
 */
@Composable
fun SubscribeSortBar(
    sortState : Boolean,
    showState : Boolean,
    changeSortStateEvent: () -> Unit,
    changeShowStateEvent: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .height(30.dp)
            .fillMaxWidth()
    ) {
        val (sort, sortText, show) = createRefs()
        Icon(
            painter = painterResource(id = R.drawable.ic_sort_my_subscribe_item),
            contentDescription = if (sortState) "订阅类型" else "经常访问",
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .size(18.dp)
                .wrapContentSize()
                .background(color = Color.Transparent)
                .constrainAs(sort) {
                    start.linkTo(parent.start, margin = 12.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { changeSortStateEvent() })
                }
        )
        IconButton(
            onClick = { changeSortStateEvent() },
            modifier = Modifier
                .constrainAs(sortText) {
                    start.linkTo(sort.end, margin = 4.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            AnimatedVisibility(
                visible = sortState,
                enter = slideInVertically(tween(300)) { fullHeight: Int -> -fullHeight } + fadeIn(tween(300)),
                exit = slideOutVertically(tween(300)) { fullHeight: Int -> 2 * fullHeight } + fadeOut(tween(300)),
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { changeSortStateEvent() })
                    }
            ) {
                Text(text = "订阅类型", color = MaterialTheme.colors.onSurface, fontSize = 14.sp)
            }
            AnimatedVisibility(
                visible = !sortState,
                enter = slideInVertically(tween(300)) { fullHeight: Int -> -fullHeight } + fadeIn(tween(300)),
                exit = slideOutVertically(tween(300)) { fullHeight: Int -> 2 * fullHeight } + fadeOut(tween(300)),
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { changeSortStateEvent() })
                    }
            ) {
                Text(text = "经常访问", color = MaterialTheme.colors.onSurface, fontSize = 14.sp)
            }
        }
        IconButton(
            onClick = changeShowStateEvent,
            modifier = Modifier.constrainAs(show) {
                top.linkTo(parent.top)
                end.linkTo(parent.end, margin = 10.dp)
                bottom.linkTo(parent.bottom)
            }
        ) {
            AnimatedVisibility(
                visible = showState,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_show_my_subscribe_1),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(
                visible = !showState,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_show_my_subscribe_0),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/** 跳转到订阅项目对应的页面
 *  @param  subscribe   音频订阅数据对象
 *  @param  navController   导航控制器对象
 */
fun navigationToSubscribePage(
    subscribe: Subscribe,
    navController: NavController
) {
    when (subscribe.type) {
        1000 -> {
            navController.navigate(route = AudioHomeRoute.PlayListScreen.route + "/${subscribe.itemId}")
        }
        100 -> {
            navController.navigate(route = AudioHomeRoute.ArtistScreen.route + "?artist_id=${subscribe.itemId}")
        }
        10 -> {
            navController.navigate(route = AudioHomeRoute.AlbumScreen.route + "?album_id=${subscribe.itemId}")
        }
    }
}


/* 过滤选择 Chip 标识 */
sealed class SubscribeFilterChip(
    val title : String,
    val logo : Int,
) {
    object All : SubscribeFilterChip(title = "全部", logo = R.drawable.ic_all_subscribe)
    object PlayList : SubscribeFilterChip(title = "节目单", logo = R.drawable.ic_playlist_subscribe)
    object Album : SubscribeFilterChip(title = "专辑", logo = R.drawable.ic_alnum_subscribe)
    object Artist : SubscribeFilterChip(title = "艺人", logo = R.drawable.ic_artist_subscribe)
    object PlayListOfMyCreated : SubscribeFilterChip(title = "我创建的", R.drawable.personnel)
    object Clear : SubscribeFilterChip(title = "", logo = R.drawable.ic_clear_common)
}

@Composable
@Preview(showBackground = true)
fun PreviewSubscribeSortBar() {
    val (sortState, sortStateChange) = remember {
        mutableStateOf(false)
    }
    val (showState, showStateChange) = remember {
        mutableStateOf(false)
    }
    SubscribeSortBar(
        sortState = sortState,
        showState = showState,
        changeSortStateEvent = { sortStateChange(!sortState) },
        changeShowStateEvent = { showStateChange(!showState) },
    )
}