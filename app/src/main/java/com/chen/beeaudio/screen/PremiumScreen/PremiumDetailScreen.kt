package com.chen.beeaudio.screen.PremiumScreen

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.chen.beeaudio.R
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.mock.PremiumMock
import com.chen.beeaudio.model.audio.Premium
import com.chen.beeaudio.model.blog.RequestUser
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.ui.theme.Green400
import com.chen.beeaudio.ui.theme.Red400
import com.chen.beeaudio.ui.theme.Red500
import com.chen.beeaudio.utils.TimeUtils
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.PremiumDetailVM
import com.simonsickle.compose.barcodes.Barcode
import com.simonsickle.compose.barcodes.BarcodeType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@Composable
fun PremiumDetailScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    premiumDetailVM: PremiumDetailVM = hiltViewModel()
) {
    /* 上下文 */
    val context : Context = LocalContext.current

    val currentUserInfo = mainViewModel.currentUserDetailInfo.collectAsState()

    /* QR 显示标识 */
    val QRshowSignal = remember { mutableStateOf(false) }

    /* Premium 当前套餐信息 */
    val premiumInfoState = premiumDetailVM.premiumInfoState.collectAsState()

    /* 家庭组 Premium 套餐成员信息 */
    val familyPremiumNumbers = premiumDetailVM.premiumFamilyInfoState.collectAsState()

    /* Premium 套餐历史记录状态 */
    val premiumHistoryState = premiumDetailVM.historyPremiumRecord.collectAsState()

    /* 移除用户警告窗口 显示标识 */
    val alertRemoveNumberState = remember { mutableStateOf(false) }
    val alertRemoveNumberInfo = remember { mutableStateOf(RequestUser(0, "", "","")) }
    AlertRemoveFamilyPremiumNumber(
        visible = alertRemoveNumberState.value && alertRemoveNumberInfo.value.Uid != 0.toLong(),
        userInfo = alertRemoveNumberInfo.value,
        onRemoveEvent = {
            premiumDetailVM.apply {
                removeNumbers(
                    context = context,
                    currentUserId = mainViewModel.currentUserId,
                    targetUserId = alertRemoveNumberInfo.value.Uid
                )
                loadPremiumInfo(currentUserId = mainViewModel.currentUserId)
                loadFamilyPremiumNumbersInfo(currentUserId = mainViewModel.currentUserId)
            }
        },
        onDismissEvent = { alertRemoveNumberState.value = false }
    )

    /* 信息初始化加载 */
    premiumDetailVM.loadPremiumInfo(currentUserId = mainViewModel.currentUserId)
    /* 更新 MainViewModel 中的 Premium 信息数据 */
    LaunchedEffect(
        key1 = Unit,
        block = {
            mainViewModel.loadIsPremiumTag(userId = mainViewModel.currentUserId)
        }
    )

    premiumDetailVM.loadHistoryPremiumRecord(currentUserId = mainViewModel.currentUserId)
    if (premiumInfoState.value.card_type == 1) {
        /* 若当前Premium套餐为家庭组Premium版本， 则请求加载Premium套餐内成员信息 */
        premiumDetailVM.loadFamilyPremiumNumbersInfo(currentUserId = mainViewModel.currentUserId)
    }

    PremiumQRAlert(
        visible = QRshowSignal.value,
        premiumCardID = premiumInfoState.value.card_id,
        onDismissRequest = { QRshowSignal.value = false }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back up")
                    }
                },
                title = { Text(text = "我的 Premium") },
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                backgroundColor = MaterialTheme.colors.surface
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(paddingValues = it)
        ) {
            item {
                AnimatedVisibility(
                    visible = premiumInfoState.value.card_id.isNotEmpty(),
                    enter = fadeIn(tween(300)) + slideInVertically { fullHeight -> - fullHeight } ,
                    exit = fadeOut(tween(300)) + slideOutVertically { fullHeight -> - fullHeight }
                ) {
                    PremiumCard(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        premiumInfo = premiumInfoState.value,
                        userInfo = currentUserInfo.value,
                        showQRBlock = { QRshowSignal.value = true },
                    )
                }
            }
            item {
                AnimatedVisibility(
                    visible = familyPremiumNumbers.value.formalNumbers.isNotEmpty(),
                    enter = fadeIn(tween(300)) + slideInVertically { fullHeight -> 2 * fullHeight } ,
                    exit = fadeOut(tween(300)) + slideOutVertically { fullHeight -> 2 * fullHeight }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_premium_family_group),
                                contentDescription = "我的Premium家庭组成员",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colors.onSurface.copy(alpha = .8f)
                            )
                            Text(
                                text = "我的Premium家庭组成员",
                                style = MaterialTheme.typography.subtitle1.copy(
                                    color = MaterialTheme.colors.onSurface.copy(alpha = .8f)
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(6.dp))
                            familyPremiumNumbers.value.formalNumbers.forEachIndexed { index, user ->
                                SingleGroupNumberCard(
                                    index = index,
                                    userInfo = user,
                                    isAdminAction = mainViewModel.currentUserId == familyPremiumNumbers.value.formalNumbers[0].Uid,
                                    currentActionUserId = mainViewModel.currentUserId,
                                ) {
                                    alertRemoveNumberInfo.value = user
                                    alertRemoveNumberState.value = true
                                }
                            }
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visible = familyPremiumNumbers.value.applyNumbers.isNotEmpty()
                            && familyPremiumNumbers.value.formalNumbers[0].Uid == mainViewModel.currentUserId,
                    enter = fadeIn(tween(300)) + slideInVertically { fullHeight -> 2 * fullHeight } ,
                    exit = fadeOut(tween(300)) + slideOutVertically { fullHeight -> 2 * fullHeight }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_premium_family_apply),
                                contentDescription = "请求加入Premium家庭组的用户",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colors.onSurface.copy(alpha = .8f)
                            )
                            Text(
                                text = "请求加入Premium家庭组的用户",
                                style = MaterialTheme.typography.subtitle1.copy(
                                    color = MaterialTheme.colors.onSurface.copy(alpha = .8f)
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        AnimatedVisibility(
                            visible = familyPremiumNumbers.value.applyNumbers.isNotEmpty()
                                    && familyPremiumNumbers.value.formalNumbers[0].Uid == mainViewModel.currentUserId,
                            enter = fadeIn(tween(300)) + slideInVertically { fullHeight -> 2 * fullHeight } ,
                            exit = fadeOut(tween(300)) + slideOutVertically { fullHeight -> 2 * fullHeight }
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(12.dp))
                                familyPremiumNumbers.value.applyNumbers.forEach { user ->
                                    SingleApplyNumberCard(
                                        userInfo = user,
                                        agreeEvent = {
                                            premiumDetailVM.agreeApplyNumbers(
                                                context = context,
                                                currentUserId = mainViewModel.currentUserId,
                                                targetUserId = user.Uid
                                            )
                                            premiumDetailVM.loadFamilyPremiumNumbersInfo(mainViewModel.currentUserId)
                                        },
                                        disagreeEvent = {
                                            premiumDetailVM.removeNumbers(
                                                context = context,
                                                currentUserId = mainViewModel.currentUserId,
                                                targetUserId = user.Uid
                                            )
                                            premiumDetailVM.loadFamilyPremiumNumbersInfo(mainViewModel.currentUserId)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visible = premiumHistoryState.value.isNotEmpty(),
                    enter = fadeIn(tween(300)) + slideInVertically { fullHeight -> 2 * fullHeight } ,
                    exit = fadeOut(tween(300)) + slideOutVertically { fullHeight -> 2 * fullHeight }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_premium_pay_record),
                                contentDescription = "我的Premium套餐使用记录",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colors.onSurface.copy(alpha = .8f)
                            )
                            Text(
                                text = "我的Premium套餐使用记录",
                                style = MaterialTheme.typography.subtitle1.copy(
                                    color = MaterialTheme.colors.onSurface.copy(alpha = .8f)
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        premiumHistoryState.value.forEach { premiumInfo ->
                            PremiumHistoryItem(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                singlePremium = premiumInfo
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 家庭组 显示成员
 *
 */
@Composable
fun SingleGroupNumberCard(
    index: Int,
    userInfo: RequestUser,
    isAdminAction: Boolean,
    currentActionUserId: Long,
    removeEvent: () -> Unit,
) {
    /* 执行菜单弹出标识 */
    val menuExpandSignal = remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .height(154.dp)
            .width(124.dp)
            .padding(horizontal = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    if (isAdminAction && currentActionUserId != userInfo.Uid) {
                        menuExpandSignal.value = true
                    }
                })
            },
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .padding(vertical = 6.dp, horizontal = 1.dp)
                    .size(64.dp),
                shape = CircleShape
            ) {
                AsyncImage(
                    model = if (userInfo.AvatarUrl.contains("avatar")) LOCAL_SERVER_URL + userInfo.AvatarUrl else userInfo.AvatarUrl,
                    contentDescription = userInfo.Name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = userInfo.Name,
                style = MaterialTheme.typography.subtitle1.copy(
                    color = MaterialTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            DropdownMenu(
                expanded = menuExpandSignal.value, 
                onDismissRequest = { menuExpandSignal.value = false }
            ) {
                DropdownMenuItem(onClick = {
                    menuExpandSignal.value = false
                    removeEvent()
                }) {
                    Text(text = "从当前家庭组移除")
                }
            }
            Text(
                text = if (index == 0) "管理员" else "组员",
                style = MaterialTheme.typography.subtitle2.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = .5f),
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp
                )
            )
        }
    }
}

/** 申请加入Premium就爱听组 显示成员
 *  @param  userInfo    此条申请用户ID
 *  @param  agreeEvent  同意该申请事件
 *  @param  disagreeEvent   不同意该申请事件
 */
@Composable
fun SingleApplyNumberCard(
    userInfo: RequestUser,
    agreeEvent: () -> Unit,
    disagreeEvent: () -> Unit,
) {
    Card(
        modifier = Modifier
            .height(154.dp)
            .width(124.dp)
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .padding(vertical = 6.dp, horizontal = 1.dp)
                    .size(64.dp),
                shape = CircleShape
            ) {
                AsyncImage(
                    model = if (userInfo.AvatarUrl.contains("avatar")) LOCAL_SERVER_URL + userInfo.AvatarUrl else userInfo.AvatarUrl,
                    contentDescription = userInfo.Name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = userInfo.Name,
                style = MaterialTheme.typography.subtitle1.copy(
                    color = MaterialTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { agreeEvent() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_premium_agree),
                        contentDescription = "agree this apply",
                        tint = Green400,
                        modifier = Modifier.size(24.dp)
                    )
                }
                TextButton(
                    onClick = { disagreeEvent() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_premium_disagree),
                        contentDescription = "disagree this apply",
                        tint = Red400,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/** Premium 卡片
 *  @param  modifier    修饰符参数
 *  @param  premiumInfo Premium套餐信息
 *  @param  userInfo    用户信息
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    premiumInfo: Premium,
    userInfo: RequestUserDetail,
    showQRBlock: () -> Unit,
) {
    Card(
        modifier = modifier
            .height(240.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.onSurface.copy(alpha = 0.08f))
        ) {
            val (bar, backgroundIcon) = createRefs()
            val baseline = createGuidelineFromTop(.4f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .constrainAs(bar) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(baseline)
                    }
                    .background(color = MaterialTheme.colors.onSurface.copy(alpha = 0.08f))
            )
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .size(160.dp)
                    .constrainAs(backgroundIcon) {
                        end.linkTo(parent.end, margin = 4.dp)
                        bottom.linkTo(parent.bottom)
                    },
                color = Color.Transparent
            ) {
                val compositeResult : LottieCompositionResult = rememberLottieComposition(
                    spec = LottieCompositionSpec.Asset("lottie/backgroun_premium_card.json")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .padding(top = 12.dp)
            ) {
                (0 until 3).forEach { _ ->
                    Row {
                        premiumInfo.card_id.map {
                            Box(
                                modifier = Modifier
                                    .width(((it.code - 48) / 3).dp)
                                    .fillMaxHeight()
                                    .background(color = MaterialTheme.colors.onSurface)
                            )
                            Box(modifier = Modifier.width(2.dp))
                        }
                    }
                }
            }
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = premiumInfo.card_id
                    .mapIndexed{ index, c -> if ((index>=6) and (index<=14)) '*' else c }
                    .toString()
                    .replace(",","").replace("[","").replace("]","").replace(" ","")
                    .trim(),
                letterSpacing = 4.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .padding(vertical = 4.dp)
                        .padding(start = 16.dp, end = 8.dp)
                        .size(62.dp),
                    border = BorderStroke(width = 3.dp, color = MaterialTheme.colors.primary),
                    shape = CircleShape
                ) {
                    AsyncImage(
                        model = if (userInfo.avatar_url.contains("avatar")) LOCAL_SERVER_URL + userInfo.avatar_url else userInfo.avatar_url,
                        contentDescription = userInfo.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier.wrapContentHeight(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "UID: ${userInfo.uid}",
                        style = MaterialTheme.typography.subtitle2.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = .5f),
                            fontWeight = FontWeight.Light,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userInfo.name,
                        style = MaterialTheme.typography.subtitle1.copy(
                            color = MaterialTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                }
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = (-20).dp, y = (-9).dp),
                    color = Color.Transparent,
                ) {
                    val compositeResult : LottieCompositionResult = rememberLottieComposition(
                        spec = LottieCompositionSpec.Asset("lottie/icon-premium-vk.json")
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
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = .5f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (premiumInfo.card_type == 0) "个人Premium套餐" else if (premiumInfo.card_type == 1) "家庭组Premium套餐" else "Premium体验版",
                    style = MaterialTheme.typography.body2.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = .5f),
                        fontSize = 14.sp
                    )
                )
            }
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
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
                    text = "于 ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(TimeUtils.strToDateTime(premiumInfo.server_expired))} 到期",
                    style = MaterialTheme.typography.body2.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = .5f),
                        fontSize = 14.sp
                    )
                )
            }
            IconButton(
                onClick = { showQRBlock() },
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_show_premium_qr),
                    contentDescription = "show my premium group"
                )
            }
        }
    }
}

/** Premium 套餐历史记录信息， 单条
 *  @param  modifier    修饰符参数
 *  @param  singlePremium   单条Premium记录信息
 */
@Composable
fun PremiumHistoryItem(
    modifier: Modifier = Modifier,
    singlePremium : Premium
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Divider(modifier = Modifier.padding(horizontal = 2.dp, vertical = 3.dp))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (singlePremium.card_type == 0) "Premium 个人版套餐/12个月" else "Premium 家庭组套餐/12个月",
                style = MaterialTheme.typography.body1.copy(
                    fontSize = 18.sp
                )
            )
            Text(
                text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(TimeUtils.strToDateTime(singlePremium.server_in)).toString(),
                style = MaterialTheme.typography.body1.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                    fontSize = 16.sp
                )
            )
        }
    }
}

/** Premium 二维码 弹窗
 *
 */
@Composable
fun PremiumQRAlert(
    visible: Boolean,
    premiumCardID: String,
    onDismissRequest: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(400))
    ) {
        Dialog(
            onDismissRequest = { onDismissRequest() }
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colors.surface)
            ) {
                Barcode(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(220.dp)
                        .padding(horizontal = 28.dp),
                    resolutionFactor = 10, // Optionally, increase the resolution of the generated image
                    type = BarcodeType.QR_CODE, // pick the type of barcode you want to render
                    value = premiumCardID // The textual representation of this code
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = premiumCardID,
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = .8f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** 移除Premium家庭组内成员警告窗口
 *
 */
@Composable
fun AlertRemoveFamilyPremiumNumber(
    visible: Boolean,
    userInfo: RequestUser,
    onRemoveEvent: () -> Unit,
    onDismissEvent: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300))
    ) {
        AlertDialog(
            title = { Text(text = "移除家庭组成员") },
            text = { Text(text = "你确定要从当前 Premium家庭组 中移除成员 “${userInfo.Name}” 吗？ ") },
            confirmButton = {
                TextButton(onClick = {
                    onRemoveEvent()
                    onDismissEvent()
                }) {
                    Text(text = "确定移除", color = Red500)
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissEvent() }) {
                    Text(text = "取消")
                }
            },
            onDismissRequest = { onDismissEvent() }
        )
    }
}