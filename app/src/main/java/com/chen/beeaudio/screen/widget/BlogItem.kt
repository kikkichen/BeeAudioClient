package com.chen.beeaudio.screen.widget

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.chen.beeaudio.init.LOCAL_SERVER_THUMBNAIL_PREFIX
import com.chen.beeaudio.init.LOCAL_SERVER_URL
import com.chen.beeaudio.init.WEIBO_MIDDLE_IMAGE_URL_PREFIX
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.model.blog.RequestBlog
import com.chen.beeaudio.model.localmodel.Blog
import com.chen.beeaudio.model.localmodel.RetweetedBlog
import com.chen.beeaudio.navigation.BlogRoute
import com.chen.beeaudio.navigation.NavigationConfig
import com.chen.beeaudio.screen.SendScreen.ShareMusicBlockCardForBlog
import com.chen.beeaudio.screen.isNullOfRequestRetweeted
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.ui.theme.Grey300
import com.chen.beeaudio.ui.theme.Grey500
import com.chen.beeaudio.ui.theme.Indigo50
import com.chen.beeaudio.utils.TimeUtils
import java.util.*

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun BlogItem(
    navController: NavController,
    blogData : Blog,
    onPlayEvent: (Track) -> Unit,
) {
    val descriptionSendTime by remember {
        mutableStateOf("发布自 ${TimeUtils.descriptionBlogTimeByText(Date(blogData.Created).toString())}")
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()             // 宽度拉满
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
            .clickable {
                navController.navigate(
                    route = BlogRoute.BlogDetail.route + "?id=${blogData.Id}&isRetweeted=${
                        !isNullOfRetweeted(
                            blogData
                        )
                    }"
                )
            },       // 点击跳转到博文动态详情
        shape = MaterialTheme.shapes.medium,
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        Column {
            // 用户信息栏
            Row(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
            ) {
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable {
                            navController.navigate(
                                route = BlogRoute.UserDetail.route + "?uid=${blogData.Uid}"
                            )
                        }
                ) {
                    AsyncImage(
                        model = if (blogData.UserAvatar.contains("avatar")) LOCAL_SERVER_URL + blogData.UserAvatar else blogData.UserAvatar,
                        contentDescription = "This is ${blogData.UserName}'s avatar. Look, he's so handsome.",
                        modifier = Modifier.background(color = Grey300)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = blogData.UserName, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = blogData.UserDescription,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 28.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
            // 发布时间 (计算时间差， 生成描述)
            Text(
                modifier = Modifier.padding(start = 14.dp, top = 2.dp, bottom = 2.dp),
                text = descriptionSendTime,
                fontSize = 12.sp,
                color = Grey500
            )
            // 正文栏
            Text(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 4.dp
                ),
                text = blogData.Text
            )

            // 判断博文转发
            if (!isNullOfRetweeted(blogData)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 4.dp, bottom = 4.dp, end = 12.dp),
                    shape = MaterialTheme.shapes.medium,
                    elevation = 0.dp,
                    backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.05f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 6.dp, top = 6.dp, end = 4.dp, bottom = 4.dp)
                            .clickable {
                                val isRetweeted = false
                                navController.navigate(
                                    route = BlogRoute.BlogDetail.route + "?id=${blogData.RetweetedStatus.Id}&isRetweeted=${isRetweeted}"
                                )
                            }
                    ) {
                        Text(text = "${blogData.RetweetedStatus.UserName} : \n${blogData.RetweetedStatus.Text}")
                        Spacer(modifier = Modifier.height(4.dp))
                        // 图片组存在判断
                        if (blogData.RetweetedStatus.UrlGroup.replace("[","")
                                .replace("]","").isNotEmpty()) {
                            PicGroupBox(
                                userId = blogData.RetweetedStatus.Uid,
                                blogData.RetweetedStatus.UrlGroup
                                    .trim().replace(" ", "")
                                    .replace("[","")
                                    .replace("]","")
                                    .split(","),
                                isRetweetedGroup = true
                            ) { url, userId ->
                                navController.navigate(
                                    route = BlogRoute.ImageBrowser.route + "?${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_PIC_GROUP}={${blogData.RetweetedStatus.UrlGroup}}&${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_CURRENT}={$url}&post_user_id=$userId")
                            }
                        }
                        /* 媒体分享 */
                        ShareMusicBlockCardForBlog(
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
                            visible = blogData.RetweetedStatus.MediaUrl.isNotEmpty(),
                            rawJson = blogData.RetweetedStatus.MediaUrl,
                            navController = navController,
                            onPlayEvent = onPlayEvent
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 10.dp)
                        ) {
                            RetweetedComboBar(
                                blogData.RetweetedStatus.ReportCounts,
                                blogData.RetweetedStatus.CommentCounts,
                                blogData.RetweetedStatus.Attitudes
                            )
                        }
                    }
                }
            } else {
                Column {
                    // 空
                }
            }

            // 图片组存在判断
            if (blogData.UrlGroup.replace("[","")
                    .replace("]","").isNotEmpty()) {
                PicGroupBox(
                    userId = blogData.Uid,
                    picGroup = blogData.UrlGroup
                        .trim().replace(" ", "")
                        .replace("[","")
                        .replace("]","")
                        .split(","),
                    isRetweetedGroup = false
                ) { url, userId ->
                    navController.navigate(
                        route = BlogRoute.ImageBrowser.route + "?${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_PIC_GROUP}={${blogData.UrlGroup}}&${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_CURRENT}={$url}&post_user_id=$userId")
                }
            }

            /* 媒体分享 */
            ShareMusicBlockCardForBlog(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                visible = blogData.MediaUrl.isNotEmpty(),
                rawJson = blogData.MediaUrl,
                navController = navController,
                onPlayEvent = onPlayEvent
            )

            // 转发、 评论、 点赞
            ComboBar(blogData.ReportCounts, blogData.CommentCounts, blogData.Attitudes)
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun BlogItemForRequestBlog(
    navController: NavController,
    blogData : RequestBlog,
    onPlayEvent: (Track) -> Unit,
) {
    val descriptionSendTime by remember {
        mutableStateOf("发布自 ${TimeUtils.descriptionBlogTimeByText(Date(
            TimeUtils.convertStrToLongTimeUnit(
                blogData.Created
            )
        ).toString())}")
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()             // 宽度拉满
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
            .clickable {
                navController.navigate(
                    route = BlogRoute.BlogDetail.route + "?id=${blogData.Id}&isRetweeted=${
                        !isNullOfRequestRetweeted(
                            blogData
                        )
                    }"
                )
            },       // 点击跳转到博文动态详情
        shape = MaterialTheme.shapes.medium,
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
            ) {
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = if (blogData.User.AvatarUrl.contains("avatar")) LOCAL_SERVER_URL + blogData.User.AvatarUrl else blogData.User.AvatarUrl,
                        contentDescription = "This is ${blogData.User.Name}'s avatar. Look, he's so handsome.",
                        modifier = Modifier.background(color = Grey300)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = blogData.User.Name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = blogData.User.Description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 28.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
            // 发布时间 (计算时间差， 生成描述)
            Text(
                modifier = Modifier.padding(start = 14.dp, top = 2.dp, bottom = 2.dp),
                text = descriptionSendTime,
                fontSize = 12.sp,
                color = Grey500
            )
            // 正文栏
            Text(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 4.dp
                ),
                text = blogData.Text
            )

            // 判断博文转发
            if (!isNullOfRequestRetweeted(blogData)) {
                val isRetweeted = false
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 4.dp, bottom = 4.dp, end = 12.dp),
                    shape = MaterialTheme.shapes.medium,
                    elevation = 0.dp,
                    backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
                    onClick = { navController.navigate(
                        route = BlogRoute.BlogDetail.route + "?id=${blogData.RetweetedStatus.Id}&isRetweeted=${isRetweeted}"
                    ) }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 6.dp, top = 6.dp, end = 4.dp, bottom = 4.dp)
                            .clickable { }
                    ) {
                        Text(text = "${blogData.RetweetedStatus.User.Name} : \n${blogData.RetweetedStatus.Text}")
                        Spacer(modifier = Modifier.height(4.dp))
                        // 图片组存在判断
                        if (blogData.RetweetedStatus.UrlGroup[0].url.isNotBlank()) {
                            PicGroupBox(
                                userId = blogData.RetweetedStatus.User.Uid,
                                picGroup = blogData.RetweetedStatus.UrlGroup.map { it.url },
                                isRetweetedGroup = true
                            ) { url, userId ->
                                navController.navigate(
                                    route = BlogRoute.ImageBrowser.route + "?${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_PIC_GROUP}={${blogData.RetweetedStatus.UrlGroup.map { it.url }}}&${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_CURRENT}={$url}&post_user_id=$userId")
                            }
                        }
                        /* 媒体分享 */
                        ShareMusicBlockCardForBlog(
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
                            visible = blogData.RetweetedStatus.MediaUrl.isNotEmpty(),
                            rawJson = blogData.RetweetedStatus.MediaUrl,
                            navController = navController,
                            onPlayEvent = onPlayEvent
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 10.dp)
                        ) {
                            RetweetedComboBar(
                                blogData.RetweetedStatus.ReportCounts,
                                blogData.RetweetedStatus.CommentCounts,
                                blogData.RetweetedStatus.Attitudes
                            )
                        }
                    }
                }
            } else {
                Column {
                    // 空
                }
            }

            // 图片组存在判断
            if (blogData.UrlGroup[0].url.isNotBlank()) {
                PicGroupBox(
                    userId = blogData.User.Uid,
                    picGroup = blogData.UrlGroup.map { it.url },
                    isRetweetedGroup = false
                ) { url, userId ->
                    navController.navigate(
                        route = BlogRoute.ImageBrowser.route + "?${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_PIC_GROUP}={${blogData.UrlGroup.map { it.url }}}&${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_CURRENT}={$url}&post_user_id=$userId")
                }
            }

            /* 媒体分享 */
            ShareMusicBlockCardForBlog(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                visible = blogData.MediaUrl.isNotEmpty(),
                rawJson = blogData.MediaUrl,
                navController = navController,
                onPlayEvent = onPlayEvent
            )
            // 转发、 评论、 点赞
            ComboBar(blogData.ReportCounts, blogData.CommentCounts, blogData.Attitudes)
        }
    }
}

