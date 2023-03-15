package com.chen.beeaudio.screen.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.navigation.PersonRoute
import com.chen.beeaudio.ui.theme.Red600
import com.chen.beeaudio.ui.theme.Red700

/** 账户操作弹窗
 *
 */
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun AccountControlDialog(
    visible: Boolean,
    navController: NavController,
    userInfo: RequestUserDetail,
    parentWidth : Int,
    uploadAvatarEvent: () -> Unit,
    logoutAccountEvent: () -> Unit,
    dismissEvent: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(400))
    ) {
        Dialog(
            onDismissRequest = { dismissEvent() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            AccountControlBody(
                navController = navController,
                userInfo = userInfo,
                parentWidth = parentWidth,
                uploadAvatarEvent = uploadAvatarEvent,
                logoutAccountEvent = logoutAccountEvent,
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun AccountControlBody(
    navController: NavController,
    userInfo: RequestUserDetail,
    parentWidth : Int,
    uploadAvatarEvent: () -> Unit,
    logoutAccountEvent: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width((parentWidth * 0.7f).dp)
            .wrapContentHeight()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp,
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center),
                    shape = CircleShape,
                    onClick = { uploadAvatarEvent() }
                ) {
                    AsyncImage(
                        model = if (userInfo.avatar_url.contains("avatar")) LOCAL_SERVER_URL + userInfo.avatar_url else userInfo.avatar_url,
                        contentDescription = userInfo.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(color = MaterialTheme.colors.surface.copy(alpha = .5f)),
                    ) {
                        Icon(
                            painter = painterResource(id = com.chen.beeaudio.R.drawable.ic_replace_avatar),
                            contentDescription = "replace this avatar",
                            modifier = Modifier.size(24.dp).align(Alignment.Center)
                        )
                    }
                }
            }
            OutlinedButton(
                onClick = {
                    navController.navigate(route = BlogRoute.UserDetail.route + "?uid=${userInfo.uid}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "我的个人主页",
                        modifier = Modifier.size(30.dp).weight(1f)
                    )
                    Text(
                        text = "我的个人主页",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.weight(3f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            OutlinedButton(
                onClick = { 
                    navController.navigate(route = PersonRoute.EditUserDetailScreen.route + "?user_id=${userInfo.uid}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = com.chen.beeaudio.R.drawable.ic_edit_user_detail),
                        contentDescription = "修改个人信息",
                        modifier = Modifier.size(30.dp).weight(1f)
                    )
                    Text(
                        text = "修改个人信息",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.weight(3f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            OutlinedButton(
                onClick = {
                    logoutAccountEvent()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Red700, contentColor = Color.Black)
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = com.chen.beeaudio.R.drawable.ic_user_logout),
                        contentDescription = "退出当前账户",
                        modifier = Modifier.size(26.dp).padding(start = 6.dp).weight(1f),
                        tint = Color.Black
                    )
                    Text(
                        text = "退出当前账户",
                        style = MaterialTheme.typography.subtitle1,
                        color = Color.Black,
                        modifier = Modifier.weight(3f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}