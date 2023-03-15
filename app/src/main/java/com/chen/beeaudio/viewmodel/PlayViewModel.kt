package com.chen.beeaudio.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.ImageLoader
import com.chen.beeaudio.mock.SingleTrackFileMock
import com.chen.beeaudio.mock.SingleTrackMock
import com.chen.beeaudio.navigation.AudioHomeRoute
import com.chen.beeaudio.net.LocalApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class PlayViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi,
) : ViewModel() {
    /* 当前处理音频信息 */
    val currentTrack = MutableStateFlow(SingleTrackMock)
    val currentTrackFile = MutableStateFlow(SingleTrackFileMock)

    /* 当前播放曲目ID */
    var currentTrackId : Long = 0
    /* 依据当前曲目ID获取曲目详细信息 */
    fun loadCurrentTrackDetailInfo() {
        /* TODO: 修改为从MusicItem获取的逻辑 */
        viewModelScope.launch {
            try {
                currentTrack.value = localApi.getTrackDetail(currentTrackId.toString()).data[0]
                currentTrackFile.value = localApi.getTracksUrlInfo(currentTrackId.toString()).data[0]
            } catch (e : Throwable) {
                e.printStackTrace()
                Log.d("_chen", "重新进行请求")
                try {
                    currentTrack.value = localApi.getTrackDetail(currentTrackId.toString()).data[0]
                    currentTrackFile.value = localApi.getTracksUrlInfo(currentTrackId.toString()).data[0]
                } catch (e : Throwable) {
                    e.printStackTrace()
                    Log.d("_chen", "重新进行请求")
                    try {
                        currentTrack.value = localApi.getTrackDetail(currentTrackId.toString()).data[0]
                        currentTrackFile.value = localApi.getTracksUrlInfo(currentTrackId.toString()).data[0]
                    } catch (e : Throwable) {
                        e.printStackTrace()
                        Log.d("_chen", "请求超时")
                    }
                }
            }
        }
    }

    /* 毫秒时间转换为格式化的时分字符串 */
    private fun formatLong(value: Long): String {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        return dateFormat.format(value)
    }

    fun openArtistPage(navController: NavController) {
        navController.popBackStack()
        navController.navigate(
            route = AudioHomeRoute.ArtistScreen.route + "?artist_id=${currentTrack.value.ar[0].id}"
        )
    }
}