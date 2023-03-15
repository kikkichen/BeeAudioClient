package com.chen.beeaudio.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chen.beeaudio.R

sealed class BottomBarRoute(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val painter: Int = 0,
    val iconSize: Dp = 24.dp
) {
    object AudioHome: BottomBarRoute(
        route = "home",
        title = "主页",
        icon = Icons.Default.Settings,
        painter = R.drawable.ic_bottom_audio
    )

    object BlogHome: BottomBarRoute(
        route = "blogs",
        title = "动态",
        icon = Icons.Default.Settings,
        painter = R.drawable.ic_bottom_blog
    )

    object Profile: BottomBarRoute(
        route = "profile",
        title = "我的",
        icon = Icons.Default.Settings,
        painter = R.drawable.ic_bottom_person,
        iconSize = 26.dp
    )
}
