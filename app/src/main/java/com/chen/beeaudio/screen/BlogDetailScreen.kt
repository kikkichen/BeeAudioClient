package com.chen.beeaudio.screen

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.ImageLoader
import coil.compose.AsyncImage
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.mock.*
import com.chen.beeaudio.model.blog.Attitude
import com.chen.beeaudio.model.blog.Comment
import com.chen.beeaudio.model.blog.RequestBlog
import com.chen.beeaudio.model.blog.Retweeted
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.navigation.NavigationConfig
import com.chen.beeaudio.screen.widget.ErrorDataTipsWidget
import com.chen.beeaudio.screen.widget.PicGroupBox
import com.chen.beeaudio.screen.widget.RetweetedComboBar
import com.chen.beeaudio.ui.theme.*
import com.chen.beeaudio.utils.TimeUtils
import com.chen.beeaudio.utils.TimeUtils.convertStrToLongTimeUnit
import com.chen.beeaudio.viewmodel.BlogActionState
import com.chen.beeaudio.viewmodel.BlogDetailRequestState
import com.chen.beeaudio.viewmodel.BlogDetailViewModel
import com.chen.beeaudio.viewmodel.MainViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

/** 博文详情页面
 *  @param  currentBlogId   当前动态博文详情页显示博文信息ID
 *  @param  isRetweetedBlog 当前动态博文是否为转发博文
 *  @param  navController   导航控制器对象
 *  @param  blogDetailViewModel 当前博文详情页视图模型ViewModel
 *  @param  mainViewModel   Main ViewModel
 */
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun BlogDetailScreen(
    currentBlogId : Long,
    isRetweetedBlog : Boolean,
    navController: NavController,
    blogDetailViewModel: BlogDetailViewModel = hiltViewModel(),
    mainViewModel: MainViewModel
) {
    /* 当前动态是否为转发博文动态表示 */
    val isCurrentRetweetedBlog = remember { mutableStateOf(isRetweetedBlog) }
    /* 当前博文动态详情状态 */
    val blogDetailState = blogDetailViewModel.currentBlogDetail.collectAsState()

    /* 加载当前博文详细信息 */
    blogDetailViewModel.loadCurrentBlogDetail(currentBlogId)

    /* 转发、评论、点赞 结果数据 */
    val retweetedListForPaging = blogDetailViewModel.retweetedList.collectAsLazyPagingItems()
    val commentListForPaging = blogDetailViewModel.commentList.collectAsLazyPagingItems()
    val attitudeListForPaging = blogDetailViewModel.attitudeList.collectAsLazyPagingItems()

    val pagerState = rememberPagerState()

    /* 转发行为状态 */
    val retweetedActionState = blogDetailViewModel.retweetedState.collectAsState()
    /* 评论行为状态 */
    val commentActionState = blogDetailViewModel.commentState.collectAsState()
    /* 点赞行为状态 */
    val attitudeActionState = blogDetailViewModel.attitudeState.collectAsState()

    /* scaffold脚手架状态 */
    val scaffoldState = rememberScaffoldState()

    /* 当前使用用户对当前动态博文有无点赞记录状态 */
    val isAttitudeRecordState = blogDetailViewModel.isAttitudeRecord.collectAsState()
    /* 查询有无点赞记录 */
    blogDetailViewModel.checkAttitudeRecord(mainViewModel.currentUserId, currentBlogId)

    /* 转发、评论行为触发产生的SnackBar反馈响应提示 */
    LaunchedEffect(
        key1 = retweetedActionState.value,
        key2 = commentActionState.value,
    ) {
        /* 针对不同的执行效果给予提示 */
        if (retweetedActionState.value == BlogActionState.Running || commentActionState.value == BlogActionState.Running) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = "发送中...",
            )
        } else if (retweetedActionState.value is BlogActionState.RetweetedSuccess) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = (retweetedActionState.value as BlogActionState.RetweetedSuccess).data,
                actionLabel = "确认"
            )
        } else if (commentActionState.value is BlogActionState.CommentSuccess) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = (commentActionState.value as BlogActionState.CommentSuccess).data,
                actionLabel = "确认"
            )
        }
    }

    /* 视图UI架构 */
    Scaffold(
        topBar = {
            BlogDetailTopAppBar(
                isAttitude = isAttitudeRecordState.value,
                onPostAttitudeEvent = {
                    blogDetailViewModel.attitudeWork(uid = mainViewModel.currentUserId, bid = currentBlogId)
                },
                onReportEvent = { /*TODO*/ },
                onBackEvent = { navController.navigateUp() }
            )
        },
        bottomBar = {
            BottomSendBar(
                currentBlogId = currentBlogId,
                blogDetailViewModel = blogDetailViewModel,
                pagerState = pagerState,
                onSendEvent = { currentBlogId, pageState ->
                    when (pageState.currentPage) {
                        0 -> {
                            /* 转发博文动态逻辑 */
                            blogDetailViewModel.retweetedWork(
                                uid = mainViewModel.currentUserId,
                                blogId = currentBlogId
                            )
                        }
                        else -> {
                            /* 回复博文动态逻辑 */
                            blogDetailViewModel.commentWork(
                                uid = mainViewModel.currentUserId,
                                bid = currentBlogId,
                                rootId = currentBlogId
                            )
                        }
                    }
                }
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                hostState = it,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    ) {
        val pagerTitle = listOf("转发","评论","点赞")
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues = it)
        ) {
            item {
                when(blogDetailState.value) {
                    is BlogDetailRequestState.Loading -> {
                        LoadingStateBlogBodyPart()
                    }
                    is BlogDetailRequestState.Error -> {
                        ErrorDataTipsWidget(
                            text = "动态博文详情加载遇到了些小问题，\n点击尝试重新加载",
                            modifier = Modifier.fillMaxWidth().height(340.dp),
                            onClickEvent = {
                                blogDetailViewModel.loadCurrentBlogDetail(currentBlogId)
                            }
                        )
                    }
                    is BlogDetailRequestState.Success -> {
                        val blogData = (blogDetailState.value as BlogDetailRequestState.Success).data
                        BlogBodyPart(
                            modifier = Modifier
                                .fillMaxWidth(),
                            navController = navController,
                            blogData = blogData,
                            openUserDetailEvent = {
                                navController.navigate(
                                    route = BlogRoute.UserDetail.route + "?uid=${blogData.User.Uid}"
                                )
                            }
                        )
                    }
                }
            }

            stickyHeader {
                /* TabRow 顶栏 */
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
                            text = if (blogDetailState.value is BlogDetailRequestState.Success) {
                                val blogData = (blogDetailState.value as BlogDetailRequestState.Success).data
                                when (index) {
                                    0 -> {
                                        "$title(${blogData.ReportCounts})"
                                    }
                                    1 -> {
                                        "$title(${blogData.CommentCounts})"
                                    }
                                    else -> {
                                        "$title(${blogData.Attitudes})"
                                    }
                                }
                            } else {
                                pagerTitle[index]
                            },
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
            }
            item {
                HorizontalPager(
                    count = pagerTitle.size,
                    state = pagerState
                ) { page ->
                    when(page) {
                        0 -> {}
                        1 -> {}
                        2 -> {}
                    }
                }
            }
            when (pagerState.currentPage) {
                /* 转发列表 */
                0 -> {
                    retweetedItems(
                        currentBlogId = currentBlogId,
                        isRetweetedBlog = isCurrentRetweetedBlog.value,
                        navController = navController,
                        retweetedListForPaging = retweetedListForPaging,
                        blogDetailViewModel = blogDetailViewModel
                    )
                }
                /* 评论列表 */
                1 -> {
                    commentItems(
                        currentBlogId = currentBlogId,
                        navController = navController,
                        commentListForPaging = commentListForPaging,
                        blogDetailViewModel = blogDetailViewModel
                    )
                }
                /* 点赞列表 */
                2 -> {
                    attitudeItems(
                        currentBlogId = currentBlogId,
                        navController = navController,
                        attitudeListForPaging = attitudeListForPaging,
                        blogDetailViewModel = blogDetailViewModel
                    )
                }
            }
        }
    }
}
/** 转发列表
 *
 */
