package com.chen.beeaudio.screen.SendScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.chen.beeaudio.model.blog.TopicItem
import com.chen.beeaudio.R
import com.chen.beeaudio.model.blog.TopicData
import com.chen.beeaudio.ui.theme.*
import com.chen.beeaudio.viewmodel.HotTopicViewModel
import kotlinx.coroutines.flow.catch

/**
 * 获取话题信息
 * @param   netWorkErrorState   网络错误状态，网络状态正常为 true
 * @param   viewModel   该页ViewModel
 */
@Composable
fun loadTopicData(
    netWorkErrorState: MutableState<Boolean>,
    viewModel: HotTopicViewModel
): State<TopicData> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    return produceState(
        initialValue = TopicData(emptyList(), emptyList()),
        viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.loadHotTopicList()
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
 *  热门话题 顶栏
 */
@Composable
fun HotTopScreen(
    navController: NavController,
    viewModel: HotTopicViewModel = hiltViewModel()
) {
    /* 网络状态 */
    val isOpenNetWorkErrorDialog = remember { mutableStateOf(false) }
    NetErrorDialog(isNetworkErrorExist = isOpenNetWorkErrorDialog) {
        navController.navigateUp()
        isOpenNetWorkErrorDialog.value = false
    }

    /* 获取话题 */
    val topicListState : State<TopicData> = loadTopicData(netWorkErrorState = isOpenNetWorkErrorDialog ,viewModel = viewModel)

    Scaffold(
        topBar = { HotTopicTopBar { navController.navigateUp() } },
        content = { padding ->
            if (topicListState.value.topList.isEmpty() and topicListState.value.hotList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = padding)
                ) {
                    /* 置顶 */
                    items(topicListState.value.topList.size) { index ->
                        TopicDataItem(
                            index = index,
                            topic = topicListState.value.topList[index]
                        ) {
                            /* 添加话题逻辑 */
                            navController
                                .previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("topic", topicListState.value.topList[index].title)
                            navController.navigateUp()
                        }
                    }
                    /* 热门 */
                    items(topicListState.value.hotList.size) { index ->
                        TopicDataItem(
                            index = index + 1,
                            topic = topicListState.value.hotList[index]
                        ) {
                            /* 添加话题逻辑 */
                            navController
                                .previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("topic", topicListState.value.hotList[index].title)
                            navController.navigateUp()
                        }
                    }
                }
            }
        }
    )
}

/**
 *  热搜页顶栏
 */
@Composable
fun HotTopicTopBar(
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
                    }.statusBarsPadding()
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
                text = "热门话题",
                modifier = Modifier.clickable {  }
            )
        },
        backgroundColor = MaterialTheme.colors.background
    )
}

/**
 *  热门话题 条目子项
 *  @param  index   序列号
 *  @param  topic   话题
 *  @param  onClickItemEvent    条目点击事件
 */
@Composable
fun TopicDataItem(
    index: Int,
    topic: TopicItem,
    onClickItemEvent: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(42.dp)
            .background(color = MaterialTheme.colors.surface)
            .clickable { onClickItemEvent() }
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (number, shell, topicText, icon, topIcon ,div) = createRefs()
            Box(
                modifier = Modifier.constrainAs(number) {
                    start.linkTo(parent.start, margin = 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(shell.start)
                }
            ) {
                Text(
                    text = "$index",
                    color = MaterialTheme.colors.onSurface.copy(alpha = .7f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(shell) {
                        start.linkTo(number.end, margin = 4.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(topicText.start)
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_topic_vector),
                    contentDescription = "topic: ${topic.title}"
                )
            }

            Box(
                modifier = Modifier.constrainAs(topicText){
                    start.linkTo(shell.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                }
            ) {
                Text(
                    text = topic.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            /* 若为置顶热门话题，则添加TOP tag */
            if (index == 0) {
                Box(
                    modifier = Modifier.constrainAs(topIcon) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 8.dp)
                    }
                ) {
                    Surface(
                        modifier = Modifier.defaultMinSize(minWidth = 26.dp),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "TOP",
                            fontSize = 10.sp,
                            fontWeight = MaterialTheme.typography.h4.fontWeight,
                            color = MaterialTheme.colors.surface,
                            modifier = Modifier.background(color = LightGreen300),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

//            Box(
//                modifier = Modifier.constrainAs(icon) {
//                    top.linkTo(parent.top)
//                    bottom.linkTo(parent.bottom)
//                    end.linkTo(parent.end, margin = 12.dp)
//                }
//            ) {
//                Surface(
//                    modifier = Modifier.defaultMinSize(minWidth = 26.dp),
//                    shape = CircleShape
//                ) {
//                    Text(
//                        text = convertIcon(topic),
//                        fontSize = 10.sp,
//                        fontWeight = MaterialTheme.typography.h4.fontWeight,
//                        color = MaterialTheme.colors.surface,
//                        modifier = Modifier.background(
//                            color = if (topic.icon == "新") LightBlue300 else if (topic.icon == "热") Red500 else if (topic.icon == "爆" || topic.icon == "沸") Orange700 else MaterialTheme.colors.surface
//                        ),
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }

            Divider(
                modifier = Modifier
                    .height(1.dp)
                    .constrainAs(div) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            )
        }
    }
}

@Composable
fun NetErrorDialog (
    isNetworkErrorExist: MutableState<Boolean>,
    onConfirmEvent: () -> Unit
) {
    if (isNetworkErrorExist.value) {
        AlertDialog(
            title = { Text(text = "网络异常") },
            text = { Text(text = "哦漏！看来网络出了岔子") },
            onDismissRequest = { isNetworkErrorExist.value = false },
            dismissButton = {
                TextButton(
                    enabled = false,
                    onClick = {  }
                ) {
                    Text("取消")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmEvent()
                        isNetworkErrorExist.value = false
                    }
                ) {
                    Text("确认")
                }
            }
        )
    }
}

fun convertIcon(topic: TopicItem) : String{
    return if (topic.icon == "新") "NEW" else if (topic.icon == "热") "HOT" else topic.icon
}

@Composable
@Preview
fun PreviewHotTopicScreen() {
    BeeAudioTheme {
        TopicDataItem(0, TopicItem("广州多区宣布解除疫情防控临时管控区", "广州多区宣布解除疫情防控临时管控区", "新"), {  })
    }
}