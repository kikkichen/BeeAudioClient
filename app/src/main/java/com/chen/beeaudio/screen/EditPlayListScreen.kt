package com.chen.beeaudio.screen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.ui.theme.Red600
import com.chen.beeaudio.viewmodel.EditPlayListViewModel
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.PlayListDetailLoadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun EditPlayListScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    editPlayListViewModel: EditPlayListViewModel = hiltViewModel()
) {
    /* 上下文 */
    val context : Context = LocalContext.current
    /* 协程域 */
    val coroutineScope = rememberCoroutineScope()

    /* 歌单信息 */
    val playlistInfo = editPlayListViewModel.playlistState.collectAsState()

    /* 标题数据 */
    val titleState = editPlayListViewModel.playlistTitle.collectAsState()
    /* Tag 数据 */
    val tagState = editPlayListViewModel.playListTags.collectAsState()
    /* 可见性数据 */
    val publicState = editPlayListViewModel.playListPublic.collectAsState()
    /* 歌单描述数据 */
    val descriptionState = editPlayListViewModel.playlistDescription.collectAsState()

    /* 封面Uri */
    val coverImageUri = editPlayListViewModel.image.collectAsState()
    /* 图片选择器 */
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) {
        if (it.toString().isNotEmpty()) {
            editPlayListViewModel.image.value = it.toString()
        }
    }

    /* 移除(删除)歌单警告窗口显示状态 */
    val deleteAlertState = remember { mutableStateOf(false) }

    if (deleteAlertState.value && playlistInfo.value is PlayListDetailLoadState.Success) {
        val playlist = (playlistInfo.value as PlayListDetailLoadState.Success).playList
        AlertDeleteCurrentPlaylistWindow(
            playlistTitle = playlist.name,
            deleteAction = {
                if (playlist.id != mainViewModel.myFavoritePlaylistId) {
                    editPlayListViewModel.deleteMyCreatedPlaylist(
                        context = context,
                        currentUserId = mainViewModel.currentUserId,
                        finishedEvent = {
                            coroutineScope.launch(Dispatchers.Main) {
                                Toast.makeText(context, "成功删除", Toast.LENGTH_SHORT).show()
                                /* 返回两次 */
                                navController.navigateUp()
                                navController.navigateUp()
                            }
                        }
                    )
                } else {
                    coroutineScope.launch(Dispatchers.IO) {
                        Toast.makeText(context, "用户默认喜爱歌不可删除！", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            dismissAlert = {deleteAlertState.value = false}
        )
    }

    Scaffold(
        topBar = {
            EditPlayListScreenTopAppBar(
                pageModel = editPlayListViewModel.playlistId == 0.toLong(),     /* 由导航参数判断当前页面标题为“歌单创建”还是"歌单编辑" */
                onBackUpEvent = { navController.navigateUp() },     /* 返回上一页逻辑 */
                onDeleteEvent = {   /* 删除歌单逻辑 */
                    deleteAlertState.value = true
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editPlayListViewModel.saveEditForm(
                        context = context,
                        currentUserId = mainViewModel.currentUserId,
                        finishedEvent = {
                            if (editPlayListViewModel.playlistId != 0.toLong()) {
                                coroutineScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, "歌单信息修改成功！", Toast.LENGTH_SHORT).show()
                                    navController.navigateUp()
                                }
                            } else {
                                coroutineScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, "歌单创建成功！", Toast.LENGTH_SHORT).show()
                                    navController.navigateUp()
                                }
                            }
                        }
                    )
                },
                modifier = Modifier
                    .offset(y = (-16).dp)
                    .width(98.dp),
                shape = CircleShape,
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (editPlayListViewModel.playlistId == 0.toLong()) Icons.Default.Add else Icons.Default.Done,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (editPlayListViewModel.playlistId == 0.toLong()) "创建" else "保存")
                }
            }
        }
    ) {
        when (playlistInfo.value) {
            is PlayListDetailLoadState.Loading -> {
                LoadingPlaylistBlock(
                    modifier = Modifier
                        .padding(paddingValues = it)
                        .fillMaxSize()
                )
            }
            else -> {
                val playlist = (playlistInfo.value as PlayListDetailLoadState.Success).playList
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues = it)
                        .padding(horizontal = 12.dp)
                        .padding(top = 8.dp)
                ) {
                    /* Tips 提示 */
                    if (editPlayListViewModel.playlistId == 0.toLong()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Tips",
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "创建歌单时请仔细检查填写项目是否遗漏。\n歌单创建完成后会自动返回，在“歌单信息编辑”页面中能够更改歌单封面以及这些原始偏好设置\n 下面祝您享用愉快～",
                                    style = MaterialTheme.typography.subtitle2.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = .7f)
                                    ),
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .weight(1f)
                                )
                            }
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .clickable {
                                        /* 启动图片选择器 */
                                        photoPicker.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(82.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    /* 歌单封面选择 */
                                    AsyncImage(
                                        model = if (coverImageUri.value.isNotEmpty()) {
                                            coverImageUri
                                        } else {
                                            if (playlist.coverImageUrl.contains("playlist")) LOCAL_SERVER_URL + playlist.coverImageUrl else playlist.coverImageUrl
                                        },
                                        contentDescription = "Upload Cover Image",
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                if (coverImageUri.value.isEmpty()) {
                                    Text(
                                        text = "上传更换封面",
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                } else {
                                    TextButton(
                                        onClick = {
                                            editPlayListViewModel.uploadPlaylistCoverImage(
                                                context = context,
                                                currentUserId = mainViewModel.currentUserId,
                                                finishedEvent = {
                                                    Toast.makeText(context, "封面上传成功!", Toast.LENGTH_SHORT).show()
                                                    editPlayListViewModel.loadPlaylistBaseInfo()
                                                    editPlayListViewModel.image.value = ""
                                                }
                                            )
                                        }
                                    ) {
                                        Text(text = "上传封面")
                                    }
                                }
                            }
                        }
                    }
                    /* 歌单标题输入框 */
                    item {
                        OutlinedTextField(
                            value = titleState.value,
                            onValueChange = { words -> editPlayListViewModel.changePlaylistTitle(words) },
                            label = { Text(text = "歌单标题") },
                            trailingIcon = {
                                if (titleState.value.isNotEmpty()) Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "清除文本",
                                    modifier = Modifier.clickable { editPlayListViewModel.changePlaylistTitle("") }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                    /* 歌单Tag选择框 */
                    item {
                        TagDropDownMenu(
                            modifier = Modifier.padding(10.dp),
                            innerText = tagState.value,
                            changeTextEvent = { tags ->
                                editPlayListViewModel.changePlayListTags(tags)
                            }
                        )
                    }
                    /* 歌单描述多行输入框 */
                    item {
                        OutlinedTextField(
                            value = descriptionState.value,
                            onValueChange = { words -> editPlayListViewModel.changeDescription(words) },
                            label = { Text(text = "歌单描述") },
                            trailingIcon = {
                                if (titleState.value.isNotEmpty()) Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "清除文本",
                                    modifier = Modifier.clickable { editPlayListViewModel.changePlaylistTitle("") }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(184.dp)
                                .padding(10.dp),
                            shape = MaterialTheme.shapes.medium
                        )
                    }

                    /* 歌单公开可见性 */
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "歌单公开性: ")
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = publicState.value,
                                onCheckedChange = { editPlayListViewModel.changePlayListPublic(!publicState.value) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary)
                            )
                        }
                    }

                    /* 留空 */
                    item {
                        Spacer(modifier = Modifier.height(180.dp))
                    }
                }
            }
        }
    }
}

