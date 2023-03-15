package com.chen.beeaudio.screen.widget

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.ImageLoader
import coil.compose.AsyncImage
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.ui.theme.Cyan200

/** 热门歌单列表子项
 *  @param  playListItem    歌单信息对象
 *  @param  imageLoader     coil 加载器对象
 *  @param  onClickEvent    歌单块点击跳转事件
 *  @param  onPlayEvent     点击“play”案件切换播放列表
 *
 */
@Composable
fun HotRecommendPlayListItem(
    playListItem: PlayList,
    imageLoader: ImageLoader,
    onClickEvent: () -> Unit,
    onPlayEvent: () -> Unit,
) {
    Card(
        modifier = Modifier
            .height(154.dp)
            .fillMaxHeight()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(width = 1.dp, MaterialTheme.colors.onSurface.copy(alpha = .3f)),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.6f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClickEvent() }
        ) {
            Row {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(120.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    /* 歌单封面 */
                    Surface(
                        modifier = Modifier
                            .size(height = 114.dp, width = 114.dp)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        AsyncImage(
                            model = if (playListItem.coverImageUrl.contains("playlist")) {
                                LOCAL_SERVER_URL + playListItem.coverImageUrl
                            } else {
                                playListItem.coverImageUrl
                            },
                            contentDescription = "Current Playlist is ${playListItem.name}",
                            contentScale = ContentScale.Crop,
                            imageLoader = imageLoader,
                            placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder),
                        )
                    }
                    /* 歌单创建者昵称 */
                    Text(
                        text = playListItem.creator.nickName,
                        style = MaterialTheme.typography.body1.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = .4f),
                            fontSize = 10.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 0.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight(.9f)
                        .align(Alignment.CenterVertically)
                        .padding(end = 12.dp),
                ) {
                    /* 歌单标题 */
                    Text(
                        text = playListItem.name,
                        style = MaterialTheme.typography.subtitle1.copy(
                            color = MaterialTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                            .padding(horizontal = 0.dp, vertical = 4.dp)
                    )
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val endGuideline = createGuidelineFromEnd(.2f)
                        val bottomGuideline = createGuidelineFromBottom(.2f)
                        val (description, tags, playButton) = createRefs()
                        Text(
                            text = playListItem.description,
                            style = MaterialTheme.typography.subtitle1.copy(
                                color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                                fontWeight = FontWeight.Light,
                                fontSize = 10.sp
                            ),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.constrainAs(description) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                                end.linkTo(endGuideline)
                                bottom.linkTo(bottomGuideline)
                                width = Dimension.fillToConstraints
                            }
                        )
                        Row(
                            modifier = Modifier.constrainAs(tags) {
                                start.linkTo(parent.start)
                                top.linkTo(bottomGuideline)
                                bottom.linkTo(parent.bottom)
                            }
                        ) {
                            if (playListItem.tags!!.isNotEmpty()) {
                                for (tagString in playListItem.tags) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                color = MaterialTheme.colors.primary.copy(
                                                    alpha = .4f
                                                )
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tagString,
                                            style = MaterialTheme.typography.body2.copy(
                                                color = MaterialTheme.colors.surface,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }
                        }
                        IconButton(
                            onClick = { onPlayEvent() },
                            modifier = Modifier
                                .constrainAs(playButton) {
                                    start.linkTo(endGuideline, margin = 4.dp)
                                    top.linkTo(parent.top)
                                    end.linkTo(parent.end)
                                    bottom.linkTo(parent.bottom, margin = 4.dp)
                                }
                                .clip(CircleShape)
                                .background(color = Cyan200),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Start Play This PlayList",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 热门歌单懒加载子项 - 加载状态
 *
 */
@Composable
fun HotRecommendPlayListItemLoadState() {
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
            .height(154.dp)
            .fillMaxHeight()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(width = 1.dp, MaterialTheme.colors.onSurface.copy(alpha = .3f)),
        elevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(120.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    /* 歌单封面 */
                    Surface(
                        modifier = Modifier
                            .size(height = 114.dp, width = 114.dp)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        AsyncImage(
                            model = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder,
                            contentDescription = "Current Playlist Info is Loading",
                            contentScale = ContentScale.Crop,
                        )
                    }
                    /* 歌单创建者昵称 */
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .padding(start = 12.dp, top = 0.dp, end = 30.dp, bottom = 0.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.LightGray.copy(alpha = alpha))
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight(.9f)
                        .align(Alignment.CenterVertically)
                        .padding(end = 12.dp),
                ) {
                    /* 歌单标题 */
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                            .padding(horizontal = 0.dp, vertical = 4.dp)
                            .background(Color.LightGray.copy(alpha = alpha))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                            .padding(horizontal = 0.dp, vertical = 4.dp)
                            .background(Color.LightGray.copy(alpha = alpha))
                    )
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val endGuideline = createGuidelineFromEnd(.2f)
                        val bottomGuideline = createGuidelineFromBottom(.2f)
                        val (description, tags, playButton) = createRefs()
                        Column(
                            modifier = Modifier.constrainAs(description) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                                end.linkTo(endGuideline)
                                bottom.linkTo(bottomGuideline)
                                width = Dimension.fillToConstraints
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .padding(horizontal = 0.dp, vertical = 4.dp)
                                    .background(Color.LightGray.copy(alpha = alpha))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .padding(horizontal = 0.dp, vertical = 4.dp)
                                    .background(Color.LightGray.copy(alpha = alpha))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .padding(horizontal = 0.dp, vertical = 4.dp)
                                    .background(Color.LightGray.copy(alpha = alpha))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .padding(horizontal = 0.dp, vertical = 4.dp)
                                    .background(Color.LightGray.copy(alpha = alpha))
                            )
                        }
                        Row(
                            modifier = Modifier.constrainAs(tags) {
                                start.linkTo(parent.start)
                                top.linkTo(bottomGuideline)
                                bottom.linkTo(parent.bottom)
                            }
                        ) {
                            for (tagString in 0 until 2) {
                                Box(
                                    modifier = Modifier
                                        .height(10.dp)
                                        .width(28.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(color = Color.LightGray.copy(alpha = alpha))
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .constrainAs(playButton) {
                                    start.linkTo(endGuideline, margin = 4.dp)
                                    top.linkTo(parent.top)
                                    end.linkTo(parent.end)
                                    bottom.linkTo(parent.bottom, margin = 4.dp)
                                }
                                .clip(CircleShape)
                                .background(color = Color.LightGray.copy(alpha = alpha)),
                        )
                    }
                }
            }
        }
    }
}