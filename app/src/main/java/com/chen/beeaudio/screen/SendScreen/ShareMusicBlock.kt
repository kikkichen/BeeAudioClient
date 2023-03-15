package com.chen.beeaudio.screen.SendScreen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.mock.SingleTrackMock
import com.chen.beeaudio.model.audio.*
import com.chen.beeaudio.navigation.AudioHomeRoute
import com.google.gson.Gson

/** 分享对象显示框
 *  @param  modifier    修饰符参数
 *  @param  picUrl  图片Url
 *  @param  title   标题
 *  @param  subTitle    副标题
 *  @param  label   标签
 *  @param  onClickAction   点击事件
 */
@ExperimentalMaterialApi
@Composable
fun ShareMusicBlock(
    modifier: Modifier = Modifier,
    picUrl : String,
    title : String,
    subTitle : String,
    label : String,
    onClickAction : () -> Unit = {  }
) {
    Card(
        modifier = modifier
            .height(68.dp),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color = MaterialTheme.colors.onSurface.copy(alpha = .2f)),
        backgroundColor = Color.Transparent,
        contentColor = Color.Transparent,
        onClick = { onClickAction() },
        elevation = 0.dp
    ) {
        ConstraintLayout(
            modifier.fillMaxSize()
        ) {
            val centerGuideLine = createGuidelineFromTop(.5f)
            val (cover, titleText, subTitleText, labelText, logo) = createRefs()
            AsyncImage(
                model = picUrl,
                contentDescription = title,
                imageLoader = ImageLoader.Builder(LocalContext.current).build(),
                placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder ),
                modifier = Modifier
                    .height(66.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .constrainAs(cover) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .padding(vertical = 1.dp)
                    .padding(start = 4.dp, end = 10.dp)
            )
            /* 标题 */
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1.copy(
                    color = MaterialTheme.colors.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(titleText) {
                        start.linkTo(cover.end, margin = 4.dp)
                        bottom.linkTo(centerGuideLine, margin = 1.dp)
                        end.linkTo(logo.start, margin = 10.dp)
                        width = Dimension.fillToConstraints
                    }
            )
            /* 副标题 */
            Text(
                text = subTitle,
                style = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(subTitleText) {
                        start.linkTo(cover.end, margin = 4.dp)
                        top.linkTo(centerGuideLine, margin = 5.dp)
                        end.linkTo(logo.start, margin = 16.dp)
                        width = Dimension.fillToConstraints
                    }
            )
            /* 标签 */
            Text(
                text = label,
                style = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(labelText) {
                        end.linkTo(parent.end, margin = 8.dp)
                        bottom.linkTo(parent.bottom)
                    }
            )
            Icon(
                painter = if (label.contains("专辑")) {
                    painterResource(id = R.drawable.ic_alnum_subscribe)
                } else if (label.contains("艺人")) {
                     painterResource(id = R.drawable.ic_artist_subscribe)
                } else if (label.contains("歌单")) {
                    painterResource(id = R.drawable.ic_playlist_subscribe)
                } else {
                    painterResource(id = R.drawable.ic_share_music)
                },
                contentDescription = null,
                tint = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                modifier = Modifier.size(28.dp).constrainAs(logo) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end, margin = 16.dp)
                    bottom.linkTo(parent.bottom, margin = 8.dp)
                }
            )
        }
    }
}

/** 分享卡
 *
 */
@SuppressLint("UnusedCrossfadeTargetStateParameter")
@ExperimentalMaterialApi
@Composable
fun ShareMusicBlockCard(
    modifier: Modifier = Modifier,
    visible: Boolean,
    rawJson: String,
    navController: NavController,
    dismissEvent: () -> Unit,
) {
    /* 更多信息菜单弹出 状态变量 */
    val menuExpanded = remember { mutableStateOf(false) }

    Crossfade(
        targetState = visible
    ) {
        if (rawJson.contains("[share_track]")) {
            val track = Gson().fromJson(rawJson.replace("[share_track]", ""), com.chen.beeaudio.model.audio.Track::class.java)
            ShareMusicBlock(
                modifier = modifier.padding(4.dp).pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { menuExpanded.value = true }
                    )
                },
                picUrl = track.al.picUrl ?: "",
                title = track.name,
                subTitle = track.ar.map { item -> item.name }.toString().replace("[","").replace("]", ""),
                label = "单曲•分享",
            )
        } else if (rawJson.contains("[share_album]")) {
            val album = Gson().fromJson(rawJson.replace("[share_album]", ""), Album::class.java)
            ShareMusicBlock(
                modifier = modifier.padding(4.dp).pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { menuExpanded.value = true },
                        onTap = { navController.navigate(route = AudioHomeRoute.AlbumScreen.route + "?album_id=${album.id}") }
                    )
                },
                picUrl = album.picUrl,
                title = album.name,
                subTitle = album.artist.name,
                label = "专辑•分享"
            )
        } else if (rawJson.contains("[share_album_detail]")) {
            val albumDetail = Gson().fromJson(rawJson.replace("[share_album_detail]", ""), AlbumDetail::class.java)
            ShareMusicBlock(
                modifier = modifier.padding(4.dp).pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { menuExpanded.value = true },
                        onTap = { navController.navigate(route = AudioHomeRoute.AlbumScreen.route + "?album_id=${albumDetail.album.id}") }
                    )
                },
                picUrl = albumDetail.album.picUrl,
                title = albumDetail.album.name,
                subTitle = albumDetail.album.artist.name,
                label = "专辑•分享"
            )
        } else if (rawJson.contains("[share_artist]")) {
            val artist = Gson().fromJson(rawJson.replace("[share_artist]", ""), Artist::class.java)
            ShareMusicBlock(
                modifier = modifier.padding(4.dp).pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { menuExpanded.value = true },
                        onTap = { navController.navigate(route = AudioHomeRoute.ArtistScreen.route + "?artist_id=${artist.id}") }
                    )
                },
                picUrl = if (artist.picUrl.isNotEmpty()) artist.picUrl else artist.cover,
                title = artist.name,
                subTitle = if (artist.briefDesc.isNotEmpty()) "艺人 • ${artist.briefDesc}" else "艺人",
                label = "艺人•分享",
            )
        } else if (rawJson.contains("[share_playlist]")) {
            val playList = Gson().fromJson(rawJson.replace("[share_playlist]",""), PlayList::class.java)
            ShareMusicBlock(
                modifier = modifier.padding(4.dp).pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { menuExpanded.value = true },
                        onTap = { navController.navigate(route = AudioHomeRoute.PlayListScreen.route + "/${playList.id}") }
                    )
                },
                picUrl = if (playList.coverImageUrl.contains("playlist")) LOCAL_SERVER_URL + playList.coverImageUrl else playList.coverImageUrl,
                title = playList.name,
                subTitle = playList.description,
                label = "歌单•分享"
            )
        }
        DropdownMenu(
            expanded = menuExpanded.value,
            onDismissRequest = { menuExpanded.value = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    dismissEvent()
                    menuExpanded.value = false
                }
            ) {
                Text(text = "移除分享")
            }
        }
    }
}

