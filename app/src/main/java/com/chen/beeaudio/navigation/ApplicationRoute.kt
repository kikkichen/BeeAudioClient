package com.chen.beeaudio.navigation

sealed class ApplicationRoute(
    val route: String,
    val name: String,
) {
    object AudioHome: ApplicationRoute(
        route = "home",
        name = "Home",
    )

    object Settings: ApplicationRoute(
        route = "settings",
        name = "Settings",
    )

    object Profile: ApplicationRoute(
        route = "profile",
        name = "Profile",
    )
}