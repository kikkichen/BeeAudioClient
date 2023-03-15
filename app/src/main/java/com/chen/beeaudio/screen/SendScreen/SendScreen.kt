package com.chen.beeaudio.screen.SendScreen

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.utils.TextLengthCounter
import com.chen.beeaudio.viewmodel.SendViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.model.audio.*
import com.chen.beeaudio.model.blog.BlogDraft
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.navigation.AudioHomeRoute
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.ui.theme.Amber100
import com.chen.beeaudio.utils.TimeUtils.getCurrentTime
import com.chen.beeaudio.viewmodel.BlogUploadState
import com.chen.beeaudio.viewmodel.MainViewModel
import com.google.gson.Gson

@ExperimentalMaterialApi
@SuppressLint("StateFlowValueCalledInComposition", "MutableCollectionMutableState")
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SendScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    viewModel: SendViewModel = hiltViewModel()
) {
    /* 从草稿页获取草稿 */
    val resultFromDraft = navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String?>("the_draft")?.observeAsState()

    /* 话题页获取话题 */
    val resultFromTopic = navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String?>("topic")?.observeAsState()

    /* @好友页获取好友ID */
    val resultFromCallFriend = navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String?>("at_a_friend")?.observeAsState()

    /* 分享音乐页获取音频项目对象 */
    val resultFromShareMusic = navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String?>("share_item")?.observeAsState()

    val scope = rememberCoroutineScope()
    /* 返回按键调度器 */
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    /* 输入文本 */
    val (textContext, setTextContext) = remember { mutableStateOf(viewModel.text.value) }
    /* 若草稿变量非空，则向文本中填充草稿 */
    if (resultFromDraft?.value != null) {
        viewModel.text.value = resultFromDraft.value!!  /* 变更viewModel中的值 */
        setTextContext(resultFromDraft.value!!)         /* 变更textContext状态中的值 */
        navController.currentBackStackEntry?.savedStateHandle?.set("the_draft", null)       /* 变更正文值结束后重置草稿返回键值对为空 */
    }
    /* 若话题变量非空，则向文本头填充话题 */
    if (resultFromTopic?.value != null) {
        ("#${resultFromTopic.value}# " + viewModel.text.value).apply {
            viewModel.text.value = this
            setTextContext(this)
            navController.currentBackStackEntry?.savedStateHandle?.set("topic", null)
        }
    }
    /* 若@好友变量非空，则向文本头填充话题 */
    if (resultFromCallFriend?.value != null) {
        (viewModel.text.value + " @${resultFromCallFriend.value} ").apply {
            viewModel.text.value = this
            setTextContext(this)
            navController.currentBackStackEntry?.savedStateHandle?.set("at_a_friend", null)
        }
    }
    /* 若音频分享数据非空, 则向MediaData中填充数据 */
    if (resultFromShareMusic?.value != null) {
        viewModel.changeMediaData(resultFromShareMusic.value ?: "")
        navController.currentBackStackEntry?.savedStateHandle?.set("share_item", null)
    }
    /* 文本输入框焦点 */
    val focusRequester = remember { FocusRequester() }
    /* 选择图片队列 */
    val imageUris: State<MutableList<String>> = viewModel.images.collectAsState()
    /* 多图选择器 */
    val multiplePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) {
//        imageUris.value.clear()
        viewModel.images.value.clear()
        for (item in it) {
//            imageUris.value.add(item.toString())
            viewModel.images.value.add(item.toString())
        }
    }

    /* 进入图畔删除模式状态 */
    var isDeletePicMode by remember { mutableStateOf(false) }
    if (imageUris.value.isEmpty()) isDeletePicMode = false

    /* 弹出设置小尾巴 输入对话框 */
    val isOpenSettingTailState = remember { mutableStateOf(false) }
    StartInputDialog(isOpenSettingTailState)

    /* 未处理文本保存为草稿 对话弹窗 */
    val isSaveToDraftBoxDialog = remember { mutableStateOf(false) }
    SaveAsDraftDialog(
        navController = navController,
        isSaveAsDialogState = isSaveToDraftBoxDialog,
        onConfirmEvent = {
            scope.launch {
                viewModel.saveAsDraft(
                    BlogDraft(
                        wroteTime = getCurrentTime(),
                        wroteContext = textContext
                    )
                )
            }
            setTextContext("")
            viewModel.text.value = ""
        }
    )

    /* 音频分享数据 */
    val mediaShareDataState = viewModel.mediaData.collectAsState()

    /* Emoji 表情软键盘 启动状态*/
    val isOpenEmojiKeyBoard = remember { mutableStateOf(false) }

    /* 动态博文发布状态 */
    val sentState = viewModel.blogUploadState.collectAsState()

    /* 动态博文发布状态提示对话框 */
    SentProcessDialog(
        navController = navController,
        imageSize = imageUris.value.size,
        viewModel = viewModel,
        sentState = sentState.value
    )

    /* 上下文  */
    val context : Context = LocalContext.current

    Scaffold(
        topBar = {
            SendScreenBar(
                navController = navController,
                userDetail = mainViewModel.currentUserDetailInfo.value
            )
        },
        content = {
            Box(
                Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(paddingValues = it)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 14.dp)
                    .verticalScroll(rememberScrollState())
                ) {
                    Box {
                        this@Column.AnimatedVisibility(
                            visible = textContext.isEmpty(),
                            enter = fadeIn(tween(200)),
                            exit = fadeOut(tween(200))
                        ) {
                            Text(text = "说点什么吧...", color = MaterialTheme.colors.onSurface.copy(alpha = .5f), fontStyle = MaterialTheme.typography.body1.fontStyle)
                        }
                        /* 多行文本框 */
                        BasicTextField(
                            value = textContext,
                            onValueChange = { words ->
                                setTextContext(words)
                                viewModel.text.value = words
                            },
                            textStyle = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSurface),
                            singleLine = false,
                            maxLines = Int.MAX_VALUE,
                            enabled = true,
                            cursorBrush = SolidColor(MaterialTheme.colors.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 200.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        isOpenEmojiKeyBoard.value = false
                                        isDeletePicMode = false
                                    }
                                },
//                            decorationBox = { innerTextField ->
//                             q
//                            }
                        )
                    }
                    /* 音频项目分享 */
                    ShareMusicBlockCard(
                        visible = (mediaShareDataState.value.isNotEmpty() and mediaShareDataState.value.isNotBlank()),
                        rawJson = mediaShareDataState.value,
                        navController = navController,
                        dismissEvent = {
                            viewModel.changeMediaData("")
                        }
                    )
                    /* 多图选择器 */
                    MultiplePicturePreviewBlock(
                        isDeleteMode = isDeletePicMode ,
                        pictureGroup = imageUris.value,
                        onEnterDeleteMode = {
                            isDeletePicMode = true
                        },
                        onRemoveFromList = { index ->
                            viewModel.images.value.removeAt(index)
                        }
                    )
                }
                Column(
                    modifier = Modifier.align(
                        Alignment.BottomCenter
                    )
                ) {
                    SendBottomSelectBar(
                        viewModel,
                        navController,
                        setTextContext,
                        isDeletePicMode,
                        isOpenEmojiKeyBoard,
                        myFavoritePlaylistId = mainViewModel.myFavoritePlaylistId,
                        { isDeletePicMode = false },    /* 关闭删除图片模式 */
                        { isOpenSettingTailState.value = true },   /* 打开设置小尾巴 对话输入框 */
                        imageUris.value,
                        multiplePhotoPicker,
                        onSentBlogEvent = {
                            viewModel.sendBlog(mainViewModel.currentUserId, context)
                        }
                    )
                }
            }
        },
        bottomBar = {
        }
    )
    /* 文本不为空，询问是否需要保存为草稿 */
    if (backDispatcher != null) {
        BackHandler(
            backDispatcher = backDispatcher
        ) {
            if (viewModel.text.value.isNotEmpty()) {
                isSaveToDraftBoxDialog.value = true
            } else {
                /* 返回上一级界面 */
                navController.navigateUp()
            }
        }
    }
}

