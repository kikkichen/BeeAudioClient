package com.chen.beeaudio.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.model.audio.HotAndAllTags
import com.chen.beeaudio.model.audio.Tag
import com.chen.beeaudio.navigation.AudioHomeRoute
import com.chen.beeaudio.screen.widget.SearchWidget
import com.chen.beeaudio.ui.theme.*
import com.chen.beeaudio.utils.BlurTransformation
import com.chen.beeaudio.viewmodel.NetTagsResults
import com.chen.beeaudio.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** 音乐功能搜索页面
 * @param   navController   navigation导航控制器
 * @param   mViewModel  搜索页视图模型ViewModel
 *
 */
@Composable
fun SearchScreen(
    navController: NavController,
    mViewModel : SearchViewModel = hiltViewModel(),
    onOpenTagPlayListCollectionPage : (String) -> Unit,
    onOpenPlayListPage : (Long) -> Unit,
) {
    val hotAndAllTagsState = mViewModel.hotPlaylistTags.collectAsState()
    /* scaffold脚手架状态 */
    val scaffoldState : ScaffoldState = rememberScaffoldState()

    /* 协程域 */
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
             SearchWidget(
                 text = mViewModel.searchKeyWords.value,
                 onTextChange = {
                    mViewModel.changeSearchKeyWords(it)
                 },
                 onSearchClicked = {
                     if (it == "" || it.isEmpty() || it.isBlank()) {
                         coroutineScope.launch {
                             scaffoldState.snackbarHostState.showSnackbar(
                                 message = "\uD83D\uDE15 搜索框没有一点关键字呢",
                                 actionLabel = "知道了"
                             )
                         }
                     } else {
                         navController.navigate(
                             route = AudioHomeRoute.SearchResultScreen.route + "?keywords=${mViewModel.searchKeyWords.value}"
                         )
                     }
                 },
                 onCloseClicked = { navController.navigateUp() }
             )
        },
        scaffoldState = scaffoldState,
        content = {
            LazyColumn(
                state = rememberLazyListState(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = it)
            ) {
                item {
                    ShowTagsPage(
                        tagsState = hotAndAllTagsState,
                        onOpenTargetPage = onOpenTagPlayListCollectionPage
                    )
                }
                item {
                    when (hotAndAllTagsState.value) {
                        is NetTagsResults.Loading -> {
                            LoadingBlockTagItemGroup()
                        }
                        is NetTagsResults.Success -> {
                            val tagsResults = (hotAndAllTagsState.value as NetTagsResults.Success).tags.allTags
                            Column {
                                for (i in 0 until 5) {
                                    TagCategoryTitle(i)
                                    BlockTagItemGroup(
                                        tags = tagsResults.filter { tag -> tag.category == i },
                                        onClick = onOpenTagPlayListCollectionPage
                                    )
                                    Spacer(modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp))
                                }
                            }
                        }
                        else -> {
                            ErrorOccurredBlockTagItemGroup{
                                mViewModel.getHotAndAllTagsData()
                            }
                        }
                    }
                }
            }
        }
    )
}

/** 热门标签组
 *  @param  tagsState   请求歌单索引标签结果状态
 *  @param  onOpenTargetPage    点击标签跳转到指定标签内容索引页
 */
@Composable
fun ShowTagsPage(
    tagsState : State<NetTagsResults<HotAndAllTags>>,
    onOpenTargetPage : (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val singleHotTagItemWidth = configuration.screenWidthDp/2 - 10

    when (tagsState.value) {
        is NetTagsResults.Success -> {
            val hotTags = (tagsState.value as NetTagsResults.Success).tags.hotTags
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                for (count in 0 until (hotTags.size/2)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SingleHotTagItem(
                            tagText = hotTags[(count)*2].name,
                            itemWidth = singleHotTagItemWidth,
                            onClick = onOpenTargetPage
                        )
                        SingleHotTagItem(
                            tagText = hotTags[(count)*2 + 1].name,
                            itemWidth = singleHotTagItemWidth,
                            onClick = onOpenTargetPage
                        )
                    }
                }
            }
        }
        is NetTagsResults.Loading -> {
            for (i in 0 until 5) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SingleHotItemLoadingState(singleHotTagItemWidth)
                        SingleHotItemLoadingState(singleHotTagItemWidth)
                    }
                }
            }
        }
        else -> { /* Empty */ }
    }
}

/** 为获得热门标签前的加载状态组件
 *  @param  itemWidth   单个子项标签组件宽度
 */
@Composable
fun SingleHotItemLoadingState(
    itemWidth : Int,
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

    Card(
        modifier = Modifier
            .height(86.dp)
            .width(itemWidth.dp)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        shape = RoundedCornerShape(10.dp),
        backgroundColor = Color.LightGray.copy(alpha = alpha),
        elevation = 0.dp,
    ) {
        /* Empty */
    }
}

/** 热门Tag Item
 *  @param  tagText Tag标签
 *  @param  itemWidth   单个子项标签组件宽度
 */
@Composable
fun SingleHotTagItem(
    tagText : String,
    itemWidth : Int,
    onClick : (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .height(86.dp)
            .width(itemWidth.dp)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.03f),
        elevation = 0.dp,
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick(tagText) }
        ) {
            val (backgroundImage, text) = createRefs()
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("$LOCAL_SERVER_URL/tags/cover?cat=$tagText")
                    .crossfade(durationMillis = 500)
                    .transformations(listOf(
                        BlurTransformation(scale = 0.95f, radius = 5)
                    )).build(),
                contentScale = ContentScale.Crop,
                contentDescription = tagText,
                placeholder = painterResource(id = R.drawable.ic_image_placeholder),
                colorFilter = if (isSystemInDarkTheme()) ColorFilter.tint(Color.DarkGray.copy(alpha = .8f), BlendMode.Darken) else null,
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(backgroundImage) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            )
            Text(
                text = tagText,
                style = MaterialTheme.typography.h5.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .constrainAs(text) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    },
            )
        }
    }
}

