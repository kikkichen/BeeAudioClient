package com.chen.beeaudio.mock

import com.chen.beeaudio.model.blog.RequestUser
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.model.blog.SimpleUser
import com.chen.beeaudio.model.blog.SimpleUserCount

/* Email: kikkichen@163.com */
val UserIDMock : Long = 9900619251

val UserCountMock = SimpleUserCount(
    follows = 48,
    fans = 1,
    friends = 0
)

val RequestUserDetailMock = RequestUserDetail(
    uid = 2330418701,
    name = "_阿咕_",
    description = "游戏战斗\\\\关卡设计师 | 头像:咿籽itsu——b站id:10880609",
    avatar_url = "https://tvax1.sinaimg.cn/crop.0.0.600.600.180/002xIcmply8gu8cvrn7zoj60go0gowfx02.jpg?KID=imgbed,tva&Expires=1672169956&ssig=F35jBJ%2FfoW",
    created_at = "2022-12-28T00:36:33+08:00",
    user_type = 0,
    email = "2330418701@bee.com",
    phone = "",
    birthday = "0001-01-01T00:00:00Z"
)

val SimpleUserMock = SimpleUser(
    Id = 1004745095,
    name = "五风普洱",
    description = "你说得对，但《苏菲的炼金工房 ～不可思议之书的炼金术士～》，是由Gust Co. Ltd.制作，Koei Tecmo发行的一款角色扮演游戏（ry）",
    avatar = "https://tvax3.sinaimg.cn/crop.0.0.400.400.180/3be33187ly8h91dqq1hmxj20b40b4my3.jpg?KID=imgbed,tva&Expires=1672169955&ssig=79KXNehf%2B1",
    createAt = "2022-12-28T00:36:34+08:00",
    followState = 1
)