package com.chen.beeaudio.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.chen.beeaudio.init.LOCAL_SERVER_LARGE_PREFIX
import com.chen.beeaudio.init.WEIBO_LARGE_IMAGE_URL_PREFIX
import com.google.accompanist.pager.*
import kotlin.math.absoluteValue

/**
 * 大图预览
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImageBrowserScreen(
    images: List<String>,
    postUserId : Long,
    selectImage: String,
    navController: NavController
) {
    var currentIndex = 0

    images.forEachIndexed { index, image ->
        if (image == selectImage) {
            currentIndex = index
            return@forEachIndexed
        }
    }
    /** 界面状态变更 */
    val pageState = rememberPagerState(initialPage = currentIndex)

    Box {
        HorizontalPager(
            count = images.size,
            state = pageState,
            contentPadding = PaddingValues(horizontal = 0.dp),
            modifier = Modifier.fillMaxSize()
        ) { page ->
//            println("ImageBrowserItem current page: ${images[page]}")
            ImageBrowserItem(images[page], postUserId, page, this) { navController.navigateUp() }
        }

        HorizontalPagerIndicator(
            pagerState = pageState,
            activeColor = Color.White,
            inactiveColor = Color.Black.copy(.3f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(60.dp)
        )
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = {
                    navController.navigateUp()
                }) {
                    Icon(
                        painter = painterResource(id = com.chen.beeaudio.R.drawable.ic_image_back_arrow),
                        contentDescription = "Back Up",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                }
            },
            title = {
                Text(text = "${pageState.currentPage + 1}/${images.size}", color = Color.White)
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(color = Color.Transparent)
                .statusBarsPadding(),
            backgroundColor = Color.Transparent.copy(0.4f),
            elevation = 0.dp
        )

        LaunchedEffect(pageState) {
            snapshotFlow { pageState }.collect { pageState ->
//                println("ImageBrowserItem LaunchedEffect pageState currentPageOffset: $pageState.currentPageOffset")
            }
        }
    }


}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImageBrowserItem(image: String, postUserId: Long, page: Int = 0, pagerScope: PagerScope, onBackUp: () -> Unit) {
    /**
     * 缩放比例
     */
    var scale by remember { mutableStateOf(1f) }

    /**
     * 偏移量
     */
    var offset  by remember { mutableStateOf(Offset.Zero) }

    /**
     * 监听手势状态变换
     */
    var state =
        rememberTransformableState(onTransformation = { zoomChange, offsetChange, _ ->
            scale = (zoomChange * scale).coerceAtLeast(1f)
            scale = if (scale > 5f) {
                5f
            } else {
                scale
            }
            offset += offsetChange
        })

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Color.Black,
    ) {
        AsyncImage(
            model = if (image.contains("LOCALSERVER_")) "$LOCAL_SERVER_LARGE_PREFIX/$postUserId/$image" else "$WEIBO_LARGE_IMAGE_URL_PREFIX/$image",
            contentDescription = "",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .transformable(state = state)
                .graphicsLayer {  //布局缩放、旋转、移动变换
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y

                    val pageOffset =
                        pagerScope.calculateCurrentOffsetForPage(page = page).absoluteValue
                    if (pageOffset == 1.0f) {
                        scale = 1.0f
                    }
//                    println("ImageBrowserItem pagerScope calculateCurrentOffsetForPage pageOffset: $pageOffset")
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
//                            println("ImageBrowserItem detectTapGestures onDoubleTap offset: $it")
                            scale = if (scale <= 1f) {
                                2f
                            } else {
                                1f
                            }
                            offset = Offset.Zero
                        },
                        onTap = {
                            onBackUp()
                        }
                    )
                }
        )
    }
}