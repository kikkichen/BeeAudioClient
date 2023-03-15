package com.chen.beeaudio.navigation

import android.app.Person
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.chen.beeaudio.screen.EditPlayListScreen
import com.chen.beeaudio.screen.EditUserDetailScreen
import com.chen.beeaudio.screen.ForgetPasswordScreen
import com.chen.beeaudio.screen.HistoryScreen
import com.chen.beeaudio.screen.PremiumScreen.*
import com.chen.beeaudio.viewmodel.MainViewModel

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
fun NavGraphBuilder.personNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    navigation(
        route = Graph.Person,
        startDestination = PersonRoute.HistoryScreen.route
    ) {
        /* 播放历史页面 */
        composable(
            route = PersonRoute.HistoryScreen.route + "?user_id={user_id}",
            arguments = listOf(
                navArgument("user_id") {
                    type = NavType.LongType
                }
            )
        ) {
            HistoryScreen(navController = navController, mainViewModel = mainViewModel)
        }

        /* 创建歌单 / 编辑歌单页面 */
        composable(
            route = PersonRoute.EditPlayListScreen.route + "?playlistId={playlistId}",
            arguments = listOf(
                navArgument("playlistId") {
                    type = NavType.LongType
                }
            )
        ) {
            EditPlayListScreen(navController = navController, mainViewModel= mainViewModel)
        }
    }

    /* 升级 Premium 套餐 引导页 */
    composable(
        route = PersonRoute.GuidePremiumScreen.route
    ) {
        GuidePremiumScreen(navController = navController, mainViewModel = mainViewModel)
    }

    /* 查看政策 */
    composable(
        route = PersonRoute.PremiumPolicyScreen.route
    ) {
        PremiumPolicyScreen(navController = navController)
    }
    /* 加入 Premium 家庭组 */
    composable(
        route = PersonRoute.JoinPremiumFamilyScreen.route
    ) {
        JoinPremiumFamilyScreen(navController = navController, mainViewModel = mainViewModel)
    }
    /* 我的 Premium 会员信息 */
    composable(
        route = PersonRoute.PremiumDetailScreen.route
    ) {
        PremiumDetailScreen(navController = navController, mainViewModel = mainViewModel)
    }
    /* 扫描 Premium QR */
    composable(
        route = PersonRoute.PremiumQRScanScreen.route
    ) {
        PremiumQRScanScreen(navController = navController, mainViewModel = mainViewModel)
    }
    /* 修改用户基本信息页 */
    composable(
        route = PersonRoute.EditUserDetailScreen.route + "?user_id={user_id}",
        arguments = listOf(
            navArgument("user_id") {
                type = NavType.LongType
            }
        )
    ) {
        EditUserDetailScreen(navController = navController, mainViewModel = mainViewModel)
    }
    composable(
        route = PersonRoute.ForgotPasswordScreen.route + "?account={account}",
        arguments = listOf(
            navArgument("account") {
                type = NavType.LongType
            }
        )
    ) {
        ForgetPasswordScreen(navController = navController)
    }
}

sealed class PersonRoute(
    val name: String,
    val route: String,
) {
    object HistoryScreen : PersonRoute(name = "history_screen", route = "HISTORY_SCREEN")
    object EditPlayListScreen : PersonRoute(name = "edit_playlist_screen", route = "EDIT_PLAYLIST_SCREEN")
    object GuidePremiumScreen : PersonRoute(name = "guide_premium_screen", route = "GUIDE_PREMIUM_SCREEN")
    object PremiumPolicyScreen : PersonRoute(name = "premium_policy_screen", route = "PREMIUM_POLICY_SCREEN")
    object JoinPremiumFamilyScreen : PersonRoute(name = "join_premium_family_screen", route = "JOIN_PREMIUM_FAMILY_SCREEN")
    object PremiumDetailScreen : PersonRoute(name = "premium_detail_screen", route = "PREMIUM_DETAIL_SCREEN")
    object PremiumQRScanScreen : PersonRoute(name = "premium_qr_scan_screen", route = "PREMIUM_QR_SCAN_SCREEN")
    object EditUserDetailScreen : PersonRoute(name = "edit_user_detail_screen", route = "EDIT_USER_DETAIL_SCREEN")
    object ForgotPasswordScreen : PersonRoute(name = "forget_password_screen", route = "FORGET_PASSWORD_SCREEN")
}