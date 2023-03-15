package com.chen.beeaudio.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.model.blog.RequestBlog
import com.chen.beeaudio.model.blog.RequestUser
import com.chen.beeaudio.net.LocalApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SearchBlogAndUserViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi
) : ViewModel() {
    /* 搜索框内关键字 */
    val searchKeywords = MutableStateFlow("")
    fun setSearchKeyword(newWords : String) {
        searchKeywords.value = newWords
    }

    /* 搜索博文动态结果 */
    private val _targetBlogs = MutableStateFlow<BlogSearchState>(BlogSearchState.None)
    val targetBlogs = _targetBlogs
    /* 搜索用户结果 */
    private val _targetUsers = MutableStateFlow<BlogSearchState>(BlogSearchState.None)
    val targetUsers = _targetUsers

    /* 通过关键字加载博文 */
    fun loadSearchBlogs(keyword : String) {
        viewModelScope.launch {
            try {
                _targetBlogs.value = BlogSearchState.Loading
                val data = localApi.searchBlogByKeywords(keyword).data
                _targetBlogs.value = BlogSearchState.BlogSearchSuccess(list = data)
            } catch (e : Throwable) {
                when (e) {
                    is NullPointerException -> {
                        _targetBlogs.value = BlogSearchState.BlogSearchSuccess(list = emptyList())
                    }
                    else -> {
                        _targetBlogs.value = BlogSearchState.Error(e)
                    }
                }
            }
        }
    }

    /* 通过关键字加载用户 */
    fun loadSearchUsers(keyword : String) {
        viewModelScope.launch {
            try {
                _targetUsers.value = BlogSearchState.Loading
                val data = localApi.searchUserByKeywords(keyword).data
                _targetUsers.value = BlogSearchState.UserSearchSuccess(list = data)
            } catch (e : Throwable) {
                when(e) {
                    is NullPointerException -> {
                        _targetUsers.value = BlogSearchState.UserSearchSuccess(list = emptyList())
                    }
                    else -> {
                        _targetUsers.value = BlogSearchState.Error(e)
                    }
                }
            }
        }
    }
}

/** 博文 / 用户 查询结果状态
 *
 */
sealed class BlogSearchState() {
    object None : BlogSearchState()                                             // 无状态
    object Loading : BlogSearchState()                                          // 加载状态
    class Error(e : Throwable) : BlogSearchState()                              // 错误状态
    class BlogSearchSuccess(val list : List<RequestBlog>) : BlogSearchState()       // 博文搜索结果加载成功状态
    class UserSearchSuccess(val list : List<RequestUser>) : BlogSearchState()       // 用户搜索结果加载成功状态
}