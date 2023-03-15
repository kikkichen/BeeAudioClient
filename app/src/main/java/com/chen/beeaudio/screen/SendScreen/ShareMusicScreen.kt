package com.chen.beeaudio.screen.SendScreen

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import com.chen.beeaudio.R
import com.chen.beeaudio.mock.SingleAlbum
import com.chen.beeaudio.mock.SingleArtistMock
import com.chen.beeaudio.mock.SinglePlayListData
import com.chen.beeaudio.mock.SingleTrackMock
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.audio.Artist
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.navigation.AudioHomeRoute
import com.chen.beeaudio.screen.widget.PremiumLogo
import com.chen.beeaudio.viewmodel.MainViewModel
import com.chen.beeaudio.viewmodel.ShareMusicViewModel
import com.google.gson.Gson

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun ShareMusicScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    shareMusicViewModel: ShareMusicViewModel = hiltViewModel()
) {
    /* 上下文对象 */
    val context : Context = LocalContext.current
    /* 搜索关键字字段 */
    val searchKeywordState = shareMusicViewModel.currentKeyWords.collectAsState()

    /* 搜索结果 */
    val songListState = shareMusicViewModel.resultSongs.collectAsState()
    val albumListState = shareMusicViewModel.resultAlbums.collectAsState()
    val artistListState = shareMusicViewModel.resultArtists.collectAsState()
    val playlistListState = shareMusicViewModel.resultPlayList.collectAsState()

    /* 我的默认喜爱曲目 */
    val likeSongList = shareMusicViewModel.myFavoriteList.collectAsState()

    /* 当前项目选择状态 */
    val (currentSelectedId, changeCurrentSelectedId) = remember {
        mutableStateOf(0.toLong())
    }

    /* 当前项目类型选择状态 */
    val (currentSelectedType, changeCurrentSelectedType) = remember {
        mutableStateOf(0)
    }

    /* 当前选择项目JSON数据 */
    val (currentSelectedJSON, changeCurrentSelectedJSON) = remember {
        mutableStateOf("")
    }

    /* 搜索栏折叠状态 */
    var searchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                backgroundColor = MaterialTheme.colors.surface,
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Backup"
                        )
                    }
                },
                title = {
                    AnimatedVisibility(
                        visible = !searchExpanded
                    ) {
                        Text(text = "分享音乐")
                    }
                    KeywordSearchBar(
                        isSearchExpanded = searchExpanded,
                        searchKeyword = searchKeywordState.value,
                        changeSearchKeyword = { newWord -> shareMusicViewModel.changeCurrentKeyWords(newWord) },
                        onCloseExpanded = { searchExpanded = false },
                        onSearchActionEvent = {
                            shareMusicViewModel.changeCurrentKeyWords(newWords = searchKeywordState.value)
                        }
                    )
                },
                actions = {
                    /* 搜索按钮 */
                    IconButton(
                        onClick = {
                            if (!searchExpanded) {
                                searchExpanded = true
                            } else {
                                shareMusicViewModel.changeCurrentKeyWords(newWords = searchKeywordState.value)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    }
                    AnimatedVisibility(
                        visible = currentSelectedId > 0,
                        enter = fadeIn(tween(400)),
                        exit = fadeOut(tween(400))
                    ) {
                        Button(
                            onClick = {
                                navController
                                    .previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set(
                                        key = "share_item",
                                        value = when (currentSelectedType) {
                                            1 -> {
                                                "[share_track]$currentSelectedJSON"
                                            }
                                            10 -> {
                                                "[share_album]$currentSelectedJSON"
                                            }
                                            100 -> {
                                                "[share_artist]$currentSelectedJSON"
                                            }
                                            1000 -> {
                                                "[share_playlist]$currentSelectedJSON"
                                            }
                                            else -> {
                                                null
                                            }
                                        }
                                    )
                                navController.navigateUp()
                            },  // 将选择项目返回发送页
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(text = "分享")
                        }
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(paddingValues = it)
        ) {
            try {
                if (searchKeywordState.value.isEmpty()) {
                    stickyHeader {
                        Text(
                            text = "我最近喜欢",
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colors.background)
                                .padding(horizontal = 12.dp)
                                .padding(top = 6.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colors.primary,
                        )
                    }
                    items(
                        items = likeSongList.value,
                        key = { track -> track.id }
                    ) { track ->
                        MusicSelected(
                            track = track,
                            isSelected = currentSelectedId == track.id,
                            onSelectEvent = {
                                if (currentSelectedId != track.id) {
                                    changeCurrentSelectedId(track.id)
                                    changeCurrentSelectedType(1)
                                    changeCurrentSelectedJSON(Gson().toJson(track))
                                } else {
                                    changeCurrentSelectedId(0.toLong())
                                    changeCurrentSelectedType(0)
                                    changeCurrentSelectedJSON("")
                                }
                            },
                            onPlayEvent = {
                                mainViewModel.playTargetAudio(track = track, context = context)
                            }
                        )
                    }
                } else {
                    if (songListState.value.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                text = "曲目",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = MaterialTheme.colors.background)
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 6.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colors.primary
                            )
                        }
                        items(
                            items = songListState.value.subList(0, 5),
                            key = { track -> track.id }
                        ) { track ->
                            MusicSelected(
                                track = track,
                                isSelected = currentSelectedId == track.id,
                                onSelectEvent = {
                                    if (currentSelectedId != track.id) {
                                        changeCurrentSelectedId(track.id)
                                        changeCurrentSelectedType(1)
                                        changeCurrentSelectedJSON(Gson().toJson(track))
                                    } else {
                                        changeCurrentSelectedId(0.toLong())
                                        changeCurrentSelectedType(0)
                                        changeCurrentSelectedJSON("")
                                    }
                                },
                                onPlayEvent = {
                                    mainViewModel.playTargetAudio(track = track, context = context)
                                }
                            )
                        }
                    }
                    if (albumListState.value.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                text = "专辑",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = MaterialTheme.colors.background)
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 6.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colors.primary
                            )
                        }
                        items(
                            items = albumListState.value.subList(0, 5),
                            key = { album -> album.id }
                        ) { album ->
                            AlbumSelected(
                                album = album,
                                isSelected = currentSelectedId == album.id,
                                imageLoader = shareMusicViewModel.myImageLoader,
                                onSelectEvent = {
                                    if (currentSelectedId != album.id) {
                                        changeCurrentSelectedId(album.id)
                                        changeCurrentSelectedType(10)
                                        changeCurrentSelectedJSON(Gson().toJson(album))
                                    } else {
                                        changeCurrentSelectedId(0.toLong())
                                        changeCurrentSelectedType(0)
                                        changeCurrentSelectedJSON("")
                                    }
                                },
                                onOpenAlbumPage = {
                                    navController.navigate(route = AudioHomeRoute.AlbumScreen.route + "?album_id=${album.id}")
                                }
                            )
                        }
                    }
                    if (artistListState.value.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                text = "艺人",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = MaterialTheme.colors.background)
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 6.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colors.primary
                            )
                        }
                        items(
                            items = artistListState.value.subList(0, 5),
                            key = { artist -> artist.id }
                        ) { artist ->
                            ArtistSelected(
                                isSelected = currentSelectedId == artist.id,
                                artist = artist,
                                imageLoader = shareMusicViewModel.myImageLoader,
                                onSelectEvent = {
                                    if (currentSelectedId != artist.id) {
                                        changeCurrentSelectedId(artist.id)
                                        changeCurrentSelectedType(100)
                                        changeCurrentSelectedJSON(Gson().toJson(artist))
                                    } else {
                                        changeCurrentSelectedId(0.toLong())
                                        changeCurrentSelectedType(0)
                                        changeCurrentSelectedJSON("")
                                    }
                                },
                                onOpenArtistPage = {
                                    navController.navigate(route = AudioHomeRoute.ArtistScreen.route + "?artist_id=${artist.id}")
                                }
                            )
                        }
                    }
                    if (playlistListState.value.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                text = "歌单",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = MaterialTheme.colors.background)
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 6.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colors.primary
                            )
                        }
                        items(
                            items = playlistListState.value.subList(0, 5),
                            key = { playlist -> playlist.id }
                        ) { playlist ->
                            PlaylistSelected(
                                isSelected = currentSelectedId == playlist.id,
                                playlist = playlist,
                                imageLoader = shareMusicViewModel.myImageLoader,
                                onSelectEvent = {
                                    if (currentSelectedId != playlist.id) {
                                        changeCurrentSelectedId(playlist.id)
                                        changeCurrentSelectedType(1000)
                                        changeCurrentSelectedJSON(Gson().toJson(playlist))
                                    } else {
                                        changeCurrentSelectedId(0.toLong())
                                        changeCurrentSelectedType(0)
                                        changeCurrentSelectedJSON("")
                                    }
                                },
                                onOpenPlaylistPage = {
                                    navController.navigate(route = AudioHomeRoute.PlayListScreen.route + "/${playlist.id}")
                                }
                            )
                        }
                    }
                }
            } catch (e : Throwable) {
                e.printStackTrace()
            }
        }
    }
}

