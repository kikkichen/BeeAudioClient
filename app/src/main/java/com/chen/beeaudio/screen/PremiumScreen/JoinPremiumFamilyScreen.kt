package com.chen.beeaudio.screen.PremiumScreen

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.*
import com.chen.beeaudio.viewmodel.JoinPremiumFamilyVM
import com.chen.beeaudio.viewmodel.MainViewModel

@Composable
fun JoinPremiumFamilyScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    joinPremiumFamilyVM: JoinPremiumFamilyVM = hiltViewModel()
) {
    /* 搜索家庭Premium Card ID 卡号 */
    val inputCodeState = joinPremiumFamilyVM.code.collectAsState()

    /* 请求结果Premium信息 */
    val scanResultState = joinPremiumFamilyVM.premiumGroup.collectAsState()

    /* 上下文 */
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            JoinPremiumFamilyTopAppBar(
                navController = navController,
                codeString = inputCodeState.value,
                searchEvent = {
                    if (inputCodeState.value.length >= 25) {
                        joinPremiumFamilyVM.loadPremiumGroupInfo()
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(paddingValues = it)
        ) {
            item {
                TextField(
                    value = inputCodeState.value,
                    onValueChange = { newWord ->
                        joinPremiumFamilyVM.changeCodeString(newWord)
                        Log.d("_chen", "len : ${inputCodeState.value.length}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .padding(top = 12.dp, bottom = 24.dp),
                    placeholder = {
                        Text(text = "xxxxx-xxxxx-xxxxx-xxxxx-xxxxx")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { /* TODO */ }
                    ),
                    singleLine = true,
                    label = { Text(text = "卡号") }
                )
            }
            item {
                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .animateContentSize(),
                ) {
                    AnimatedVisibility(
                        visible = scanResultState.value.numbers.isNotEmpty() && inputCodeState.value.length >= 24,
                        enter = fadeIn() + slideInVertically { fullHeight: Int -> fullHeight * 2 },
                        exit = fadeOut() + slideOutVertically { fullHeight: Int -> fullHeight * 2 }
                    ) {
                        /* 存在扫描结果 */
                        PreviewGroupCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .statusBarsPadding(),
                            navController = rememberNavController(),
                            familyPremium = scanResultState.value,
                            postApplyEvent = {
                                joinPremiumFamilyVM.postJoinPremiumGroupApply(
                                    context = context,
                                    currentUserId = mainViewModel.currentUserId
                                )
                            }
                        )
                    }
                    if (inputCodeState.value.length >= 25) {
                        /* 扫描中 */
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .offset(y = (-20).dp),
                            color = Color.Transparent,
                        ) {
                            val compositeResult : LottieCompositionResult = rememberLottieComposition(
                                spec = LottieCompositionSpec.Asset("lottie/searching_premium_group.json")
                            )
                            val progressAnimation by animateLottieCompositionAsState(
                                compositeResult.value,
                                isPlaying = true,
                                iterations = LottieConstants.IterateForever,
                                speed = 1.0f
                            )
                            LottieAnimation(composition = compositeResult.value, progress = progressAnimation)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JoinPremiumFamilyTopAppBar(
    navController: NavController,
    codeString: String,
    searchEvent: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "backup previous page"
                )
            }
        },
        title = { Text(text = "加入 Premium 家庭组") },
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        actions = {
            AnimatedVisibility(
                visible = codeString.length >= 25,
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(400))
            ) {
                Button(
                    onClick = { searchEvent() },
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        Text(text = "查询")
                    }
                }
            }
        }
    )
}