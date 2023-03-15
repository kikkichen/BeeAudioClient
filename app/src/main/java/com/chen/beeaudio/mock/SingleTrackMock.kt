package com.chen.beeaudio.mock

import com.chen.beeaudio.model.audio.Al
import com.chen.beeaudio.model.audio.Ar
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.model.audio.TrackFile

val SingleTrackMock : Track = Track(
    id = 514765042,
    name = "Don't say \"lazy\" [5人Ver.]",
    ar = listOf(Ar(id = 161782, name = "放課後ティータイム")),
    al = Al(
        id = 36634131,
        name = "けいおん!はいれぞ!「Come with Me!!」セット",
        picUrl = "https://p1.music.126.net/e1n_xjLFAm_GY8ZETmka4g==/109951163048673023.jpg",
        pic = 109951163048673020
    ),
    dt = 263906,
    fee = 1,
    noCopyrightRcmd = "",
    source = "126.net",
    usable = true,
    privilegeSignal = 0,
)

val SingleTrackFileMock : TrackFile = TrackFile(
    id = 514765042,
    url = "http://m8.music.126.net/20221231003555/533d43e681175660a05c39152c9b89f9/ymusic/4814/79e4/ac0a/eeb466ea976f1cc1f67e491721605ba3.mp3",
    size = 4223521,
    md5 = "eeb466ea976f1cc1f67e491721605ba3",
    encodeType = "mp3",
    time = 263906,
)

val CollectionTracksMock : List<Track> = listOf(
    Track(
        id = 25706282,
        name = "夜空中最亮的星",
        ar = listOf(Ar(id = 12977, name = "逃跑计划")),
        al = Al(
            id = 2285010,
            name = "世界",
            picUrl = "http://p4.music.126.net/625-tE8OzdM-rWO37PgqlQ==/109951168111472442.jpg",
            pic = 109951168111472450
        ),
        dt = 252235,
        fee = 8,
        noCopyrightRcmd = "",
        source = "126.net",
        usable = true,
        privilegeSignal = 1,
    ),
    Track(
        id = 514543065,
        name = "Oh My ギー太!!",
        ar = listOf(Ar(id = 17966, name = "豊崎愛生")),
        al = Al(
            id = 36634131,
            name = "けいおん!はいれぞ!「Come with Me!!」セット",
            picUrl = "https://p1.music.126.net/e1n_xjLFAm_GY8ZETmka4g==/109951163048673023.jpg",
            pic = 109951163048673020
        ),
        dt = 252133,
        fee = 1,
        noCopyrightRcmd = "",
        source = "126.net",
        usable = true,
        privilegeSignal = 0,
    ),
    Track(
        id = 514765036,
        name = "青春Vibration",
        ar = listOf(Ar(id = 16495, name = "日笠陽子")),
        al = Al(
            id = 36634131,
            name = "けいおん!はいれぞ!「Come with Me!!」セット",
            picUrl = "https://p1.music.126.net/e1n_xjLFAm_GY8ZETmka4g==/109951163048673023.jpg",
            pic = 109951163048673020
        ),
        dt = 216160,
        fee = 1,
        noCopyrightRcmd = "",
        source = "126.net",
        usable = true,
        privilegeSignal = 0,
    ),
    Track(
        id = 514765042,
        name = "Don't say \"lazy\" [5人Ver.]",
        ar = listOf(Ar(id = 161782, name = "放課後ティータイム")),
        al = Al(
            id = 36634131,
            name = "けいおん!はいれぞ!「Come with Me!!」セット",
            picUrl = "https://p1.music.126.net/e1n_xjLFAm_GY8ZETmka4g==/109951163048673023.jpg",
            pic = 109951163048673020
        ),
        dt = 281893,
        fee = 1,
        noCopyrightRcmd = "",
        source = "126.net",
        usable = true,
        privilegeSignal = 0,
    )
)