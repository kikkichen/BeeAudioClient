package com.chen.beeaudio.screen.SendScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.chen.beeaudio.ui.theme.DeepPurple50
import com.chen.beeaudio.R

/**
 *  显示尾巴Chip
 */
@Composable
fun TailChipSetting(
    onOpenDialog: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(top = 2.dp, end = 16.dp, bottom = 2.dp, start = 8.dp)
            .clip(CircleShape)
            .clickable {
                onOpenDialog()
            },
        color = if (isSystemInDarkTheme()) MaterialTheme.colors.surface else DeepPurple50
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_show_tail),
                contentDescription = "Show my Tail",
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.CenterVertically),
            )
            Divider(Modifier.width(4.dp))
            Text(
                text = "显示小尾巴",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}

/**
 *  更改小尾巴 输入对话框
 *  @param  isOpenDialog    打开对话框状态
 */
@ExperimentalComposeUiApi
@Composable
fun StartInputDialog(
    isOpenDialog: MutableState<Boolean>,
) {
    /* 当前视图上下文信息 */
    val context = LocalContext.current

    /* 输入文本 */
    var inputString by remember { mutableStateOf("") }

    if (isOpenDialog.value) {
        Dialog(
            onDismissRequest = { isOpenDialog.value = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = true
            )
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .background(color = MaterialTheme.colors.surface.copy(alpha = 0f))
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 2.dp),
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "设置小尾巴",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = .7f))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputString,
                        onValueChange = { inputString = it },
                        label = { Text(text = "尾巴设置") },
                        placeholder = { Text(text = "设置你的博文小尾巴 \uD83D\uDCF1") },
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp, top = 2.dp, end = 2.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row {
                            TextButton(
                                onClick = {
                                    Toast.makeText(context, "取消按钮触发", Toast.LENGTH_SHORT).show()
                                    isOpenDialog.value = false
                                }
                            ) {
                                Text(text = "取消")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = {
                                    Toast.makeText(context, "确认按钮触发", Toast.LENGTH_SHORT).show()
                                    isOpenDialog.value = false
                                },
                            ) {
                                Text(text = "确认")
                            }
                        }
                    }
                }
            }
        }
    }
}