/** 顶栏搜索框
 *  @param  modifier    修饰符参数
 *  @param  isSearchExpanded    搜索栏折叠标识
 *  @param  searchKeyword   搜索关键字
 *  @param  changeSearchKeyword 变更搜索关键字事件
 *  @param  onCloseExpanded     折叠搜索栏
 *  @param  onSearchActionEvent 执行搜索事件
 *
 */
@Composable
fun KeywordSearchBar(
    modifier : Modifier = Modifier,
    isSearchExpanded : Boolean,
    searchKeyword : String,
    changeSearchKeyword : (String) -> Unit,
    onCloseExpanded : () -> Unit,
    onSearchActionEvent : () -> Unit,
) {
    Row (
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = isSearchExpanded,
            modifier = Modifier.weight(1f)
        ) {
            BasicTextField(
                value = searchKeyword,
                onValueChange = { changeSearchKeyword(it) },
                cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        /* 搜索事件触发 */
                        onSearchActionEvent()
                    }
                ),
                textStyle = MaterialTheme.typography.body1.copy(
                    color = MaterialTheme.colors.onSurface
                ),
                decorationBox = { innerTextField ->
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val (trailing, textContent) = createRefs()
                        Box(
                            modifier = Modifier.constrainAs(textContent) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                                end.linkTo(trailing.start)
                                bottom.linkTo(parent.bottom)
                                width = Dimension.fillToConstraints
                            },
                        ) {
                            if (searchKeyword.isEmpty()) Text(
                                "搜索你感兴趣的音乐",
                                style = LocalTextStyle.current.copy(
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            innerTextField()
                        }
                        IconButton(
                            modifier = Modifier
                                .alpha(alpha = ContentAlpha.medium)
                                .constrainAs(trailing) {
                                    top.linkTo(parent.top)
                                    end.linkTo(parent.end)
                                    bottom.linkTo(parent.bottom)
                                }
                            ,
                            onClick = {
                                changeSearchKeyword("")
                                onCloseExpanded()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Icon",
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                    }
                }
            )
        }
    }
}