@Composable
fun SendScreenBar(
    navController: NavController,
    userDetail: RequestUserDetail,
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { }
                ) {
                    AsyncImage(
                        model = if (userDetail.avatar_url.contains("avatar")) LOCAL_SERVER_URL + userDetail.avatar_url else userDetail.avatar_url,
                        contentDescription = "My Avatar",
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "发布动态",
                        fontFamily = MaterialTheme.typography.h6.fontFamily,
                        fontSize = 18.sp,
                        color = if (isSystemInDarkTheme()) MaterialTheme.colors.onSurface else Color.Black.copy(alpha = .8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Shigure Sasebokichi",
                        fontSize = 12.sp,
                        color = if (isSystemInDarkTheme()) MaterialTheme.colors.onSurface else Color.Black.copy(alpha = .8f)
                    )

                }
            }
        },
        actions = {
            Icon(
                painter = painterResource(id = R.drawable.ic_send_blog_draft),
                contentDescription = "My Blog Draft",
                modifier = Modifier
                    .size(40.dp)
                    .padding(horizontal = 8.dp)
                    .clickable {
                        navController.navigate(
                            route = BlogRoute.DraftScreen.route
                        )
                    }
            )
            Spacer(modifier = Modifier.width(10.dp))
        },
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(color = Color.Transparent),
        backgroundColor = MaterialTheme.colors.surface
    )
}

