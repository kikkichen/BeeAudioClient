package com.chen.beeaudio.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.model.net.AuthLoginResponse
import com.chen.beeaudio.net.AuthApi
import com.chen.beeaudio.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class LoginViewModel @Inject constructor(
    @Named("AuthServer") private val authApi: AuthApi,
    val repository: DataStoreRepository
) : ViewModel() {
    /* 用户登陆账户 （邮箱、手机号码） */
    val accountText = MutableStateFlow("")
    /* 登陆账号文本变更事件 */
    fun changeAccountText(newText: String) {
        accountText.value = newText
    }

    /* 密码文本框绑定变量 */
    val password = MutableStateFlow("")
    /* 密码框文本变更事件 */
    fun changePasswordText(newPassword: String) {
        password.value = newPassword
    }

    /* 登陆状态 */
    var loginState : MutableState<LoginResultState> = mutableStateOf(LoginResultState.None)
    /* 登陆执行 */
    fun loginAction(
        afterLoginEvent: suspend () -> Unit
    ) {
        if (accountText.value.isEmpty() or password.value.isEmpty()) {
            Log.d("_chen", "文本框有空")
            /* 文本框有空 */
            loginState.value = LoginResultState.FormatError
        } else {
            viewModelScope.launch {
                try {
                    val result = authApi.loginAndGetToken(
                        userName = accountText.value,
                        password = password.value
                    )
                    loginState.value = if (result.accessToken.isNotEmpty()) {
                        Log.d("_chen", "储存Token逻辑")
                        /* 将Token储存到 protobuf */
                        repository.saveData(
                            accessToken = result.accessToken,
                            expiresIn = result.expiresIn.toLong(),
                            refreshToken = result.refreshToken,
                            scope = result.scope,
                            tokenType = result.tokenType
                        )
                        launch(Dispatchers.Main) {
                            afterLoginEvent()
                        }
                        /* 将登陆状态定义为成功 */
                        LoginResultState.Success(result)
                    } else {
                        /* 失败的登陆 */
                        LoginResultState.OtherError
                    }
                } catch (e : Throwable) {
                    loginState.value = LoginResultState.OtherError
                    throw CancellationException()
                }
            }
        }
    }

}

sealed class LoginResultState{
    object None: LoginResultState()     // 无动作
    object OtherError: LoginResultState()
    object FormatError : LoginResultState()
    class Success(val authLoginResponse: AuthLoginResponse) : LoginResultState()
}