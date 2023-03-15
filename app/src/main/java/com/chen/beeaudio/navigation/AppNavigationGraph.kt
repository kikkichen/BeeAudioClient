package com.chen.beeaudio.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.paging.ExperimentalPagingApi
import com.chen.beeaudio.MainScreen
import com.google.accompanist.pager.ExperimentalPagerApi
import me.onebone.toolbar.ExperimentalToolbarApi

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalPagingApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalToolbarApi
@ExperimentalFoundationApi
@Composable
fun AppNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Graph.Home,
        route = Graph.AppRoot
    ) {
        composable(route = Graph.Home) {
            MainScreen()
        }
    }
}

object Graph {
    const val AppRoot = "app_route_graph"
    const val Auth = "auth_graph"
    const val Blog = "blog_graph"
    const val Person = "person_graph"
    const val Home = "home_graph"
    const val AudioHome = "audio_home_graph"
}