package com.chen.beeaudio.screen.SendScreen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chen.beeaudio.utils.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlin.random.Random

@ExperimentalPagerApi
@Composable
fun EmojiKeyBoard(
    addEmoji: (String) -> Unit
) {
    val horizontalPagerState = rememberPagerState()

    Column(
        modifier = Modifier
            .wrapContentSize()
            .background(color = MaterialTheme.colors.surface)
    ) {
        Spacer(modifier = Modifier.height(4.dp).fillMaxWidth().background(color = MaterialTheme.colors.surface))
        /* 内容切换 淡入淡出效果的主题 */
        Crossfade(
            targetState = currentPagerTitle(horizontalPagerState.currentPage),
        ) {
            Text(
                text = it,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 2.dp),
            )
        }
        HorizontalPager(
            state = horizontalPagerState,
            count = 9
        ) { page: Int ->
            when (page) {
                0 -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(194.dp)
                            .background(color = MaterialTheme.colors.surface),
                        columns = GridCells.Fixed(7),
                        contentPadding = PaddingValues(8.dp),
                        userScrollEnabled = true
                    ) {
                        items(FaceEmoji.values()) { item ->
                            Card(
                                modifier = Modifier.padding(4.dp).clickable { addEmoji(item.emojiContext) },
                                backgroundColor = Color(
                                    red = Random.nextInt(0, 255),
                                    green = Random.nextInt(0, 255),
                                    blue = Random.nextInt(0, 255)
                                )
                            ) {
                                Text(
                                    text = item.emojiContext,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
                1 -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(194.dp)
                            .background(color = MaterialTheme.colors.surface)
                        ,
                        columns = GridCells.Fixed(7),
                        contentPadding = PaddingValues(8.dp),
                        userScrollEnabled = true
                    ) {
                        items(BodyEmoji.values()) { item ->
                            Card(
                                modifier = Modifier.padding(4.dp).clickable { addEmoji(item.emojiContext) },
                                backgroundColor = Color(
                                    red = Random.nextInt(0, 255),
                                    green = Random.nextInt(0, 255),
                                    blue = Random.nextInt(0, 255)
                                )
                            ) {
                                Text(
                                    text = item.emojiContext,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
                2 -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(194.dp)
                            .background(color = MaterialTheme.colors.surface)
                        ,
                        columns = GridCells.Fixed(7),
                        contentPadding = PaddingValues(8.dp),
                        userScrollEnabled = true
                    ) {
                        items(RoleEmoji.values()) { item ->
                            Card(
                                modifier = Modifier.padding(4.dp).clickable { addEmoji(item.emojiContext) },
                                backgroundColor = Color(
                                    red = Random.nextInt(0, 255),
                                    green = Random.nextInt(0, 255),
                                    blue = Random.nextInt(0, 255)
                                )
                            ) {
                                Text(
                                    text = item.emojiContext,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
                3 -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(194.dp)
                            .background(color = MaterialTheme.colors.surface)
                        ,
                        columns = GridCells.Fixed(7),
                        contentPadding = PaddingValues(8.dp),
                        userScrollEnabled = true
                    ) {
                        items(AnimalEmoji.values()) { item ->
                            Card(
                                modifier = Modifier.padding(4.dp).clickable { addEmoji(item.emojiContext) },
                                backgroundColor = Color(
                                    red = Random.nextInt(0, 255),
                                    green = Random.nextInt(0, 255),
                                    blue = Random.nextInt(0, 255)
                                )
                            ) {
                                Text(
                                    text = item.emojiContext,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
                4 -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(194.dp)
                            .background(color = MaterialTheme.colors.surface)
                        ,
                        columns = GridCells.Fixed(7),
                        contentPadding = PaddingValues(8.dp),
                        userScrollEnabled = true
                    ) {
                        items(FoodEmoji.values()) { item ->
                            Card(
                                modifier = Modifier.padding(4.dp).clickable { addEmoji(item.emojiContext) },
                                backgroundColor = Color(
                                    red = Random.nextInt(0, 255),
                                    green = Random.nextInt(0, 255),
                                    blue = Random.nextInt(0, 255)
                                )
                            ) {
                                Text(
                                    text = item.emojiContext,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
                5 -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(194.dp)
                            .background(color = MaterialTheme.colors.surface)
                        ,
                        columns = GridCells.Fixed(7),
                        contentPadding = PaddingValues(8.dp),
                        userScrollEnabled = true
                    ) {
                        items(ActivityEmoji.values()) { item ->
                            Card(
                                modifier = Modifier.padding(4.dp).clickable { addEmoji(item.emojiContext) },
                                backgroundColor = Color(
                                    red = Random.nextInt(0, 255),
                                    green = Random.nextInt(0, 255),
                                    blue = Random.nextInt(0, 255)
                                )
                            ) {
                                Text(
                                    text = item.emojiContext,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
                6 -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(194.dp)
                            .background(color = MaterialTheme.colors.surface)
                        ,
                        columns = GridCells.Fixed(7),
                        contentPadding = PaddingValues(8.dp),
                        userScrollEnabled = true
                    ) {
                        items(PlaceEmoji.values()) { item ->
                            Card(
                                modifier = Modifier.padding(4.dp).clickable { addEmoji(item.emojiContext) },
                                backgroundColor = Color(
                                    red = Random.nextInt(0, 255),
                                    green = Random.nextInt(0, 255),
                                    blue = Random.nextInt(0, 255)
                                )
                            ) {
                                Text(
                                    text = item.emojiContext,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
                7 -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(194.dp)
                            .background(color = MaterialTheme.colors.surface)
                        ,
                        columns = GridCells.Fixed(7),
                        contentPadding = PaddingValues(8.dp),
                        userScrollEnabled = true
                    ) {
                        items(ObjectEmoji.values()) { item ->
                            Card(
                                modifier = Modifier.padding(4.dp).clickable { addEmoji(item.emojiContext) },
                                backgroundColor = Color(
                                    red = Random.nextInt(0, 255),
                                    green = Random.nextInt(0, 255),
                                    blue = Random.nextInt(0, 255)
                                )
                            ) {
                                Text(
                                    text = item.emojiContext,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
                8 -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(194.dp)
                            .background(color = MaterialTheme.colors.surface)
                        ,
                        columns = GridCells.Fixed(7),
                        contentPadding = PaddingValues(8.dp),
                        userScrollEnabled = true
                    ) {
                        items(SymbolsEmoji.values()) { item ->
                            Card(
                                modifier = Modifier.padding(4.dp).clickable { addEmoji(item.emojiContext) },
                                backgroundColor = Color(
                                    red = Random.nextInt(0, 255),
                                    green = Random.nextInt(0, 255),
                                    blue = Random.nextInt(0, 255)
                                )
                            ) {
                                Text(
                                    text = item.emojiContext,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        HorizontalPagerIndicator(
            pagerState = horizontalPagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(color = MaterialTheme.colors.surface)
                .padding(top = 6.dp, bottom = 3.dp),
        )
    }
}

/**
 *  获取当前表情页的标题名称
 *  @param indexOfPage  页码
 */
fun currentPagerTitle(
    indexOfPage : Int
): String {
    return when(indexOfPage) {
        0 -> "面部表情"
        1 -> "肢体动表情"
        2 -> "虚构人物"
        3 -> "动物 & 植物"
        4 -> "食物 & 饮品"
        5 -> "日常活动"
        6 -> "旅行 & 地理"
        7 -> "物品表情"
        8 -> "符号标志"
        else -> "未知表情组"
    }
}