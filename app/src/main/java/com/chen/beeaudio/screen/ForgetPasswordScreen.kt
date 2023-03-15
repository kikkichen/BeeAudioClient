package com.chen.beeaudio.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.viewmodel.ForgetPasswordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun ForgetPasswordScreen(
    navController: NavController,
    viewModel: ForgetPasswordViewModel = hiltViewModel()
) {
    /* 上下文 */
    val context: Context = LocalContext.current

    /* 输入属性 */
    val email = viewModel.accountString.collectAsState()
    val userId = viewModel.accountNumber.collectAsState()
    val originalPassword = viewModel.originalPassword.collectAsState()
    val newPassword1 = viewModel.newPassword1.collectAsState()
    val newPassword2 = viewModel.newPassword2.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "忘记密码") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回登陆页")
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (viewModel.verifyAllTextFieldNotEmpty(context)
                        && viewModel.verifyNewPasswordSample(context)
                    ) {
                        viewModel.postModifierPassword(
                            toastEvent = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            },
                            finishedEvent = {
                                navController.navigateUp()
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
                    Text(text = "修改密码")
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
            if (viewModel.isModifierPasswordByEmail) {
                item{
                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { words -> viewModel.setAccountString(words) },
                        label = { Text(text = "用户Email") },
                        trailingIcon = {
                            if (email.value.isNotEmpty()) Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "清除Email文本",
                                modifier = Modifier.clickable { viewModel.setAccountString("") }
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
            } else {
                item {
                    OutlinedTextField(
                        value = userId.value.toString(),
                        onValueChange = { /* empty */ },
                        label = { Text(text = "用户UID") },
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
            item{
                OutlinedTextField(
                    value = originalPassword.value,
                    onValueChange = { words -> viewModel.setOriginalPassword(words) },
                    label = { Text(text = "原密码") },
                    trailingIcon = {
                        if (email.value.isNotEmpty()) Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "清除密码",
                            modifier = Modifier.clickable { viewModel.setOriginalPassword("") }
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    shape = MaterialTheme.shapes.medium
                )
            }
            item{
                OutlinedTextField(
                    value = newPassword1.value,
                    onValueChange = { words -> viewModel.setNewPassword1(words) },
                    label = { Text(text = "新密码") },
                    trailingIcon = {
                        if (email.value.isNotEmpty()) Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "清除密码",
                            modifier = Modifier.clickable { viewModel.setNewPassword1("") }
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    shape = MaterialTheme.shapes.medium
                )
            }
            item{
                OutlinedTextField(
                    value = newPassword2.value,
                    onValueChange = { words -> viewModel.setNewPassword2(words) },
                    label = { Text(text = "重复新密码") },
                    trailingIcon = {
                        if (email.value.isNotEmpty()) Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "清除密码",
                            modifier = Modifier.clickable { viewModel.setNewPassword2("") }
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
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
@Preview(showBackground = true)
fun PreviewForgetPasswordScreen() {
    BeeAudioTheme {
        ForgetPasswordScreen(rememberNavController())
    }
}