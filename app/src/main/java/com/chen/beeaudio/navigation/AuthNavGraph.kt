package com.chen.beeaudio.navigation

import androidx.navigation.*
import androidx.navigation.compose.composable
import com.chen.beeaudio.screen.ForgetPasswordScreen
import com.chen.beeaudio.screen.LoginScreen
import com.chen.beeaudio.screen.RegisterScreen
import com.chen.beeaudio.viewmodel.MainViewModel
import com.google.accompanist.pager.ExperimentalPagerApi

@ExperimentalPagerApi
fun NavGraphBuilder.authNavGraph(navController: NavHostController, mainViewModel: MainViewModel) {
    navigation(
        route = Graph.Auth,
        startDestination = AuthRoute.Login.route
    ) {
        composable(route = AuthRoute.Login.route) {
            LoginScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(route = AuthRoute.SignUp.route) {
            RegisterScreen(navController = navController)
        }
    }
}

sealed class AuthRoute(
    val name: String,
    val route: String,
) {
    object Login : AuthRoute(name = "auth_login_screen", route = "AUTH_LOGIN")
    object SignUp : AuthRoute(name = "auth_sign_up_screen", route = "AUTH_SIGN_UP")
}