/** 当前编辑歌单 / 创建歌单 页面顶栏
 *  @param  pageModel   当前页面功能模式， true 为 "创建歌单"， false 为 “编辑歌单”
 *  @param  onBackUpEvent   返回事件
 *  @param  onDeleteEvent   删除歌单事件
 */
@Composable
fun EditPlayListScreenTopAppBar(
    pageModel : Boolean,
    onBackUpEvent : () -> Unit,
    onDeleteEvent : () -> Unit
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
            .statusBarsPadding(),
        navigationIcon = {
            IconButton(onClick = { onBackUpEvent() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        title = {
            Text(text = if (pageModel)  "创建新的歌单" else "编辑歌单")
        },
        actions = {
            if (!pageModel) {
                IconButton(onClick = { onDeleteEvent() }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Current PlayList")
                }
            }
        },
        backgroundColor = MaterialTheme.colors.surface
    )
}

/** 标签下拉选择菜单
 *  @param  innerText   显示文本
 *  @param  changeTextEvent 变更内容事件
 */
@Composable
fun TagDropDownMenu(
    modifier: Modifier = Modifier,
    innerText: String,
    changeTextEvent: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val list = listOf("综艺", "流行", "影视原声", "华语", "清晨", "怀旧", "摇滚", "ACG", "欧美", "清新", "夜晚", "儿童", "民谣", "日语", "浪漫", "学习", "校园", "韩语", "工作", "电子", "粤语", "舞曲", "伤感", "午休", "游戏", "下午茶", "70后", "治愈", "说唱", "轻音乐", "80后", "地铁", "放松", "90后", "爵士", "驾车", "孤独", "乡村", "运动", "感动", "网络歌曲", "兴奋", "R&B/Soul", "旅行", "KTV", "经典", "快乐", "散步", "古典", "安静", "酒吧", "翻唱", "民族", "吉他", "思念", "英伦", "钢琴", "金属", "朋克", "器乐", "蓝调", "榜单", "雷鬼", "00后", "世界音乐", "拉丁", "New Age", "古风", "后摇", "Bossa Nova")

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )

    Column(
        modifier = modifier
    ) {
        ConstraintLayout {
            val (textFieldBlock, dropMenuBlock) = createRefs()
            OutlinedTextField(
                value = innerText,
                onValueChange = { changeTextEvent(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    }
                    .constrainAs(textFieldBlock) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    },
                label = { Text(text = "选择标签") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "",
                        modifier = Modifier
                            .clickable { expanded = !expanded }
                            .rotate(rotationState)
                    )
                },
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    .constrainAs(dropMenuBlock) {
                        start.linkTo(parent.start)
                        top.linkTo(textFieldBlock.bottom)
                        end.linkTo(parent.end)
                    }
                    .height(210.dp)
            ) {
                list.forEach { label ->
                    DropdownMenuItem(
                        onClick = {
                            if (!innerText.contains(label)) {
                                val originalTags = innerText.replace("[","").replace("]","")
                                changeTextEvent(
                                    if (originalTags.isNotEmpty()) {
                                        "[$originalTags, $label]"
                                    } else {
                                        "[$label]"
                                    }
                                )
                            }
                            expanded = false
                        }
                    ) {
                        Text(text = label)
                    }
                }
            }
        }
    }
}