/** 分享卡
 *
 */
@SuppressLint("UnusedCrossfadeTargetStateParameter")
@ExperimentalMaterialApi
@Composable
fun ShareMusicBlockCardForBlog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    rawJson: String,
    navController: NavController,
    onPlayEvent: (Track) -> Unit,
) {
    Crossfade(
        targetState = visible
    ) {
        if (rawJson.contains("[share_track]")) {
            val track = Gson().fromJson(rawJson.replace("[share_track]", ""), com.chen.beeaudio.model.audio.Track::class.java)
            ShareMusicBlock(
                modifier = modifier.padding(4.dp).clickable { onPlayEvent(track) },
                picUrl = track.al.picUrl ?: "",
                title = track.name,
                subTitle = track.ar.map { item -> item.name }.toString().replace("[","").replace("]", ""),
                label = "单曲•分享",
            )
        } else if (rawJson.contains("[share_album]")) {
            val album = Gson().fromJson(rawJson.replace("[share_album]", ""), Album::class.java)
            ShareMusicBlock(
                modifier = modifier.padding(4.dp).clickable {
                    navController.navigate(route = AudioHomeRoute.AlbumScreen.route + "?album_id=${album.id}")
                },
                picUrl = album.picUrl,
                title = album.name,
                subTitle = album.artist.name,
                label = "专辑•分享"
            )
        } else if (rawJson.contains("[share_album_detail]")) {
            val albumDetail = Gson().fromJson(rawJson.replace("[share_album_detail]", ""), AlbumDetail::class.java)
            ShareMusicBlock(
                modifier = modifier.padding(4.dp).clickable {
                    navController.navigate(route = AudioHomeRoute.AlbumScreen.route + "?album_id=${albumDetail.album.id}")
                },
                picUrl = albumDetail.album.picUrl,
                title = albumDetail.album.name,
                subTitle = albumDetail.album.artist.name,
                label = "专辑•分享"
            )
        } else if (rawJson.contains("[share_artist]")) {
            val artist = Gson().fromJson(rawJson.replace("[share_artist]", ""), Artist::class.java)
            ShareMusicBlock(
                modifier = modifier.padding(4.dp).clickable {
                    navController.navigate(route = AudioHomeRoute.ArtistScreen.route + "?artist_id=${artist.id}")
                },
                picUrl = if (artist.picUrl.isNotEmpty()) artist.picUrl else artist.cover,
                title = artist.name,
                subTitle = if (artist.briefDesc.isNotEmpty()) "艺人 • ${artist.briefDesc}" else "艺人",
                label = "艺人•分享",
            )
        } else if (rawJson.contains("[share_playlist]")) {
            val playList = Gson().fromJson(rawJson.replace("[share_playlist]",""), PlayList::class.java)
            ShareMusicBlock(
                modifier = modifier.padding(4.dp).clickable {
                    navController.navigate(route = AudioHomeRoute.PlayListScreen.route + "/${playList.id}")
                },
                picUrl = if (playList.coverImageUrl.contains("playlist")) LOCAL_SERVER_URL + playList.coverImageUrl else playList.coverImageUrl,
                title = playList.name,
                subTitle = playList.description,
                label = "歌单•分享"
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
@Preview(showBackground = true)
fun PreviewShareMusicBlock() {
    val track = SingleTrackMock
    ShareMusicBlock(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        picUrl = track.al.picUrl ?: "",
        title = track.name,
        subTitle = track.ar[0].name,
        label = "单曲•分享",
        onClickAction = {  }
    )
}