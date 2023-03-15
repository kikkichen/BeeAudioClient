package com.chen.beeaudio.screen.SendScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chen.beeaudio.R

/*
*   多图选择橱窗
*
*
*  */
@ExperimentalAnimationApi
@Composable
fun MultiplePicturePreviewBlock(
    isDeleteMode: Boolean,
    pictureGroup: MutableList<String>,
    onEnterDeleteMode: () -> Unit,
    onRemoveFromList: (Int) -> Unit
) {
    val urlSize = pictureGroup.size

    Column(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (urlSize <=0) {
            Spacer(modifier = Modifier.height(1.dp))
        } else if (urlSize == 1) {     // 仅有一张图
            SingleImage(180, pictureGroup[0], isDeleteMode, onEnterDeleteMode) { onRemoveFromList(0) }
        } else if (urlSize <= 4) {      // 四张图内
            Row {
                SingleImage(sideSize = 136, uri = pictureGroup[0], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(0) }
                SingleImage(sideSize = 136, uri = pictureGroup[1], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(1) }
            }
            if (urlSize - 2 > 0 ) {
                Row {
                    SingleImage(sideSize = 136, uri = pictureGroup[2], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(2) }
                    if (urlSize == 4) {
                        SingleImage(sideSize = 136, uri = pictureGroup[3], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(3) }
                    }
                }
            }
        } else {    // 五张图起步
            Row {
                SingleImage(sideSize = 100, uri = pictureGroup[0], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(0) }
                SingleImage(sideSize = 100, uri = pictureGroup[1], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(1) }
                SingleImage(sideSize = 100, uri = pictureGroup[2], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(2) }
            }
            Row {
                SingleImage(sideSize = 100, uri = pictureGroup[3], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(3) }
                SingleImage(sideSize = 100, uri = pictureGroup[4], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(4) }
                if (urlSize > 5) {
                    SingleImage(sideSize = 100, uri = pictureGroup[5], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(5) }
                }
            }
            if (urlSize >= 7) {
                Row {
                    SingleImage(sideSize = 100, uri = pictureGroup[6], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(6) }
                    if (urlSize > 7) {
                        SingleImage(sideSize = 100, uri = pictureGroup[7], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(7) }
                    }
                    if (urlSize >= 9) {
                        SingleImage(sideSize = 100, uri = pictureGroup[8], isDeleteMode = isDeleteMode, onEnterDeleteMode) { onRemoveFromList(8) }
                    }
                }
            }
        }
    }
}

/**
 *  单个图片相框
 *  @param sideSize 单个图片单边变长
 *  @param uri      图片uri地址
 *  @param isDeleteMode 是否处于图片删除模式
 * `@param onRemoveThisPicture  删除该图片回调方法
 *
 * */
@ExperimentalAnimationApi
@Composable
fun SingleImage(
    sideSize: Int ,
    uri : String,
    isDeleteMode: Boolean,
    onEnterDeleteMode: () -> Unit,
    onRemoveThisPicture: () -> Unit
) {
    /* 振动反馈 */
    val haptic = LocalHapticFeedback.current
    /* 抖动动画效果 - 图片 */
    val infiniteTransition = rememberInfiniteTransition()
    val rotationShaking by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 80
                5f at 20
                -5f at 60
            },
            repeatMode = RepeatMode.Reverse
        )
    )
    /* 图片框本体 */
    Box(
        modifier = Modifier
            .padding(2.dp)
    ) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    if (isDeleteMode) {
                        rotationZ = rotationShaking
                    }
                }
                .width(sideSize.dp)
                .height(sideSize.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onEnterDeleteMode()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }        /* 长按触发进入删除图片模式逻辑 */
                    )
                },
            shape = MaterialTheme.shapes.medium
        ) {
            AsyncImage(
                model = uri,
                contentDescription = "This is number xxx picture ",
                contentScale = ContentScale.Crop
            )
        }
        AnimatedVisibility(
            visible = isDeleteMode,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            /* 删除角标 */
            Surface(
                modifier = Modifier
                    .padding(10.dp)
                    .clip(CircleShape)
                    .wrapContentSize()
                    .background(MaterialTheme.colors.onSurface.copy(alpha = .3f))
                    .align(Alignment.TopEnd)
                    .clickable { onRemoveThisPicture() },       /* 从列表中删除当前图片逻辑 */
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete_single_img),
                    contentDescription = "Delete this Picture",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.onSurface.copy(alpha = .3f))
                        .padding(6.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}