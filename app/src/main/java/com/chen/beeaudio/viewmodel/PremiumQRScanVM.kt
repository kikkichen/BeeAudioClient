package com.chen.beeaudio.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.model.audio.FamilyPremium
import com.chen.beeaudio.model.audio.Premium
import com.chen.beeaudio.net.LocalPremiumApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class PremiumQRScanVM @Inject constructor(
    @Named("LocalPremiumServer")
    private val premiumApi: LocalPremiumApi
) : ViewModel() {
    private val _scanId : MutableStateFlow<String> = MutableStateFlow("")
    val scanId = _scanId.asStateFlow()

    private val _premiumGroup : MutableStateFlow<FamilyPremium> =
        MutableStateFlow(
            FamilyPremium(numbers = emptyList(), summarize = Premium("",0,"","",0))
        )
    val premiumGroup = _premiumGroup.asStateFlow()

    /* 搜索对应ID的 Premium家庭组套餐 */
    fun loadPremiumGroupInfo() {
        viewModelScope.launch {
            try {
                _premiumGroup.value = premiumApi.getPremiumCardInfo(cardId = _scanId.value).data
            } catch (e : Throwable) {
                throw CancellationException()
            }
        }
    }

    /* 提交加入家庭组的申请 */
    fun postJoinPremiumGroupApply(context: Context, currentUserId: Long) {
        viewModelScope.launch {
            try {
                val result = premiumApi.postJoinInPremiumGroup(targetId = currentUserId, cardId = _scanId.value)
                if (result.data) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "申请提交成功，待管理员审核 ~", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "服务器忙，请稍后尝试重新提交", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e : Throwable) {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "申请提交出了写问题，请尝试重新提交", Toast.LENGTH_SHORT).show()
                }
                throw CancellationException()
            }
        }
    }

    /* 扫描到新的字符串并替换原有字符 */
    fun changeScanId(newString: String) {
        _scanId.value = newString
    }
}