fun LazyListScope.retweetedItems(
    currentBlogId: Long,
    isRetweetedBlog: Boolean,
    navController: NavController,
    retweetedListForPaging: LazyPagingItems<Retweeted>,
    blogDetailViewModel: BlogDetailViewModel
) {
    /* 加载数据 */
    if (isRetweetedBlog) {
        /* 当前博文动态属于转发动态 */
        blogDetailViewModel.loadRetweetBlogRetweetedList(blogId = currentBlogId)
    } else {
        /* 当前博文动态属于原创动态 */
        blogDetailViewModel.loadOriginalBlogRetweetedList(blogId = currentBlogId)
    }
    if (retweetedListForPaging.itemCount == 0 ) {
        item { EmptyDataBlock("看来还没有人转发，快来搭一楼吧！") }
    } else {
        items(
            items = retweetedListForPaging,
            key = { retweeted ->
                retweeted.bid
            }
        ) { retweeted ->
            retweeted?.let { item ->
                RetweetedItem(
                    retweeted = item,
                    imageLoader = blogDetailViewModel.myImageLoader,
                    itemClickEvent = { /* TODO */ },
                    openUserDetailEvent = {
                        navController.navigate(
                            route = BlogRoute.UserDetail.route + "?uid=${item.uid}"
                        )
                    }
                )
            }
        }
    }
}