// 图片篇列表
@ExperimentalFoundationApi
@Composable
fun PicGroupBox(
    userId : Long,
    picGroup : List<String>,
    isRetweetedGroup : Boolean,
    onBrowserImage: (String, Long) -> Unit
) {
    val urlSize = remember { picGroup.size }

    Column(
        modifier = Modifier.padding(start = if (isRetweetedGroup) 8.dp else 18.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
    ) {
        if (urlSize <= 1) {     // 仅有一张图
            SingleImage(180, userId, picGroup[0], onBrowserImage)
        } else if (urlSize <= 4) {      // 四张图内
            Row {
                SingleImage(sideSize = 136, userId, url = picGroup[0], onBrowserImage)
                SingleImage(sideSize = 136, userId, url = picGroup[1], onBrowserImage)
            }
            if (urlSize - 2 > 0 ) {
                Row {
                    SingleImage(sideSize = 136, userId, url = picGroup[2], onBrowserImage)
                    if (urlSize == 4) {
                        SingleImage(sideSize = 136, userId, url = picGroup[3], onBrowserImage)
                    }
                }
            }
        } else {    // 五张图起步
            Row {
                SingleImage(sideSize = 100, userId, url = picGroup[0], onBrowserImage)
                SingleImage(sideSize = 100, userId, url = picGroup[1], onBrowserImage)
                SingleImage(sideSize = 100, userId, url = picGroup[2], onBrowserImage)
            }
            Row {
                SingleImage(sideSize = 100, userId, url = picGroup[3], onBrowserImage)
                SingleImage(sideSize = 100, userId, url = picGroup[4], onBrowserImage)
                if (urlSize > 5) {
                    SingleImage(sideSize = 100, userId, url = picGroup[5], onBrowserImage)
                }
            }
            if (urlSize > 6) {
                Row {
                    SingleImage(sideSize = 100, userId, url = picGroup[6], onBrowserImage)
                    if (urlSize > 7) {
                        SingleImage(sideSize = 100, userId, url = picGroup[7], onBrowserImage)
                    }
                    if (urlSize >= 9) {
                        SingleImage(sideSize = 100, userId, url = picGroup[8], onBrowserImage)
                    }
                }
            }
        }
    }
}

@Composable
fun SingleImage(sideSize: Int , userId : Long, url : String, onBrowserImage: (String, Long) -> Unit) {
    Box(modifier = Modifier.padding(2.dp)) {
        Surface(
            modifier = Modifier
                .width(sideSize.dp)
                .height(sideSize.dp)
                .clickable {
                    onBrowserImage(url, userId)
                }
            ,
            shape = MaterialTheme.shapes.medium
        ) {
            /* TODO: 建议使用ImageLoader的单例共享方案提升性能 */
            AsyncImage(
                model = if (url.contains("LOCALSERVER_")) "$LOCAL_SERVER_THUMBNAIL_PREFIX/$userId/$url" else "$WEIBO_MIDDLE_IMAGE_URL_PREFIX/$url",
                contentDescription = "This is number xxx picture ",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(
                    id = if (isSystemInDarkTheme())
                        com.chen.beeaudio.R.drawable.ic_image_placeholder_night
                    else
                        com.chen.beeaudio.R.drawable.ic_image_placeholder
                ),
            )
        }
    }
}

// 转发、 点赞、 收藏
@Composable
fun ComboBar(
    reposts : Int,
    comments : Int,
    attitudes : Int,
) {
    val repostCount = remember{ reposts }
    val commentCount = remember { comments }
    val attitudeCount = remember { attitudes }

    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .height(24.dp)
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            // 转发
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxHeight()
                ) {
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically),
                        painter = painterResource(id = com.chen.beeaudio.R.drawable.ic_retween_icon),
                        contentDescription = "retweeted"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(contentAlignment = Alignment.Center) {
                        if (attitudeCount > 0) {
                            Text(text = "$repostCount", textAlign = TextAlign.Center)
                        } else {
                            Text(text = "转发", textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            // 评论
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxHeight()
                ) {
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically),
                        painter = painterResource(id = com.chen.beeaudio.R.drawable.ic_comments_icon),
                        contentDescription = "comment",
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        if (attitudeCount > 0) {
                            Text(text = "$commentCount", textAlign = TextAlign.Center)
                        } else {
                            Text(text = "评论", textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // 点赞
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxHeight()
                ) {
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically),
                        painter = painterResource(id = com.chen.beeaudio.R.drawable.ic_attitudes_before),
                        contentDescription = "like"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(contentAlignment = Alignment.Center) {
                        if (attitudeCount > 0) {
                            Text(text = "$attitudeCount", textAlign = TextAlign.Center)
                        } else {
                            Text(text = "点赞", textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

/**
 *  Item底部三连Button
 *  @param  reposts 转发数量
 *  @param  comments    评论数量
 *  @param  attitudes   点赞数量
 */
@Composable
fun RetweetedComboBar(
    reposts : Int,
    comments : Int,
    attitudes : Int,
) {
    val repostCount = remember { reposts }
    val commentCount = remember { comments }
    val attitudeCount = remember { attitudes }

    Row{
        Row(Modifier.padding(horizontal = 3.dp)) {
            Text(
                text = "转发",
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "$repostCount",
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f)
            )
        }
        Spacer(
            Modifier
                .width(1.dp)
                .background(color = MaterialTheme.colors.onSurface.copy(alpha = .6f)))
        Row(Modifier.padding(horizontal = 3.dp)) {
            Text(
                text = "评论",
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "$commentCount",
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f)
            )
        }
        Spacer(
            Modifier
                .width(1.dp)
                .background(color = MaterialTheme.colors.onSurface.copy(alpha = .6f)))
        Row(Modifier.padding(horizontal = 3.dp)) {
            Text(
                text = "点赞",
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "$attitudeCount",
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f)
            )
        }
    }
}

/**
 *  判断Retweeted推转为空
 */
fun isNullOfRetweeted(obj : Blog) : Boolean {
    val longZero : Long = 0
    return obj.RetweetedStatus.Id == longZero
}


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun BlogItemPreview() {
    BeeAudioTheme {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(
                color = Indigo50
            )
        ) {
            val navController = rememberNavController()
            BlogItem(
                navController = navController,
                blogData = Blog(
                    Id = 4834700462133265,
                    Created = TimeUtils.strToDateTime("Fri Nov 18 20:40:14 +0800 2022").time,
                    Source = "微博 weibo.com",
                    Text = "电锯人 同人作品——《Go Out With A Bang》 全20页\\n（※提前预警，这篇作品各种意义上的震惊）\\n\\n原作者：Raggumba\\n<a  href=\\\"https://weibo.cn/sinaurl?u=https%3A%2F%2Ftwitter.com%2FRaggumba%2Fstatus%2F1551563685860364289\\\" data-hide=\\\"\\\">网页链接\\n昨天在Reddit版上看到了这篇，可以说非常震惊，自翻了一下全篇\\n授权&amp;\uD83D\uDD17见评 ",
                    RetweetedStatus = RetweetedBlog(
                        Id = 6137455005,
                        Created = TimeUtils.strToDateTime("Fri Nov 18 20:40:14 +0800 2022").time,
                        Text = "【城市冰火人】皮克斯动画电影《疯狂元素城》中文先导预告公开，2023年6月6日在北美上映，讲述火女和水男无法触碰的爱情。 http://t.cn/A6KPlYdO \u200B",
                        UrlGroup = listOf(
                            "https://tva1.sinaimg.cn/bmiddle/006rmOQxgy1h8qir3qw54j30ag0aeaa8.jpg",
                            "https://tva1.sinaimg.cn/bmiddle/006rmOQxgy1h8qir3qw54j30ag0aeaa8.jpg",
                            "https://tva1.sinaimg.cn/bmiddle/006rmOQxgy1h8qir3qw54j30ag0aeaa8.jpg",
                            "https://tva1.sinaimg.cn/bmiddle/006rmOQxgy1h8qir3qw54j30ag0aeaa8.jpg",
                            "https://tva1.sinaimg.cn/bmiddle/006rmOQxgy1h8qir3qw54j30ag0aeaa8.jpg",
                            "http://wx1.sinaimg.cn/thumbnail/c3fe0ea7gy1h88nqju8s0j21hc0u07wh.jpg",
                            "http://wx1.sinaimg.cn/thumbnail/c3fe0ea7gy1h88nqju8s0j21hc0u07wh.jpg",
                            "http://wx1.sinaimg.cn/thumbnail/c3fe0ea7gy1h88nqju8s0j21hc0u07wh.jpg",
                            "http://wx1.sinaimg.cn/thumbnail/c3fe0ea7gy1h88nqju8s0j21hc0u07wh.jpg",
                        ).toString(),
                        Uid = 6137455005,
                        UserName = "每日鉴游君",
                        UserAvatar = "https://tvax2.sinaimg.cn/crop.38.0.961.961.50/006Hm8pLly8h6asmuuu6tj30rs0rs3zb.jpg",
                        UserDescription = "分享游戏特惠情报、新作资讯以及一些有趣的......",
                        ReportCounts = 12,
                        CommentCounts = 6,
                        Attitudes = 27,
                        MediaUrl = ""
                    ),
                    ReportCounts = 8205,
                    CommentCounts = 212,
                    Attitudes = 10544,
                    Uid = 3105001013,
                    UserName = "FFF团微博支部",
                    UserAvatar = "https://tvax1.sinaimg.cn/crop.0.0.750.750.180/b9128e35ly8g8dmk5z01hj20ku0kut9f.jpg?KID=imgbed,tva&Expires=1668682139&ssig=%2FMTtUXbsuQ",
                    UserDescription = "分享游戏特惠情报、新作资讯以及一些有趣的......",
                    UrlGroup = "",
                    MediaUrl = ""
                ),
                onPlayEvent = {  }
            )
        }
    }
}