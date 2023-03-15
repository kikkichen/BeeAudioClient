package com.chen.beeaudio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import coil.ImageLoader
import com.chen.beeaudio.model.blog.Attitude
import com.chen.beeaudio.model.blog.Comment
import com.chen.beeaudio.model.blog.RequestBlog
import com.chen.beeaudio.model.blog.Retweeted
import com.chen.beeaudio.model.localmodel.Blog
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.BlogDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class BlogDetailViewModel @Inject constructor(
    @Named("PlayListCover")
    private val imageLoader: ImageLoader,
    @Named("LocalServer")
    private val localApi: LocalApi,
    private val repository: BlogDetailRepository
) : ViewModel() {
    val myImageLoader = imageLoader

    val currentBlogDetail = MutableStateFlow<BlogDetailRequestState>(BlogDetailRequestState.Loading)
    /* 请求当前博文的详细信息 */
    fun loadCurrentBlogDetail(blogId : Long) {
        viewModelScope.launch {
            try {
                currentBlogDetail.value = BlogDetailRequestState.Loading
                val originalData = localApi.getTargetBlogDetail(blogId = blogId)
                if (originalData.ok != 1) {
                    currentBlogDetail.value = BlogDetailRequestState.Error(Throwable(message = "No Premission"))
                } else {
                    currentBlogDetail.value = BlogDetailRequestState.Success(data = originalData.data)
                }
            } catch (e : Throwable) {
                currentBlogDetail.value = BlogDetailRequestState.Error(e)
            }
        }
    }

    /* 针对当前用户是否对当前博文有过点赞记录的状态 */
    val isAttitudeRecord : MutableStateFlow<Boolean> = MutableStateFlow(false)

    /* 转发、评论、点赞行为状态 */
    val retweetedState : MutableStateFlow<BlogActionState> = MutableStateFlow(BlogActionState.None)
    val commentState : MutableStateFlow<BlogActionState> = MutableStateFlow(BlogActionState.None)
    val attitudeState : MutableStateFlow<BlogActionState> = MutableStateFlow(BlogActionState.None)

    /* 回复框文本 */
    val replyTextContent = MutableStateFlow("")
    /* 回复框文本变更时间 */
    fun changeReplyTextEvent(newWords : String) {
        replyTextContent.value = newWords
    }

    /* 转发列表信息 */
    private val _retweetedList = MutableStateFlow<PagingData<Retweeted>>(PagingData.empty())
    val retweetedList = _retweetedList

    /* 评论列表信息 */
    private val _commentList = MutableStateFlow<PagingData<Comment>>(PagingData.empty())
    val commentList = _commentList

    /* 点赞列表信息 */
    private val _attitudeList = MutableStateFlow<PagingData<Attitude>>(PagingData.empty())
    val attitudeList = _attitudeList

    /* 加载转发列表信息 (原创博文动态) - 分页 */
    fun loadOriginalBlogRetweetedList(blogId : Long) {
        viewModelScope.launch {
            repository
                .getRetweetedList(blogId = blogId, isReport = false)
                .cachedIn(viewModelScope)
                .collect {
                    _retweetedList.value = it
                }
        }
    }

    /* 加载转发列表信息 (转发博文动态) - 分页 */
    fun loadRetweetBlogRetweetedList(blogId : Long) {
        viewModelScope.launch {
            repository
                .getRetweetedList(blogId = blogId, isReport = true)
                .cachedIn(viewModelScope)
                .collect {
                    _retweetedList.value = it
                }
        }
    }

    /* 加载评论列表信息 - 分页 */
    fun loadCommentList(blogId : Long) {
        viewModelScope.launch {
            repository
                .getCommentList(blogId = blogId)
                .cachedIn(viewModelScope)
                .collect {
                    _commentList.value = it

                }
        }
    }

    /* 加载点赞信息 - 分页 */
    fun loadAttitudeList(blogId : Long) {
        viewModelScope.launch {
            repository
                .getAttitudeList(blogId = blogId)
                .cachedIn(viewModelScope)
                .collect {
                    _attitudeList.value = it
                }
        }
    }

    /* 转发行为 */
    fun retweetedWork(uid: Long, blogId: Long) {
        viewModelScope.launch {
            /* 转发状态处于运行时 */
            retweetedState.value = BlogActionState.Running
            /* 捕获异常 */
            try {
                /* 发送转发博文，并等待响应结果 */
                val result = localApi.retweetedBlog(
                    uid = uid,
                    text = replyTextContent.value,
                    source = "BeeAudio客户端",
                    retweetedId = blogId
                )
                if (result.ok == 1) {
                    /* 响应表示： 动态博文转发成功 */
                    retweetedState.value = BlogActionState.RetweetedSuccess(data = result.data)
                    replyTextContent.value = ""
                } else {
                    /* 出现来自服务端的错误或者其他原因导致转发失败 */
                    retweetedState.value = BlogActionState.Failed
                }
            } catch (e: Exception) {
                /* 由于网络通信造成的异常 */
                retweetedState.value = BlogActionState.Error(e)
            } finally {
                /* 延时后状态恢复为无状态 */
                delay(3000)
                retweetedState.value = BlogActionState.None
            }
        }
    }

    /* 评论行为 */
    fun commentWork(uid: Long, bid: Long, rootId: Long) {
        viewModelScope.launch {
            try {
                /* 发送评论，并等待响应结果 */
                val result = localApi.commentBlog(
                    uid = uid,
                    text = replyTextContent.value,
                    source = "BeeAudio客户端",
                    rootId = rootId,
                    bid = bid
                )
                if (result.ok == 1) {
                    /* 响应表示： 动态博文评论成功 */
                    commentState.value = BlogActionState.CommentSuccess(data = result.data)
                    replyTextContent.value = ""
                } else {
                    /* 出现来自服务端的错误或者其他原因导致评论失败 */
                    commentState.value = BlogActionState.Failed
                }
            } catch (e : Exception) {
                /* 由于网络通信造成的异常 */
                commentState.value = BlogActionState.Error(e)
            } finally {
                /* 延时后状态恢复为无状态 */
                delay(3000)
                commentState.value = BlogActionState.None
            }
        }
    }

    /* 点赞行为 */
    fun attitudeWork(uid: Long, bid: Long) {
        viewModelScope.launch {
            try {
                /* 发送评论，并等待响应结果 */
                val result = localApi.attitudeBlog(
                    uid = uid,
                    source = "BeeAudio客户端",
                    bid = bid
                )
                if (result.ok == 1) {
                    /* 响应表示： 动态博文评论成功 */
                    attitudeState.value = BlogActionState.AttitudeSuccess(data = result.data)
                    replyTextContent.value = ""
                    /* 修正点赞记录状态 */
                    isAttitudeRecord.value = result.data
                } else {
                    /* 出现来自服务端的错误或者其他原因导致评论失败 */
                    attitudeState.value = BlogActionState.Failed
                }
            } catch (e : Exception) {
                /* 由于网络通信造成的异常 */
                attitudeState.value = BlogActionState.Error(e)
            } finally {
                /* 延时后状态恢复为无状态 */
                delay(3000)
                attitudeState.value = BlogActionState.None
            }
        }
    }

    /* 查询当前用户是否对当前博文有过点赞记录 */
    fun checkAttitudeRecord(uid : Long, blogId : Long) {
        viewModelScope.launch {
            try {
                val result = localApi.isAttitudeCheck(uid = uid, bid = blogId)
                if (result.ok == 1) {
                    isAttitudeRecord.value = result.data
                } else {
                    isAttitudeRecord.value = false
                }
            } catch (e : Exception) {
                isAttitudeRecord.value = false
            }
        }
    }
}

/* 转发、点赞、收藏 等POST操作执行逻辑状态 */
sealed class BlogActionState() {
    object None : BlogActionState()
    object Running : BlogActionState()
    object Failed : BlogActionState()
    class Error(e : Exception) : BlogActionState()
    class RetweetedSuccess(val data : String) : BlogActionState()  // 转发成功的状态
    class CommentSuccess(val data : String) : BlogActionState()     // 评论成功的状态
    class AttitudeSuccess(val data : Boolean) : BlogActionState()    // 点赞成功的状态
}

/* 动态博文详细信息请求结果状态 */
sealed class BlogDetailRequestState() {
    object Loading : BlogDetailRequestState()
    class Error(val e : Throwable) : BlogDetailRequestState()
    class Success(val data : RequestBlog) : BlogDetailRequestState()
}