/** 单曲选择条目
 * @param   modifier    修饰符参数
 *  @param  isSelected  被选择标识
 *  @param  track   音频对象
 *  @param  onPlayEvent  播放事件
 *  @param  onSelectEvent  当前条目被选择事件
 */
@ExperimentalMaterialApi
@Composable
fun MusicSelected(
    modifier : Modifier = Modifier,
    isSelected : Boolean,
    track: Track,
    onSelectEvent: () -> Unit,
    onPlayEvent: () -> Unit,
) {
    val artists : String = track.ar.map { it.name }.toString()
    Card(
        modifier = modifier
            .height(64.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colors.primary) else BorderStroke(0.dp, Color.Transparent),
        elevation = 0.dp,
        onClick = { onSelectEvent() },
        backgroundColor = MaterialTheme.colors.background
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = isSelected
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_select_fill),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(start = 4.dp)
                        .weight(.8f),
                    tint = MaterialTheme.colors.primary
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(5f),
                verticalArrangement = Arrangement.Center
            ) {
                /* 音频名字 */
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.name,
                        style = MaterialTheme.typography.subtitle1.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = if (track.usable) 1f else .5f),      /* 音频可可用性判断， 结果通过颜色深重反馈到界面上 */
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(end = 4.dp)
                    )
                    if (track.privilegeSignal!! >= 1) {
                        PremiumLogo(Modifier.padding(start = 6.dp, top = 4.dp, end= 2.dp, bottom = 6.dp ))
                    }
                }
                /* 音频艺人与专辑信息 */
                Text(
                    text = "${artists.replace("[","").replace("]", "")} - ${track.al.name}",
                    style = MaterialTheme.typography.body2.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 1.dp)
                )
            }
            IconButton(
                onClick = { onPlayEvent() },
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "播放来自 $artists 的 ${track.name}"
                )
            }
        }
    }
}

