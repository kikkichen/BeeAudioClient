package com.chen.beeaudio.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chen.beeaudio.R
import com.chen.beeaudio.navigation.AuthRoute
import com.chen.beeaudio.navigation.BottomBarRoute
import com.chen.beeaudio.navigation.PersonRoute
import com.chen.beeaudio.viewmodel.LoginResultState
import com.chen.beeaudio.viewmodel.LoginViewModel
import com.chen.beeaudio.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    /* SnackBar 状态变量 */
    val snackBarState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit, block = {
        mainViewModel.clearSubscribeAndTrackDatabase()
    } )

    /* test */
    val accessToken = mainViewModel.accessToken.collectAsState()

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colors.surface)
            .background(color = MaterialTheme.colors.onSurface.copy(alpha = 0f))
    ) {
        when (loginViewModel.loginState.value) {
            is LoginResultState.FormatError -> {
                LaunchedEffect(loginViewModel.loginState.value) {
                    scope.launch {
                        snackBarState.showSnackbar("❌ 文本框内容不得为空", "关闭")
                    }
                }
            }
            is LoginResultState.OtherError -> {
                LaunchedEffect(loginViewModel.loginState.value) {
                    scope.launch {
                        snackBarState.showSnackbar("❌ 服务端错误", "关闭")
                    }
                }
            }
            is LoginResultState.Success -> {
                LaunchedEffect(loginViewModel.loginState.value) {
                    scope.launch {
                        snackBarState.showSnackbar("✅ 欢迎回来", "关闭")
                        /* 进入主页 */
                        delay(1000)
                        navController.navigate(
                            route = BottomBarRoute.AudioHome.route
                        )
                    }
                }
            }
            else -> {  }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = .5f)
                .clip(shape = RoundedCornerShape(bottomEnd = 60.dp))
                .background(color = MaterialTheme.colors.onSurface.copy(alpha = 0f)),
        ) {
            Image(
                painter = painterResource(id = R.drawable.login_background),
                contentDescription = "Top Login Image - Megumi Kato",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.surface)
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(shape = RoundedCornerShape(topStart = 60.dp))
                .background(color = MaterialTheme.colors.surface)
            ) {
                Column() {
                    Text(
                        text = "Please Login in",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .clickable {
                                mainViewModel.loadTokenData()
                                Log.d("_chen", "accessToken = ${accessToken.value}")
                            },
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    )

                    OutlinedTextField(
                        value = loginViewModel.accountText.collectAsState().value,
                        onValueChange = { loginViewModel.changeAccountText(it) },
                        label = { Text(text = "邮箱或手机号码") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 16.dp)
                    )

                    OutlinedTextField(
                        value = loginViewModel.password.collectAsState().value,
                        onValueChange = { loginViewModel.changePasswordText(it) },
                        label = { Text(text = "密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 16.dp)
                    )
                    Text(text = "? 忘记密码",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .clickable {
                                val argument = 0.toLong()
                                navController.navigate(
                                    route = PersonRoute.ForgotPasswordScreen.route + "?account=$argument"
                                )
                            },
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 14.sp)
                    )
                    Button(modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(80.dp)),
                        onClick = {
                            /* 开发模式测试 */
//                            if (loginViewModel.accountText.value == "kikkichen@163.com") {
//                                navController.navigate(
//                                    route = BottomBarRoute.AudioHome.route
//                                )
//                            }
                            loginViewModel.loginAction{
                                scope.launch(Dispatchers.Main) {
                                    /* 进入主页 */
                                    delay(1000)
                                    navController.navigate(
                                        route = BottomBarRoute.AudioHome.route
                                    ) {
                                        popUpTo(AuthRoute.Login.route) {
                                            inclusive = true
                                        }
                                    }
                                    snackBarState.showSnackbar("✅ 欢迎回来", "关闭")
                                }
                            }
                        }
                    ) {
                        Text(text = "登陆")
                    }
                    TextButton(modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(80.dp)),
                        onClick = {
                            navController.navigate(
                                route = AuthRoute.SignUp.route
                            )
                        }
                    ) {
                        Text(text = "注册")
                    }
                }
            }
        }
    }
}

//@Composable
//@Preview(showBackground = true)
//fun PreviewLoginScreen() {
//    BeeAudioTheme {
//        LoginScreen()
//    }
//}