/** 评论列表
 *
 */
fun LazyListScope.commentItems(
    currentBlogId: Long,
    navController: NavController,
    commentListForPaging: LazyPagingItems<Comment>,
    blogDetailViewModel: BlogDetailViewModel
) {
    blogDetailViewModel.loadCommentList(blogId = currentBlogId)
    if (commentListForPaging.itemCount == 0) {
        item { EmptyDataBlock("看来还没有人评论，快来搭一楼吧！") }
    } else {
        items(
            items = commentListForPaging,
            key = { comment ->
                comment.cid
            }
        ) { comment ->
            comment?.let { item ->
                CommentItem(
                    comment = item,
                    imageLoader = blogDetailViewModel.myImageLoader,
                    itemClickEvent = { /*TODO*/ },
                    openUserDetailEvent = {
                        navController.navigate(
                            route = BlogRoute.UserDetail.route + "?uid=${item.uid}"
                        )
                    }
                )
            }
        }
    }
}


/** 点赞列表
 *
 */
fun LazyListScope.attitudeItems(
    currentBlogId: Long,
    navController: NavController,
    attitudeListForPaging: LazyPagingItems<Attitude>,
    blogDetailViewModel: BlogDetailViewModel
) {
    blogDetailViewModel.loadAttitudeList(blogId = currentBlogId)
    if (attitudeListForPaging.itemCount == 0 ) {
        item { EmptyDataBlock("看来还没有人评论，给博主点个小红心吧！") }
    } else {
        items(
            items = attitudeListForPaging,
            key = { attitude ->
                attitude.aid
            }
        ) { attitude ->
            attitude?.let { item ->
                AttitudeItem(
                    attitude = item,
                    imageLoader = blogDetailViewModel.myImageLoader,
                    itemClickEvent = { /* TODO */ },
                    openUserDetailEvent = {
                        navController.navigate(
                            route = BlogRoute.UserDetail.route + "?uid=${item.uid}"
                        )
                    },
                )
            }
        }
    }
}

/** 博文详情 -顶栏
 *  @param  isAttitude      是否当前动态博文有过点赞记录
 *  @param  onPostAttitudeEvent 点赞/取消点赞 点击事件
 *  @param  onReportEvent   举报点击事件
 *  @param  onBackEvent     点击返回事件
 */
