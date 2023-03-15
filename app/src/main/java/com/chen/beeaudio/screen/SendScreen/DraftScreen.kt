package com.chen.beeaudio.screen.SendScreen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import com.chen.beeaudio.R
import com.chen.beeaudio.model.blog.BlogDraft
import com.chen.beeaudio.viewmodel.DraftViewModel

@Composable
fun loadDraftsData(
    viewModel: DraftViewModel
): State<List<BlogDraft>> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    return produceState(
        initialValue = emptyList(),
        viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.getAllDrafts().collect {
                value = it
            }
        }
    }
}

/**
 *  草稿箱页
 */
@ExperimentalMaterialApi
@Composable
fun DraftScreen(
    navController: NavController,
    viewModel: DraftViewModel = hiltViewModel()
) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    /* 草稿条目列表 状态 */
    val draftItemList = loadDraftsData(viewModel = viewModel)

    /* 清空草稿箱 前提示弹窗 打开状态 */
    val isClearAlertDialog = remember { mutableStateOf(false) }

    /* 清空草稿箱警告弹窗 */
    ClearDraftDialog(isClearAlert = isClearAlertDialog) {
        /* 清空草稿箱逻辑 */
        scope.launch {
            viewModel.clearDraftBox()
        }
    }

    /* 草稿详情展开 */
    var expendedDraft by remember { mutableStateOf<BlogDraft?>(null) }

    Scaffold(
        topBar = {
            DraftScreenTopBar(
                draftList = draftItemList.value,
                onBackEvent = { navController.navigateUp() },      /* 返回到SendScreen */
                onClearDraftsEvent = { isClearAlertDialog.value = true }
            )
        },
        content = { padding ->
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                state = lazyListState,
                modifier = Modifier
                    .padding(padding)
                    .background(color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f))
                    .animateContentSize()
            ) {
                items(count = draftItemList.value.size) { i ->
                    val draftItem = draftItemList.value.getOrNull(i)
                    if (draftItem != null) {
                        key(draftItem) {
                            SingleDraftItem(
                                draftItem = draftItem,
                                expended = expendedDraft == draftItem,
                                onClickItem = { expendedDraft = if (expendedDraft == draftItem) null else draftItem },
                                onSelectDraftItem = { item ->
                                    scope.launch { viewModel.deleteSingleDraft(item) }
                                    navController
                                        .previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("the_draft", item.wroteContext)
                                    navController.navigateUp()
                                }
                            ) {
                                scope.launch {
                                    viewModel.deleteSingleDraft(draftItem)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

/**
 *  草稿箱页 标题栏
 *  @param  draftList   草稿列表
 *  @param  onBackEvent 返回事件
 *  @param  onClearDraftsEvent  清空列表按钮事件
 */
@Composable
fun DraftScreenTopBar(
    draftList: List<BlogDraft>,
    onBackEvent: () -> Unit,
    onClearDraftsEvent: () -> Unit
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
                text = "草稿箱",
                modifier = Modifier.clickable {
                    Log.d("_chen", "现在的草稿条目数量： ${draftList.size}")
                }
            )
        },
        actions = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 12.dp)
                    .clip(CircleShape)
                    .clickable { onClearDraftsEvent() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clear_all_draft),
                    contentDescription = "Clear All the Drafts",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
        },
        backgroundColor = MaterialTheme.colors.background
    )
}

/**
 *  单个草稿项目
 *  @param  draftItem   草稿信息条目对象
 *  @param  expended    草稿条目展开状态
 *  @param  onSelectDraftItem   草稿点击选中事件
 *  @param  onClickItem 点击条目事件
 *  @param  onRemoveAction  删除当前草稿条目行为回调
 */
@ExperimentalMaterialApi
@Composable
fun SingleDraftItem(
    draftItem: BlogDraft,
    expended: Boolean,
    onClickItem: () -> Unit,
    onSelectDraftItem: (BlogDraft) -> Unit,
    onRemoveAction: () -> Unit
) {
    AnimatedVisibility(visible = expended) {
        Spacer(modifier = Modifier.height(8.dp))
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (expended) 4.dp else 0.dp)
            .swipeToDismiss { onRemoveAction() }
        ,        /* 添加 滑动移除 的动作效果逻辑 */
        elevation = if (expended) 4.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(modifier = Modifier
                .size(32.dp)
                .align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit_write_draft),
                    contentDescription = "Adapt this draft saved on ${draftItem.wroteTime}",
                    modifier = Modifier
                        /* 携带草稿正文返回 */
                        .clickable {
                            onSelectDraftItem(draftItem)
                        }
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                if (expended) {
                    Text(
                        text = draftItem.wroteContext,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                            .clickable { onClickItem() }
                    )
                } else {
                    Text(
                        text = draftItem.wroteContext,
                        style = MaterialTheme.typography.body1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable { onClickItem() }
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = draftItem.wroteTime,
                    fontSize = 12.sp,
                    color =  MaterialTheme.colors.onSurface.copy(alpha = .7f)
                )
            }
        }
    }
    AnimatedVisibility(visible = expended) {
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 *  修饰符元素扩展函数 为该元素添加水平滑动行为
 *  @param  onDismissed 滑动行为回调函数
 */
private fun Modifier.swipeToDismiss(
    onDismissed: () -> Unit
): Modifier = composed{
    /* 该元素水平偏移量的 动画 数据 */
    val offsetX = remember { Animatable(0f) }
    pointerInput(Unit) {
        /* 通过计算该触发动画的初始位置获得衰减参数 */
        val decay = splineBasedDecay<Float>(this)
        /* 启动协程来使用花旗函数处理触发事件与东湖 */
        coroutineScope {
            while (true) {
                /* 等待一个触发事件 */
                val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                /* 打断将要进行的动画 */
                offsetX.stop()
                /* 为拖动事件记录触发的速度 */
                val velocityTracker = VelocityTracker()
                /* 等待拖动事件 */
                awaitPointerEventScope {
                    horizontalDrag(pointerId) { change ->
                        /* 记录偏移后位置 */
                        val horizontalDragOffset = offsetX.value + change.positionChange().x
                        launch {
                            /* 当元素正在被拖动时 应用其偏移量，更新它的实时位置 */
                            offsetX.snapTo(horizontalDragOffset)
                        }
                        /* 记录拖动的速度 */
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        /* 消费手势事件， 不需要处理额外的事件 */
                        if (change.positionChange() != Offset.Zero) change.consume()
                    }
                }
                /* 拖动手势结束时， 计算惯性的速度 */
                val velocity = velocityTracker.calculateVelocity().x
                /* 计算元素由于拖动后惯性影响造成的偏移量，得到目标偏移位置 */
                val targetOffset = decay.calculateTargetValue(offsetX.value, velocity)
                /* 为该元素设置拖动触发时的边界值 */
                offsetX.updateBounds(
                    lowerBound = -size.width.toFloat(),
                    upperBound = size.width.toFloat()
                )
                launch {
                    if (targetOffset.absoluteValue <= size.width) {
                        /* 初速度未达到其边界值， 该元素滑回原处 */
                        offsetX.animateTo(targetValue = 0f, initialVelocity = velocity)
                    } else {
                        /* 达到初速度，动画启动，惯性参数应用其衰减 */
                        offsetX.animateDecay(velocity, decay)
                        /* 响应滑动触发的事件 */
                        onDismissed()
                    }
                }
            }
        }
    }
        /* 为该元素应用水平偏移量 */
        .offset {
            IntOffset(offsetX.value.roundToInt(), 0)
        }
}

/**
 *  清空列表对话框
 *  @param  isClearAlert    警告对话窗口打开状态
 *  @param  onConfirmEvent  确认按钮事件
 */
@Composable
fun ClearDraftDialog(
    isClearAlert: MutableState<Boolean>,
    onConfirmEvent: () -> Unit
) {
    if (isClearAlert.value) {
        AlertDialog(
            title = { Text(text = "清空草稿箱") },
            text = { Text(text = "您再三斟酌一下， 确定要清空草稿箱吗？") },
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


@ExperimentalMaterialApi
@Composable
@Preview
fun PreviewDraftScreen() {
    BeeAudioTheme {
        val controller = rememberNavController()
        DraftScreen(controller)
    }
}