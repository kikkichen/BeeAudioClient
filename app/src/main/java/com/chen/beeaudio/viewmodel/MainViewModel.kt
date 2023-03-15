package com.chen.beeaudio.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.mock.*
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.audio.Premium
import com.chen.beeaudio.model.audio.TrackWithUrl
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.model.localmodel.Subscribe
import com.chen.beeaudio.model.localmodel.TrackSummary
import com.chen.beeaudio.net.AuthApi
import com.chen.beeaudio.repository.SubscribeRepository
import com.chen.beeaudio.repository.TrackSummaryRepository
import com.chen.beeaudio.repository.database.SubscribeDatabase
import com.chen.beeaudio.repository.database.TrackSummaryDatabase
import com.chen.beeaudio.server.AudioPlayerService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import snow.player.PlayMode
import snow.player.PlaybackState
import snow.player.Player
import snow.player.PlayerClient
import snow.player.SleepTimer
import snow.player.audio.MusicItem
import snow.player.playlist.Playlist
import snow.player.util.LiveProgress
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    @Named("LocalServer")
    private val localApi: LocalApi,
    @Named("AuthServer")
    private val authApi: AuthApi,
    private val repository: DataStoreRepository,
    private val trackSummaryRepository: TrackSummaryRepository,
    private val subscribeRepository: SubscribeRepository,
    private val subscribeDatabase: SubscribeDatabase,
    private val trackSummaryDatabase: TrackSummaryDatabase,
) : ViewModel() {
    /* DataStore - protobuf repository */
    private val _accessToken : MutableStateFlow<String> = MutableStateFlow("")
    val accessToken = _accessToken.asStateFlow()

    private val _refreshToken: MutableStateFlow<String> = MutableStateFlow("")
    val refreshToken = _refreshToken.asStateFlow()

    /* token时效状态 */
    val tokenAgingState = MutableStateFlow<TokenAging>(TokenAging.NONE)
    /* 用户ID */
    var currentUserId : Long = 0.toLong()

    /* Premium 用户标识 */
    val premiumUserInfo : MutableStateFlow<Premium> = MutableStateFlow(Premium("", 0, "", "", 0))

    /* 用户信息 */
    val currentUserDetailInfo = MutableStateFlow(RequestUserDetail("","","","","","","",0.toLong(),0))

    /* 我的歌单 - beta */
    val myCollectionPlayList : MutableList<PlayList> = PlayListCollectionMock1.toMutableList()
    /* 当前用户的默认收藏歌单ID */
    var myFavoritePlaylistId : Long = 0

    /* 播放器客户端 */
    val playerClient = PlayerClient.newInstance(context, AudioPlayerService::class.java)

    /* 当前播放器是否处于播放状态 */
    val isPlayingState : MutableStateFlow<Boolean> = MutableStateFlow(false)

    /* 播放模式 */
    val playingMode : MutableStateFlow<PlayMode> = MutableStateFlow(PlayMode.PLAYLIST_LOOP)

    /* 当前播放曲目是否为我默认喜爱歌单收录的曲目 */
    val isMyFavoriteTrack : MutableStateFlow<Boolean> = MutableStateFlow(false)

    /* 当前播放曲目及其列表位置、播放进度信息 */
    val currentPlayingMusicItem : MutableStateFlow<MusicItem?> = MutableStateFlow(MusicItem())
    val currentPlayingPosition : MutableStateFlow<Int> = MutableStateFlow(0)
    val currentPlayingDuration : MutableStateFlow<Int> = MutableStateFlow(0)
    val currentPlayingProgress : MutableStateFlow<Int> = MutableStateFlow(0)
    val currentPlayingFormatDuration : MutableStateFlow<String> = MutableStateFlow("00:00")
    val currentPlayingFormatProgress : MutableStateFlow<String> = MutableStateFlow("00:00")

    /* 缓冲状态 */
    val isCacheState : MutableStateFlow<Boolean> = MutableStateFlow(false)

    /* 播放进度 */
    private val liveProgress = LiveProgress(playerClient) { progressSec, durationSec, textProgress, textDuration ->
        currentPlayingProgress.value = progressSec
        currentPlayingDuration.value = durationSec
        currentPlayingFormatProgress.value = textProgress
        currentPlayingFormatDuration.value = textDuration
    }

    /* 当前播放列表 */
    val currentPlaylist : MutableStateFlow<MutableList<MusicItem>> = MutableStateFlow(mutableListOf())

    init {
        /* 加载token状态 */
        loadTokenData()
    }

    fun mainInit(context: Context) {
        viewModelScope.launch {
            val audioTask = async(Dispatchers.IO) {
                /* 更新用户音频订阅项目数据 */
                if (currentUserId != 0.toLong()) {
                    /* 从服务器获取当前用户的音频订阅数据 */
                    try {
                        val updateSubscribeResponse = localApi.getMySubscribeData(uid = currentUserId)
                        /* 验证数据有效 */
                        if (updateSubscribeResponse.ok == 1 && updateSubscribeResponse.data.uid == currentUserId) {
                            /* 解析Json数据 */
                            val updateSubscribeDataList = Gson().fromJson<List<Subscribe>>(updateSubscribeResponse.data.subscribeData, object : TypeToken<List<Subscribe>>() {}.type)
                            /* 将解析数据更新到本地数据库 */
                            if (updateSubscribeDataList.isNotEmpty()) {
                                subscribeDatabase.subscribeDao().clearAllSubscribeData()
                                subscribeDatabase.subscribeDao().insertAllSubscribeData(updateSubscribeDataList)
                                Log.d("_chen", "订阅数据更新成功")
                            } else {
                                Log.d("_chen", "订阅数据更新有误")
                                /* 网络等意外因素造成的订阅数据初始化失败，不进行数据库订阅数据清空 */
//                                subscribeDatabase.subscribeDao().clearAllSubscribeData()
                            }
                        } else {
                            launch(Dispatchers.Main) {
                                Toast.makeText(context, "订阅数据同步有误，将在稍后跟新", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e : Throwable) {
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "同步过程出现了些许错误，稍后将重试～", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                        throw CancellationException()
                    }
                }
                /* 获取当前用户基本信息 */
                launch {
                    loadCurrentUserDetail()
                }
                /* 初始化请求当前用户默认歌单ID */
                launch {
                    loadCurrentUserFavoritePlayListId()
                }
                /* 当前用户的会员状态 */
                launch {
                    loadIsPremiumTag(currentUserId)
                }
                /* 更新自建歌单的曲目列表 */
                val myCreatedPlayList = subscribeDatabase.subscribeDao().getAllMyCreatedPlayList()
                if (myCreatedPlayList.isNotEmpty()) {
                    try {
                        for (playlist in myCreatedPlayList) {
                            launch {
                                /* 获取歌单信息，以保证获取歌单完整的曲目ID列表 */
                                val playlistInfo = localApi.getPlayListDetail(playListId = playlist.itemId).data
                                if (!playlistInfo.trackIds.isNullOrEmpty()) {
                                    /* 清空原有数据 */
                                    trackSummaryDatabase.trackSummaryDao().deleteTrackSummaryWithPlaylist(playlistId = playlistInfo.id)
                                    /* 将曲目ID转换为曲目概要数据模型注入数据库 */
                                    val tempTrackSummaryList : MutableList<TrackSummary> = mutableListOf()
                                    for (trackId in playlistInfo.trackIds) {
                                        tempTrackSummaryList.add(TrackSummary(songId = trackId.id, playlistId = playlistInfo.id))
                                    }
                                    trackSummaryDatabase.trackSummaryDao().addMultipleTrackSummary(tempTrackSummaryList)
                                }
                            }
                        }
                    } catch (e : Throwable) {
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "同步过程出现了些许错误，稍后将重试～", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                        throw CancellationException()
                    }
                }
            }

            audioTask.await()
            Log.d("_chen", "audio task 结束")
            val playerListenerTask = async {
                /* 播放器客户端初始化 */
                playerClient.connect { success ->
                    if (success) {
                        /* 播放状态监听 */
                        playerClient.addOnPlayingMusicItemChangeListener { musicItem, position, _ ->
                            currentPlayingMusicItem.value = musicItem
                            currentPlayingPosition.value = position
                            Log.d("_chen", "当前播放: 《${musicItem?.title} - ${musicItem?.artist}》")
                            /* 添加历史记录 */
                            viewModelScope.launch {
                                val result = musicItem?.musicId?.toLong()
                                    ?.let { localApi.addMyHistoryItem(currentUserId, it) }
                                if (result != null) {
                                    if (!result.data) {
                                        Toast.makeText(context, "历史记录未完成更新", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "网络因素 ,历史记录未完成更新", Toast.LENGTH_SHORT).show()
                                }
                            }
                            viewModelScope.launch(Dispatchers.IO) {
                                /* 更新 当前曲目是否为我“喜欢”的曲目 标识 */
                                loadCurrentTrackIsMyLike()
                            }
                            /* 更新相关订阅条目的权重 */
                            viewModelScope.launch(Dispatchers.IO) {
                                updateSubscribeItemWeight(trackId = musicItem?.musicId?.toLong() ?: SingleTrackMock.id)
                            }
                        }
                        /* 播放器状态监听 - 细粒度 */
                        playerClient.addOnPlaybackStateChangeListener { playbackState, stalled ->
                            isPlayingState.value = playbackState == PlaybackState.PLAYING
                            isCacheState.value = stalled
                        }
                        /* 监听播放器正在播放的列表 */
                        playerClient.addOnPlaylistChangeListener { playlistManager, position ->
                            /* 更新播放列表 */
                            playlistManager.getPlaylist { playlist ->
                                currentPlaylist.value.clear()
                                playlist.forEach { musicItem ->
//                                    Log.d("_chen", "《${musicItem.title}》 - ${musicItem.artist} ")
                                    currentPlaylist.value.add(musicItem)
                                }
                            }
                            /* 更新列表播放位置信息 */
                            currentPlayingPosition.value = position
                        }

                        playerClient.addOnPlayModeChangeListener { playMode ->
                            /* 更新播放模式信息， 默认为列表循环 */
                            playingMode.value = playMode ?: PlayMode.PLAYLIST_LOOP
                        }

                        /* 监听播放器播放进度调整完成事件 */
//                playerClient.addOnSeekCompleteListener { progress, updateTime, stalled ->
//                    TODO("Not yet implemented")
//                }

                        liveProgress.subscribe()
                        initRequestPlayList()
                    }
                }
            }
            playerListenerTask.await()
            Log.d("_chen", "player listener task 结束")
            tokenAgingState.value = TokenAging.FINISHED
        }
    }

    /** 加载用户Premium会员状态
     *  @param  userId
     */
    suspend fun loadIsPremiumTag(userId : Long) {
        try {
            premiumUserInfo.value = localApi.getIsPremium(userId = userId).data
        } catch (e : Throwable) {
            e.printStackTrace()
            throw CancellationException()
        }
    }

    /** 更新 当前曲目是否为我“喜欢”的曲目 标识
     *
     */
    suspend fun loadCurrentTrackIsMyLike() {
        trackSummaryRepository.updateCurrentTrackIsMyFavoriteSignal(
            songId = currentPlayingMusicItem.value?.musicId?.toLong() ?: SingleTrackMock.id,
            onMyFavoriteTrackStateChangeEvent = { summaryList ->
                isMyFavoriteTrack.value = summaryList.map { it.playlistId }.contains(myFavoritePlaylistId)
            }
        )
    }

    /** 更新数据库中音频订阅项目的权重
     *  @param  trackId 当前播放曲目ID
     */
    private suspend fun updateSubscribeItemWeight(trackId : Long) {
        try {
            val trackDetail = localApi.getTrackDetail(trackIds = trackId.toString()).data.first()
            subscribeRepository.IncreasingSubscribeItemWeight(track = trackDetail)
        } catch (e : Throwable) {
            e.printStackTrace()
            throw CancellationException()
        }
    }

    /** 处理收藏曲目的默认收藏事务
     *  @param  context 上下文对象参数
     */
    fun dealWithTrackCollect(context: Context) {
        /* 处理曲目收藏事务 */
        viewModelScope.launch(Dispatchers.IO) {
            trackSummaryRepository.dealWithCollectMyFavoritePlayList(
                songId = currentPlayingMusicItem.value?.musicId?.toLong() ?: SingleTrackMock.id,
                currentUserId = currentUserId,
                myFavoritePlaylistId = myFavoritePlaylistId,
                toastEvent = {
                    launch(Dispatchers.Main) { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                }
            )
            /* 更新 当前曲目是否为我“喜欢”的曲目 标识 */
            trackSummaryRepository.updateCurrentTrackIsMyFavoriteSignal(
                songId = currentPlayingMusicItem.value?.musicId?.toLong() ?: SingleTrackMock.id,
                onMyFavoriteTrackStateChangeEvent = { summaryList ->
                    isMyFavoriteTrack.value = summaryList.map { it.playlistId }.contains(myFavoritePlaylistId)
                }
            )
        }
    }

    /* 获取当前用户默认喜爱歌单ID */
    private suspend fun loadCurrentUserFavoritePlayListId() {
        myFavoritePlaylistId = try {
            localApi.getUserFavoritePlayListId(currentUserId).data
        } catch (e : Throwable) {
            PlayListCollectionMock1[0].id
            throw CancellationException()
        }
    }

    /** 请求当前用户的简易信息
     *
     */
    suspend fun loadCurrentUserDetail() {
        try {
            val result = localApi.getUserDetail(currentUserId)
            if (result.ok == 1) {
                currentUserDetailInfo.value = result.data
            } else {
                delay(500)
                val result2 = localApi.getUserDetail(currentUserId)
                if (result2.ok == 1) {
                    delay(500)
                    currentUserDetailInfo.value = result2.data
                } else {
                    currentUserDetailInfo.value = localApi.getUserDetail(currentUserId).data
                }
            }
        } catch (e : Throwable) {
            throw CancellationException()
        }
    }

    /* 初始化重新获取曲目Url及其信息 */
    private fun initRequestPlayList() {
        /* 上一次播放的曲目 MusicItem对象信息 */
        val playMusicItem = playerClient.playingMusicItem
        /* 播放位置 */
        var playPosition = 0
        /* 获取播放列表 MusicItem 信息 */
        playerClient.getPlaylist { playlist ->
            if (playlist.size() != 0) {
                val trackIds = playlist.map { it.musicId }
                viewModelScope.launch {
                    try {
                        val tracks = localApi.getTrackDetail(trackIds = trackIds.toString().replace(" ","").replace("[","").replace("]","")).data
                        val trackFiles = localApi.getTracksUrlInfo(trackIds = trackIds.toString().replace(" ","").replace("[","").replace("]","")).data
                        val trackWithUrls : MutableList<TrackWithUrl> = mutableListOf()

                        /* 歌单有效歌曲添加，按照计划，曲目列表与曲目url列表数量一致，且曲目信息按位置一一对应 */
                        try {
                            tracks.forEachIndexed { index, track ->
                                for (trackFile in trackFiles) {
                                    if (track.id == trackFile.id) {
                                        trackWithUrls.add(track.mapToWithUrl(trackFile))
                                        /* 若发现上一次播放程序的曲目，则更新其新的位置信息 */
                                        if (playMusicItem != null && playMusicItem.musicId == track.id.toString()) {
                                            playPosition = index
                                        }
                                        break
                                    }
                                }
                            }
                        } catch (e : IndexOutOfBoundsException) {
                            /* 越界异常捕获 */
                            e.printStackTrace()
                        }

                        val newPlayList = Playlist.Builder()
                        trackWithUrls.forEachIndexed { index, trackWithUrl ->
                            val tempSong : MusicItem = MusicItem.Builder().apply {
                                setTitle(trackWithUrl.name)
                                setArtist(trackWithUrl.ar?.map { it.name }.toString().replace("[", "").replace("]",""))
                                trackWithUrl.al?.let {
                                    setAlbum(it.name)
                                }
                                setDuration(trackWithUrl.dt?.toInt() ?: tracks[index].dt.toInt())
                                setUri(trackWithUrl.url)
                                trackWithUrl.al?.picUrl?.let { setIconUri(it) }
                                setMusicId(trackWithUrl.id.toString())
                            }.build()
                            newPlayList.append(tempSong)
                        }

                        playerClient.setPlaylist(newPlayList.build(), playPosition, false)
                    } catch (e : Throwable) {
                        e.printStackTrace()
                        throw CancellationException()
                    }
                }
            }
        }
    }

    /* 播放当前选择的曲目 */
    fun playTargetAudio(track: Track, context: Context) {
        viewModelScope.launch {
            try {
                /* 请求目的曲目的Url以及详细文件信息 */
                val trackFileResult = localApi.getTracksUrlInfo(track.id.toString()).data[0]
                val trackWithUrl = track.mapToWithUrl(trackFileResult)
                /* 构筑当前选择曲目的 MusicItem */
                val tempSong : MusicItem = MusicItem.Builder().apply {
                    setTitle(trackWithUrl.name)
                    setArtist(trackWithUrl.ar?.map { it.name }.toString().replace("[", "").replace("]",""))
                    trackWithUrl.al?.let {
                        setAlbum(it.name)
                    }
                    setDuration(track.dt.toInt())
                    setUri(trackWithUrl.url)
                    track.al.picUrl?.let {
                        setIconUri(it)
                    }
                    setMusicId(trackWithUrl.id.toString())
                }.build()

                /* 检查列表中是否存在这首曲目，当目标曲目加入到播放列表，并取代下一首曲目的位置时，立即播放下一首 */
                playerClient.getPlaylist{ playlist ->
                    /* 判断列表是否为空， 若为空则需要创建新的播放列表 */
                    if (playlist.size() == 0) {
                        val newPlayList = Playlist.Builder().append(tempSong).build()
                        playerClient.setPlaylist(newPlayList, true)
                    } else {
                        /* 判断播放列表中是否已经存在该条目 */
                        val musicItemIdList = currentPlaylist.value.map { it.musicId }
                        if (musicItemIdList.contains(track.id.toString())) {
                            /* 若曲目中已经存在该曲目，则立即播放该曲目 */
                            val originalPosition = musicItemIdList.indexOf(track.id.toString())
                            val targetMusicItem = currentPlaylist.value[originalPosition]
                            playerClient.removeMusicItem(targetMusicItem)
                        }
                        /* 设置下一首播放的曲目为当前选择曲目 */
                        playerClient.setNextPlay(tempSong)
                        /* 当指定下一首曲目时， MusicItem数据并不会插入到下一首的位置，此时需要等待下一首的顺利插入， 然后执行立即播放的逻辑 */
                        viewModelScope.launch(Dispatchers.Default) {
                            /* 总之时间等待的坑在这里 */
                            delay(100)
                            /* 立即播放下一首 */
                            playerClient.skipToNext()
                        }
                    }
                }
            } catch (e : Throwable) {
                if (e is FileNotFoundException) {
                    Toast.makeText(context, "该曲目文件解析有误，即将为您播放下一曲",Toast.LENGTH_SHORT).show()
                    delay(1000)
                    playerClient.skipToNext()
                    throw CancellationException()
                } else {
                    throw CancellationException()
                }
            }
        }
    }

    /**
     *  清空除当前播放音频之外的播放列表内容
     */
    fun clearPlayList() {
        playerClient.getPlaylist { playlist ->
            playlist.forEach { musicItem ->
                if (musicItem != currentPlayingMusicItem.value) {
                    playerClient.removeMusicItem(musicItem)
                }
            }
        }
    }

    /** 立即播放 当前播放列表中指定位置的曲目
     *  @param  position    曲目在当前播放列表中的指定位置
     */
    fun playTargetPositionMusic(position: Int) {
        playerClient.skipToPosition(position)
    }

    /** 从播放列表中移除该曲目
     *  @param  musicItem   移除目标曲目的 musicItem 对象
     *  @param  context     上下文对象参数
     */
    fun removeTargetMusic(musicItem: MusicItem, context: Context) {
        if (currentPlayingMusicItem.value == musicItem) {
            Toast.makeText(context, "正在播放的曲目不能移除哦！",Toast.LENGTH_SHORT).show()
        } else {
            playerClient.removeMusicItem(musicItem)
        }
    }

    /** 顺序播放新的歌单
     *  @param  trackIds    歌单曲目ID列表
     */
    fun startPlayListPlaying(trackIds: List<Long>) : Boolean {
        /* 判断现在是否为当前播放歌单 */
        if (currentPlaylist.value.size == trackIds.size) {
            for (index in trackIds.indices) {
                if (trackIds[index] != currentPlaylist.value[index].musicId.toLong()) break
                else if (index == trackIds.size - 1) return false
            }
        }
        /* 切换歌单 重歌单队列首开始播放 */
        viewModelScope.launch (Dispatchers.IO) {
            /* 上一次播放的曲目 MusicItem对象信息 */
            val playMusicItem = playerClient.playingMusicItem
            /* 播放位置 */
            var playPosition = 0
            /* 获取歌单全部曲目Url */
            try {
                val tracks = localApi.getTrackDetail(trackIds = trackIds.toString().replace(" ","").replace("[","").replace("]","")).data
                val trackFiles = localApi.getTracksUrlInfo(trackIds = trackIds.toString().replace(" ","").replace("[","").replace("]","")).data
                val trackWithUrls : MutableList<TrackWithUrl> = mutableListOf()

                /* 歌单有效歌曲添加，按照计划，曲目列表与曲目url列表数量一致，且曲目信息按位置一一对应 */
                try {
                    tracks.forEachIndexed { index, track ->
                        for (trackFile in trackFiles) {
                            if (track.id == trackFile.id) {
                                trackWithUrls.add(track.mapToWithUrl(trackFile))
                                /* 若发现上一次播放程序的曲目，则更新其新的位置信息 */
                                if (playMusicItem != null && playMusicItem.musicId == track.id.toString()) {
                                    playPosition = index
                                }
                                break
                            }
                        }
                    }
                } catch (e : IndexOutOfBoundsException) {
                    /* 越界异常捕获 */
                    e.printStackTrace()
                }

                val newPlayList = Playlist.Builder()
                trackWithUrls.forEachIndexed { index, trackWithUrl ->
                    val tempSong : MusicItem = MusicItem.Builder().apply {
                        setTitle(trackWithUrl.name)
                        setArtist(trackWithUrl.ar?.map { it.name }.toString().replace("[", "").replace("]",""))
                        trackWithUrl.al?.let {
                            setAlbum(it.name)
                        }
                        setDuration(trackWithUrl.dt?.toInt() ?: tracks[index].dt.toInt())
                        setUri(trackWithUrl.url)
                        trackWithUrl.al?.picUrl?.let { setIconUri(it) }
                        setMusicId(trackWithUrl.id.toString())
                    }.build()
                    newPlayList.append(tempSong)
                }

                playerClient.setPlaylist(newPlayList.build(), playPosition, true)
            } catch (e : Throwable) {
                e.printStackTrace()
                throw CancellationException()
            }
        }
        return true
    }

    /** 在播放列表的尾部追加曲目
     *  @param  track   添加目标曲目的 track 对象
     *  @param  context     上下文对象参数
     */
    fun appendTargetMusic(track: Track, context: Context) {
        if (currentPlaylist.value.map { it.musicId }.contains(track.id.toString())) {
            Toast.makeText(context, "播放列表中已经存在该曲目了哦！", Toast.LENGTH_SHORT).show()
        } else {
            try {
                viewModelScope.launch {
                    /* 请求目的曲目的Url以及详细文件信息 */
                    val trackFileResult = localApi.getTracksUrlInfo(track.id.toString()).data[0]
                    val trackWithUrl = track.mapToWithUrl(trackFileResult)
                    /* 构筑当前选择曲目的 MusicItem */
                    val tempSong : MusicItem = MusicItem.Builder().apply {
                        setTitle(trackWithUrl.name)
                        setArtist(trackWithUrl.ar?.map { it.name }.toString().replace("[", "").replace("]",""))
                        trackWithUrl.al?.let {
                            setAlbum(it.name)
                        }
                        setDuration(track.dt.toInt())
                        setUri(trackWithUrl.url)
                        track.al.picUrl?.let {
                            setIconUri(it)
                        }
                        setMusicId(trackWithUrl.id.toString())
                    }.build()
                    playerClient.appendMusicItem(tempSong)
                }
            } catch (e : Throwable) {
                Toast.makeText(context, "曲目加入队列出错，请稍后重试", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    /** 设置目标位置的曲目为下一首播放曲目
     *  @param  musicItem   添加目标曲目的 musicItem 对象
     *  @param  context     上下文对象参数
     */
    fun moveToNextPlayMusic(musicItem: MusicItem, context: Context) {
        val originalPosition = currentPlaylist.value.indexOf(musicItem)
        if (originalPosition == currentPlayingPosition.value + 1) {
            Toast.makeText(context, "别急，下一首就是啦～", Toast.LENGTH_SHORT).show()
        } else {
            playerClient.moveMusicItem(originalPosition, currentPlayingPosition.value + 1)
        }
    }


    /* 播放、暂停 */
    fun playOrPauseAction() {
        playerClient.playPause()
    }

    /* 播放下一曲 */
    fun playNextMusic() {
        playerClient.skipToNext()
    }

    /* 播放上一曲 */
    fun playPreviousMusic() {
        playerClient.skipToPrevious()
    }

    /* 调节播放位置 */
    fun seekToTargetPosition(position : Int) {
        playerClient.seekTo(position)
    }

    /* 设置播放模式 */
    fun setPlayModel(playMode : PlayMode) {
        playerClient.playMode = playMode
    }

    /* 定时播放设置 */
    fun setStartPlaySleepTime(long: Long) {
        playerClient.startSleepTimer(long, SleepTimer.TimeoutAction.PAUSE)
    }

    fun setCancelPlaySleepTime() {
        playerClient.cancelSleepTimer()
    }

    /* 读取protobuf中的 token 信息 */
    fun loadTokenData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.tokenModel.collect { tokenModel ->
                _accessToken.value = tokenModel.accessToken
                _refreshToken.value = tokenModel.refreshToken
            }
        }
    }

    /* 向服务端验证Token时效信息 */
    fun requestTokenAgingInfo(context: Context, token : String, invalidEvent: () -> Unit) {
        viewModelScope.launch {
            val result = localApi.verifyAccountToken(token)
            if (result.code == 200) {
                tokenAgingState.value = TokenAging.USEFUL(result.data)
                currentUserId = result.data
                /* 初始化全局信息 */
                mainInit(context = context)
            } else {
                tokenAgingState.value = TokenAging.INVALID
                clearSubscribeAndTrackDatabase()
                launch(Dispatchers.Main) {
                    invalidEvent()
                }
            }
        }
    }

    /* 刷新Token */
    fun refreshToken(context: Context, refreshToken : String) {
        viewModelScope.launch {
            val result = authApi.refreshToken(refreshToken = refreshToken)
            if (result.refreshToken.isNotEmpty()) {
                /* 若数据刷新有效 */
                repository.saveData(
                    accessToken = result.accessToken,
                    expiresIn = result.expiresIn.toLong(),
                    refreshToken = result.refreshToken,
                    scope = result.scope,
                    tokenType = result.tokenType
                )
                /* 再次向服务端验证Token时效信息 */
                requestTokenAgingInfo(context = context, token = _accessToken.value) { /* empty */ }
            } else {
                /* 若刷新后无效 */
                clearSubscribeAndTrackDatabase()
                repository.saveData(
                    accessToken = "",
                    expiresIn = 0,
                    refreshToken = "",
                    scope = "all",
                    tokenType = "Bearer"
                )
            }
        }
    }

    /* 清除token记录 - 退出登陆场景 */
    fun clearToken() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveData(
                accessToken = "",
                expiresIn = 0,
                refreshToken = "",
                scope = "all",
                tokenType = "Bearer"
            )
        }
    }

    /* 清楚数据库中的曲目内容与订阅数据 */
    fun clearSubscribeAndTrackDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            subscribeRepository.clearAllData()
            trackSummaryDatabase.trackSummaryDao().clearAllTrackSummary()
        }
    }
}

/* token 时效状态 */
sealed class TokenAging() {
    object NONE : TokenAging()
    object INVALID : TokenAging()
    class USEFUL(val userId : Long) : TokenAging()
    object FINISHED : TokenAging()
}