package com.chen.beeaudio.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chen.beeaudio.R
import com.chen.beeaudio.navigation.AuthRoute
import com.chen.beeaudio.navigation.PersonRoute
import com.chen.beeaudio.viewmodel.EditUserDetailViewModel
import com.chen.beeaudio.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditUserDetailScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    editUserDetailViewModel: EditUserDetailViewModel = hiltViewModel()
) {
    /* 上下文 */
    val context : Context = LocalContext.current

    /* 协程域 */
    val coroutineScope = rememberCoroutineScope()

    /* 各个信息属性 */
    val name = editUserDetailViewModel.name.collectAsState()
    val description = editUserDetailViewModel.description.collectAsState()
    val email = editUserDetailViewModel.email.collectAsState()
    val phone = editUserDetailViewModel.phone.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            EditUserDetailTopAppBar (
                onBackEvent = { navController.navigateUp() },
                onModifierPasswordEvent = { navController.navigate(route = PersonRoute.ForgotPasswordScreen.route + "?account=${editUserDetailViewModel.currentUserId}") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (editUserDetailViewModel.verifyTextNotEmpty(context)
                        && editUserDetailViewModel.verifyTextHasChange(context)
                    ) {
                        editUserDetailViewModel.updateUserDetail(
                            toastEvent = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            },
                            finishedEvent = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    mainViewModel.loadCurrentUserDetail()
                                    launch(Dispatchers.Main) {
                                        delay(500)
                                        navController.navigateUp()
                                    }
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .offset(y = (-16).dp)
                    .width(98.dp),
                shape = CircleShape,
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "修改")
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues = it)
                .padding(horizontal = 12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }
            /* 昵称输入框 */
            item {
                OutlinedTextField(
                    value = name.value,
                    onValueChange = { words -> editUserDetailViewModel.setName(words) },
                    label = { Text(text = "用户昵称") },
                    trailingIcon = {
                        if (name.value.isNotEmpty()) Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "清除文本",
                            modifier = Modifier.clickable { editUserDetailViewModel.setName("") }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    shape = MaterialTheme.shapes.medium
                )
            }
            /* 用户简介输入框 */
            item {
                OutlinedTextField(
                    value = description.value,
                    onValueChange = { words -> editUserDetailViewModel.setDescription(words) },
                    label = { Text(text = "用户简介") },
                    trailingIcon = {
                        if (description.value.isNotEmpty()) Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "清除文本",
                            modifier = Modifier.clickable { editUserDetailViewModel.setDescription("") }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(184.dp)
                        .padding(10.dp),
                    shape = MaterialTheme.shapes.medium
                )
            }
            /* 邮箱输入框 */
            item {
                OutlinedTextField(
                    value = email.value,
                    onValueChange = { words -> editUserDetailViewModel.setEmail(words) },
                    label = { Text(text = "邮箱") },
                    trailingIcon = {
                        if (email.value.isNotEmpty()) Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "清除文本",
                            modifier = Modifier.clickable { editUserDetailViewModel.setEmail("") }
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    shape = MaterialTheme.shapes.medium
                )
            }
            /* 电话号码输入框 */
            item {
                OutlinedTextField(
                    value = phone.value,
                    onValueChange = { words ->
                        if (words.length <= 13) {
                            editUserDetailViewModel.setPhone(words)
                        }
                    },
                    label = { Text(text = "电话号码") },
                    trailingIcon = {
                        if (phone.value.isNotEmpty()) Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "清除文本",
                            modifier = Modifier.clickable { editUserDetailViewModel.setPhone("") }
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    }
}

@Composable
fun EditUserDetailTopAppBar(
    onBackEvent: () -> Unit,
    onModifierPasswordEvent: () -> Unit
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        backgroundColor = MaterialTheme.colors.surface,
        navigationIcon = {
            IconButton(
                onClick = { onBackEvent() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back previous page"
                )
            }
        },
        title = {
            Text(text = "修改我的个人信息")
        },
        actions = {
            IconButton(onClick = {
                onModifierPasswordEvent()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user_password_modifier),
                    contentDescription = "modifier user password",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}