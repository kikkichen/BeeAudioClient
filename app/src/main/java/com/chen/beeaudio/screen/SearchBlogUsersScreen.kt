package com.chen.beeaudio.screen

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.chen.beeaudio.ErrorOccurredWidget
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.model.blog.RequestUser
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.screen.widget.BlogItemForRequestBlog
import com.chen.beeaudio.viewmodel.BlogSearchState
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.SearchBlogAndUserViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun SearchBlogUserScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    searchBlogAndUserViewModel: SearchBlogAndUserViewModel = hiltViewModel()
) {
    /* 上下文 */
    val context : Context = LocalContext.current

    /* TabRow 标题列表 */
    val pagerTitle = listOf("博文动态", "用户")

    val blogsState = searchBlogAndUserViewModel.targetBlogs.collectAsState()
    val usersState = searchBlogAndUserViewModel.targetUsers.collectAsState()

    /* 搜索关键字 */
    val text = searchBlogAndUserViewModel.searchKeywords.collectAsState()

    /* scaffold脚手架状态 */
    val scaffoldState : ScaffoldState = rememberScaffoldState()

    /* 协程域 */
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            SearchBlogAndUserWidget(
                text = text.value,
                onTextChange = {
                    searchBlogAndUserViewModel.setSearchKeyword(it)
                },
                onSearchClicked = {
                    if (text.value == "" || text.value.isEmpty() || text.value.isBlank()) {
                        coroutineScope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = "\uD83D\uDE15 搜索框没有一点关键字呢"
                            )
                        }
                    } else {
                        searchBlogAndUserViewModel.loadSearchBlogs(it)
                        searchBlogAndUserViewModel.loadSearchUsers(it)
                    }
                },
                onCloseClicked = {
                    navController.navigateUp()
                }
            )
        },
        scaffoldState = scaffoldState
    ) {
        Column(
            modifier = Modifier.padding(it)
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
                        /* 博文搜索结果 */
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colors.surface)
                        ) {
                            when(blogsState.value) {
                                is BlogSearchState.None -> {
                                    item {
                                        NoneDataStateBlock(
                                            text = "快来搜索你想看到的动态吧！",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 58.dp)
                                        )
                                    }
                                }
                                is BlogSearchState.Error -> {
                                    item {
                                        ErrorOccurredWidget(
                                            onRetryRequestDataEvent = {
                                                if (text.value == "" || text.value.isEmpty() || text.value.isBlank()) {
                                                    coroutineScope.launch {
                                                        scaffoldState.snackbarHostState.showSnackbar(
                                                            message = "\uD83D\uDE15 搜索框没有一点关键字呢"
                                                        )
                                                    }
                                                } else {
                                                    searchBlogAndUserViewModel.loadSearchBlogs(searchBlogAndUserViewModel.searchKeywords.value)
                                                }
                                            }
                                        )
                                    }
                                }
                                is BlogSearchState.Loading -> {
                                    item {
                                        ConstraintLayout(
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            val circle = createRef()
                                            CircularProgressIndicator(
                                                modifier = Modifier
                                                    .constrainAs(circle) {
                                                        start.linkTo(parent.start)
                                                        top.linkTo(parent.top)
                                                        end.linkTo(parent.end)
                                                    }
                                                    .padding(top = 58.dp)
                                            )
                                        }
                                    }
                                }
                                is BlogSearchState.BlogSearchSuccess -> {
                                    val listData = (blogsState.value as BlogSearchState.BlogSearchSuccess).list
                                    if (listData.isNotEmpty()) {
                                        items(
                                            items = listData,
                                            key = { blog ->
                                                blog.Id
                                            }
                                        ) { blog ->
                                            BlogItemForRequestBlog(
                                                navController = navController,
                                                blogData = blog,
                                                onPlayEvent = { track ->
                                                    mainViewModel.playTargetAudio(track = track, context = context)
                                                }
                                            )
                                        }
                                    } else {
                                        item {
                                            DataEmptyBlock(
                                                text = "貌似什么都没搜到...",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 58.dp)
                                                    .size(48.dp)
                                            )
                                        }
                                    }
                                }
                                else -> { /* NotToDo */ }
                            }
                        }
                    }
                    1 -> {
                        /* 用户搜索结果 */
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colors.surface)
                        ) {
                            when(usersState.value) {
                                is BlogSearchState.None -> {
                                    item {
                                        NoneDataStateBlock(
                                            text = "快来搜索你感兴趣到的人吧！",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 58.dp)
                                        )
                                    }
                                }
                                is BlogSearchState.Error -> {
                                    item {
                                        ErrorOccurredWidget(
                                            onRetryRequestDataEvent = {
                                                if (text.value == "" || text.value.isEmpty() || text.value.isBlank()) {
                                                    coroutineScope.launch {
                                                        scaffoldState.snackbarHostState.showSnackbar(
                                                            message = "\uD83D\uDE15 搜索框没有一点关键字呢"
                                                        )
                                                    }
                                                } else {
                                                    searchBlogAndUserViewModel.loadSearchUsers(searchBlogAndUserViewModel.searchKeywords.value)
                                                }
                                            }
                                        )
                                    }
                                }
                                is BlogSearchState.Loading -> {
                                    item {
                                        ConstraintLayout(
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            val circle = createRef()
                                            CircularProgressIndicator(
                                                modifier = Modifier
                                                    .constrainAs(circle) {
                                                        start.linkTo(parent.start)
                                                        top.linkTo(parent.top)
                                                        end.linkTo(parent.end)
                                                    }
                                                    .padding(top = 58.dp)
                                            )
                                        }
                                    }
                                }
                                is BlogSearchState.UserSearchSuccess -> {
                                    val listData = (usersState.value as BlogSearchState.UserSearchSuccess).list
                                    if (listData.isNotEmpty()) {
                                        items(
                                            items = listData,
                                            key = { user ->
                                                user.Uid
                                            }
                                        ) { user ->
                                            SearchUserItem(
                                                user = user,
                                                onClickUserEvent = {
                                                    navController.navigate(
                                                        route = BlogRoute.UserDetail.route + "?uid=${user.Uid}"
                                                    )
                                                }
                                            )
                                        }
                                    } else {
                                        item {
                                            DataEmptyBlock(
                                                text = "貌似什么都没搜到...",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 58.dp)
                                            )
                                        }
                                    }
                                }
                                else -> { /* NotToDo */ }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBlogAndUserWidget(
    text: String,
    onTextChange: (String) -> Unit,
    onSearchClicked: (String) -> Unit,
    onCloseClicked: () -> Unit
) {
    TopAppBar(
        modifier = Modifier
            .background(color = MaterialTheme.colors.surface)
            .statusBarsPadding(),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "SearchWidget"
                }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            elevation = 0.dp,
            color = MaterialTheme.colors.onSurface.copy(alpha = .1f),
            shape = CircleShape
        ) {
            BasicTextField(
                value = text,
                onValueChange = {
                    onTextChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "TextField"
                    }
                    .height(50.dp)
                    .background(color = Color.Transparent),
                textStyle = TextStyle(
                    color = MaterialTheme.colors.onSurface,
                    fontSize = 16.sp,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchClicked(text)
                    }
                ),
                cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
                decorationBox = { innerTextField ->
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val (leading, trailing, textContent) = createRefs()
                        IconButton(
                            modifier = Modifier
                                .constrainAs(leading) {
                                    start.linkTo(parent.start)
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                }
                                .semantics {
                                    contentDescription = "CloseButton"
                                },
                            onClick = {
                                if (text.isNotEmpty()) {
                                    onTextChange("")
                                } else {
                                    onCloseClicked()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Close Icon",
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier.constrainAs(textContent) {
                                start.linkTo(leading.end)
                                top.linkTo(parent.top)
                                end.linkTo(trailing.start)
                                bottom.linkTo(parent.bottom)
                                width = Dimension.fillToConstraints
                            },
                        ) {
                            if (text.isEmpty()) Text(
                                "输入搜索关键字",
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
                                onSearchClicked(text)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                    }
                }
            )
        }
    }
}

