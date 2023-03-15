package com.chen.beeaudio.navigation

import android.os.Bundle
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.chen.beeaudio.mock.RequestBlogMock
import com.chen.beeaudio.mock.RequestUserDetailMock
import com.chen.beeaudio.mock.SimpleUserMock
import com.chen.beeaudio.navigation.argument.NavShareType
import com.chen.beeaudio.navigation.argument.ShareType
import com.chen.beeaudio.screen.*
import com.chen.beeaudio.screen.SendScreen.*
import com.chen.beeaudio.viewmodel.MainViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
fun NavGraphBuilder.blogNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    navigation(
        route = Graph.Blog,
        startDestination = BlogRoute.SendScreen.route
    ) {
        /* 博文详情页 */
        composable(
            route = BlogRoute.BlogDetail.route + "?id={id}&isRetweeted={isRetweeted}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                },
                navArgument("isRetweeted") {
                    type = NavType.BoolType
                }
            )
        ) { backStackEntry ->
            val blogId = backStackEntry.arguments?.getLong("id") ?: RequestBlogMock.Id
            val isRetweeted = backStackEntry.arguments?.getBoolean("isRetweeted") ?: false
            BlogDetailScreen(
                currentBlogId = blogId,
                isRetweetedBlog = isRetweeted,
                navController = navController,
                mainViewModel = mainViewModel,
            )
        }
        /* 用户个人页面 */
        composable(
            route = BlogRoute.UserDetail.route + "?uid={uid}",
            arguments = listOf(
                navArgument("uid") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getLong("uid") ?: RequestUserDetailMock.uid
            UserDetailScreen(
                userId = uid,
                navController = navController,
                mainViewModel = mainViewModel
            )
        }
        /* 导航至大图浏览页 */
        composable(
            route = BlogRoute.ImageBrowser.route + "?${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_PIC_GROUP}={${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_PIC_GROUP}}&${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_CURRENT}={${NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_CURRENT}}&post_user_id={post_user_id}",
            arguments = listOf(
                navArgument("post_user_id") {
                    type = NavType.LongType
                },
                navArgument(NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_PIC_GROUP) {
                    type = NavType.StringType
                },
                navArgument(NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_CURRENT) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            /* 发布博文用户名 */
            val postUserId = backStackEntry.arguments?.getLong("post_user_id") ?: 9900619251
            /* 获取原始图片组字符串 */
            val rawImages = backStackEntry.arguments?.getString(NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_PIC_GROUP)
            /* 图片组字符串转为List集合 */
            val images = rawImages.toString().trim()
                .replace(" ", "")
                .replace("{","")
                .replace("}","")
                .replace("[","")
                .replace("]","").split(",")
            val selectImage = backStackEntry.arguments?.getString(NavigationConfig.IMAGE_BROWSER_SCREEN_PARAMS_CURRENT)
            ImageBrowserScreen(
                images = images,
                postUserId = postUserId,
                selectImage = selectImage!!,
                navController = navController
            )
        }
        /* 发布博文动态页 */
        composable(
            route = BlogRoute.SendScreen.route
        ) {
            SendScreen(navController = navController, mainViewModel = mainViewModel)
        }
        /* 伴随分享音频数据参数访问 发布博文动态页 */
        composable(
            route = BlogRoute.SendScreen.route + "?share_item_out={share_item_out}",
            arguments = listOf(
                navArgument("share_item_out") {
                    type = NavShareType()
                }
            )
        ) {
            SendScreen(navController = navController, mainViewModel = mainViewModel)
        }
        /* 草稿箱页 */
        composable(
            route = BlogRoute.DraftScreen.route
        ) {
            DraftScreen(navController = navController)
        }
        /* 热门话题页 */
        composable(
            route = BlogRoute.HotTopicScreen.route
        ) {
            HotTopScreen(navController = navController)
        }
        /* 关联好友(关注)页面 */
        composable(
            route = BlogRoute.CallMyFollowerScreen.route
        ) {
            CallMyFollowerScreen(
                navController = navController,
                mainViewModel = mainViewModel,
            )
        }
        /* 关注用户页 */
        composable(
            route = BlogRoute.FollowUserScreen.route + "?uid={uid}&username={username}",
            arguments = listOf(
                navArgument("uid") {
                    type = NavType.LongType
                },
                navArgument("username") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val uid : Long = backStackEntry.arguments?.getLong("uid") ?: SimpleUserMock.Id
            val userName : String = backStackEntry.arguments?.getString("username") ?: SimpleUserMock.name
            FollowUserScreen(
                userId = uid,
                userName = userName,
                navController = navController,
                mainViewModel = mainViewModel
            )
        }
        /* 粉丝用户页 */
        composable(
            route = BlogRoute.FansUserScreen.route + "?uid={uid}&username={username}",
            arguments = listOf(
                navArgument("uid") {
                    type = NavType.LongType
                },
                navArgument("username") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val uid : Long = backStackEntry.arguments?.getLong("uid") ?: SimpleUserMock.Id
            val userName : String = backStackEntry.arguments?.getString("username") ?: SimpleUserMock.name
            FansUserScreen(
                userId = uid,
                userName = userName,
                navController = navController,
                mainViewModel = mainViewModel
            )
        }
        /* 博文动态、用户搜索页 */
        composable(
            route = BlogRoute.BlogSearch.route
        ) {
            SearchBlogUserScreen(navController = navController, mainViewModel = mainViewModel)
        }
        /* 分享曲目选择页面 */
        composable(
            route = BlogRoute.ShareMusicScreen.route + "?my_like_playlist={my_like_playlist}",
            arguments = listOf(
                navArgument("my_like_playlist") {
                    type = NavType.LongType
                }
            )
        ) {
            ShareMusicScreen(navController = navController, mainViewModel = mainViewModel)
        }
    }
}

sealed class BlogRoute(
    val name: String,
    val route: String,
) {
    object BlogDetail : BlogRoute(name = "blog_detail_screen", route = "BLOG_DETAIL_SCREEN")
    object UserDetail : BlogRoute(name = "user_detail_screen", route = "USER_DETAIL_SCREEN")
    object BlogSearch : BlogRoute(name = "blog_search_screen", route = "BLOG_SEARCH_SCREEN")
    object ImageBrowser : BlogRoute(name = "large_screen", route = "IMAGE_BROWSER")
    object SendScreen : BlogRoute(name = "send_screen", route = "SEND_SCREEN")
    object DraftScreen : BlogRoute(name = "draft_screen", route = "DRAFT_SCREEN")
    object HotTopicScreen : BlogRoute(name = "hot_topic_screen", route = "HOT_TOPIC_SCREEN")
    object CallMyFollowerScreen : BlogRoute(name = "call_friend_screen", route = "CALL_MY_FOLLOWER_SCREEN")
    object ShareMusicScreen : BlogRoute(name = "share_music_screen", route = "SHARE_MUSIC_SCREEN")
    object FollowUserScreen : BlogRoute(name = "follow_user_screen", route = "FOLLOW_USER_SCREEN")
    object FansUserScreen : BlogRoute(name = "fans_user_screen", route = "FANS_USER_SCREEN")
}

object NavigationConfig {
    /* 大图查看页 以及其参数 */
    const val IMAGE_BROWSER_SCREEN = "large_screen"
    const val IMAGE_BROWSER_SCREEN_PARAMS_PIC_GROUP = "pic_url"
    const val IMAGE_BROWSER_SCREEN_PARAMS_CURRENT = "select_url"
}
