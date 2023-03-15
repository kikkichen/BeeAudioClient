package com.chen.beeaudio.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chen.beeaudio.viewmodel.RegisterConformResult
import com.chen.beeaudio.viewmodel.RegisterViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@Composable
fun RegisterScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = hiltViewModel()
) {
    /* 显示SnackBar提示 */
    var showSnackBarState = remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()
    Scaffold(
        topBar = { RegisterTopBar { navController.navigateUp() } },
        scaffoldState = scaffoldState
    ) {
        if (registerViewModel.registerResultState.value != RegisterConformResult.NONE) {
            LaunchedEffect(registerViewModel.registerResultState.value) {
                when(registerViewModel.registerResultState.value) {
                    is RegisterConformResult.EMAIL_FORMAT_ERROR -> {
                        scaffoldState.snackbarHostState.showSnackbar("❌ 邮箱格式错误", "Register Feedback")
                    }
                    is RegisterConformResult.PASSWORD_FORMAT_ERROR -> {
                        scaffoldState.snackbarHostState.showSnackbar("❌ 密码格式错误 6~20位字母数字组合", "Register Feedback")
                    }
                    is RegisterConformResult.PASSWORD_CONFIRM_ERROR -> {
                        scaffoldState.snackbarHostState.showSnackbar("❌ 请确保密码输入无误", "Register Feedback")
                    }
                    is RegisterConformResult.OTHER_ERROR -> {
                        scaffoldState.snackbarHostState.showSnackbar("❌ 服务端响应出错", "Register Feedback")
                    }
                    is RegisterConformResult.SUCCESS -> {
                        val userId = (registerViewModel.registerResultState.value as RegisterConformResult.SUCCESS).userid
                        scaffoldState.snackbarHostState.showSnackbar("✔ 你好，用户$userId, 快去登陆吧", "Register Feedback")
                        delay(3000)
                        navController.navigateUp()
                    }
                    else -> {  }
                }
                registerViewModel.registerResultState.value = RegisterConformResult.NONE
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues = it)
        ) {
            val pagerList = listOf("邮箱注册", "手机号码注册")
            val pagerState = rememberPagerState()
            TabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    PagerTabIndicator(tabPositions = tabPositions, pagerState = pagerState)
                },
                backgroundColor = MaterialTheme.colors.surface
            ) {
                val scope: CoroutineScope = rememberCoroutineScope()
                pagerList.forEachIndexed { index, title ->
                    PagerTab(
                        pagerState = pagerState,
                        index = index,
                        pageCount = pagerList.size,
                        text = title,
                        modifier = Modifier
                            .height(50.dp)
                            .clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }
            HorizontalPager(
                count = pagerList.size,
                state = pagerState
            ) { page ->
                when(page) {
                    0 -> {
                        RegisterByEmailBlock(registerViewModel = registerViewModel) {
                            registerViewModel.registerUser(isEmail = true)
                        }
                    }
                    1 -> {
                        RegisterByPhoneBlock(registerViewModel = registerViewModel)
                    }
                }
            }
        }
    }
}

/** 注册页顶栏
 *
 */
@Composable
fun RegisterTopBar(
    onBackLoginEvent : () -> Unit
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
            .statusBarsPadding(),
        title = { Text(text = "注册新用户") },
        navigationIcon = {
            IconButton(onClick = onBackLoginEvent) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回登陆页")
            }
        },
        backgroundColor = MaterialTheme.colors.surface,
    ) 
}

/** 通过邮箱注册， 页面块
 *
 */
