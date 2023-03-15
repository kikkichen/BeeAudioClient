package com.chen.beeaudio.screen.PremiumScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chen.beeaudio.utils.PolicyContext

@Composable
fun PremiumPolicyScreen(
    navController: NavController
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "backup previous page"
                        )
                    }
                },
                title = {
                    Text(text = "查看政策")
                },
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(paddingValues = it),
            content = {
                item {
                    Text(
                        text = PolicyContext,
                        modifier = Modifier
                            .padding(10.dp)
                    )
                }
            }
        )
    }
}