package com.chen.beeaudio.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.mock.RequestUserDetailMock
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class EditUserDetailViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi : LocalApi,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    /* 用户ID */
    val currentUserId : Long = savedStateHandle.get<Long>("user_id") ?: 0.toLong()

    /* 用户信息 */
    private val currentUserDetailData = MutableStateFlow(
        RequestUserDetail("", "", "","","", "", "", 0, 0)
    )

    /* 昵称 */
    private val _name : MutableStateFlow<String> = MutableStateFlow("")
    val name = _name.asStateFlow()

    /* 简介 */
    private val _description : MutableStateFlow<String> = MutableStateFlow("")
    val description = _description.asStateFlow()

    /* 电子邮件 */
    private val _email : MutableStateFlow<String> = MutableStateFlow("")
    val email = _email.asStateFlow()

    /* 电话 */
    private val _phone : MutableStateFlow<String> = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    init {
        loadCurrentUserDetail()
    }

    fun setName(s : String) {
        _name.value = s
    }

    fun setDescription(s : String) {
        _description.value = s
    }

    fun setEmail(s : String) {
        _email.value = s
    }

    fun setPhone(s : String) {
        _phone.value = s
    }

    /** 请求当前用户的简易信息
     *
     */
    fun loadCurrentUserDetail() {
        viewModelScope.launch {
            try {
                val result = localApi.getUserDetail(currentUserId)
                if (result.ok == 1) {
                    currentUserDetailData.value = result.data
                } else {
                    delay(500)
                    val result2 = localApi.getUserDetail(currentUserId)
                    if (result2.ok == 1) {
                        delay(500)
                        currentUserDetailData.value = result2.data
                    } else {
                        currentUserDetailData.value = localApi.getUserDetail(currentUserId).data
                    }
                }
                _name.value = currentUserDetailData.value.name
                _description.value = currentUserDetailData.value.description
                _email.value = currentUserDetailData.value.email
                _phone.value = currentUserDetailData.value.phone
            } catch (e : Throwable) {
                throw CancellationException()
            }
        }
    }

    /** 更新用户信息
     *  @param  toastEvent  Toast提示框弹出事件
     *  @param  finishedEvent   结束事件
     */
    fun updateUserDetail(toastEvent: (String) -> Unit, finishedEvent: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = localApi.editUserDetail(
                    uid = currentUserId,
                    name = _name.value,
                    description = _description.value,
                    email = _email.value,
                    phone = _phone.value,
                )
                if (result.ok == 1 ) {
                    toastEvent("个人信息修改成功！")
                    finishedEvent()
                } else {
                    toastEvent("服务器忙，请稍后重试")
                }
            } catch (e : Throwable) {
                launch(Dispatchers.Main) {
                    toastEvent("请求出现错误，请稍后重试")
                }
                throw CancellationException()
            }
        }
    }

    /** 验证关键信息不为空
     *  @param  context 上下文参数
     */
    fun verifyTextNotEmpty(context: Context) : Boolean {
        return if (_name.value.isNotEmpty() and _email.value.isNotEmpty()) {
            true
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "请确认昵称与邮箱信息不为空！", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    /** 验证关键信息有被更改
     *  @param  context 上下文参数
     */
    fun verifyTextHasChange(context: Context) : Boolean {
        return if (_name.value != currentUserDetailData.value.name
            || _description.value != currentUserDetailData.value.description
            || _email.value != currentUserDetailData.value.email
        ) {
            true
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "所有关键信息未有变更 ～", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
}