@Composable
fun RegisterByEmailBlock(
    registerViewModel : RegisterViewModel,
    onRegisterEvent : () -> Unit
) {
    val emailContent = registerViewModel.registerEmail.collectAsState()
    val passwordContent = registerViewModel.password.collectAsState()
    val confirmPasswordContent = registerViewModel.confirmPassword.collectAsState()

    ConstraintLayout {
        val (formBlock, registerButton) = createRefs()
        Column(
            modifier = Modifier.constrainAs(formBlock) {
                start.linkTo(parent.start, margin = 16.dp)
                top.linkTo(parent.top)
                end.linkTo(parent.end, margin = 16.dp)
            }.fillMaxSize()
        ) {
            Spacer(
                modifier = Modifier
                    .height(24.dp)
                    .fillMaxWidth()
            )
            /* 邮箱文本框 */
            OutlinedTextField(
                value = emailContent.value,
                onValueChange = { registerViewModel.changeTextEmail(it) },
                label = { Text(text = "邮箱") },
                trailingIcon = {
                   if (emailContent.value.isNotEmpty()) Icon(
                       imageVector = Icons.Outlined.Close,
                       contentDescription = "清除文本",
                       modifier = Modifier.clickable { registerViewModel.changeTextEmail("") }
                   )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = MaterialTheme.shapes.medium
            )
            /* 密码文本框 */
            OutlinedTextField(
                value = passwordContent.value,
                onValueChange = { registerViewModel.changePasswordText(it) },
                label = { Text(text = "密码") },
                trailingIcon = {
                    if (passwordContent.value.isNotEmpty()) Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "清除文本",
                        modifier = Modifier.clickable { registerViewModel.changePasswordText("") }
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = MaterialTheme.shapes.medium
            )
            /* 确认密码文本框 */
            OutlinedTextField(
                value = confirmPasswordContent.value,
                onValueChange = { registerViewModel.changeConfirmPasswordText(it) },
                trailingIcon = {
                    if (confirmPasswordContent.value.isNotEmpty()) Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "清除文本",
                        modifier = Modifier.clickable { registerViewModel.changeConfirmPasswordText("") }
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                label = { Text(text = "确认密码") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = MaterialTheme.shapes.medium
            )
        }
        Button(
            onClick = onRegisterEvent,
            modifier = Modifier
                .constrainAs(registerButton) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .clip(CircleShape)
        ) {
            Text(text = "邮箱注册")
        }
    }
}

/** 通过手机号码注册 页面块
 *
 */
@Composable
fun RegisterByPhoneBlock(
    registerViewModel : RegisterViewModel
) {
    val phoneContent = registerViewModel.registerPhone.collectAsState()
    val passwordContent = registerViewModel.password.collectAsState()
    val confirmPasswordContent = registerViewModel.confirmPassword.collectAsState()

    ConstraintLayout {
        val (formBlock, registerButton) = createRefs()
        Column(
            modifier = Modifier.constrainAs(formBlock) {
                start.linkTo(parent.start, margin = 16.dp)
                top.linkTo(parent.top)
                end.linkTo(parent.end, margin = 16.dp)
            }
        ) {
            Spacer(
                modifier = Modifier
                    .height(24.dp)
                    .fillMaxWidth()
            )
            /* 邮箱文本框 */
            OutlinedTextField(
                value = phoneContent.value,
                onValueChange = { registerViewModel.changeTextPhone(it) },
                label = { Text(text = "手机号码") },
                trailingIcon = {
                    if (phoneContent.value.isNotEmpty()) Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "清除文本",
                        modifier = Modifier.clickable { registerViewModel.changeTextPhone("") }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = MaterialTheme.shapes.medium
            )
            /* 密码文本框 */
            OutlinedTextField(
                value = passwordContent.value,
                onValueChange = { registerViewModel.changePasswordText(it) },
                label = { Text(text = "密码") },
                trailingIcon = {
                    if (passwordContent.value.isNotEmpty()) Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "清除文本",
                        modifier = Modifier.clickable { registerViewModel.changePasswordText("") }
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = MaterialTheme.shapes.medium
            )
            /* 确认密码文本框 */
            OutlinedTextField(
                value = confirmPasswordContent.value,
                onValueChange = { registerViewModel.changeConfirmPasswordText(it) },
                trailingIcon = {
                    if (confirmPasswordContent.value.isNotEmpty()) Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "清除文本",
                        modifier = Modifier.clickable { registerViewModel.changeConfirmPasswordText("") }
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                label = { Text(text = "确认密码") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = MaterialTheme.shapes.medium
            )
        }
        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .constrainAs(registerButton) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .clip(CircleShape)
        ) {
            Text(text = "邮箱注册")
        }
    }
}
