package com.chen.beeaudio.screen.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import com.chen.beeaudio.mock.SingleArtistMock
import com.chen.beeaudio.model.audio.Artist
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.ui.theme.shimmerEffect

/** 艺人显示条目
 *  @param  artist  艺人信息
 *  @param  imageLoader coil图像加载对象
 *  @param  onOpenArtistDetailPage  跳转到艺人详情页点击事件
 *
 */
@Composable
fun ArtistShowItemWidget(
    artist: Artist,
    imageLoader: ImageLoader,
    onOpenArtistDetailPage : (Long) -> Unit
) {
    /* 更多信息菜单弹出 状态变量 */
    val menuExpanded = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .clickable { onOpenArtistDetailPage(artist.id) }
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (artistImage, artistName, musicVolume, moreIcon) = createRefs()
            val bottomGuideLine = createGuidelineFromBottom(0.4f)
            AsyncImage(
                model = artist.picUrl,
                contentDescription = artist.name,
                imageLoader = imageLoader,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder ),
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .constrainAs(artistImage) {
                        start.linkTo(parent.start, margin = 16.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )
            Text(
                text = artist.name,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(artistName) {
                    start.linkTo(artistImage.end, margin = 8.dp)
                    bottom.linkTo(bottomGuideLine, margin = 8.dp)
                    end.linkTo(moreIcon.start)
                    width = Dimension.fillToConstraints
                }
            )
            Text(
                text = "专辑 ： ${artist.albumSize} 张",
                style = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(musicVolume) {
                    start.linkTo(artistImage.end, margin = 8.dp)
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
                    DropdownMenuItem(onClick = { onOpenArtistDetailPage(artist.id) }) {
                        Text(text = "查看")
                    }
                }
            }
        }
    }
}

/** 加载状态的艺人条目组件
 *
 */
@Composable
fun LoadingArtistShowItemWidget_1() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val (showImage, showBlock) = createRefs()
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
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
                    .weight(3f)
                )
                Spacer(modifier = Modifier.weight(5f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier
                    .width(138.dp)
                    .height(12.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
                    .weight(1f)
                )
                Spacer(modifier = Modifier.weight(4f))
            }
        }
    }
}

/** 加载状态的艺人条目组件 2
 *
 */
@Composable
fun LoadingArtistShowItemWidget_2() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val (showImage, showBlock) = createRefs()
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
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
                    .weight(2f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
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
fun PreviewArtistShowItemWidget() {
    BeeAudioTheme {
        ArtistShowItemWidget(
            artist = SingleArtistMock,
            imageLoader = ImageLoader.Builder(LocalContext.current).build()
        ) {}
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewLoadingArtistShowItemWidget() {
    Column {
        LoadingArtistShowItemWidget_1()
        LoadingArtistShowItemWidget_2()
    }
}