/**
 *  发布动态页 底栏
 *  @param  viewModel     ViewModel参数
 *  @param  isDeleteMode    图片删除模式启动入口
 *  @param  onCloseDeleteMode   关闭图片删除模式毁掉
 *  @param onSettingTailDialog  打开设置小尾巴 输入对话框
 *  @param  imageUris   图片列表数组
 *  @param  pickManager 资源管理器
 */
@SuppressLint("StateFlowValueCalledInComposition")
@ExperimentalPagerApi
@ExperimentalAnimationApi
@Composable
fun SendBottomSelectBar(
    viewModel: SendViewModel,
    navController: NavController,
    setText: (String) -> Unit,
    isDeleteMode: Boolean,
    emojiKeyBoardState: MutableState<Boolean>,
    myFavoritePlaylistId : Long,
    onCloseDeleteMode: () -> Unit,
    onSettingTailDialog: () -> Unit,
    imageUris: MutableList<String>,
    pickManager: ManagedActivityResultLauncher<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>,
    onSentBlogEvent : () -> Unit,
) {
    Column(modifier = Modifier.wrapContentHeight()) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colors.surface.copy(alpha = 0f),
                            MaterialTheme.colors.surface.copy(alpha = 0.8f),
                            MaterialTheme.colors.surface.copy(1f)
                        )
                    )
                )
                .fillMaxWidth()
                .padding(top = 14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                TailChipSetting { onSettingTailDialog() }       /* 设置小尾巴 */
            }
            /* 文本计数器 */
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .offset(y = (-4).dp)
            ) {
                ContextLengthCounter(viewModel.text.value)
            }
        }

        Spacer(modifier = Modifier
            .height(4.dp)
            .background(color = MaterialTheme.colors.surface)
            .fillMaxWidth())

        AnimatedVisibility(
            visible = emojiKeyBoardState.value
        ) {
            /* Emoji软键盘 */
            EmojiKeyBoard {
                setText(viewModel.text.value + it)
                viewModel.text.value = viewModel.text.value + it
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = MaterialTheme.colors.surface)
        ) {
            Row(
                modifier = Modifier
                    .height(70.dp)
                    .wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(10.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_images),
                    contentDescription = "Add Image",
                    modifier = Modifier
                        .height(34.dp)
                        .padding(4.dp)
                        .clickable {
                            pickManager.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                )
                Spacer(Modifier.width(10.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_topic_vector) ,
                    contentDescription = "Select a Topic",
                    modifier = Modifier
                        .height(36.dp)
                        .padding(4.dp)
                        .clickable {
                            navController.navigate(
                                route = BlogRoute.HotTopicScreen.route
                            )
                        }
                )
                Spacer(Modifier.width(10.dp))
                Icon(
                    painter = painterResource(id = R.drawable.at_with_my_friend),
                    contentDescription = "At my friends",
                    modifier = Modifier
                        .height(32.dp)
                        .padding(4.dp)
                        .clickable {
                            navController.navigate(
                                route = BlogRoute.CallMyFollowerScreen.route
                            )
                        },
                )
                Spacer(Modifier.width(10.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_select_emotion),
                    contentDescription = "Add a Emotion",
                    modifier = Modifier
                        .height(34.dp)
                        .padding(4.dp)
                        .clickable { emojiKeyBoardState.value = !emojiKeyBoardState.value },
                )
                Spacer(Modifier.width(10.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_share_music),
                    contentDescription = "Share a music",
                    modifier = Modifier
                        .height(30.dp)
                        .padding(4.dp)
                        .clickable {
                            navController.navigate(
                                route = BlogRoute.ShareMusicScreen.route + "?my_like_playlist=${myFavoritePlaylistId}"
                            )
                        }
                )
            }
            /* 发送 / 结束图片删除模式 按钮 */
            Card(
                modifier = Modifier
                    .height(56.dp)
                    .defaultMinSize(
                        minWidth = 120.dp
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterEnd)
                    .clickable {
                        /* 若处于图片删除模式下 点击按钮逻辑 */
                        if (isDeleteMode) {
                            /* 关闭图片删除模式 */
                            onCloseDeleteMode()
                        } else {
                            /* 发送博文逻辑 */
                            onSentBlogEvent()
                        }
                    },
                elevation = 0.dp,
            ) {
                /* 背景颜色样式 */
                val sendButtonColor by animateColorAsState(if (isDeleteMode) Amber100 else MaterialTheme.colors.primary.copy(alpha = .3f))
                Box(
                    modifier = Modifier
                        .background(color = sendButtonColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = if (isDeleteMode) R.drawable.ic_cancel_delete_mode else R.drawable.ic_send_blog_context),
                        contentDescription = "Send blog",
                        modifier = Modifier
                            .size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 *  保存为草稿对话框
 *
 */
@Composable
fun SaveAsDraftDialog(
    navController: NavController,
    isSaveAsDialogState: MutableState<Boolean>,
    onConfirmEvent: () -> Unit
) {
    if (isSaveAsDialogState.value) {
        AlertDialog(
            title = { Text(text = "保存为草稿") },
            text = { Text(text = "检测到您的文本还未执行发送，是否要将该条编辑动态保存到草稿箱？") },
            onDismissRequest = { isSaveAsDialogState.value = false },
            dismissButton = {
                TextButton(
                    onClick = {
                        isSaveAsDialogState.value = false
                        navController.navigateUp()
                    }
                ) {
                    Text("取消")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmEvent()
                        isSaveAsDialogState.value = false
                        navController.navigateUp()
                    }
                ) {
                    Text("保存")
                }
            },
        )
    }
}

/** 发布过程提示框
 *
 */
@Composable
fun SentProcessDialog(
    navController: NavController,
    imageSize : Int,
    viewModel: SendViewModel,
    sentState : BlogUploadState,
) {
    if (sentState is BlogUploadState.None) {
        /* 不进行显示 */
    } else {
        AlertDialog(
            title = {
                if (sentState is BlogUploadState.Success) {
                    Text(text = "发布成功")
                } else {
                    Text(text = "动态发布中")
                }
            },
            text = {
                when (sentState) {
                    is BlogUploadState.TextRunning -> {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(text = "动态发送中...")
                        }
                    }
                    is BlogUploadState.ImageRunning -> {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(text = "${sentState.position}/$imageSize 上传图片中...")
                        }
                    }
                    is BlogUploadState.Failed -> {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_error_occurred),
                                contentDescription = "Error",
                                modifier = Modifier.size(28.dp)
                            )
                            Text(text = "糟糕，发送出现问题了")
                        }
                    }
                    is BlogUploadState.Success -> {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_success_task),
                                contentDescription = "Success",
                                modifier = Modifier.size(28.dp)
                            )
                            Text(text = "发布成功！")
                        }
                    }
                    else -> {
                        val errorState = sentState as BlogUploadState.Error
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_error_occurred),
                                contentDescription = "Error",
                                modifier = Modifier.size(28.dp)
                            )
                            Text(text = " 貌似发生了错误:\n ${errorState.e}")
                        }
                    }
                }
            },
            dismissButton = {
                if (sentState is BlogUploadState.Failed || sentState is BlogUploadState.Error) {
                    TextButton(
                        onClick = {
                            viewModel.blogUploadState.value = BlogUploadState.None
                        }
                    ) {
                        Text("确认")
                    }
                }
            } ,
            confirmButton = {
                AnimatedVisibility (visible = sentState is BlogUploadState.Success) {
                    TextButton(
                        onClick = {
                            viewModel.apply {
                                text.value = ""
                                images.value.clear()
                                blogUploadState.value = BlogUploadState.None
                            }
                            navController.navigateUp()
                        }
                    ) {
                        Text("完成")
                    }
                }
            },
            onDismissRequest = {  }
        )
    }
}