@Composable
fun BlogDetailTopAppBar(
    isAttitude : Boolean,
    onPostAttitudeEvent : () -> Unit,
    onReportEvent : () -> Unit,
    onBackEvent : () -> Unit,
) {
    /* 更多信息菜单弹出 状态变量 */
    val menuExpanded = remember { mutableStateOf(false) }

    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
            .statusBarsPadding(),
        backgroundColor = MaterialTheme.colors.surface,
        title = { Text(text = "动态正文") },
        navigationIcon = {
            IconButton(
                onClick = { onBackEvent() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回博文动态页",
                    modifier = Modifier
                        .size(28.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = {
                // 为该条博文点赞处理逻辑
                onPostAttitudeEvent()
            }) {
                Icon(
                    painter = painterResource(id = if (isAttitude) R.drawable.ic_blog_get_attitude_solid else R.drawable.ic_blog_get_attitude_empty),
                    contentDescription = "为这条动态点赞",
                    modifier = Modifier.size(20.dp),
                    tint = if (isAttitude) Red400 else MaterialTheme.colors.onSurface
                )
            }
            IconButton(
                onClick = { menuExpanded.value = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多"
                )
                /* 弹出菜单 */
                DropdownMenu(
                    expanded = menuExpanded.value,
                    onDismissRequest = { menuExpanded.value = false }
                ) {
                    DropdownMenuItem(
                        onClick = { onReportEvent() }
                    ) {
                        Text(text = "举报")
                    }
                }
            }
        }
    )
}

/** 博文详情 正文主体
 *  @param  navController   导航控制器对象
 *  @param  blogData    博文数据对象
 *  @param  openUserDetailEvent     进入用户详情页点击事件
 */
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun BlogBodyPart(
    modifier: Modifier = Modifier,
    navController: NavController,
    blogData : RequestBlog,
    openUserDetailEvent: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        // 用户信息栏
        Row(
            modifier = Modifier
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
                .clickable {
                    openUserDetailEvent()
                }
        ) {
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
            ) {
                AsyncImage(
                    model = if (blogData.User.AvatarUrl.contains("avatar")) LOCAL_SERVER_URL + blogData.User.AvatarUrl else blogData.User.AvatarUrl,
                    contentDescription = "This is ${blogData.User.Name}'s avatar. Look, he's so handsome.",
                    modifier = Modifier.background(color = Grey300)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = blogData.User.Name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = blogData.User.Description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 28.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
        // 发布时间 (计算时间差， 生成描述)
        Text(
            modifier = Modifier.padding(start = 14.dp, top = 2.dp, bottom = 2.dp),
            text = "发布自 ${TimeUtils.descriptionBlogTimeByText(Date(convertStrToLongTimeUnit(blogData.Created)).toString())}",
            fontSize = 12.sp,
            color = Grey500
        )
        // 正文栏
        Text(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 4.dp
            ),
            text = blogData.Text
        )

        // 判断博文转发
        if (!isNullOfRequestRetweeted(blogData)) {
            val isRetweeted = false
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 4.dp, bottom = 4.dp, end = 12.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = 0.dp,
                backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
                onClick = {
                    navController.navigate(
                        route = BlogRoute.BlogDetail.route + "?id=${blogData.RetweetedStatus.Id}&isRetweeted=${isRetweeted}"
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 6.dp, top = 6.dp, end = 4.dp, bottom = 4.dp)
                        .clickable { }
                ) {
                    Text(text = "${blogData.RetweetedStatus.User.Name} : \n${blogData.RetweetedStatus.Text}")
                    Spacer(modifier = Modifier.height(4.dp))
                    // 图片组存在判断
                    if (blogData.RetweetedStatus.UrlGroup[0].url.isNotBlank()) {
                        PicGroupBox(
                            userId = blogData.RetweetedStatus.User.Uid,
                            picGroup = blogData.RetweetedStatus.UrlGroup.map { it.url },
                            isRetweetedGroup = true
                        ) { url, userId ->
                            navController.navigate(
                                route = BlogRoute.ImageBrowser.route + "?${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_PIC_GROUP}={${blogData.RetweetedStatus.UrlGroup.map { it.url }}}&${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_CURRENT}={$url}&post_user_id=$userId")
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 10.dp)
                    ) {
                        RetweetedComboBar(
                            blogData.RetweetedStatus.ReportCounts,
                            blogData.RetweetedStatus.CommentCounts,
                            blogData.RetweetedStatus.Attitudes
                        )
                    }
                }
            }
        } else {
            /* Empty */
        }

        // 图片组存在判断
        if (blogData.UrlGroup[0].url.isNotBlank()) {
            PicGroupBox(
                userId = blogData.User.Uid,
                picGroup = blogData.UrlGroup.map { it.url },
                isRetweetedGroup = false
            ) { url, userId ->
                navController.navigate(
                    route = BlogRoute.ImageBrowser.route + "?${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_PIC_GROUP}={${blogData.UrlGroup.map { it.url }}}&${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_CURRENT}={$url}&post_user_id=$userId")
            }
        }
    }
}

/** 加载状态的博文动态正文 - 过渡动画
 *
 */