@Composable
fun SearchUserItem(
    user: RequestUser,
    onClickUserEvent: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = 2.dp,
        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clickable {
                onClickUserEvent()
            }
    ) {
        Surface(
            modifier = Modifier
                .height(62.dp)
                .fillMaxWidth()
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxWidth()
            ) {
                val (avatar, userInfo, spacer) = createRefs()
                val barrier = createEndBarrier(userInfo)
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .constrainAs(avatar) {
                            start.linkTo(parent.start, margin = 8.dp)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                        .padding(3.dp),
                    shape = CircleShape,
                    border = BorderStroke(2.dp, MaterialTheme.colors.primary),
                ) {
                    AsyncImage(
                        model = if (user.AvatarUrl.contains("avatar")) LOCAL_SERVER_URL + user.AvatarUrl else user.AvatarUrl,
                        contentDescription = "$'s avatar",
                        placeholder = painterResource(id = R.drawable.personnel)
                    )
                }
                Column(
                    modifier = Modifier
                        .constrainAs(userInfo) {
                            start.linkTo(avatar.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                        .fillMaxWidth(0.75f)
                        .padding(
                            horizontal = 10.dp
                        ),
                ) {
                    Text(
                        text = user.Name,
                        fontStyle = MaterialTheme.typography.h2.fontStyle,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .align(Alignment.Start)
                            .weight(1f)
                    )
                    Text(
                        text = user.Description,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colors.onSurface.copy(.6f),
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 4.dp)
                            .align(Alignment.Start)
                            .weight(1f)
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(12.dp)
                        .constrainAs(spacer) {
                            start.linkTo(barrier)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                        }
                )
            }
        }
    }
}

/* 数据空显示 */
@Composable
fun NoneDataStateBlock(
    text: String,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (emptyIcon, textBody) = createRefs()
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Data empty",
            modifier = Modifier
                .size(36.dp)
                .constrainAs(emptyIcon) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
            tint = MaterialTheme.colors.onSurface.copy(alpha = .7f)
        )
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSurface.copy(alpha = .4f),
            fontWeight = FontWeight.Light,
            fontSize = 14.sp,
            modifier = Modifier.constrainAs(textBody) {
                start.linkTo(parent.start)
                top.linkTo(emptyIcon.bottom, margin = 12.dp)
                end.linkTo(parent.end)
            }
        )
    }
}

/* 数据空显示 */
@Composable
fun DataEmptyBlock(
    text: String,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (emptyIcon, textBody) = createRefs()
        Icon(
            painter = painterResource(id = R.drawable.ic_blog_empty),
            contentDescription = "Data empty",
            modifier = Modifier
                .size(36.dp)
                .constrainAs(emptyIcon) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
            tint = MaterialTheme.colors.onSurface.copy(alpha = .7f)
        )
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSurface.copy(alpha = .4f),
            fontWeight = FontWeight.Light,
            fontSize = 14.sp,
            modifier = Modifier.constrainAs(textBody) {
                start.linkTo(parent.start)
                top.linkTo(emptyIcon.bottom, margin = 12.dp)
                end.linkTo(parent.end)
            }
        )
    }
}