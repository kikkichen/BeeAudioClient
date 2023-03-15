package com.chen.beeaudio.screen.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.chen.beeaudio.navigation.PersonRoute

@ExperimentalComposeUiApi
@Composable
fun PremiumRecommendDialog(
    visible : Boolean,
    navController: NavController,
    parentWidth : Int,
    onDismissEvent : () -> Unit,
) {
    AnimatedVisibility (
        visible = visible,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(500)),
    ) {
        Dialog(
            onDismissRequest = { onDismissEvent() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            PremiumRecommendBody(
                navController = navController,
                parentWidth = parentWidth,
                onDismissEvent = onDismissEvent
            )
        }
    }
}

@Composable
fun PremiumRecommendBody(
    navController: NavController,
    parentWidth : Int,
    onDismissEvent : () -> Unit,
) {
    val compositeResult : LottieCompositionResult = rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("lottie/recommend_premium.json")
    )
    val progressAnimation by animateLottieCompositionAsState(
        compositeResult.value,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
        speed = 1.0f
    )
    Column(
        modifier = Modifier
            .width((parentWidth * 0.8f).dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(color = MaterialTheme.colors.surface)
            .animateContentSize(),
    ) {
        Surface(
            modifier = Modifier.height(240.dp)
        ) {
            LottieAnimation(composition = compositeResult.value, progress = progressAnimation )
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize()
        ) {
            Text(
                text = "该曲播放需要将账号升级到 Premium 套餐才能享用，您是否愿意考虑升级账户呢？",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.body2
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            /* 取消按键 */
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onDismissEvent() }
            ) {
                Text(
                    text = "我再考虑",
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Light,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onSurface.copy(alpha = .7f)
                )
            }
            /* 确定按键 */
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = MaterialTheme.colors.primary)
                    .clickable {
                        navController.navigate(route = PersonRoute.GuidePremiumScreen.route)
                        onDismissEvent()
                    },
            ) {
                Text(
                    text = "现在看看",
                    color = MaterialTheme.colors.surface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center)
                )
            }
        }
    }
}