@Composable
fun LoadingStateBlogBodyPart(
    modifier : Modifier = Modifier
) {
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
    Column(
        modifier = modifier
    ) {
        ConstraintLayout(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
        ) {
            val (avatar, name, subName) = createRefs()
            val startGuideLine = createGuidelineFromStart(0.15f)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .constrainAs(avatar) {
                        start.linkTo(parent.start, margin = 22.dp)
                        top.linkTo(parent.top, margin = 16.dp)
                        end.linkTo(startGuideLine, margin = 10.dp)
                    }
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = alpha))
            )
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(138.dp)
                    .padding(horizontal = 0.dp, vertical = 4.dp)
                    .background(Color.LightGray.copy(alpha = alpha))
                    .constrainAs(name) {
                        start.linkTo(startGuideLine, margin = 8.dp)
                        top.linkTo(parent.top, margin = 16.dp)
                        bottom.linkTo(subName.top, margin = 0.dp)
                    }
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(208.dp)
                    .padding(horizontal = 0.dp, vertical = 4.dp)
                    .background(Color.LightGray.copy(alpha = alpha))
                    .constrainAs(subName) {
                        start.linkTo(startGuideLine, margin = 8.dp)
                        top.linkTo(parent.top, margin = 0.dp)
                        bottom.linkTo(subName.top, margin = 4.dp)
                    }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        for (i in 0 until 5) {
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 20.dp)
                    .background(Color.LightGray.copy(alpha = alpha))
                    .clip(RoundedCornerShape(4.dp)),
            )
        }
        Box(
            modifier = Modifier
                .height(28.dp)
                .width(182.dp)
                .padding(vertical = 4.dp, horizontal = 20.dp)
                .background(Color.LightGray.copy(alpha = alpha))
        )
        Row(modifier = Modifier.padding(horizontal = 18.dp)) {
            Box(
                modifier = Modifier
                    .height(128.dp)
                    .width(128.dp)
                    .padding(vertical = 12.dp, horizontal = 4.dp)
                    .background(Color.LightGray.copy(alpha = alpha))
                    .clip(RoundedCornerShape(12.dp)),
            )
            Box(
                modifier = Modifier
                    .height(128.dp)
                    .width(128.dp)
                    .padding(vertical = 12.dp, horizontal = 4.dp)
                    .background(Color.LightGray.copy(alpha = alpha))
                    .clip(RoundedCornerShape(12.dp)),
            )
        }
    }
}

/** 发送框
 *
 */