/** 空数据加载布局
 *  @param  modifier    修饰符参数
 */
@Composable
fun LoadingPlaylistBlock(
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (circleProgressBar, text) = createRefs()
        Text(
            text = "加载中...",
            style = MaterialTheme.typography.subtitle2.copy(
                color = MaterialTheme.colors.onSurface.copy(alpha = .8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Light
            ),
            modifier = Modifier.constrainAs(text) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
        )
        CircularProgressIndicator(
            modifier = Modifier.constrainAs(circleProgressBar) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(text.top, margin = 8.dp)
            }
        )
    }
}

/** 删除歌单提示提醒弹窗
 *  @param  playlistTitle   当前编辑歌单页的歌单标题
 *  @param  deleteAction    删除行为事件
 *  @param  dismissAlert    关闭警告窗口事件
 */
@Composable
fun AlertDeleteCurrentPlaylistWindow(
    playlistTitle : String,
    deleteAction: () -> Unit,
    dismissAlert: () -> Unit,
) {
    AlertDialog(
        title = { Text(text = "移除当前歌单")},
        text = { Text(text = "您确定要移除当前歌单《$playlistTitle》吗？")},
        confirmButton = {
            TextButton(onClick = { deleteAction() }) {
                Text(text = "移除", color = Red600)
            }
        },
        dismissButton = {
            TextButton(onClick = { dismissAlert() }) {
                Text(text = "取消")
            }
        },
        onDismissRequest = dismissAlert,
        shape = MaterialTheme.shapes.medium
    )
}