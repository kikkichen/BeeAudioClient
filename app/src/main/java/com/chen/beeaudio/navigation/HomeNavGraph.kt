package com.chen.beeaudio.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.paging.ExperimentalPagingApi
import com.chen.beeaudio.AudioHome
import com.chen.beeaudio.mock.SingleAlbum
import com.chen.beeaudio.screen.*
import com.chen.beeaudio.viewmodel.ArtistViewModel
import com.chen.beeaudio.viewmodel.MainViewModel
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
fun HomeNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        route = Graph.Home,
        startDestination = BottomBarRoute.AudioHome.route
    ) {
        /* 音频服务主页 */
        composable(route = BottomBarRoute.AudioHome.route) {
            AudioHome(
                navController = navController,
                mainViewModel = mainViewModel,
                /* 导航到歌单内容详情页 */
                onOpenPlayListPage = {
                    navController.navigate(
                        route = AudioHomeRoute.PlayListScreen.route + "/$it"
                    )
                },
                /* 导航到搜索索引页 */
                onOpenSearchPage = {
                    navController.navigate(
                        route = AudioHomeRoute.SearchScreen.route
                    )
                }
            )
        }
        /* 博文动态页 */
        composable(
            route = BottomBarRoute.BlogHome.route + "?uid={uid}",
            arguments = listOf(
                navArgument("uid") {
                    type = NavType.LongType
                }
            )
        ) {
            BlogHomeScreen(navController = navController, mainViewModel = mainViewModel)
        }
        /*  */
        composable(route = BottomBarRoute.Profile.route) {
            LibraryHomeScreen(
                navController = navController,
                mainViewModel = mainViewModel,
            )
//            PersonHomeScreen(
//                navController = navController,
//                mainViewModel = mainViewModel
//            )
        }
        audioHomeNavGraph(navController = navController, mainViewModel = mainViewModel)
        authNavGraph(navController = navController, mainViewModel = mainViewModel)
        blogNavGraph(navController = navController, mainViewModel = mainViewModel)
        personNavGraph(navController = navController, mainViewModel = mainViewModel)
    }
}

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalToolbarApi
@ExperimentalFoundationApi
fun NavGraphBuilder.audioHomeNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    navigation(
        route = Graph.AudioHome,
        startDestination = AudioHomeRoute.PlayListScreen.route + "/{playlist_id}"
    ) {
        /* 导航到歌单详情页 */
        composable(
            route = AudioHomeRoute.PlayListScreen.route + "/{playlist_id}",
            arguments = listOf(navArgument("playlist_id") {
                type = NavType.LongType
            })
        ) {
            PlayListScreen(
                navController = navController,
                mainViewModel = mainViewModel,
            ) { /* play */ }
        }

        /* 导航搜索索引页 */
        composable(
            route = AudioHomeRoute.SearchScreen.route
        ) {
            SearchScreen(
                navController = navController,
                onOpenTagPlayListCollectionPage = {
                    navController.navigate(route = AudioHomeRoute.TagPlayListCollectionScreen.route + "?playlist_cat=$it")
                },
                onOpenPlayListPage = {  }
            )
        }
        /* 导航到指定索引标签的歌单列表集合页 */
        composable(
            route = AudioHomeRoute.TagPlayListCollectionScreen.route + "?playlist_cat={playlist_cat}",
            arguments = listOf(navArgument("playlist_cat") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            TagPlayListScreen(
                catOfPlayLists = backStackEntry.arguments?.getString("playlist_cat") ?: "华语",
                navController = navController,
                onOpenPlayListPage = {
                    navController.navigate(
                        route = AudioHomeRoute.PlayListScreen.route + "/$it"
                    )
                }
            )
        }
        /* 导航到目的专辑详情页 */
        composable(
            route = AudioHomeRoute.AlbumScreen.route + "?album_id={album_id}",
            arguments = listOf(navArgument("album_id"){
                type = NavType.LongType
            })
        ) {
            AlbumScreen(
                navController = navController,
                mainViewModel = mainViewModel
            )
        }
        /* 导航到目的艺人详情页 */
        composable(
            route = AudioHomeRoute.ArtistScreen.route + "?artist_id={artist_id}",
            arguments = listOf(navArgument("artist_id") {
                type = NavType.LongType
            })
        ) {
            val mViewModel = hiltViewModel<ArtistViewModel>()
            ArtistScreen(
//                artistId = backStackEntry.arguments?.getLong("artist_id") ?: SingleAlbum.id,
                navController = navController,
                mainViewModel = mainViewModel,
                mViewModel = mViewModel
            )
        }
        /* 导航到搜索结果页 */
        composable(
            route = AudioHomeRoute.SearchResultScreen.route + "?keywords={keywords}",
            arguments = listOf(navArgument("keywords") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            SearchResultScreen(
                keyWords = backStackEntry.arguments?.getString("keywords") ?: "周杰伦",
                navController = navController,
                mainViewModel = mainViewModel,
                onOpenPlayListPage = {
                    navController.navigate(
                        route = AudioHomeRoute.PlayListScreen.route + "/$it"
                    )
                }
            )
        }
        /* 导航到播放页 */
        composable(
            route = AudioHomeRoute.PlayScreen.route,
        ) {
            PlayScreen(
                navController = navController,
                mainViewModel = mainViewModel
            )
        }
    }
}

sealed class AudioHomeRoute(
    val name : String,
    val route : String
) {
    object PlayScreen : AudioHomeRoute(name = "play_screen", route = "PLAY_SCREEN")
    object PlayListScreen : AudioHomeRoute(name = "play_list_screen", route = "PLAY_LIST_SCREEN")
    object SearchScreen : AudioHomeRoute(name = "search_screen", route = "SEARCH_SCREEN")
    object AlbumScreen : AudioHomeRoute(name = "album_screen", route = "ALBUM_SCREEN")
    object ArtistScreen : AudioHomeRoute(name = "artist_screen", route = "ARTIST_SCREEN")
    object SearchResultScreen : AudioHomeRoute(name = "search_result_screen", route = "SEARCH_RESULT_SCREEN")
    object TagPlayListCollectionScreen : AudioHomeRoute(name = "tag_playlist_collection_screen", route = "TAG_PLAYLIST_COLLECTION_SCREEN")
}