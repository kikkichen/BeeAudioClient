package com.chen.beeaudio

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore.Audio
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.paging.ExperimentalPagingApi
import com.chen.beeaudio.navigation.*
import com.chen.beeaudio.screen.widget.PlayerBarWidget
import com.chen.beeaudio.ui.theme.BlueGrey700
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.TokenAging
import com.google.accompanist.pager.ExperimentalPagerApi
import me.onebone.toolbar.ExperimentalToolbarApi

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalPagingApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalToolbarApi
@ExperimentalFoundationApi
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
//    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    Scaffold(
        bottomBar = {
            BottomBar(navController = navController, mainViewModel = mainViewModel, context = context)
        },
    ) {
        HomeNavGraph(
            navController = navController,
            mainViewModel = mainViewModel
        )
    }
}

@Composable
fun BottomBar(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    context: Context
) {
    /* 登陆状态 */
    val tokenState = mainViewModel.tokenAgingState.collectAsState()

    val bottomDisplayScreens = listOf(
        BottomBarRoute.AudioHome,
        BottomBarRoute.BlogHome,
        BottomBarRoute.Profile,
    )
    val playBarUnDisplayScreen = listOf(
        AudioHomeRoute.SearchScreen,
        AudioHomeRoute.PlayScreen,
    )
    val playBarUnDisplayScreen2 = listOf(
        BottomBarRoute.BlogHome,
        BottomBarRoute.Profile
    )
    val playBarUnDisplayScreen3 = listOf(
        AuthRoute.Login,
        AuthRoute.SignUp,
    )
    val playBarUnDisplayScreen4 = listOf(
        BlogRoute.BlogDetail,
        BlogRoute.UserDetail,
        BlogRoute.ImageBrowser,
        BlogRoute.SendScreen,
        BlogRoute.DraftScreen,
        BlogRoute.CallMyFollowerScreen,
        BlogRoute.HotTopicScreen,
        BlogRoute.ShareMusicScreen,
        BlogRoute.FollowUserScreen,
        BlogRoute.FansUserScreen,
        BlogRoute.BlogSearch,
    )
    val playBarUnDisplayScreen5 = listOf(
        PersonRoute.EditPlayListScreen,
        PersonRoute.GuidePremiumScreen,
        PersonRoute.PremiumPolicyScreen,
        PersonRoute.PremiumDetailScreen,
        PersonRoute.PremiumQRScanScreen,
        PersonRoute.JoinPremiumFamilyScreen,
        PersonRoute.ForgotPasswordScreen
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    /* 判断当前路由是否为搜索页， 若是，则隐藏状态栏 */
    val playBarDestination = playBarUnDisplayScreen.any { it.route == currentDestination?.route } or
            playBarUnDisplayScreen2.any {
                try {
                    currentDestination?.route!!.contains(it.route)
                } catch (e : NullPointerException) {
                    false
                }
            } or
            playBarUnDisplayScreen3.any { it.route == currentDestination?.route } or
            playBarUnDisplayScreen4.any {
                try {
                    currentDestination?.route!!.contains(it.route)
                } catch (e : NullPointerException) {
                    false
                }
            } or
            playBarUnDisplayScreen5.any {
                try {
                    currentDestination?.route!!.contains(it.route)
                } catch (e : NullPointerException) {
                    false
                }
            }

    /* 判断当前路由是否为主页屏幕， 若不是，底部导航栏隐藏 */
    val bottomBarDestination = bottomDisplayScreens.any {
        try {
            currentDestination?.route!!.contains(it.route)
        } catch (e : NullPointerException) {
            false
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                /* 仅显示播放底栏情景下的底栏背景色 */
                if (listOf(BottomBarRoute.AudioHome).any { it.route != currentDestination?.route } or
                    listOf(
                        AudioHomeRoute.SearchResultScreen,
                        AudioHomeRoute.ArtistScreen,
                        AudioHomeRoute.AlbumScreen,
                        AudioHomeRoute.PlayListScreen,
                        AudioHomeRoute.TagPlayListCollectionScreen
                    ).any { it.route == currentDestination?.route }
                ) {
                    if (bottomDisplayScreens.any { it.route == currentDestination?.route }) {
                        if (isSystemInDarkTheme()) {
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to MaterialTheme.colors.surface.copy(alpha = .3f),
                                    0.4f to MaterialTheme.colors.surface.copy(alpha = .7f),
                                    1f to MaterialTheme.colors.surface
                                )
                            )
                        } else {
                            /* 日间模式下， 歌单推荐首页底栏渐变 */
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to Color.White.copy(alpha = .3f),
                                    0.4f to Color.White.copy(alpha = .7f),
                                    1f to Color.White
                                )
                            )
                        }
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colors.surface.copy(.5f),
                                MaterialTheme.colors.surface
                            )
                        )
                    }
                } else if (!isSystemInDarkTheme() && listOf(BottomBarRoute.AudioHome).any { it.route == currentDestination?.route }) {
                    /* 日间模式下， 歌单推荐首页底栏渐变 */
                    Brush.verticalGradient(
                        colorStops =
                        arrayOf(
                            0.0f to Color.Transparent,
                            0.1f to Color.White.copy(alpha = .1f),
                            0.4f to Color.White.copy(alpha = .8f),
                            1f to Color.White
                        )
                    )
                } else if (!isSystemInDarkTheme() && listOf(BottomBarRoute.AudioHome).any { it.route != currentDestination?.route }) {
                    /* 日间模式下， 首页底栏页面下，非歌单推荐页底栏渐变 */
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = .5f),
                            Color.White,
                            Color.White
                        )
                    )
                } else if (isSystemInDarkTheme() && listOf(BottomBarRoute.AudioHome).any { it.route == currentDestination?.route }) {
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.1f to MaterialTheme.colors.surface.copy(alpha = .1f),
                            0.4f to MaterialTheme.colors.surface.copy(alpha = .8f),
                            1f to MaterialTheme.colors.surface
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colors.surface.copy(alpha = .5f),
                            MaterialTheme.colors.surface,
                            MaterialTheme.colors.surface
                        )
                    )
                }
            )
            .animateContentSize()
    ) {
        /* 音乐媒体控制组件 */
        AnimatedVisibility(
            visible = !playBarDestination && (tokenState.value is TokenAging.FINISHED),
            enter = slideInVertically (
                initialOffsetY = { fullHeight -> fullHeight * 3 }
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight * 3 }
            )
        ) {
            PlayerBarWidget(mainViewModel) {
                /* 若播放列表为空， 则不进行播放页的展开 */
                if (mainViewModel.currentPlayingMusicItem.value == null || mainViewModel.currentPlaylist.value.size == 0) {
                    Toast.makeText(context, "你当前没有正在播放的歌曲哦～", Toast.LENGTH_SHORT).show()
                } else {
                    navController.navigate(
                        route = AudioHomeRoute.PlayScreen.route
                    )
                }
            }
        }
        /* 底部导航栏 */
        AnimatedVisibility(
            visible = bottomBarDestination,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BottomNavigation(
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                modifier = Modifier.padding(horizontal = 36.dp)
            ) {
                bottomDisplayScreens.forEach { screen ->
                    AddItem(
                        screen = screen,
                        currentDestination = currentDestination,
                        navController = navController,
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
//        Spacer(modifier = Modifier
//            .height(16.dp)
//            .fillMaxWidth()
//            .background(color = if (!isSystemInDarkTheme()) BlueGrey700 else MaterialTheme.colors.surface)
//        )
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarRoute,
    currentDestination: NavDestination?,
    navController: NavHostController,
    mainViewModel: MainViewModel,
) {
    BottomNavigationItem(
        label = {
            Text(
                text = screen.title,
                color = if (currentDestination?.hierarchy?.any {
                        it.route == screen.route
                    } == true) {
                    MaterialTheme.colors.onSurface.copy(alpha = .85f)
                } else {
                    MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
                }
            )
        },
        icon = {
            if (screen.icon == Icons.Default.Settings) {
                Icon(
                    painter = painterResource(id = screen.painter),
                    contentDescription = "Navigation Icon",
                    modifier = Modifier
                        .size(screen.iconSize)
                        .padding(bottom = 3.dp),
                    tint = if (currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true) {
                        MaterialTheme.colors.onSurface.copy(alpha = .85f)
                    } else {
                        MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
                    }
                )
            } else {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = "Navigation Icon",
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        },
        /* 当前Bottom选择判断 */
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        selectedContentColor = Color.White.copy(alpha = .85f),
        unselectedContentColor = Color.White.copy(alpha = ContentAlpha.disabled),
        onClick = {
            navController.navigate(
                if (screen.route.contains(BottomBarRoute.BlogHome.route)) screen.route + "?uid=${mainViewModel.currentUserId}" else screen.route
            ) {
                // 一级导航层级，第一次back返回HOME,第二次back退出程序
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        },
        modifier = Modifier.background(
            Color.Transparent
        )
    )
}