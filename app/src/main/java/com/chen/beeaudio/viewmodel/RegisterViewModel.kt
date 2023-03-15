package com.chen.beeaudio.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.model.net.AuthRegister
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.net.LocalApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class RegisterViewModel @Inject constructor(
    @Named("LocalServer") private val localApi: LocalApi
) : ViewModel() {
    /* 邮箱文本框绑定变量 */
    val registerEmail = MutableStateFlow("")

    /* 变更邮箱文本框绑定内容时间 */
    fun changeTextEmail(newText : String) {
        registerEmail.value = newText
    }

    /* 手机号码文本框绑定变量 */
    val registerPhone = MutableStateFlow("")
    /* 变更手机号码文本框内容事件 */
    fun changeTextPhone(newText : String) {
        registerPhone.value = newText
    }

    /* 密码文本框绑定变量 */
    val password = MutableStateFlow("")
    /* 确认密码文本框 绑定变量 */
    val confirmPassword = MutableStateFlow("")

    /* 密码框文本变更事件 */
    fun changePasswordText(newPassword: String) {
        password.value = newPassword
    }
    /* 确认密码框文本变更事件 */
    fun changeConfirmPasswordText(newConfirmPassword: String) {
        confirmPassword.value = newConfirmPassword
    }

    /** 验证两次密码文本内容一致
     *  密码内容一致返回 true, 密码内容不一致则返回 false
     */
    private fun confirmPasswordContent() : Boolean {
        return password.value == confirmPassword.value
    }

    /* 邮箱正则判断 */
    private fun isEmailFormat(email: String) : Boolean {
        val compile = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(.[a-zA-Z0-9_-]+)+\$")
        val matcher = compile.matcher(email)
        return matcher.matches()
    }

    /* 手机号码正则判断 */
    fun isPhoneFormat(phone: String): Boolean {
        val compile = Pattern.compile("^(13|14|15|16|17|18|19)\\d{9}$")
        val matcher = compile.matcher(phone)
        return matcher.matches()
    }

    /* 密码格式 正则判断 */
    private fun isPasswordFormat(password : String) : Boolean {
        val compile = Pattern.compile("^(?![0-9]+\$)(?![a-zA-Z]+\$)(?!_+\$)[a-zA-Z0-9_]{6,20}")
        val matcher = compile.matcher(password)
        return matcher.matches()
    }

    /* 注册结果标识 */
    val registerResultState : MutableState<RegisterConformResult> = mutableStateOf(RegisterConformResult.NONE)
    /* 注册执行 */
    fun registerUser(isEmail : Boolean) {
            if (isEmail) {
                if (!isEmailFormat(registerEmail.value)) {
                    registerResultState.value = RegisterConformResult.EMAIL_FORMAT_ERROR
                } else if (!isPasswordFormat(password.value)) {
                    registerResultState.value = RegisterConformResult.PASSWORD_FORMAT_ERROR
                } else if (!confirmPasswordContent()) {
                    registerResultState.value = RegisterConformResult.PASSWORD_CONFIRM_ERROR
                } else {
                    viewModelScope.launch {
                        val registerResult : ResponseBody<AuthRegister> = localApi.registerNewUserByEmail(
                            email = registerEmail.value,
                            password = password.value
                        )
                        if (registerResult.ok == 1) {
                            registerResultState.value = RegisterConformResult.SUCCESS(registerResult.data.uid)
                        } else {
                            registerResultState.value = RegisterConformResult.OTHER_ERROR
                        }
                    }
                }
            }
    }
}

/* 注册纠错结果 */
sealed class RegisterConformResult() {
    object EMAIL_FORMAT_ERROR : RegisterConformResult()         // 邮箱格式错误
    object PHONE_FORMAT_ERROR : RegisterConformResult()         // 手机号码格式错误
    object PASSWORD_FORMAT_ERROR : RegisterConformResult()      // 密码格式错误
    object PASSWORD_CONFIRM_ERROR : RegisterConformResult()     // 两次密码核对不一致
    object OTHER_ERROR : RegisterConformResult()        // 服务端返回错误
    object NONE : RegisterConformResult()               // 无状态
    class SUCCESS(val userid: Long) : RegisterConformResult()       // 核对正确
}