/** 专辑选择条目
 *  @param  modifier    修饰符参数
 *  @param  isSelected  被选择标识
 *  @param  album   专辑对象
 *  @param  onOpenAlbumPage  播放事件
 *  @param  onSelectEvent  当前条目被选择事件
 */
@ExperimentalMaterialApi
@Composable
fun AlbumSelected(
    modifier : Modifier = Modifier,
    isSelected : Boolean,
    album: Album,
    imageLoader : ImageLoader,
    onSelectEvent: () -> Unit,
    onOpenAlbumPage: () -> Unit,
) {
    Card(
        modifier = modifier
            .height(64.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colors.primary) else BorderStroke(0.dp, Color.Transparent),
        elevation = 0.dp,
        onClick = { onSelectEvent() },
        backgroundColor = MaterialTheme.colors.background
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = isSelected
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_select_fill),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(start = 4.dp)
                        .weight(.8f),
                    tint = MaterialTheme.colors.primary
                )
            }
            AsyncImage(
                model = album.picUrl,
                contentDescription = album.name,
                imageLoader = imageLoader,
                placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder ),
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .padding(start = 8.dp, end = 2.dp)
                    .weight(1f)
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(4f),
                verticalArrangement = Arrangement.Center
            ) {
                /* 音频名字 */
                Text(
                    text = "《${album.name}》",
                    style = MaterialTheme.typography.subtitle1.copy(      /* 音频可可用性判断， 结果通过颜色深重反馈到界面上 */
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(end = 4.dp)
                )
                /* 音频艺人与专辑信息 */
                Text(
                    text = "专辑 • ${album.artist.name}",
                    style = MaterialTheme.typography.body2.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 1.dp)
                )
            }
            IconButton(
                onClick = { onOpenAlbumPage() },
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "打开专辑页"
                )
            }
        }
    }
}

/** 专辑选择条目
 *  @param  modifier    修饰符参数
 *  @param  isSelected  被选择标识
 *  @param  artist   专辑对象
 *  @param  onOpenArtistPage  播放事件
 *  @param  onSelectEvent  当前条目被选择事件
 */
@ExperimentalMaterialApi
@Composable
fun ArtistSelected(
    modifier : Modifier = Modifier,
    isSelected : Boolean,
    artist: Artist,
    imageLoader : ImageLoader,
    onSelectEvent: () -> Unit,
    onOpenArtistPage: () -> Unit,
) {
    Card(
        modifier = modifier
            .height(64.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colors.primary) else BorderStroke(0.dp, Color.Transparent),
        elevation = 0.dp,
        onClick = { onSelectEvent() },
        backgroundColor = MaterialTheme.colors.background
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = isSelected
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_select_fill),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(start = 4.dp)
                        .weight(.8f),
                    tint = MaterialTheme.colors.primary
                )
            }
            AsyncImage(
                model = artist.picUrl,
                contentDescription = artist.name,
                imageLoader = imageLoader,
                placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder ),
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .padding(start = 8.dp, end = 2.dp)
                    .weight(1f)
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(4f),
                verticalArrangement = Arrangement.Center
            ) {
                /* 音频名字 */
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.subtitle1.copy(      /* 音频可可用性判断， 结果通过颜色深重反馈到界面上 */
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                )
                /* 音频艺人与专辑信息 */
                Text(
                    text = "艺人",
                    style = MaterialTheme.typography.body2.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 1.dp)
                )
            }
            IconButton(
                onClick = { onOpenArtistPage() },
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "打开艺人页"
                )
            }
        }
    }
}

