package com.chen.beeaudio.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chen.beeaudio.R
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.screen.widget.ErrorDataTipsWidget
import com.chen.beeaudio.screen.widget.SimpleUserShowItemWidget
import com.chen.beeaudio.viewmodel.FansResultState
import com.chen.beeaudio.viewmodel.FansUserViewModel
import com.chen.beeaudio.viewmodel.MainViewModel

@Composable
fun FansUserScreen(
    userName : String,
    userId : Long,
    navController: NavController,
    mainViewModel: MainViewModel,
    fansUserViewModel: FansUserViewModel = hiltViewModel()
) {
    val fansState = fansUserViewModel.fansUsers.collectAsState()
    fansUserViewModel.loadFansUserList(userId = userId, mainViewModel.currentUserId)
    Scaffold(
        topBar = { FansUserScreenTopBar(userName) { navController.navigateUp() } }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = it)
                .background(color = MaterialTheme.colors.surface)
        ) {
            try {
                if (fansState.value is FansResultState.Success) {
                    val fans = (fansState.value as FansResultState.Success).list
                    if (fans.isEmpty()) {
                        item {
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
                                    text = "貌似一个粉丝也没有...",
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
                    } else {
                        items(
                            items = fans,
                            key = { user ->
                                user.Id
                            }
                        ) { userInfo ->
                            SimpleUserShowItemWidget(
                                user = userInfo,
                                onClickUserEvent = { userId ->
                                    navController.navigate(
                                        route = BlogRoute.UserDetail.route + "?uid=$userId"
                                    )
                                },
                                onFollowOnButtonEvent = {
                                    fansUserViewModel.dealWithFollowAction(myId = mainViewModel.currentUserId, targetUserId = userId)
                                }
                            )
                        }
                    }
                } else if (fansState.value is FansResultState.Error) {
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
                } else if (fansState.value is FansResultState.Loading) {
                    item { ErrorDataTipsWidget(
                        text = "错误导致没有加载出任何动态",
                        modifier = Modifier.fillMaxWidth()
                            .height(120.dp)
                    ) }
                }
            } catch (e : java.lang.NullPointerException) {
                item {
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
                            text = "貌似一个粉丝也没有...",
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
        }
    }
}

/** 正在关注的用户 页面顶栏
 *
 */
@Composable
fun FansUserScreenTopBar(
    userName : String,
    onBackEvent : () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onBackEvent
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回到用户页"
                )
            }
        },
        title = {
            Text(text = "$userName 的粉丝用户")
        },
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier.background(color = Color.Transparent)
            .statusBarsPadding()
    )
}