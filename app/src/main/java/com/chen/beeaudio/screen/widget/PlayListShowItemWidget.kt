package com.chen.beeaudio.screen.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.ImageLoader
import coil.compose.AsyncImage
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.mock.SinglePlayListData
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.ui.theme.shimmerEffect

@Composable
fun PlayListShowItemWidget(
    playList: PlayList,
    imageLoader: ImageLoader,
    onOpenPlayListDetailPage : (Long) -> Unit
) {
    /* 更多信息菜单弹出 状态变量 */
    val menuExpanded = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onOpenPlayListDetailPage(playList.id) }
        ) {
            val (coverImage, title, creator, moreIcon) = createRefs()
            val bottomGuideLine = createGuidelineFromBottom(0.4f)
            AsyncImage(
                model = if (playList.coverImageUrl.contains("playlist")) {
                    LOCAL_SERVER_URL + playList.coverImageUrl
                } else {
                    playList.coverImageUrl
                },
                contentDescription = "来自 ${playList.creator.nickName} 的歌单 《${playList.name}》",
                imageLoader = imageLoader,
                placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder ),
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .constrainAs(coverImage) {
                        start.linkTo(parent.start, margin = 16.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )
            Text(
                text = playList.name,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(title) {
                    start.linkTo(coverImage.end, margin = 8.dp)
                    bottom.linkTo(bottomGuideLine, margin = 8.dp)
                    end.linkTo(moreIcon.start)
                    width = Dimension.fillToConstraints
                }
            )
            /* 歌单创建者信息 */
            Text(
                text = playList.creator.nickName,
                style = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(creator) {
                    start.linkTo(coverImage.end, margin = 8.dp)
                    top.linkTo(bottomGuideLine)
                    end.linkTo(moreIcon.start)
                    width = Dimension.fillToConstraints
                }
            )
            /* 更多选项菜单按钮 */
            IconButton(
                onClick = { menuExpanded.value = true },
                modifier = Modifier.constrainAs(moreIcon) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end, margin = 2.dp)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More"
                )
                /* 弹出菜单 */
                DropdownMenu(
                    expanded = menuExpanded.value,
                    onDismissRequest = { menuExpanded.value = false }
                ) {
                    DropdownMenuItem(onClick = { onOpenPlayListDetailPage(playList.id) }) {
                        Text(text = "查看")
                    }
                }
            }
        }
    }
}

/** 加载状态的播放列表条目组件 1
 *
 */
@Composable
fun LoadingPlayListShowItemWidget() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val (showImage, showBlock) = createRefs()
        Box(
            modifier = Modifier
                .size(46.dp)
                .shimmerEffect()
                .constrainAs(showImage) {
                    start.linkTo(parent.start, margin = 16.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 8.dp, end = 20.dp)
                .constrainAs(showBlock) {
                    start.linkTo(showImage.end)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end, margin = 12.dp)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier
                    .height(18.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
                    .weight(5f)
                )
                Spacer(modifier = Modifier.weight(2f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier
                    .width(138.dp)
                    .height(12.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
                    .weight(2f)
                )
                Spacer(modifier = Modifier.weight(4f))
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewPlayListShowItemWidget() {
    BeeAudioTheme {
        PlayListShowItemWidget(
            playList = SinglePlayListData,
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            onOpenPlayListDetailPage = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewLoadingPlayListShowItemWidget() {
    LoadingPlayListShowItemWidget()
}