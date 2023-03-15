package com.chen.beeaudio.mock

import com.chen.beeaudio.model.audio.Creator
import com.chen.beeaudio.model.audio.PlayList

val EmptyPlayListData = PlayList(
    id = 0,
    name = "",
    coverImageUrl = "",
    userId = 0,
    createTime = 0,
    description = "",
    tags = emptyList(),
    creator = Creator(0, "", "", "", "", ""),
    tracks = null,
    trackIds = null
)

val SinglePlayListData = PlayList(
    id = 502767290,
    name = "冰気电子 ' 沉溺于绮旷的荒芜之境",
    coverImageUrl = "http://p2.music.126.net/0CfAMyTrAMo9g4r3MZjqEg==/109951165078377520.jpg",
    userId = 7394345,
    createTime = 1478334530408,
    description = "岁暮天寒，冷若冰霜。本歌单精心收集了一些冷系电音，顾名思义，即听时让人感觉寒冷的电音，而造成这种效果的因素有很多，比如冰裂音效、空灵旋律、激萌鼓点、低音贝斯、碎拍音效等等，给听者冻结双耳、寒彻心扉之感。\\n\\n在收集曲目的时候，特别注重了对曲子封面的选择，封面图都给人凛冽或凄凉之感。结合歌单名称、歌单封面、环境音效，让听者达到身临其境的感觉。在聆听时，你会被它旋律的结构和环境的音效，所营造出来的氛围与你情绪产生共鸣。\\n\\n标签 : Future Garage / Chillstep / Ambient / Color Bass\\n封面 : 摄影艺术家Victoria Siemer逆像景色作品\\n\\nBy : 朩朩青尘\\nDate : 2016.11.8",
    tags = listOf("电子", "孤独", "另类/独立"),
    creator = Creator(
        userId = 7394345,
        nickName = "YouTube全球音乐",
        signatrue = "",
        description = "",
        avatarUrl = "http://p1.music.126.net/7dBKfR0-rS-feI38DEDreQ==/109951165604373914.jpg",
        backGroundUrl = "http://p1.music.126.net/ZzyarRrIQkmww7e7t1ta2Q==/109951166291589488.jpg"
    ),
    tracks = emptyList(),
    trackIds = emptyList()
)