@ExperimentalPagerApi
@Composable
fun BottomSendBar(
    currentBlogId: Long,
    blogDetailViewModel: BlogDetailViewModel,
    pagerState: PagerState,
    onSendEvent: (Long, PagerState) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
    ) {
        val (textField, sendButton) = createRefs()
        val endGuideLine = createGuidelineFromEnd(0.2f)
        val text = blogDetailViewModel.replyTextContent.collectAsState().value
        BasicTextField(
            value = text,
            onValueChange = { blogDetailViewModel.changeReplyTextEvent(it) },
            maxLines = 3,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            textStyle = TextStyle(
                color = MaterialTheme.colors.onSurface,
                fontSize = 16.sp,
            ),
            modifier = Modifier.constrainAs(textField) {
               start.linkTo(parent.start, margin = 8.dp)
                top.linkTo(parent.top)
                end.linkTo(endGuideLine, margin = 8.dp)
                bottom.linkTo(parent.bottom, margin = 10.dp)
                width = Dimension.fillToConstraints
            },
            cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
            decorationBox = { innerTextField ->
                ConstraintLayout(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val placeholder = createRef()
                    Box(modifier = Modifier.constrainAs(placeholder){
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom, margin = 10.dp)
                        width = Dimension.fillToConstraints
                    }) {
                        if (text.isEmpty()) Text(
                            text = if (pagerState.currentPage == 0) "在这里写入转发内容" else "在这里填入评论",
                            style = LocalTextStyle.current.copy(
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                fontSize = 16.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        innerTextField()
                    }
                }
            }
        )
        /* 转发、 评论按键 */
        val sendButtonColor by animateColorAsState(if (pagerState.currentPage == 0) Cyan300 else MaterialTheme.colors.primary.copy(alpha = .3f))
        Button(
            /* 发送 转发/评论 内容 */
            onClick = {
                onSendEvent(currentBlogId, pagerState)
            },
            modifier = Modifier
                .height(38.dp)
                .constrainAs(sendButton) {
                    start.linkTo(endGuideLine)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
            colors = ButtonDefaults.buttonColors(backgroundColor = sendButtonColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                painter = painterResource(id = if(pagerState.currentPage == 0) R.drawable.ic_retween_icon else R.drawable.ic_comments_icon),
                contentDescription = if (pagerState.currentPage == 0) "转发当前博文" else "评论当前博文",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/** 转发条目子项
 *  @param  retweeted   转发数据对象
 *  @param  imageLoader Coil图像加载器对象
 *  @param  itemClickEvent  条目单击事件
 */
@Composable
fun RetweetedItem(
    retweeted : Retweeted,
    imageLoader: ImageLoader,
    itemClickEvent: () -> Unit,
    openUserDetailEvent: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (avatar, name, subtext, contentBody) = createRefs()
            val startGuideLine = createGuidelineFromStart(0.15f)
            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .constrainAs(avatar) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top, margin = 12.dp)
                        end.linkTo(startGuideLine)
                    }
                    .clickable { openUserDetailEvent() }
            ) {
                AsyncImage(
                    model = if (retweeted.avatar_url.contains("avatar")) LOCAL_SERVER_URL + retweeted.avatar_url else retweeted.avatar_url,
                    contentDescription = "This is ${retweeted.name}'s avatar. Look, he's so handsome.",
                    modifier = Modifier.background(color = Grey300),
                    imageLoader = imageLoader
                )
            }
            Text(
                text = retweeted.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .constrainAs(name) {
                        start.linkTo(startGuideLine)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end, margin = 12.dp)
                        bottom.linkTo(subtext.top)
                        width = Dimension.fillToConstraints
                    }
            )
            Text(
                text = "${TimeUtils.descriptionBlogTimeByText(Date(convertStrToLongTimeUnit(retweeted.post_at)).toString())} 来自 ${retweeted.source}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 28.dp)
                    .constrainAs(subtext) {
                        start.linkTo(startGuideLine)
                        top.linkTo(name.bottom)
                        end.linkTo(parent.end, margin = 12.dp)
                        width = Dimension.fillToConstraints
                    },
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            // 正文栏
            Text(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .constrainAs(contentBody) {
                        start.linkTo(startGuideLine)
                        top.linkTo(subtext.bottom)
                        end.linkTo(parent.end, margin = 12.dp)
                        bottom.linkTo(parent.bottom, margin = 8.dp)
                        width = Dimension.fillToConstraints
                    },
                text = retweeted.text
            )
        }
    }
}

/** 评论条目子项
 *  @param  comment   转发数据对象
 *  @param  imageLoader Coil图像加载器对象
 *  @param  itemClickEvent  条目单击事件
 */
@Composable
fun CommentItem(
    comment: Comment,
    imageLoader: ImageLoader,
    itemClickEvent: () -> Unit,
    openUserDetailEvent: () -> Unit
) {
    /* 当前评论条目点赞状态 */
    val likeState = remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (avatar, name, subtext, likeButton, contentBody) = createRefs()
            val startGuideLine = createGuidelineFromStart(0.15f)
            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .constrainAs(avatar) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top, margin = 12.dp)
                        end.linkTo(startGuideLine)
                    }
                    .clickable { openUserDetailEvent() }
            ) {
                AsyncImage(
                    model = if (comment.avatar_url.contains("avatar")) LOCAL_SERVER_URL + comment.avatar_url else comment.avatar_url,
                    contentDescription = "This is ${comment.name}'s avatar. Look, he's so handsome.",
                    modifier = Modifier.background(color = Grey300),
                    imageLoader = imageLoader
                )
            }
            Text(
                text = comment.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .constrainAs(name) {
                        start.linkTo(startGuideLine)
                        top.linkTo(parent.top)
                        end.linkTo(likeButton.start, margin = 12.dp)
                        bottom.linkTo(subtext.top)
                        width = Dimension.fillToConstraints
                    }
            )
            Text(
                text = "${TimeUtils.descriptionBlogTimeByText(Date(convertStrToLongTimeUnit(comment.post_at)).toString())} ${comment.source}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 28.dp)
                    .constrainAs(subtext) {
                        start.linkTo(startGuideLine)
                        top.linkTo(name.bottom)
                        end.linkTo(likeButton.start, margin = 12.dp)
                        width = Dimension.fillToConstraints
                    },
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            IconButton(
                onClick = { likeState.value = !likeState.value },
                modifier = Modifier.constrainAs(likeButton) {
                    top.linkTo(parent.top, margin = 2.dp)
                    end.linkTo(parent.end, margin = 6.dp)
                }
            ) {
                Icon(
                    painter = painterResource(id = if (likeState.value) R.drawable.ic_audio_like_confirm else R.drawable.ic_audio_like_unconfirm),
                    contentDescription = "为这条评论",
                    modifier = Modifier.size(24.dp)
                )
            }
            // 正文栏
            Text(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .constrainAs(contentBody) {
                        start.linkTo(startGuideLine)
                        top.linkTo(subtext.bottom)
                        end.linkTo(parent.end, margin = 12.dp)
                        bottom.linkTo(parent.bottom, margin = 8.dp)
                        width = Dimension.fillToConstraints
                    },
                text = comment.text
            )
        }
    }
}

