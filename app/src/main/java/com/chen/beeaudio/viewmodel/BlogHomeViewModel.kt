package com.chen.beeaudio.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.cachedIn
import com.chen.beeaudio.mock.UserIDMock
import com.chen.beeaudio.repository.BlogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class BlogHomeViewModel @Inject constructor(
    blogRepository: BlogRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val currentUserId = savedStateHandle.get<Long>("uid") ?: UserIDMock
    /* 从 BlogRepository 加载Blog列表 */
    val loadBlogList = blogRepository.getAllSubscribeBlogs(currentUserId).cachedIn(viewModelScope)
}