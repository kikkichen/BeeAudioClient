package com.chen.beeaudio.mock

import com.chen.beeaudio.model.blog.*
import com.chen.beeaudio.model.localmodel.Blog
import com.chen.beeaudio.model.localmodel.RetweetedBlog
import com.chen.beeaudio.utils.TimeUtils

val BlogMock = Blog(
    Id = 4834700462133265,
    Created = TimeUtils.strToDateTime("Fri Nov 18 20:40:14 +0800 2022").time,
    Source = "微博 weibo.com",
    Text = "电锯人 同人作品——《Go Out With A Bang》 全20页\\n（※提前预警，这篇作品各种意义上的震惊）\\n\\n原作者：Raggumba\\n<a  href=\\\"https://weibo.cn/sinaurl?u=https%3A%2F%2Ftwitter.com%2FRaggumba%2Fstatus%2F1551563685860364289\\\" data-hide=\\\"\\\">网页链接\\n昨天在Reddit版上看到了这篇，可以说非常震惊，自翻了一下全篇\\n授权&amp;\uD83D\uDD17见评 ",
    RetweetedStatus = RetweetedBlog(
        Id = 6137455005,
        Created = TimeUtils.strToDateTime("Fri Nov 18 20:40:14 +0800 2022").time,
        Text = "【城市冰火人】皮克斯动画电影《疯狂元素城》中文先导预告公开，2023年6月6日在北美上映，讲述火女和水男无法触碰的爱情。 http://t.cn/A6KPlYdO \u200B",
        UrlGroup = listOf(
            "https://tva1.sinaimg.cn/bmiddle/006rmOQxgy1h8qir3qw54j30ag0aeaa8.jpg",
            "https://tva1.sinaimg.cn/bmiddle/006rmOQxgy1h8qir3qw54j30ag0aeaa8.jpg",
            "https://tva1.sinaimg.cn/bmiddle/006rmOQxgy1h8qir3qw54j30ag0aeaa8.jpg",
            "https://tva1.sinaimg.cn/bmiddle/006rmOQxgy1h8qir3qw54j30ag0aeaa8.jpg",
            "https://tva1.sinaimg.cn/bmiddle/006rmOQxgy1h8qir3qw54j30ag0aeaa8.jpg",
            "http://wx1.sinaimg.cn/thumbnail/c3fe0ea7gy1h88nqju8s0j21hc0u07wh.jpg",
            "http://wx1.sinaimg.cn/thumbnail/c3fe0ea7gy1h88nqju8s0j21hc0u07wh.jpg",
            "http://wx1.sinaimg.cn/thumbnail/c3fe0ea7gy1h88nqju8s0j21hc0u07wh.jpg",
            "http://wx1.sinaimg.cn/thumbnail/c3fe0ea7gy1h88nqju8s0j21hc0u07wh.jpg",
        ).toString(),
        Uid = 6137455005,
        UserName = "每日鉴游君",
        UserAvatar = "https://tvax2.sinaimg.cn/crop.38.0.961.961.50/006Hm8pLly8h6asmuuu6tj30rs0rs3zb.jpg",
        UserDescription = "分享游戏特惠情报、新作资讯以及一些有趣的......",
        ReportCounts = 12,
        CommentCounts = 6,
        Attitudes = 27,
        MediaUrl = ""
    ),
    ReportCounts = 8205,
    CommentCounts = 212,
    Attitudes = 10544,
    Uid = 3105001013,
    UserName = "FFF团微博支部",
    UserAvatar = "https://tvax1.sinaimg.cn/crop.0.0.750.750.180/b9128e35ly8g8dmk5z01hj20ku0kut9f.jpg?KID=imgbed,tva&Expires=1668682139&ssig=%2FMTtUXbsuQ",
    UserDescription = "分享游戏特惠情报、新作资讯以及一些有趣的......",
    UrlGroup = "",
    MediaUrl = ""
)

