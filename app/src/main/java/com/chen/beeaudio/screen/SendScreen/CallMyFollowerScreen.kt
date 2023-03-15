package com.chen.beeaudio.screen.SendScreen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.chen.beeaudio.model.blog.SimpleUser
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.R
import com.chen.beeaudio.viewmodel.CallFollowerViewModel
import com.chen.beeaudio.viewmodel.MainViewModel
import kotlinx.coroutines.flow.catch

@Composable
fun loadFollowerData(
    netWorkErrorState: MutableState<Boolean>,
    mainViewModel: MainViewModel,
    viewModel: CallFollowerViewModel,
): State<List<SimpleUser>> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    return produceState(
        initialValue = emptyList(), viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.loadFollowerList(mainViewModel.currentUserId, mainViewModel.currentUserId)
                .catch { e : Throwable ->
                    Log.d("_chen", "网络异常：\n$e")
                    netWorkErrorState.value = true
                }.collect {
                    value = it
                }
        }
    }
}

/**
 *  At @ 我关注的好友 页面
 */
@Composable
fun CallMyFollowerScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    viewModel: CallFollowerViewModel = hiltViewModel()
) {
    /* 网络状态 */
    val isOpenNetWorkErrorDialog = remember { mutableStateOf(false) }
    NetErrorDialog(isNetworkErrorExist = isOpenNetWorkErrorDialog) {
        navController.navigateUp()
        isOpenNetWorkErrorDialog.value = false
    }

    /* 关注列表状态 */
    val followerList: State<List<SimpleUser>> = loadFollowerData(
        netWorkErrorState = isOpenNetWorkErrorDialog,
        mainViewModel = mainViewModel,
        viewModel = viewModel
    )

    Scaffold(
        topBar = {
            CallMyFollowerTopBar { navController.navigateUp() }
        },
        content = {
            if (followerList.value.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues = it)
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(8.dp),
                    userScrollEnabled = true,
                    modifier = Modifier.padding(paddingValues = it)
                ) {
                    items(followerList.value) { item ->
                        CallFollowerItem(user = item) {
                            navController
                                .previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("at_a_friend", item.name)
                            navController.navigateUp()
                        }
                    }
                }
            }
        }
    )
}

/**
 *  在SendScreen中 @ 一位好友 顶栏
 */
@Composable
fun CallMyFollowerTopBar(
    onBackEvent: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        navigationIcon = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable {
                        onBackEvent()  /* 触发返回操作 */
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back To Send Blog Page",
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center)
                )
            }
        },
        title = {
            Text(
                text = "选择 @ 的用户"
            )
        },
        backgroundColor = MaterialTheme.colors.background
    )
}

/**
 *  单条关注用户子项
 */
@Composable
fun CallFollowerItem(
    user: SimpleUser,
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
                        model = user.avatar,
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
                        text = user.name,
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
                        text = user.description,
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

@Composable
@Preview
fun PreviewCallMyFollowerScreen() {
    BeeAudioTheme {
        CallFollowerItem(
            SimpleUser(
                Id = 10001,
                name = "_LUNAX_",
                description = "取关请单向( ̀⌄ ́)取关请单向( ̀⌄ ́)取关请单向( ̀⌄ ́)取关请单向( ̀⌄ ́)取关请单向( ̀⌄ ́)取关请单向( ̀⌄ ́)取关请单向( ̀⌄ ́)取关请单向( ̀⌄ ́)取关请单向( ̀⌄ ́)取关请单向( ̀⌄ ́)",
                avatar = "https://tvax1.sinaimg.cn/crop.0.0.600.600.180/002xIcmply8gu8cvrn7zoj60go0gowfx02.jpg?KID=imgbed,tva&Expires=1669833535&ssig=YsIB9MzfVN",
                createAt = "",
                followState = 1
            )
        ) { }
    }
}