package com.chen.beeaudio.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.model.audio.FamilyPremiumNumbers
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
class PremiumDetailVM @Inject constructor(
    @Named("LocalPremiumServer")
    private val premiumApi: LocalPremiumApi
) : ViewModel() {

    private val _premiumInfoState : MutableStateFlow<Premium> = MutableStateFlow(Premium("",0,"","",0))
    val premiumInfoState = _premiumInfoState.asStateFlow()

    private val _premiumFamilyInfoState : MutableStateFlow<FamilyPremiumNumbers> =
        MutableStateFlow(FamilyPremiumNumbers(applyNumbers = emptyList(), formalNumbers = emptyList()))
    val premiumFamilyInfoState = _premiumFamilyInfoState.asStateFlow()

    private val _historyPremiumRecord : MutableStateFlow<List<Premium>> = MutableStateFlow(emptyList())
    val historyPremiumRecord = _historyPremiumRecord.asStateFlow()

    /** 请求加载用户的 Premium 套餐信息
     *  @param  currentUserId   当前用户ID
     */
    fun loadPremiumInfo(currentUserId : Long) {
        viewModelScope.launch {
            try {
                _premiumInfoState.value = premiumApi.getIsPremium(userId = currentUserId).data
            } catch (e : Throwable) {
                throw CancellationException()
            }
        }
    }

    /** 请求当前用户仅 20 次Premium套餐的历史支付信息记录
     *  @param  currentUserId   当前用户ID
     */
    fun loadHistoryPremiumRecord(currentUserId : Long) {
        viewModelScope.launch {
            try {
                val resultList = premiumApi.getRecentPremiumOrder(uid = currentUserId).data
                if (resultList.isEmpty()) {
                    _historyPremiumRecord.value = emptyList()
                } else {
                    _historyPremiumRecord.value = resultList
                }
            } catch (e : Throwable) {
                throw CancellationException()
            }
        }
    }

    /** 请求家庭组套餐内的成员信息
     *  @param  currentUserId   当前用户ID
     */
    fun loadFamilyPremiumNumbersInfo(currentUserId : Long) {
        viewModelScope.launch {
            try {
                _premiumFamilyInfoState.value =
                    premiumApi.getPremiumFamilyNumbers(
                        uid = currentUserId,
                        cardId = _premiumInfoState.value.card_id
                    ).data
            } catch (e : Throwable) {
                throw CancellationException()
            }
        }
    }

    /** 管理员权力： 移除用户 - 包括移除申请 与 移除正式成员
     *  @param  context     上下文参数
     *  @param  currentUserId   当前管理员用户ID
     *  @param  targetUserId    目标执行用户ID
     */
    fun removeNumbers(context: Context, currentUserId: Long, targetUserId: Long) {
        viewModelScope.launch {
            try {
                val result =
                    premiumApi.disagreeUserJoinInPremiumGroup(
                        uid = currentUserId,
                        targetId = targetUserId,
                        cardId = _premiumInfoState.value.card_id
                    )
                if (result.data) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "执行完成！", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "服务器忙，请稍后再次尝试", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e : Throwable) {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "请求出现错误，请稍后再次尝试", Toast.LENGTH_SHORT).show()
                }
                throw CancellationException()
            }
        }
    }

    /** 管理员权力： 同意加入家庭组的申请
     *  @param  context     上下文参数
     *  @param  currentUserId   当前管理员用户ID
     *  @param  targetUserId    目标执行用户ID
     */
    fun agreeApplyNumbers(context: Context, currentUserId: Long, targetUserId: Long) {
        viewModelScope.launch {
            try {
                val result =
                    premiumApi.agreeUserJoinInPremiumGroup(
                        uid = currentUserId,
                        targetUserId = targetUserId,
                        cardId = _premiumInfoState.value.card_id
                    )
                if (result.data) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "执行完成！", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "服务器忙，请稍后再次尝试", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e : Throwable) {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "请求出现错误，请稍后再次尝试", Toast.LENGTH_SHORT).show()
                }
                throw CancellationException()
            }
        }
    }
}