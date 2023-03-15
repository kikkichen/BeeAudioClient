package com.chen.beeaudio.screen.widget

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.mock.SimpleUserMock
import com.chen.beeaudio.model.blog.SimpleUser
import com.chen.beeaudio.ui.theme.Amber300
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.ui.theme.Cyan100
import com.chen.beeaudio.ui.theme.Cyan300

@Composable
fun SimpleUserShowItemWidget(
    user: SimpleUser,
    onClickUserEvent: (Long) -> Unit,
    onFollowOnButtonEvent : (Long) -> Unit
) {
    val relative = remember {
        mutableStateOf(user.followState)
    }
    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = 2.dp,
        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clickable {
                onClickUserEvent(user.Id)
            }
    ) {
        Surface(
            modifier = Modifier
                .height(62.dp)
                .fillMaxWidth()
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxWidth()
            ) {
                val (avatar, userInfo, followButton) = createRefs()
                val endGuideLine = createGuidelineFromEnd(.3f)
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .constrainAs(avatar) {
                            start.linkTo(parent.start, margin = 8.dp)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                        .padding(3.dp),
                    shape = CircleShape,
                    border = BorderStroke(2.dp, MaterialTheme.colors.primary),
                ) {
                    AsyncImage(
                        model = if (user.avatar.contains("avatar")) LOCAL_SERVER_URL + user.avatar else user.avatar,
                        contentDescription = "$'s avatar",
                        placeholder = painterResource(id = R.drawable.personnel)
                    )
                }
                Column(
                    modifier = Modifier
                        .constrainAs(userInfo) {
                            start.linkTo(avatar.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(endGuideLine, margin = 6.dp)
                            width = Dimension.fillToConstraints
                        }
                        .fillMaxWidth(0.75f)
                        .padding(
                            horizontal = 10.dp
                        ),
                ) {
                    Text(
                        text = user.name,
                        fontStyle = MaterialTheme.typography.h2.fontStyle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .align(Alignment.Start)
                            .weight(1f)
                    )
                    Text(
                        text = user.description,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colors.onSurface.copy(.6f),
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 4.dp)
                            .align(Alignment.Start)
                            .weight(1f)
                    )
                }
                val buttonColors by animateColorAsState(
                    when (relative.value) {
                        0 -> {
                            Cyan100
                        }
                        1 -> {
                            MaterialTheme.colors.primary
                        }
                        2 -> {
                            Amber300
                        }
                        else -> {
                            MaterialTheme.colors.primary.copy(alpha = .3f)
                        }
                    }
                )
                Button(
                    onClick = {
                        onFollowOnButtonEvent(user.Id)
                      when(relative.value) {
                          1 -> {
                              relative.value = 0
                          }
                          2 -> {
                              relative.value = 3
                          }
                          3 -> {
                              relative.value = 1
                          }
                          else -> {
                              relative.value = 1
                          }
                      }
                    },
                    modifier = Modifier
                        .width(112.dp)
                        .constrainAs(followButton) {
                            start.linkTo(endGuideLine, margin = 6.dp)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end, margin = 6.dp)
                        },
                    colors = ButtonDefaults.buttonColors(backgroundColor = buttonColors),
                    shape = CircleShape
                ) {
                    when (relative.value) {
                        1 -> {
                            Text(text = "- 取消关注", fontSize = 12.sp)
                        }
                        2 -> {
                            Text(text = "ta 关注了你", fontSize = 12.sp)
                        }
                        3 -> {
                            Text(text = "◎ 相互关注", fontSize = 12.sp)
                        }
                        else -> {
                            Text(text = "+ 关注", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewSimpleUserShowItemWidget() {
    BeeAudioTheme {
        SimpleUserShowItemWidget(
            user = SimpleUserMock,
            onClickUserEvent = {  },
            onFollowOnButtonEvent = {  },
        )
    }
}