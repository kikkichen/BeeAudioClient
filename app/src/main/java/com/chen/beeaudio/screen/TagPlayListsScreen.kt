package com.chen.beeaudio.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.chen.beeaudio.screen.widget.ErrorDataTipsWidget
import com.chen.beeaudio.screen.widget.HotRecommendPlayListItem
import com.chen.beeaudio.screen.widget.HotRecommendPlayListItemLoadState
import com.chen.beeaudio.viewmodel.TagPlayListsViewModel

/** 目标Tag标签热门歌单列表 - 分页
 *  @param  catOfPlayLists  歌单索引标签
 *  @param  navController   navigation导航控制器
 *  @param  mViewModel  当前目标Tag标签视图对象
 *  @param  onOpenPlayListPage  选择目的歌单详情页点击事件
 *
 */
@Composable
fun TagPlayListScreen(
    catOfPlayLists : String,
    navController: NavController,
    mViewModel : TagPlayListsViewModel = hiltViewModel(),
    onOpenPlayListPage : (Long) -> Unit,
) {
    /* 加载与当前索引歌单标签相关的歌单内容 */
    val playlistsForPaging = mViewModel.targetPlayLists.collectAsLazyPagingItems()
    mViewModel.loadTargetIndexPlayLists(cat = catOfPlayLists)

    Scaffold(
        topBar = {
             TopAppBar(
                 modifier = Modifier
                     .fillMaxWidth()
                     .background(Color.Transparent)
                     .statusBarsPadding(),
                 elevation = 0.dp,
                 backgroundColor = MaterialTheme.colors.surface
             ) {
                 ConstraintLayout {
                     val (backButton, title) = createRefs()
                     IconButton(
                         onClick = { navController.navigateUp() },
                         modifier = Modifier.constrainAs(backButton) {
                             start.linkTo(parent.start)
                             top.linkTo(parent.top)
                             bottom.linkTo(parent.bottom)
                         }
                     ) {
                         Icon(
                             imageVector = Icons.Default.ArrowBack,
                             contentDescription = "Back to Search Page",
                         )
                     }
                     Text(
                         text = catOfPlayLists,
                         style = MaterialTheme.typography.h6,
                         modifier = Modifier.constrainAs(title) {
                             start.linkTo(backButton.end)
                             top.linkTo(parent.top)
                             bottom.linkTo(parent.bottom)
                             width = Dimension.fillToConstraints
                         }
                     )
                 }
             }
        },
        content = {
            LazyColumn(
                modifier = Modifier.padding(paddingValues = it)
            ) {
                when (playlistsForPaging.loadState.refresh) {
                    is LoadState.Loading -> {
                        item {
                            for (i in 0 until 5) {
                                HotRecommendPlayListItemLoadState()
                            }
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            ErrorDataTipsWidget(
                                text = "播放列表加载出错了，请退出该页重试",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 80.dp)
                            )
                        }
                    }
                    else -> {
                        items(
                            items = playlistsForPaging
                            /* 不设置key的原因，这里的查询结果是有可能遇到重复的歌单的情况存在 */
                        ) { playlist ->
                            playlist?.let { currentPlayList ->
                                HotRecommendPlayListItem(
                                    playListItem = currentPlayList,
                                    imageLoader = mViewModel.myImageLoader,
                                    onClickEvent = {
                                        onOpenPlayListPage(currentPlayList.id)
                                    },
                                    onPlayEvent = {  }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}