val RequestBlogMock = RequestBlog(
    Id = 4851446270658504,
    Created = "2022-12-15T17:33:56+08:00",
    Source = "iPhone 12 mini",
    Text = "时光机带我回到了过去[可爱]，你也试试？",
    RetweetedStatus = com.chen.beeaudio.model.blog.RetweetedBlog(
        Id = 4322333928199541,
        Created = "2018-12-28T22:42:56+08:00",
        Text = "啊啊啊啊啊啊啊啊啊",
        UrlGroup = listOf(
            PicUrl("c3fe0ea7ly1fymub061y9j21hc0u0hdt"),
            PicUrl("c3fe0ea7ly1fymub061y9j21hc0u0hdt"),
            PicUrl("c3fe0ea7ly1fymub061y9j21hc0u0hdt"),
            PicUrl("c3fe0ea7ly1fymub061y9j21hc0u0hdt"),
            PicUrl("c3fe0ea7ly1fymub061y9j21hc0u0hdt"),
            PicUrl("c3fe0ea7ly1fymub061y9j21hc0u0hdt"),
            PicUrl("c3fe0ea7ly1fymub061y9j21hc0u0hdt"),
            PicUrl("c3fe0ea7ly1fymub061y9j21hc0u0hdt"),
            PicUrl("c3fe0ea7ly1fymub061y9j21hc0u0hdt"),
        ),
        User = RequestUser(
            Uid = 3288207015,
            Name = "云玩家本本子",
            Description = "空气，水，食物，任天堂。 不太了解这个世界的常识。",
            AvatarUrl = "https://tvax3.sinaimg.cn/crop.0.0.379.379.180/c3fe0ea7ly8glmpunrl8nj20aj0ajq3f.jpg?KID=imgbed,tva&Expires=1672179964&ssig=RQl88miOeT"
        ),
        ReportCounts = 12,
        CommentCounts = 6,
        Attitudes = 27,
        MediaUrl = ""
    ),
    ReportCounts = 8205,
    CommentCounts = 212,
    Attitudes = 10544,
    User = RequestUser(
        Uid = 3288207015,
        Name = "云玩家本本子",
        Description = "空气，水，食物，任天堂。 不太了解这个世界的常识。",
        AvatarUrl = "https://tvax3.sinaimg.cn/crop.0.0.379.379.180/c3fe0ea7ly8glmpunrl8nj20aj0ajq3f.jpg?KID=imgbed,tva&Expires=1672179964&ssig=RQl88miOeT"
    ),
    UrlGroup = emptyList(),
    MediaUrl = ""
)

val RetweetedMock = Retweeted(
    bid = 4848677074506581,
    text = "//@潘潘_蜂蜜芥末厨:🥺//@L倏煜呀Y://@苏召奴://@佐伯羽歌: 好看[憧憬][憧憬]",
    source = "玉城地牢Android",
    reposts_count = 0,
    comments_count = 0,
    attitudes_count = 0,
    uid = 2825485270,
    name = "白芍识秋_纵横十九道",
    description = "势来不可止，势去不可遏.",
    avatar_url = "https://tvax2.sinaimg.cn/crop.0.0.1080.1080.180/a8697bd6ly8h8l8mjo6abj20u00u0whr.jpg?KID=imgbed,tva&Expires=1672170005&ssig=C88Hkoee6x",
    picture_url = "",
    retweeted_bid = 4846993795716602,
    post_at = "2022-12-20T09:02:41+08:00"
)

val CommentMock = Comment(
    cid = 4847122454677623,
    root_id = 4847122454677623,
    text = "[抱一抱][抱一抱]",
    source = "来自广东",
    uid = 6324010557,
    name = "西窗幽月",
    description = "镇魂//有狗厉害",
    avatar_url = "https://tvax1.sinaimg.cn/crop.0.0.1080.1080.180/006TYU2hly8h9fb1en27mj30u00u0n0s.jpg?KID=imgbed,tva&Expires=1672170046&ssig=FK5N3YUTeG",
    bid = 4846993795716602,
    post_at = "2022-12-16T02:05:11+08:00",
    be_like = 0
)

val AttitudeMock = Attitude(
    aid = 4851314544345578,
    bid = 4846993795716602,
    uid = 7371131022,
    name = "港迮",
    description = "",
    avatar_url = "https://tvax2.sinaimg.cn/crop.0.0.512.512.180/0082QvU2ly8gentopn38sj30e80e80xf.jpg?KID=imgbed,tva&Expires=1672170045&ssig=9%2BN7iP1OX6",
    created_at = "2022-12-28T00:40:47+08:00",
    source = "微博轻享版"
)