val PlayListCollectionMock1 = listOf<PlayList>(
    PlayList(
        id = 2966432846,
        name = "人声后摇 | 灵魂与鼓点的碰撞",
        coverImageUrl = "http://p1.music.126.net/IdlievbaIw2QU_Iftd7I2Q==/109951166142386420.jpg",
        userId = 481336063,
        createTime = 1567332334461,
        description = "后摇是摇滚静寂的力量\\n是无限接近自己的过程\\n随着鼓点一步一步步奔向云层\\n\\n“ 后摇人声采样是我觉得后摇中最有魅力的一种，在乐曲中加入人声，还原一个故事，一部电影，一次演讲，是极好的一种方式。 ” ———后摇爱好者\\n\\n提到后摇，我会联想到孤独的巨兽，那就是封面。\\n（封面不是热情亲吻，是鲸落的道别。）\\n\\n鲸鱼在呼吸的时候，一秒钟清醒，一秒钟晕眩。这就是诗意，这就是听后摇的时候，一秒钟沉沦人世，一秒钟超脱的快感吧。\\n\\n“ 后摇 非丧，非暗，乃 暗中寻光，乃 挣扎于泥泞，奋力一搏，坚守清白。我喜欢东野圭吾，川端康成，松弥太郎，弗洛伊德和太宰治。我也喜欢春天的太阳，夏天的暴雨，秋天的微风阵凉，包括冬天没有人的操场。”\\n\\n不知为何，我所理解的后摇有点奇怪。\\n我觉得后摇是在告诉我：\\n“消沉完了，发泄完了，那么，继续上路吧。”\\n\\n在我心中后摇从不是“有点丧”的音乐，不论是温柔的后摇、阴郁的后摇还是带有咆哮呐喊的后摇，它们都对我说“在我这里你可以尽情想象，哭泣，呐喊，无限接近自己，你可以在我这里休息，然后笑着面对生活吧”它让我思考，让我清醒，帮我前进，它是我的一剂良药。\\n\\n生活艰辛，人生不易，可日子还得继续，我们还得向前。就这样勇敢的大步向前好了，再多的苦难，再多的失望，日后终会到头。\\n\\n最开心的莫过于遇到你们这些口味相似的人\\n感谢欣赏。by宁",
        tags = listOf("孤独", "后摇", "夜晚"),
        creator = Creator(
            userId = 481336063,
            nickName = "为我失神",
            signatrue = "",
            description = "",
            avatarUrl = "http://p1.music.126.net/m1vLz8Zq-2v-I9XoA6CWtw==/109951168092877274.jpg",
            backGroundUrl = "http://p1.music.126.net/_f8R60U9mZ42sSNvdPn2sQ==/109951162868126486.jpg"
        ),
        tracks = emptyList(),
        trackIds = emptyList()
    ),
    PlayList(
        id = 5056460435,
        name = "致郁纯音丨那些用符号编织的梦境",
        coverImageUrl = "http://p1.music.126.net/u-Zz9CBR7ij7l1KhZjHbbA==/109951165211748358.jpg",
        userId = 111089166,
        createTime = 1591642925951,
        description = "孤独，有时候更像一杯水，没有杂质、没有污染，是一种清静幽雅的美。\\n\\n孤独的时间长了，也就再也无法回到人群中了。习惯孤独，喜欢孤独，爱上孤独，这一切是那样的理所当然。（孤独≠丧，孤独也是一种力量）\\n\\nᶘ ͡°ᴥ͡°ᶅ 别⃠熬⃠夜⃠啦⃠ 晚⃢安 ♡ n̶o̶ʎ̶ ̶ǝ̶ʌ̶o̶ן\\n\\n封面画师：CiCi\\nP站原图id：43302392",
        tags = listOf("轻音乐", "夜晚", "孤独"),
        creator = Creator(
            userId = 111089166,
            nickName = "Sakura雫",
            signatrue = "",
            description = "",
            avatarUrl = "http://p1.music.126.net/mDGs_jjyW_3Z5l8ar4LQLQ==/109951167608863031.jpg",
            backGroundUrl = "http://p1.music.126.net/zhYo6govqYiCIgH_jvQjlw==/109951167396620899.jpg"
        ),
        tracks = emptyList(),
        trackIds = emptyList()
    ),
    PlayList(
        id = 5062223334,
        name = "浪漫星空物语☆空灵嗓音与星辰旋律",
        coverImageUrl = "http://p1.music.126.net/eJ0jlYeEO7Txe60ZzA6Wuw==/109951165059015548.jpg",
        userId = 1323364095,
        createTime = 1592015628597,
        description = "抬头看看天空吧\\n你喜欢的人和你在同一片星空下呢\\n\\n包揽下一整片星空的浪漫\\n\\n--------------------------------------------------------------\\n歌单根据聆听氛围分为\\n\\n①天渐渐暗下来\\n看着蔚蓝的天空\\n\\n②深夜了 对面楼的灯都熄灭了\\n只剩下路灯杆下的匆匆过客\\n\\n-------------------------------------------------------------\\n歌单灵感来源于\\nyou were good to me\\n一首温柔的音乐\\n“轻缓小调\\n低吟男嗓\\n启声便沉醉在心底\\n浅唱女声\\n启唇便沉沦于其中\\n指尖轻弹\\n琴谱于黑白琴键间滑\\n拨动余弦\\n吟游唱出心声传向港湾”\\n-------------------------------------------------------------\\n建议音量：35%～50%\\n最佳时间:19:00后\\n\\n心动系列①",
        tags = listOf("欧美", "清新", "夜晚"),
        creator = Creator(
            userId = 1323364095,
            nickName = "樱桃味音乐",
            signatrue = "",
            description = "",
            avatarUrl = "http://p1.music.126.net/oqJmvA9ot4vWbCrkX7rCrw==/109951164873006151.jpg",
            backGroundUrl = "http://p1.music.126.net/fCBsF5ilIq6qaMnFXFo1mQ==/109951168076115120.jpg"
        ),
        tracks = emptyList(),
        trackIds = emptyList()
    ),
    PlayList(
        id = 331841455,
        name = "深度睡眠 |重度失眠者专用歌单",
        coverImageUrl = "http://p1.music.126.net/g2_Gv0dtAicJ3ChTYu28_g==/1393081239628722.jpg",
        userId = 103159696,
        createTime = 1460007339812,
        description = "好希望这里可以成为大家的一个庇护所\\n在深夜安安静静不吵不闹\\n路过的朋友留下一个故事或沉默\\n大家都彼此默契\\n等待天明。\\n\\n————————\\n\\n单主会将平时听到的一些小众的、节奏非常舒缓的、适合睡眠、冥想、写作、思考的音乐收录进来，如这个歌单里的「松田光由、小濑村晶、α·Pav、Endless Melancholy、hideyuki hashimoto…… 」他们的曲子。\\n\\n每一首曲子都是由单主精心挑选并亲自“试睡”的助眠歌曲，睡不着的时候可以用来平复心绪；如果失眠很严重的话，不要急躁，闭上眼睛深呼吸，放松身体，告诉自己“即使没有睡着也没有关系的”，因为闭上眼睛放松也是一种休息。\\n\\n本歌单持续更新，但是缓慢，因为单主想保证歌单质量，并把真正适合的歌曲收录进来，希望不管是失眠还是睡眠，这个歌单都可以一直陪伴着你，谢谢喜欢这个歌单的你们。\\n\\n晚安呀～",
        tags = listOf("轻音乐","夜晚","安静"),
        creator = Creator(
            userId = 103159696,
            nickName = "音旧-",
            signatrue = "",
            description = "",
            avatarUrl = "http://p1.music.126.net/V8vWlV3FiclTlZX6yJhOXA==/109951167291316182.jpg",
            backGroundUrl = "http://p1.music.126.net/CFtfwMmW9OTd--BN5m1rfQ==/109951163352671916.jpg"
        ),
        tracks = emptyList(),
        trackIds = emptyList()
    ),
)