/**
 *  返回键回调拦截
 */
@Composable
fun BackHandler(
    backDispatcher: OnBackPressedDispatcher,
    onBackEvent: () -> Unit
) {
    val backCallBack = remember {
        /* 匿名内部对象 back返回命令毁掉 */
        object : OnBackPressedCallback(true) {
            /* 重写方法 执行自定义的拦截逻辑 */
            override fun handleOnBackPressed() {
                onBackEvent()
            }
        }
    }
    /* 可组合项生命周期 */
    DisposableEffect(backDispatcher) {
        /* 执行Back拦截的回调方法 */
        backDispatcher.addCallback(backCallBack)
        /* 可组合项生命周期结束时执行 */
        onDispose {
            Log.d("_chen", "onDispose")
            backCallBack.remove()
        }
    }
}

/**
 *  文本长度计数器
 *
 */
@Composable
fun ContextLengthCounter(
    context : String
) {
    Text(
        text = "${TextLengthCounter.lengthCounter(context)}/146",
        color = MaterialTheme.colors.onSurface.copy(alpha = .7f),
        fontSize = 20.sp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
@Preview
fun PreviewScreen() {
    BeeAudioTheme {
//        val navController = rememberNavController()
//        SendScreen(
//            navController = navController,
//        )
    }
}