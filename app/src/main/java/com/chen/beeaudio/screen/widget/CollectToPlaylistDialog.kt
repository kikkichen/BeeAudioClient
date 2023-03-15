package com.chen.beeaudio.screen.widget

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.model.localmodel.Subscribe
import com.chen.beeaudio.ui.theme.shimmerEffect
import com.chen.beeaudio.viewmodel.CollectDialogVM
import com.chen.beeaudio.viewmodel.NetUserInfoLoadResult
import kotlinx.coroutines.flow.catch

/** 将曲目收藏到歌单提示窗
 *  @param  context 上下文对象参数
 *  @param  targetTrack 目标曲目ID
 *  @param  currentUserId   当前执行用户ID
 *  @param  myFavoritePlaylistId    当前用户默认喜爱歌单ID
 *  @param  parentWidth 父容器宽度
 *  @param  onDismissEvent  关闭窗口事件
 *  @param  onUpdateCurrent 更新当前曲目是否处于我的默认喜爱收藏歌单状态事件
 *  @param  mViewModel  当前对话框 ViewModel 视图模型
 */
@ExperimentalComposeUiApi
@Composable
fun CollectToPlaylistDialog(
    context: Context,
    targetTrack : Track,
    currentUserId : Long,
    myFavoritePlaylistId : Long,
    parentWidth : Int,
    onDismissEvent : () -> Unit,
    onUpdateCurrent: () -> Unit,
    mViewModel: CollectDialogVM = hiltViewModel()
) {
    Dialog(
        onDismissRequest = onDismissEvent,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        CollectToPlaylistBody(
            modifier = Modifier
                .width((parentWidth * .85f).dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(color = MaterialTheme.colors.surface)
                .animateContentSize(),
            targetTrack = targetTrack,
            currentUserId = currentUserId,
            myFavoritePlaylistId = myFavoritePlaylistId,
            mViewModel = mViewModel,
            confirmButton = {
                TextButton(onClick = {
                    if (mViewModel.verifyPlaylistChooseChange()) {
                        /* 执行曲目收藏事务操作 */
                        mViewModel.collectionTransitionForPlayList(context, targetTrack.id, currentUserId)
                        onUpdateCurrent()
                        onDismissEvent()
                    } else {
                        Toast.makeText(context, "您还没有对该曲目做任何变动", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(text = "保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissEvent() }) {
                    Text(text = "取消")
                }
            },
        )
    }
}

/** 将目标曲目收藏到歌单本体
 *  @param  modifier    修饰符参数
 *  @param  targetTrack 目标曲目信息
 *  @param  currentUserId   当前执行用户ID
 *  @param  myFavoritePlaylistId    当前用户默认喜爱歌单ID
 *  @param  dismissButton   取消事件触发按钮
 *  @param  confirmButton   确定事件触发按钮
 *  @param  mViewModel  当前对话框 ViewModel 视图模型
 */
@Composable
fun CollectToPlaylistBody(
    modifier: Modifier,
    targetTrack : Track,
    currentUserId : Long,
    myFavoritePlaylistId : Long,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    mViewModel : CollectDialogVM
) {
    /* 用户请求信息 */
    val userDetail = loadCurrentUserDetail(userId = currentUserId, viewModel = mViewModel)
    /* 自建歌单列表 */
    val createdPlaylistChooseList = mViewModel.createdPlayList.collectAsState()

    /* 我的自建歌单加载 */
    mViewModel.loadUserCreatedPlaylistInfo(songId = targetTrack.id)

    Column(modifier = modifier) {
        Text(
            text = "收藏到我的歌单",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(top = 20.dp)
        )
        UserTitlePanel(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            currentUserDetailResult = userDetail.value
        )
        Text(
            text = "您确定要将 《${targetTrack.name} - ${targetTrack.ar[0].name}》收录到自己的歌单吗？",
            style = MaterialTheme.typography.body2.copy(
                fontWeight = FontWeight.Light
            ),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        )
        LazyColumn {
            items(
                items = createdPlaylistChooseList.value,
                key = { playlistChoose ->
                    playlistChoose.playlist.itemId
                }
            ) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var chooseSignal by remember { mutableStateOf(item.afterChoose) }
                    Checkbox(
                        checked = chooseSignal,
                        onCheckedChange = {
                            mViewModel.changeChooseSignal(playlistChoose = item)
                            chooseSignal = !chooseSignal
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colors.primary
                        )
                    )
                    CreatedPlaylistItem(
                        modifier = Modifier.padding(vertical = 4.dp),
                        playlistSubscribeInfo = item.playlist,
                        trackCount = item.tracksAmount,
                        isMyFavoritePlaylist = item.playlist.itemId == myFavoritePlaylistId
                    )
                }
            }
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1.6f))
            Row(modifier = Modifier.weight(1f)) {
                dismissButton()
                Spacer(modifier = Modifier.width(6.dp))
                confirmButton()
            }
        }
    }
}

/** 显示用户头像与用户名信息的面板
 *  @param  modifier    修饰符参数
 *  @param  currentUserDetailResult   当前操作用户的信息请求结果状态
 */
@Composable
fun UserTitlePanel(
    modifier: Modifier,
    currentUserDetailResult : NetUserInfoLoadResult<RequestUserDetail>
) {
    if (currentUserDetailResult is NetUserInfoLoadResult.Success) {
        val currentUserDetail = currentUserDetailResult.userDetail
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (currentUserDetail.avatar_url.contains("avatar")) LOCAL_SERVER_URL + currentUserDetail.avatar_url else currentUserDetail.avatar_url,
                contentDescription = currentUserDetail.name,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder),
                modifier = Modifier
                    .clip(CircleShape)
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = currentUserDetail.name,
                fontStyle = MaterialTheme.typography.h2.fontStyle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
            )
        }
    } else {
        /* 循环加载动画 */
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(28.dp)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.width(18.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .height(22.dp)
                    .width(110.dp)
                    .shimmerEffect()
            )
        }
    }
}