/** 歌单选择条目
 *  @param  modifier    修饰符参数
 *  @param  isSelected  被选择标识
 *  @param  playlist   歌单对象
 *  @param  onOpenPlaylistPage  打开歌单想起你个页事件
 *  @param  onSelectEvent  当前条目被选择事件
 */
@ExperimentalMaterialApi
@Composable
fun PlaylistSelected(
    modifier : Modifier = Modifier,
    isSelected : Boolean,
    playlist: PlayList,
    imageLoader : ImageLoader,
    onSelectEvent: () -> Unit,
    onOpenPlaylistPage: () -> Unit,
) {
    Card(
        modifier = modifier
            .height(64.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colors.primary) else BorderStroke(0.dp, Color.Transparent),
        elevation = 0.dp,
        onClick = { onSelectEvent() },
        backgroundColor = MaterialTheme.colors.background
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = isSelected
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_select_fill),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(start = 4.dp)
                        .weight(.8f),
                    tint = MaterialTheme.colors.primary
                )
            }
            AsyncImage(
                model = playlist.coverImageUrl,
                contentDescription = playlist.name,
                imageLoader = imageLoader,
                placeholder = painterResource(id = if (isSystemInDarkTheme()) R.drawable.ic_image_placeholder_night else R.drawable.ic_image_placeholder ),
                modifier = Modifier
                    .size(50.dp)
                    .padding(start = 8.dp, end = 2.dp)
                    .weight(1f)
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(4f),
                verticalArrangement = Arrangement.Center
            ) {
                /* 音频名字 */
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.subtitle1.copy(      /* 音频可可用性判断， 结果通过颜色深重反馈到界面上 */
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 1.dp)
                )
                /* 音频艺人与专辑信息 */
                Text(
                    text = "歌单 • ${playlist.creator.nickName}",
                    style = MaterialTheme.typography.body2.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 1.dp)
                )
            }
            IconButton(
                onClick = { onOpenPlaylistPage() },
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "打开歌单页"
                )
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
@Preview(showBackground = true)
fun PreviewMusicSelected() {
    val track = SingleTrackMock
    val album = SingleAlbum
    val artist = SingleArtistMock
    val playlist = SinglePlayListData
    val (currentSelectedId, changeCurrentSelectedId) = remember {
        mutableStateOf(0.toLong())
    }
    LazyColumn {
        item {
            MusicSelected(
                track = track,
                isSelected = currentSelectedId == track.id,
                onSelectEvent = {
                    if (currentSelectedId != track.id) {
                        changeCurrentSelectedId(track.id)
                    } else {
                        changeCurrentSelectedId(0.toLong())
                    }
                },
                onPlayEvent = {  }
            )
        }
        item {
            AlbumSelected(
                album = album,
                isSelected = currentSelectedId == album.id,
                imageLoader = ImageLoader.Builder(LocalContext.current).build(),
                onSelectEvent = {
                    if (currentSelectedId != album.id) {
                        changeCurrentSelectedId(album.id)
                    } else {
                        changeCurrentSelectedId(0.toLong())
                    }
                },
                onOpenAlbumPage = {  }
            )
        }
        item {
            ArtistSelected(
                isSelected = currentSelectedId == artist.id,
                artist = artist,
                imageLoader = ImageLoader.Builder(LocalContext.current).build(),
                onSelectEvent = {
                    if (currentSelectedId != artist.id) {
                        changeCurrentSelectedId(artist.id)
                    } else {
                        changeCurrentSelectedId(0.toLong())
                    }
                },
                onOpenArtistPage = {  }
            )
        }
        item {
            PlaylistSelected(
                isSelected = currentSelectedId == playlist.id,
                playlist = playlist,
                imageLoader = ImageLoader.Builder(LocalContext.current).build(),
                onSelectEvent = {
                    if (currentSelectedId != playlist.id) {
                        changeCurrentSelectedId(playlist.id)
                    } else {
                        changeCurrentSelectedId(0.toLong())
                    }
                },
                onOpenPlaylistPage = {  }
            )
        }
    }
}