package com.chen.beeaudio.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.net.LocalPremiumApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class GuidePremiumVM @Inject constructor(
    @Named("LocalPremiumServer")
    private val localPremiumApi: LocalPremiumApi
) : ViewModel() {
    /* 升级为 个人版 Premium 套餐 */
    fun upgradePersonPremium(currentUserId: Long, toastEvent: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val code = localPremiumApi.upgradeToPremiumPerson(currentUserId)
                if (code.ok == 1) {
                    launch(Dispatchers.Main) {
                        toastEvent("🥳 恭喜, Premium 个人套餐开通成功！卡号:${code.data},\n 快去解锁限量音频吧！")
                    }
                } else {
                    launch(Dispatchers.Main) {
                        toastEvent("服务器忙，请稍后再试")
                    }
                }
            } catch (e : Throwable) {
                launch(Dispatchers.Main) {
                    toastEvent("请求出现错误， 请稍后再试")
                }
                throw CancellationException()
            }
        }
    }

    /* 开通 家庭组 Premium 套餐 */
    fun upgradeFamilyPremium(currentUserId: Long, toastEvent: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val code = localPremiumApi.upgradeToPremiumFamily(currentUserId)
                if (code.ok == 1) {
                    launch(Dispatchers.Main) {
                        toastEvent("🥳 恭喜, Premium 家庭组套餐开通成功！卡号:${code.data},\n 快去和小伙伴一起听音乐吧！")
                    }
                } else {
                    launch(Dispatchers.Main) {
                        toastEvent("服务器忙，请稍后再试")
                    }
                }
            } catch (e : Throwable) {
                launch(Dispatchers.Main) {
                    toastEvent("请求出现错误， 请稍后再试")
                }
                throw CancellationException()
            }
        }
    }
}