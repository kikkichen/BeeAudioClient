package com.chen.beeaudio.screen.PremiumScreen

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.chen.beeaudio.R
import com.chen.beeaudio.navigation.PersonRoute
import com.chen.beeaudio.ui.theme.Blue500
import com.chen.beeaudio.ui.theme.TealA700
import com.chen.beeaudio.viewmodel.GuidePremiumVM
import com.chen.beeaudio.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 升级到 Premium 套餐 引导页
 *
 */
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun GuidePremiumScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    guidePremiumVM: GuidePremiumVM = hiltViewModel()
) {
    /* 上下文 */
    val context : Context = LocalContext.current

    /* 协程域 */
    val coroutineScope = rememberCoroutineScope()

    val compositeResult : LottieCompositionResult = rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("lottie/permium_screen_animation.json")
    )
    val progressAnimation by animateLottieCompositionAsState(
        compositeResult.value,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
        speed = 1.0f
    )

    /* upgrade loading alert */
    val upgradeAlertShowState = remember { mutableStateOf(false) }

    PayForPremiumWindows(
        visible = upgradeAlertShowState.value,
        dismissEvent = { upgradeAlertShowState.value = false }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(),
    ) {
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(302.dp)
                    .background(color = Color.Transparent),
                color = Blue500
            ) {
                LottieAnimation(
                    composition = compositeResult.value,
                    progress = progressAnimation,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .statusBarsPadding()
                )
            }
        }
        stickyHeader {
            GuidePremiumTopAppBar(navController = navController)
        }
        item {
            PremiumUpgradeEnter(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .statusBarsPadding(),
                upgradeType = 1,
                context = context,
                title = "个人套餐 Premium",
                subTitle = "为你升级为个人版Premium会员套餐， 其中包含完整的Bee Audio 音频服务",
                price = "￥168",
                priceUnit = "12月",
                description = """
            该版本能够正常为您的账号升级为 个人 Premium 会员套餐，您可以凭借该套餐的身份体验一下服务：
             - 完整的付费音频体验，您可以将您享用包含Premium标识的音频分享到您的社区当中
             - 您的Premium会员标识会暴露在Bee Audio 的社交社区中。
             - 如您还有更多的疑问请在标题 ”查看政策“中阅读服务条款，并仔细分析您的受惠权益。
             
            如您对我们提供的 Premium 个人版 套餐还留有疑问，欢迎向我们的客服通道留言提问，或者向社区中的其他用户寻求帮助，祝您有一个美好舒适的 Premium 会员体验，谢谢。
        """.trimIndent(),
                serverPeople = 1,
                iconAssetName = "lottie/upgrade_person_permium.json",
                navController = navController,
                upgradeEvent = {
                    coroutineScope.launch {
                        upgradeAlertShowState.value = true
                        guidePremiumVM.upgradePersonPremium(
                            currentUserId = mainViewModel.currentUserId,
                            toastEvent = { tips ->
                                Toast.makeText(context, tips, Toast.LENGTH_SHORT).show()
                            }
                        )
                        delay(3000)
                        upgradeAlertShowState.value = false
                        /* 更新 MainViewModel 中的 Premium信息数据 */
                        mainViewModel.loadIsPremiumTag(mainViewModel.currentUserId)
                    }
                }
            )
        }
        item {
            PremiumUpgradeEnter(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .statusBarsPadding(),
                upgradeType = 2,
                context = context,
                title = "家庭组套餐 Premium",
                subTitle = "为您开通Premium家庭组套餐， 您可以与您的小伙伴一起分享 Premium 服务",
                price = "￥498",
                priceUnit = "12月",
                description = """
                    该家庭组套餐适用于所有应用于该平台的普通账号，您可以开通该家庭组套餐成为该家庭组的管理员，通过管理员身份与您的亲人、朋友们分享您的Premium家庭套餐组。
                    您与您的家庭组用户们可以凭借该套餐的身份体验一下服务：
                     - 完整的付费音频体验，您可以将您享用包含Premium标识的音频分享到您的社区当中
                     - 您的Premium会员标识会暴露在Bee Audio 的社交社区中。
                     - 如您还有更多的疑问请在标题 ”查看政策“中阅读服务条款，并仔细分析您的受惠权益。
                     
                    如您对我们提供的 Premium 家庭版 套餐还留有疑问，欢迎向我们的客服通道留言提问，或者向社区中的其他用户寻求帮助，祝您有一个美好舒适的 Premium 会员体验，谢谢。
                """.trimIndent(),
                serverPeople = 6,
                iconAssetName = "lottie/upgrade_family_permium.json",
                navController = navController,
                upgradeEvent = {
                    coroutineScope.launch {
                        upgradeAlertShowState.value = true
                        guidePremiumVM.upgradeFamilyPremium(
                            currentUserId = mainViewModel.currentUserId,
                            toastEvent = { tips ->
                                Toast.makeText(context, tips, Toast.LENGTH_SHORT).show()
                            }
                        )
                        delay(3000)
                        upgradeAlertShowState.value = false
                        /* 更新 MainViewModel 中的 Premium信息数据 */
                        mainViewModel.loadIsPremiumTag(mainViewModel.currentUserId)
                    }
                }
            )
        }
        item {
            PremiumUpgradeEnter(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .statusBarsPadding(),
                upgradeType = 0,
                context = context,
                title = "加入 Premium 家庭组",
                subTitle = "在其 Premium家庭组 管理员的同意下，加入新的 Premium家庭组。",
                price = "￥0",
                priceUnit = "12月",
                description = """
                    您可以在当前身份下，加入Premium家庭组，您可以通过您的亲人、朋友的允许下，加入他们的Premium家庭组。
                    您与您的家庭组用户们可以凭借该套餐的身份体验一下服务：
                     - 完整的付费音频体验，您可以将您享用包含Premium标识的音频分享到您的社区当中
                     - 您的Premium会员标识会暴露在Bee Audio 的社交社区中。
                     - 如您还有更多的疑问请在标题 ”查看政策“中阅读服务条款，并仔细分析您的受惠权益。
                     
                    如您对我们提供的 Premium 家庭版 套餐还留有疑问，欢迎向我们的客服通道留言提问，或者向社区中的其他用户寻求帮助，祝您有一个美好舒适的 Premium 会员体验，谢谢。
                """.trimIndent(),
                serverPeople = 6,
                iconAssetName = "lottie/join_in_family.json",
                navController = navController,
                upgradeEvent = { /* empty */ }
            )
        }
    }
}

