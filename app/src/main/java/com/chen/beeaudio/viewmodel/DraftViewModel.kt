package com.chen.beeaudio.viewmodel

import androidx.lifecycle.ViewModel
import com.chen.beeaudio.model.blog.BlogDraft
import com.chen.beeaudio.repository.database.DraftDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class DraftViewModel @Inject constructor(
    private val database: DraftDatabase
): ViewModel() {

    /**
     *  获得全部草稿数据
     */

    suspend fun getAllDrafts() : Flow<List<BlogDraft>> {
        return database.draftDao().queryAllDrafts()
    }

    /**
     *  删除单条草稿项目
     *  @param  draft   草稿类型数据
     */
    suspend fun deleteSingleDraft(draft: BlogDraft) {
        database.draftDao().deleteSingleDraft(draft)
    }

    /**
     *  清空所有草稿数据
     */
    suspend fun clearDraftBox() {
        database.draftDao().clearAllDrafts()
    }
}