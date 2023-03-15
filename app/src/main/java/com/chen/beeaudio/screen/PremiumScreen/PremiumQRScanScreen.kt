package com.chen.beeaudio.screen.PremiumScreen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.mock.PremiumGroupString
import com.chen.beeaudio.model.audio.FamilyPremium
import com.chen.beeaudio.qr.QrCodeAnalyzer
import com.chen.beeaudio.utils.TimeUtils
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.PremiumQRScanVM
import com.google.gson.Gson
import java.text.SimpleDateFormat

@Composable
fun PremiumQRScanScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    premiumQRScanVM: PremiumQRScanVM = hiltViewModel()
) {
    /* 扫描获取字符 */
    val scanIdState = premiumQRScanVM.scanId.collectAsState()

    /* 请求结果Premium信息 */
    val scanResultState = premiumQRScanVM.premiumGroup.collectAsState()

    /* 屏幕宽度 */
    val screenWidth = LocalConfiguration.current.screenWidthDp

    /* 上下文 */
    val context = LocalContext.current
    /* 生命周期 */
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    var hasCanPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCanPermission = granted
        }
    )
    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (hasCanPermission) {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(color = Color.Transparent)
            ) {
                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context)
                        val preview = Preview.Builder().build()
                        val selector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                        val imageAnalysis = ImageAnalysis.Builder()
//                        .setTargetResolution(Size(
//                            previewView.width,
//                            previewView.height
//                        ))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        imageAnalysis.setAnalyzer(
                            ContextCompat.getMainExecutor(context),
                            QrCodeAnalyzer { result ->
                                if ((result.length == 25) && (scanIdState.value != result)) {
                                    premiumQRScanVM.changeScanId(newString = result)
                                    premiumQRScanVM.loadPremiumGroupInfo()
                                }
                            }
                        )
                        try {
                            cameraProviderFuture.get().bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e : Exception) {
                            e.printStackTrace()
                        }
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.Transparent)
                            .padding(28.dp)
                            .weight(4f),
                        color = Color.Transparent,
                    ) {
                        val compositeResult : LottieCompositionResult = rememberLottieComposition(
                            spec = LottieCompositionSpec.Asset("lottie/qr_code_scanner.json")
                        )
                        val progressAnimation by animateLottieCompositionAsState(
                            compositeResult.value,
                            isPlaying = true,
                            iterations = LottieConstants.IterateForever,
                            speed = 1.0f
                        )
                        LottieAnimation(composition = compositeResult.value, progress = progressAnimation)
                    }
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .weight(2f)
                            .animateContentSize(),
                    ) {
                        AnimatedVisibility(
                            visible = scanResultState.value.numbers.isNotEmpty(),
                            enter = fadeIn() + slideInVertically { fullHeight: Int -> fullHeight * 2 },
                            exit = fadeOut() + slideOutVertically { fullHeight: Int -> fullHeight * 2 }
                        ) {
                            /* 存在扫描结果 */
                            PreviewGroupCard(
                                modifier = Modifier
                                    .width(screenWidth.dp)
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .statusBarsPadding(),
                                navController = rememberNavController(),
                                familyPremium = scanResultState.value,
                                postApplyEvent = {
                                    premiumQRScanVM.postJoinPremiumGroupApply(
                                        context = context,
                                        currentUserId = mainViewModel.currentUserId
                                    )
                                }
                            )
                        }
                        if (scanResultState.value.numbers.isEmpty()) {
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
}

/** Premium 家庭组信息卡
 *
 */
@Composable
fun PreviewGroupCard(
    modifier: Modifier = Modifier,
    navController: NavController,
    familyPremium: FamilyPremium,
    postApplyEvent: () -> Unit,
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp
    ) {
        PremiumGroupBody(
            navController = navController,
            familyPremium = familyPremium,
            postApplyEvent = postApplyEvent
        )
    }
}

@Composable
fun PremiumGroupBody(
    navController: NavController,
    familyPremium: FamilyPremium,
    postApplyEvent: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .padding(vertical = 4.dp)
                    .padding(start = 16.dp, end = 8.dp)
                    .size(56.dp),
                border = BorderStroke(width = 3.dp, color = MaterialTheme.colors.primary),
                shape = CircleShape
            ) {
                AsyncImage(
                    model = if (familyPremium.numbers[0].AvatarUrl.contains("avatar")) LOCAL_SERVER_URL + familyPremium.numbers[0].AvatarUrl else familyPremium.numbers[0].AvatarUrl,
                    contentDescription = familyPremium.numbers[0].Name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier.wrapContentHeight(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${familyPremium.numbers[0].Name}的 Premium家庭组",
                    style = MaterialTheme.typography.subtitle1.copy(
                        color = MaterialTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "共 ${familyPremium.numbers.size} 位成员",
                    style = MaterialTheme.typography.subtitle2.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = .5f),
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp
                    )
                )
            }
        }
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp),
                tint = MaterialTheme.colors.onSurface.copy(alpha = .5f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "于 ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(TimeUtils.strToDateTime(familyPremium.summarize.server_expired))} 到期",
                style = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = .5f),
                    fontSize = 14.sp,
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (postButton, numberAvatarGroup) = createRefs()
            Button(
                onClick = {
                    postApplyEvent()
                    navController.navigateUp()
                },
                modifier = Modifier
                    .constrainAs(postButton) {
                        start.linkTo(parent.start, margin = 16.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(text = "发送申请")
            }
            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .constrainAs(numberAvatarGroup) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 16.dp)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                familyPremium.numbers.forEach {
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .padding(vertical = 4.dp, horizontal = 1.dp)
                            .size(32.dp),
                        shape = CircleShape
                    ) {
                        AsyncImage(
                            model = if (it.AvatarUrl.contains("avatar")) LOCAL_SERVER_URL + it.AvatarUrl else it.AvatarUrl,
                            contentDescription = it.Name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
fun PreviewPremiumGroupBody() {
    PreviewGroupCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .statusBarsPadding(),
        navController = rememberNavController(),
        familyPremium = Gson().fromJson(PremiumGroupString, FamilyPremium::class.java),
        postApplyEvent = {  }
    )
}