/** Premium 套餐升级导航页 TopAppBar
 *
 */
@ExperimentalFoundationApi
@Composable
fun GuidePremiumTopAppBar(
    navController: NavController
) {
    /* 更多信息菜单 展开标识 */
    val menuExpandSignal = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .height(82.dp)
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Box(modifier = Modifier
                .height(24.dp)
                .fillMaxWidth()
                .background(color = Blue500))
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (backArrow, title, premiumLogo, moreVector) = createRefs()
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.constrainAs(backArrow) {
                        start.linkTo(parent.start, margin = 10.dp)
                        top.linkTo(title.top)
                        bottom.linkTo(title.bottom)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "backup previous page"
                    )
                }
                Text(
                    text = "升级到 Premium 套餐",
                    style = MaterialTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.constrainAs(title) {
                        start.linkTo(backArrow.end, margin = 8.dp)
                        top.linkTo(parent.top, margin = 10.dp)
                        end.linkTo(moreVector.start, margin = 16.dp)
                        width = Dimension.fillToConstraints
                    },
                )
                Card(
                    modifier = Modifier
                        .wrapContentSize()
                        .constrainAs(premiumLogo) {
                            start.linkTo(backArrow.end, margin = 8.dp)
                            top.linkTo(title.bottom, margin = 4.dp)
                            bottom.linkTo(parent.bottom, margin = 4.dp)
                        },
                    elevation = 0.dp,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = "PREMIUM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .background(color = TealA700)
                            .padding(horizontal = 3.dp)
                    )
                }
                IconButton(
                    onClick = { menuExpandSignal.value = true },
                    modifier = Modifier.constrainAs(moreVector) {
                        top.linkTo(title.top)
                        end.linkTo(parent.end, margin = 8.dp)
                        bottom.linkTo(title.bottom)
                    }
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "more info")
                    DropdownMenu(
                        expanded = menuExpandSignal.value,
                        onDismissRequest = { menuExpandSignal.value = false }
                    ) {
                        DropdownMenuItem(
                            onClick = { navController.navigate(route = PersonRoute.PremiumPolicyScreen.route) }
                        ) {
                            Text(text = "查看政策")
                        }
                    }
                }
            }
        }
    }
}