/** 自建歌单条目
 *  @param  modifier    修饰符参数
 *  @param  playlistSubscribeInfo   自建歌单订阅数据类型
 *  @param  trackCount  当前自建歌单收录曲目数量
 *  @param  isMyFavoritePlaylist    当前自建歌单是否为我的默认喜爱歌单
 */
@Composable
fun CreatedPlaylistItem(
    modifier: Modifier = Modifier,
    playlistSubscribeInfo : Subscribe,
    trackCount : Int,
    isMyFavoritePlaylist : Boolean
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = playlistSubscribeInfo.title,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .weight(4f)
        )
        Text(
            text = if (isMyFavoritePlaylist) "$trackCount/∞" else "$trackCount/500",
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colors.onSurface.copy(alpha = .7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .weight(1f)
        )
    }
}

/** 使用produceState将当前用户详细信息数据Flow转换为State
 *  @param  userId  当前执行用户ID
 *  @param  viewModel   当前UserDetailScreen 的ViewModel
 */
@Composable
fun loadCurrentUserDetail(
    userId : Long,
    viewModel : CollectDialogVM
) : State<NetUserInfoLoadResult<RequestUserDetail>> {
    return produceState(initialValue = NetUserInfoLoadResult.Loading as NetUserInfoLoadResult<RequestUserDetail>, userId, viewModel) {
        var currentUserDetail : RequestUserDetail? = null
        viewModel.currentUserDetailFlow(userId = userId)
            .catch {
                value = NetUserInfoLoadResult.Error
            }
            .collect {
                currentUserDetail = it
            }
        value = if (currentUserDetail == null) {
            NetUserInfoLoadResult.Error
        } else {
            NetUserInfoLoadResult.Success(currentUserDetail!!)
        }
    }
}