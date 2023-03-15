package com.chen.beeaudio.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.model.blog.BlogDraft
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.navigation.argument.ShareType
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.net.getPart
import com.chen.beeaudio.repository.database.DraftDatabase
import com.chen.beeaudio.utils.getRandomString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SendViewModel @Inject constructor(
    private val draftDatabase : DraftDatabase,
    @Named("LocalServer")
    private val localApi: LocalApi,
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    /* 博文正文 */
    var text : MutableStateFlow<String> = MutableStateFlow("")
    /* 图像uri列表 */
    var images : MutableStateFlow<MutableList<String>> = MutableStateFlow(mutableStateListOf())
    /* 音频分享数据 */
    val mediaData : MutableStateFlow<String> = MutableStateFlow("")

    /* 发送任务状态 */
    var blogUploadState : MutableStateFlow<BlogUploadState> = MutableStateFlow(BlogUploadState.None)

    /* 从导航参数中获取音频项目分享数据 */
    private var navArgumentShareItem = savedStateHandle.get<ShareType>("share_item_out") ?: ShareType("")


    init {
        if (navArgumentShareItem.content.isNotEmpty()) {
            val rawMediaDataString = navArgumentShareItem.content
            mediaData.value = rawMediaDataString
        }
    }

    /* 保存单条草稿到 Room 数据库 */
    suspend fun saveAsDraft(draft: BlogDraft) {
        draftDatabase.draftDao().addSingleDraft(draft)
    }

    /* 发送博文动态任务 */
    fun sendBlog(currentUid : Long, context: Context) {
        viewModelScope.launch {
            try {
                blogUploadState.value = BlogUploadState.TextRunning
                val sentTextResult : ResponseBody<String> = ResponseBody(ok = 1, code = 0, message = "", data = "")
                val imageNameGroup = mutableListOf<String>()
                if (images.value.size > 0) {
                    /* 为图片生成文件名 */
                    for (i in 0 until images.value.size) {
                        imageNameGroup.add("LOCALSERVER_${getRandomString(16)}")
                    }
                    localApi.sentBlog(currentUid, text.value, picUrls = imageNameGroup, mediaData = mediaData.value)
                } else {
                    localApi.sentBlog(currentUid, text.value, picUrls = emptyList(), mediaData = mediaData.value)
                }

                if (sentTextResult.ok == 1) {
                    /* 若存在图片Uri, 执行上传逻辑 */
                    if (images.value.size > 0) {
                        blogUploadState.value = BlogUploadState.ImageRunning(0)
                        images.value.forEachIndexed { index, uri ->
                            localApi.uploadBlogImageFile(
                                uid = currentUid,
                                name = imageNameGroup[index],
                                upload = getPart(context, "uploadname", ".jpg", uri.toUri())
                            )
                            blogUploadState.value = BlogUploadState.ImageRunning(index + 1)
                        }
                    }
                    blogUploadState.value = BlogUploadState.Success
                } else {
                    /* 失败 */
                    blogUploadState.value = BlogUploadState.Failed
                }
            } catch (e: Exception) {
                /* 有误 */
                blogUploadState.value = BlogUploadState.Error(e)
                Log.d("_chen", e.toString())
            }
        }
    }

    /** 变更音频分享数据内容
     *
     */
    fun changeMediaData(data : String) {
        mediaData.value = data
    }
}

/* 上传任务状态密封类 */
sealed class BlogUploadState() {
    object None : BlogUploadState()                 // 无状态
    object TextRunning : BlogUploadState()              // 发送文本中
    class ImageRunning(val position : Int) : BlogUploadState()      // 图像发送
    object Failed : BlogUploadState()                   // 发送失败
    class Error(val e : Exception) : BlogUploadState()          // 上传任务出错
    object Success : BlogUploadState()                  // 上传任务成功
}