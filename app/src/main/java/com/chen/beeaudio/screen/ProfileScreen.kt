package com.chen.beeaudio.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import me.onebone.toolbar.ExperimentalToolbarApi

@ExperimentalFoundationApi
@ExperimentalToolbarApi
@Composable
fun ProfileScreen(
    navController: NavController
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Blue),
        contentAlignment = Alignment.Center
    ) {

    }
}

@ExperimentalFoundationApi
@ExperimentalToolbarApi
@Composable
@Preview
fun ProfileScreenPreview() {
    BeeAudioTheme {
        val navController = NavHostController(LocalContext.current)
        ProfileScreen(navController)
    }
}