/** 升级方式入口
 *
 */
@ExperimentalMaterialApi
@Composable
fun PremiumUpgradeEnter(
    modifier: Modifier = Modifier,
    upgradeType: Int,
    context: Context,
    title: String,
    subTitle: String,
    price: String,
    priceUnit: String,
    description: String,
    serverPeople: Int,
    iconAssetName: String,
    navController: NavController,
    upgradeEvent: () -> Unit,
) {
    /* 项目展开标识 */
    val contentExpandSignal = remember { mutableStateOf(false) }

    val compositeResult : LottieCompositionResult = rememberLottieComposition(
        spec = LottieCompositionSpec.Asset(iconAssetName)
    )
    val progressAnimation by animateLottieCompositionAsState(
        compositeResult.value,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
        speed = 1.0f
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
        onClick = { contentExpandSignal.value = !contentExpandSignal.value }
    ) {
        ConstraintLayout {
            val (icon, backColor, dividing, serverNumber, enterButton, infoBlock) = createRefs()
            val startGuideline = createGuidelineFromStart(.25f)
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .constrainAs(backColor) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(startGuideline)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    }
            )
            when(upgradeType) {
                0 -> {
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .constrainAs(icon) {
                                start.linkTo(parent.start, margin = 12.dp)
                                top.linkTo(parent.top, margin = 44.dp)
                                end.linkTo(startGuideline)
                            }
                            .background(color = Color.Transparent),
                        contentColor = Color.Transparent,
                        color = Color.Transparent
                    ) {
                        LottieAnimation(composition = compositeResult.value, progress = progressAnimation)
                    }
                }
                1 -> {
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .constrainAs(icon) {
                                start.linkTo(parent.start, margin = 12.dp)
                                top.linkTo(parent.top, margin = 44.dp)
                                end.linkTo(startGuideline)
                            }
                            .background(color = Color.Transparent),
                        contentColor = Color.Transparent,
                        color = Color.Transparent
                    ) {
                        LottieAnimation(composition = compositeResult.value, progress = progressAnimation)
                    }
                }
                2 -> {
                    Surface(
                        modifier = Modifier
                            .height(146.dp)
                            .width(220.dp)
                            .constrainAs(icon) {
                                start.linkTo(parent.start, margin = 60.dp)
                                top.linkTo(parent.top, margin = 30.dp)
                                end.linkTo(startGuideline)
                            }
                            .background(color = Color.Transparent),
                        contentColor = Color.Transparent,
                        color = Color.Transparent
                    ) {
                        LottieAnimation(
                            composition = compositeResult.value,
                            progress = progressAnimation,
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = contentExpandSignal.value,
                modifier = Modifier
                    .constrainAs(dividing) {
                        start.linkTo(parent.start)
                        top.linkTo(icon.bottom, margin = if (upgradeType == 1) 0.dp else if (upgradeType == 2) (-12).dp else 1.dp)
                        end.linkTo(parent.end)
                    }
            ) {
                Divider(modifier = Modifier
                    .fillMaxWidth()
                )
            }
            /* 展开内容 */
            if (contentExpandSignal.value) {
                Column(
                    modifier = Modifier
                        .constrainAs(serverNumber) {
                            start.linkTo(parent.start)
                            top.linkTo(dividing.bottom, margin = 2.dp)
                            end.linkTo(startGuideline)
                            width = Dimension.fillToConstraints
                        }
                        .padding(6.dp)
                ) {
                    Text(
                        text = "提供满足",
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        PeopleNumberBlock(count = serverPeople)
                    }
                    Text(
                        text = "共${serverPeople}人份的服务",
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = Color.Black,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (upgradeType == 0) {
                    Column(
                        modifier = Modifier
                            .constrainAs(enterButton) {
                                start.linkTo(parent.start)
                                end.linkTo(startGuideline)
                                bottom.linkTo(parent.bottom)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = { navController.navigate(route = PersonRoute.PremiumQRScanScreen.route) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(color = Color.Black.copy(alpha = .1f))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_scan_qr),
                                contentDescription = "Scan QR",
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        IconButton(
                            onClick = { navController.navigate(route = PersonRoute.JoinPremiumFamilyScreen.route) },
                            modifier = Modifier
                                .padding(bottom = 30.dp)
                                .clip(CircleShape)
                                .background(color = Color.Black.copy(alpha = .1f))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_keybord_input),
                                contentDescription = "Input Code",
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = {
                          if (upgradeType == 1) {
                              upgradeEvent()
                          } else if (upgradeType == 2) {
                              upgradeEvent()
                          }
                        },
                        modifier = Modifier
                            .padding(30.dp)
                            .clip(CircleShape)
                            .background(color = Color.Black.copy(alpha = .1f))
                            .constrainAs(enterButton) {
                                start.linkTo(parent.start)
                                end.linkTo(startGuideline)
                                bottom.linkTo(parent.bottom, margin = 0.dp)
                            },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "See Detail",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .background(color = Color.Transparent)
                    .constrainAs(infoBlock) {
                        start.linkTo(startGuideline)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
            ) {
                ConstraintLayout(modifier = Modifier
                    .height(146.dp)
                    .fillMaxWidth()
                ) {
                    val (titleText, subTitleText, priceBlock) = createRefs()
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.constrainAs(titleText) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top, margin = 2.dp)
                            end.linkTo(parent.end, margin = 10.dp)
                            width = Dimension.fillToConstraints
                        }
                    )
                    Text(
                        text = subTitle,
                        style = MaterialTheme.typography.subtitle2.copy(
                            color = MaterialTheme.colors.onSurface.copy(.6f),
                            fontSize = 14.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.constrainAs(subTitleText) {
                            start.linkTo(parent.start)
                            top.linkTo(titleText.bottom, margin = 6.dp)
                            end.linkTo(parent.end, margin = 10.dp)
                            width = Dimension.fillToConstraints
                        }
                    )
                    ConstraintLayout(
                        modifier = Modifier
                            .wrapContentHeight()
                            .constrainAs(priceBlock) {
                                top.linkTo(subTitleText.bottom, margin = 4.dp)
                                bottom.linkTo(parent.bottom)
                                end.linkTo(parent.end)
                            }
                    ) {
                        val (priceString, priceUnitString) = createRefs()
                        Text(
                            text = "$price/",
                            style = MaterialTheme.typography.h4.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.constrainAs(priceString) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            }
                        )
                        Text(
                            text = priceUnit,
                            style = MaterialTheme.typography.subtitle2.copy(
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.constrainAs(priceUnitString) {
                                start.linkTo(priceString.end)
                                end.linkTo(parent.end, margin = 16.dp)
                                bottom.linkTo(parent.bottom)
                            }
                        )
                    }
                }
                if (contentExpandSignal.value) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.body2.copy(
                            color = MaterialTheme.colors.onSurface.copy(.5f),
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PeopleNumberBlock(
    count : Int,
) {
    val column = count/3
    if (column == 0) {
        Row {
            (0 until  count % 3).forEach { _ ->
                Icon(
                    painter = painterResource(id = R.drawable.ic_single_people),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    } else {
        Column {
            (0 until column).forEachIndexed { index, _ ->
                if (index != column) {
                    Row {
                        (0 until  3).forEach { _ ->
                            Icon(
                                painter = painterResource(id = R.drawable.ic_single_people),
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                } else {
                    Row {
                        (0 until  count % 3).forEach { _ ->
                            Icon(
                                painter = painterResource(id = R.drawable.ic_single_people),
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 模拟开通Premium会员事件窗口
 *  @param  visible 可见性
 *  @param  dismissEvent    窗口关闭事件
 */
@ExperimentalComposeUiApi
@Composable
fun PayForPremiumWindows(
    visible: Boolean,
    dismissEvent: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300))
    ) {
        Dialog(
            onDismissRequest = { /*TODO*/ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                usePlatformDefaultWidth = false
            ),
        ) {
            Card(
                modifier = Modifier.size(220.dp),
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Premium 开通中...",
                        style = MaterialTheme.typography.subtitle2.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = .8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
