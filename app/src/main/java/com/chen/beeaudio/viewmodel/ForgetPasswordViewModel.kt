package com.chen.beeaudio.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.net.LocalApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ForgetPasswordViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val account : Long = savedStateHandle.get<Long>("account") ?: 0.toLong()

    /* 当前密码修改模式是否是依据邮箱进行修改 */
    var isModifierPasswordByEmail : Boolean = false

    /* 字符串账户 - 邮箱 */
    private val _accountString : MutableStateFlow<String> = MutableStateFlow("")
    val accountString = _accountString.asStateFlow()

    /* 数字账户 - 用户ID */
    private val _accountNumber : MutableStateFlow<Long> = MutableStateFlow(0.toLong())
    val accountNumber = _accountNumber.asStateFlow()

    /* 原密码 */
    private val _originalPassword : MutableStateFlow<String> = MutableStateFlow("")
    val originalPassword = _originalPassword.asStateFlow()

    /* 新密码 */
    private val _newPassword1 : MutableStateFlow<String> = MutableStateFlow("")
    val newPassword1 = _newPassword1.asStateFlow()

    /* 确认新密码 */
    private val _newPassword2 : MutableStateFlow<String> = MutableStateFlow("")
    val newPassword2 = _newPassword2.asStateFlow()

    init {
        /* 邮箱账户信息初始化 */
        _accountNumber.value = account
        /* 若邮箱参数无，则当前为通过用户ID修改密码模式 */
        isModifierPasswordByEmail = account == 0.toLong()
    }

    fun setAccountString(s: String) {
        _accountString.value = s
    }

    fun setOriginalPassword(s : String) {
        _originalPassword.value = s
    }

    fun setNewPassword1(s : String) {
        _newPassword1.value = s
    }

    fun setNewPassword2(s : String) {
        _newPassword2.value = s
    }

    /* 修改密码请求 */
    fun postModifierPassword(toastEvent: (String) -> Unit, finishedEvent: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = if (isModifierPasswordByEmail) {
                    localApi.modifierUserPasswordByEmail(
                        email = _accountString.value,
                        originalPassword = _originalPassword.value,
                        newPassword = _newPassword1.value
                    )
                } else {
                    localApi.modifierUserPassword(
                        uid = _accountNumber.value,
                        originalPassword = _originalPassword.value,
                        newPassword = _newPassword1.value
                    )
                }
                if (result.ok == 1) {
                    if (result.code == 200) {
                        launch(Dispatchers.Main) {
                            toastEvent("密码修改成功！")
                            finishedEvent()
                        }
                    } else {
                        launch(Dispatchers.Main) { toastEvent("服务器忙，请稍后重试～") }
                    }
                } else {
                    launch(Dispatchers.Main) { toastEvent("原密码核对错误，请仔细检查") }
                }
            } catch (e : Throwable) {
                launch(Dispatchers.Main) { toastEvent("请求出现错误，请稍后重试") }
                throw CancellationException()
            }
        }
    }

    /** 核对两次重复新密码是否一致
     *  @param  context 上下文参数
     */
    fun verifyNewPasswordSample(context: Context) : Boolean {
        return if (_newPassword1.value == _newPassword2.value) {
            true
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "两次新密码核对不一致!", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    /** 核对所有输入框不为空
     *  @param  context 上下文参数
     */
    fun verifyAllTextFieldNotEmpty(context: Context) : Boolean {
        return if (isModifierPasswordByEmail && _accountString.value.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "邮箱信息不能为空!", Toast.LENGTH_SHORT).show()
            }
            false
        } else if (_originalPassword.value.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "原密码输入框不能为空!", Toast.LENGTH_SHORT).show()
            }
            false
        } else if (_newPassword1.value.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "新密码不能为空!", Toast.LENGTH_SHORT).show()
            }
            false
        } else true
    }
}