/** 转发条目子项
 *  @param  attitude   转发数据对象
 *  @param  imageLoader Coil图像加载器对象
 *  @param  itemClickEvent  条目单击事件
 */
@Composable
fun AttitudeItem(
    attitude: Attitude,
    imageLoader: ImageLoader,
    itemClickEvent: () -> Unit,
    openUserDetailEvent: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (avatar, name, subtext) = createRefs()
            val startGuideLine = createGuidelineFromStart(0.15f)
            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .constrainAs(avatar) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(startGuideLine)
                        bottom.linkTo(parent.bottom)
                    }
                    .clickable { openUserDetailEvent() }
            ) {
                AsyncImage(
                    model = if (attitude.avatar_url.contains("avatar")) LOCAL_SERVER_URL + attitude.avatar_url else attitude.avatar_url,
                    contentDescription = "This is ${attitude.name}'s avatar. Look, he's so handsome.",
                    modifier = Modifier.background(color = Grey300),
                    imageLoader = imageLoader
                )
            }
            Text(
                text = attitude.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .constrainAs(name) {
                        start.linkTo(startGuideLine)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end, margin = 12.dp)
                        bottom.linkTo(subtext.top)
                        width = Dimension.fillToConstraints
                    }
            )
            Text(
                text = "${TimeUtils.descriptionBlogTimeByText(Date(convertStrToLongTimeUnit(attitude.created_at)).toString())} ${attitude.source}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 28.dp)
                    .constrainAs(subtext) {
                        start.linkTo(startGuideLine)
                        top.linkTo(name.bottom)
                        end.linkTo(parent.end, margin = 12.dp)
                        bottom.linkTo(parent.bottom, margin = 12.dp)
                        width = Dimension.fillToConstraints
                    },
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Composable
fun EmptyDataBlock(
    tipText : String
) {
    Box(
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f)),
        contentAlignment = Alignment.Center
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (icon, tips) = createRefs()
            Icon(
                painter = painterResource(id = R.drawable.ic_blog_empty),
                contentDescription = "没有出现数据",
                tint = MaterialTheme.colors.onSurface.copy(alpha = .4f),
                modifier = Modifier
                    .size(40.dp)
                    .constrainAs(icon) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            )
            Text(
                text = tipText,
                color = MaterialTheme.colors.onSurface.copy(alpha = .4f),
                fontSize = 14.sp,
                modifier = Modifier.constrainAs(tips) {
                    start.linkTo(parent.start)
                    top.linkTo(icon.bottom, margin = 10.dp)
                    end.linkTo(parent.end)
                }
            )
        }
    }
}

/**
 *  判断Retweeted推转为空
 */
fun isNullOfRequestRetweeted(obj : RequestBlog) : Boolean {
    val longZero : Long = 0
    return obj.RetweetedStatus.Id == longZero
}

//@Composable
//@Preview(showBackground = true)
//fun PreviewLoadingStateBlogBodyPart() {
//    BeeAudioTheme {
//        LoadingStateBlogBodyPart(
//            modifier = Modifier
//        )
//    }
//}
//

//@Composable
//@Preview(showBackground = true)
//fun PreviewEmptyDataBlock() {
//    BeeAudioTheme {
//        EmptyDataBlock("看来还没有人转发，快来搭一楼吧！")
//    }
//}