@Composable
fun TagCategoryTitle(
    categoryText : Int
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (colorBlock, logo, title, baseline) = createRefs()
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .width(10.dp)
                    .constrainAs(colorBlock) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(baseline.top)
                    }
                    .padding(end = 4.dp)
                    .background(
                        color = when (categoryText) {
                            0 -> LightBlueA400
                            1 -> AmberA400
                            2 -> LightGreen300
                            3 -> PinkA100
                            else -> DeepPurpleA100
                        }
                    )
            )
            Icon(
                painter = painterResource(id = when (categoryText){
                    0 -> R.drawable.ic_tag_group_language
                    1 -> R.drawable.ic_tag_group_style
                    2 -> R.drawable.ic_tag_group_scenes
                    3 -> R.drawable.ic_tag_group_emotion
                    else -> R.drawable.ic_tag_group_theme
                }),
                contentDescription = when (categoryText) {
                    0 -> "语种"
                    1 -> "风格"
                    2 -> "场景"
                    3 -> "情感"
                    else -> "主题"
                },
                modifier = Modifier
                    .size(40.dp)
                    .constrainAs(logo) {
                        start.linkTo(colorBlock.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(baseline.top)
                    }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
            Text(
                text = when (categoryText) {
                    0 -> "语种"
                    1 -> "风格"
                    2 -> "场景"
                    3 -> "情感"
                    else -> "主题"
                },
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.constrainAs(title) {
                    start.linkTo(logo.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(baseline.top)
                }
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(baseline) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            )
        }
    }
}

/** 标签组合
 * @param   tags    当前歌单索引标签集合
 */
@Composable
fun BlockTagItemGroup(
    tags : List<Tag>,
    onClick: (String) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) {
        for (col in 0 until tags.size/6 + 1) {
            Row(
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                if (col == tags.size/6) {
                    for (row in 0 until tags.size%6) {
                        Spacer(modifier = Modifier.width(3.dp))
                        BlockTagItem(tagText = tags[col*6 + row].name, onClick = onClick)
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                } else {
                    for (row in 0 until 6) {
                        Spacer(modifier = Modifier.width(3.dp))
                        BlockTagItem(tagText = tags[col*6 + row].name, onClick = onClick)
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }
            }
        }
    }
}

/** 块状子项标签
 *
 */
@Composable
fun BlockTagItem(
    tagText: String,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .border(
                width = 1.dp,
                brush = SolidColor(MaterialTheme.colors.onSurface.copy(alpha = .2f)),
                shape = CircleShape
            )
            .clickable { onClick(tagText) },
        shape = CircleShape,
        elevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colors.onSurface.copy(alpha = .05f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = tagText,
                fontSize = 14.sp,
                modifier = Modifier
            )
        }
    }
}

/** 加载状态子项标签组合
 *
 */
@Composable
fun LoadingBlockTagItemGroup() {
    /* 循环动画变换状态 */
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
    Column {
        for (i in 0 until 3) {
            Row(
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = alpha))
                )
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(90.dp)
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = alpha))
                )
            }
            Divider()
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.padding(2.dp)
            ) {
                Box(modifier = Modifier
                    .height(16.dp)
                    .width(42.dp)
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = alpha)))
                Box(modifier = Modifier
                    .height(16.dp)
                    .width(62.dp)
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = alpha)))
                Box(modifier = Modifier
                    .height(16.dp)
                    .width(42.dp)
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = alpha)))
                Box(modifier = Modifier
                    .height(16.dp)
                    .width(42.dp)
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = alpha)))
                Box(modifier = Modifier
                    .height(16.dp)
                    .width(62.dp)
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = alpha)))
                Box(modifier = Modifier
                    .height(16.dp)
                    .width(62.dp)
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = alpha)))
            }
            Row(
                modifier = Modifier.padding(2.dp)
            ) {
                Box(modifier = Modifier
                    .height(16.dp)
                    .width(50.dp)
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = alpha)))
                Box(modifier = Modifier
                    .height(16.dp)
                    .width(42.dp)
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = alpha)))
                Box(modifier = Modifier
                    .height(16.dp)
                    .width(42.dp)
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = alpha)))
            }
        }
    }
}

/** 歌单标签索引获取失败显示
 *  @param  onRetryRequestEvent 点击重新请求数据事件
 */
@Composable
fun ErrorOccurredBlockTagItemGroup(
    onRetryRequestEvent: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .clickable { onRetryRequestEvent() }
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_error_occurred),
                contentDescription = "标签索引加载出错",
                modifier = Modifier.size(46.dp).padding(vertical = 6.dp)
            )
            Text(
                text = "加载标签加载有误，点击重试",
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
@Preview(showBackground = true)
fun PreviewSingleHotTagItem() {
    BeeAudioTheme {
        SingleHotTagItem(
            tagText = "摇滚",
            itemWidth = 184
        ) {  }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewTagCategoryTitle() {
    BeeAudioTheme {
        TagCategoryTitle(4)
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewBlockTagItem() {
    BeeAudioTheme {
        BlockTagItem("电子") {}
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewLoadingBlockTagItemGroup() {
    LoadingBlockTagItemGroup()
}

@Composable
@Preview(showBackground = true)
fun PreviewErrorOccurredBlockTagItemGroup() {
    ErrorOccurredBlockTagItemGroup{  }
}