package com.chen.beeaudio.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.chen.beeaudio.init.BLOGS_PER_PAGE_SIZE
import com.chen.beeaudio.model.localmodel.Blog
import com.chen.beeaudio.model.localmodel.BlogRemoteKeys
import com.chen.beeaudio.model.localmodel.RetweetedBlog
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.database.BlogsDatabase
import com.chen.beeaudio.utils.TimeUtils.convertStrToLongTimeUnit

@ExperimentalPagingApi
class BlogsRemoteMediator(
    private val currentUserId : Long,
    private val localApi: LocalApi,
    private val localDatabase: BlogsDatabase,
    private var initialPagingSize : Int
) : RemoteMediator<Int, Blog>() {

    private val blogDao = localDatabase.blogImageDao()
    private val blogRemoteKeyDao = localDatabase.blogRemoteKeysDao()
    private val remoteKeyTransition = localDatabase.remoteKeyTransaction()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Blog>
    ): MediatorResult {
        return try {
            val currentPage = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextPage?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevPage = remoteKeys?.prevPage
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    prevPage
                }
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKeys?.nextPage
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    nextPage
                }
            }
            /* 返回网络请求结果 */
//            val response = localApi.getMyFollowBlogs(myToken = ACCESS_TOKEN, count = BLOGS_PER_PAGE_SIZE, page = currentPage).requestBlog
            val response = localApi.getMyFollowBlogsV2(
                userId = currentUserId,
                page = currentPage,
                size = BLOGS_PER_PAGE_SIZE
            ).data
            val endOfPaginationReached = response.isEmpty()

            val prevPage = if (currentPage == 1) null else currentPage - 1
            val nextPage = if (endOfPaginationReached) null else if (currentPage == 1) (initialPagingSize/BLOGS_PER_PAGE_SIZE + 1) else currentPage + 1

            remoteKeyTransition.saveDataToDatabase(
                isRefresh = (loadType == LoadType.REFRESH),
                keys = response.map { requestBlog ->
                    BlogRemoteKeys(
                        id = requestBlog.Id,
                        prevPage = prevPage,
                        nextPage = nextPage
                    )
                },
                /* 网络请求体Blog 转换为 Room 中的 Blog 类型映射 */
                blogs = response.map { requestBlog ->
                    Blog(
                        Id = requestBlog.Id,
                        Created = convertStrToLongTimeUnit(requestBlog.Created),     // 本地时间工具转为时间戳
                        Text = requestBlog.Text,
                        Source = requestBlog.Source,
                        ReportCounts = requestBlog.ReportCounts,
                        CommentCounts = requestBlog.CommentCounts,
                        Attitudes = requestBlog.Attitudes,
                        MediaUrl = requestBlog.MediaUrl,
                        // User
                        Uid = requestBlog.User.Uid,
                        UserAvatar = requestBlog.User.AvatarUrl,
                        UserName = requestBlog.User.Name,
                        UserDescription = requestBlog.User.Description,

                        // 这里的警告⚠ 不要删除
                        UrlGroup = if (requestBlog.UrlGroup.isNullOrEmpty()) "" else requestBlog.UrlGroup.map { it.url }.toString(),
                        RetweetedStatus = RetweetedBlog(
                            Id = requestBlog.RetweetedStatus.Id ?: 0,
                            Created = convertStrToLongTimeUnit(requestBlog.RetweetedStatus.Created),
                            Text = requestBlog.RetweetedStatus.Text ?: "",
                            ReportCounts = requestBlog.RetweetedStatus.ReportCounts ?: 0,
                            CommentCounts = requestBlog.RetweetedStatus.CommentCounts ?: 0,
                            Attitudes = requestBlog.RetweetedStatus.Attitudes ?: 0,
                            UrlGroup = if (requestBlog.RetweetedStatus.UrlGroup.isNullOrEmpty()) "" else requestBlog.RetweetedStatus.UrlGroup.map { it.url }.toString(),
                            // Retweeted Paging
                            Uid = requestBlog.RetweetedStatus.User.Uid ?: 0,
                            UserName = requestBlog.RetweetedStatus.User.Name ?: "",
                            UserAvatar = requestBlog.RetweetedStatus.User.AvatarUrl ?: "",
                            UserDescription = requestBlog.RetweetedStatus.User.Description ?: "",
                            MediaUrl = requestBlog.RetweetedStatus.MediaUrl ?: ""
                        )
                    )
                },
                blogDao = blogDao,
                blogRemoteKeyDao = blogRemoteKeyDao
            )

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e : Exception) {
            Log.d("_chen", "协调器出现了什么问题？${e.printStackTrace()}")
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Blog>
    ) : BlogRemoteKeys? {
        return state.anchorPosition?.let{ position ->
            state.closestItemToPosition(position)?.Id?.let { id ->
                blogRemoteKeyDao.getRemoteKeys(id = id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, Blog>
    ): BlogRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { blog ->
                blogRemoteKeyDao.getRemoteKeys(id = blog.Id)
            }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, Blog>
    ): BlogRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { blog ->
                blogRemoteKeyDao.getRemoteKeys(id = blog.Id)
            }
    }
}