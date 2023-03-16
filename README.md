# BeeAudioClient

BeeAudio Android Client APP

一个面向Android客户端的在线音乐社交服务平台工程，其中服务端使用Beego框架构建。 An online music social service platform project for Android clients, where the server is built using the Beego framework.

### 功能简述

该Android客户端平台涉及音乐与社交两大块功能服务，而系统的设计中，二者有许多业务需求交织的功能接口，以此保留部分功能的特色并不失去系统的整体性。一下功能模块介绍中，不同功能模块小节中会出现来自于其他小结的部分技术内容与名词概述，旨在为功能模型的阐述寻求更完整的表达。

![function_framework.png](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/function_framework.png)

### ****客户端界面概览****

客户端主页是整个在线音乐平台客户端的功能枢纽，这里分块展示该系统两大特色服务的功能入口——音乐和社交，并且还涉及了针对使用者本身个性化的功能块区——“我的库”。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled.png)

(1)  首先，主页呈现的内容是该在线音乐平台推荐歌单内容。而歌单是该平台重要的数据实体之一，它由平台的歌单所属用户自行管理与维护。主页右上由左到右依次是“历史播放记录”与“搜索页”的入口；

(2)  社区动态博文页则是该平台“社交”功能的入口，这里能够浏览当前用户所关注的其他用户发布的动态博文，这些动态博文根据发布时间由新到旧排列而成，这里能够为给用户发布新的动态博文，又或者是前往博文搜索页以通过关键字搜索相关的动态博文与用户。

(3)  “我的库”则是用户个新化服务的功能入口，这里展示了用户自行收藏订阅的所有音频项目，包括歌单、专辑、艺人这些实体单位，这里还提供了用户创建编辑自建歌单与修改个人信息的渠道入口。

### 音乐内容索引

在线音乐服务平台需要拥有对音频的检索功能，以满足用户对于音频内容的查找的需求。这样的检索方式由发起“主动”的对象而产生两种划分方式：一是由平台软件服务主动提供的歌单标签索引，引导用户根据偏好的音乐类型查找到自己喜欢的音乐；二是有用户主动进行关键字的查找搜索与之关键字关联的音乐项目。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%201.png)

上图中展示的是应用搜索页的内容，这里分布了数十种不同歌单的标签类型，其中将热门标签单独放大并块状布局在了界面的显眼位置，其他所有的标签类型按照“语种”、“风格”、“场景”、“情感”、“主题”这五个标签大类进行分类。

而由用户主动发起搜索的结果，会在该“搜索页”跳转至“搜索结果页”后，向服务端请求关键字匹配数据。如下图4.4所示，从左到右依次是单曲、专辑、艺人、歌单的搜索结果。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%202.png)

### 音乐数据浏览

在该系统客户端的音乐功能模型设计中，所有的音频服务店铺围绕着四个主要的实体内容进行功能展开，这四个实体分别是是单曲、专辑、艺人和歌单。这些实体所代表含义与现实世界中音乐媒介所对应的名词含义一致。
单曲即代表一首歌曲的所有信息内容；专辑是由某位艺人所发行的曲目合集；而艺人是单曲和专辑的创作者；歌单则是由该系统平台参与用户自行创建的曲目集合单位。该系统平台依据这四个实体之间的属性差异与意义特性为平台的使用用户展开丰富的音频服务功能。
用户通过搜索等渠道可以查找歌单数据结果，而展示这些歌单数据的页面在该平台中称之为“歌单详情页”。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%203.png)

它包含了歌单的一些基本信息，包括歌单名、歌单创建用户、歌单封面、歌单简述以及曲目列表等信息；它也包含关于歌单收藏、分享歌单，播放歌单全部曲目的操作入口。如上图4.5所示。
艺人是该系统数据内容中不可或缺的一个实体，它是曲目的创作者，使用用户页存在依据艺人信息来检索收听曲目的需求。“艺人详情页”如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%204.png)

艺人详情页包括艺人的姓名或艺名、艺人简介信息。用户可通过“加入收藏”的案件将该页的艺人加入自己的收藏订阅中。该页会展示当前艺人的曲目作品与专辑作品。
专辑也是由艺人创作的作品单位，它是一首或多首曲目的集合，与歌单不同的是，歌单的创建主体是平台之上的用户，而专辑是现实世界中艺人创作编辑的实际产物，平台在此之上仅是完成了实时记录的工作。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%205.png)

专辑页包含了专辑的名称、描述信息以及其包含的曲目等等，用户同样能够将该专辑收藏、分享、播放。

### 音频播放

音频播放是该系统客户端中音乐服务数据输出流程的终点模块，在该系统的设计中，音频的播放控制并非只有一个角色承担，但“播放页”仍然是系统音频播放模块围绕的中心。“播放页”的播放效果如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%206.png)

播放控制由常见媒体控制功能构成，播放进度条下方的按键分别为“定时器”、“播放上一曲”、“播放、暂停”、“播放下一曲”、“切换播放模式”，其中切换播放模式可调节当前播放列表播放行为为“顺序播放”或是“随机播放”。
右下角图片能够哦查看当前播放曲目的详情信息，如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%207.png)

信息包括但不限于“时长”、“文件大小”、“可用性”、“品质”、“码率”等音频属性。
底部“播放列表”按键能够唤起抽屉栏显示当前播放列表内容，无论当前播放器的播放模式的状态如何，播放列表仍然以“顺序模式”下的排列执行显示。如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%208.png)

播放列表中的每个项目能够执行左右拖动操作：由左向右拖动目标曲目能够将目标曲目移动到“下一曲”的播放位置；由右向左拖动目标曲目能够将置顶曲目移除当前播放列表。播放列表右上角的图标按键为清空当前播放列表的入口。

为了更进一步提升用户在使用该软件播放音乐的体验，该应用还设计了一个小组件——“播放控制底栏”。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%209.png)

该“播放控制底栏”显示了当前播放曲目的标题、创作艺人、专辑封面以及播放进度等信息。在播放按键之外还有一个心形按键用于将当前播放曲目加入到当前用户的默认收藏歌单。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2010.png)

当点击心状按键时，触发添加成功的收藏反馈后，再次点击即能够取消收藏。
除此之外，该“播放控制底栏”的组件还支持“滑动手势切曲”的操作，拖动组件标题向右滑动切换到“上一曲”，向左滑动则立即播放“下一曲”。

系统状态栏以及锁屏界面中与当前程序的音频服务进行交互，能够完成例如“播放/暂停”、“切换上下曲目”、“切换播放设备”等快捷操作。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2011.png)

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2012.png)

### 社区动态博文浏览

社区是当前在线音乐服务平台系统中另外以大特色功能模块，这也是该系统中特色功能的一部分。在该平台的在线社区中，用户能够在社区分享自身喜爱的音乐，也能够分享自己的生活、工作、学习内容，还能够向其他用户的动态发表评论简介，从而是该平台的运转模式活化。
“社区博文动态页”是程序底部当行栏中的项目之一，这里也是整个系统社区功能的中央枢纽。如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2013.png)

这里展示的是当前用户与所关注其他用户发表的动态博文浏览页面，这里的动态博文按照发布时间从近到远依次排列。这里的每一个博文动态卡片会显示当前博文动态的一些必要信息，其包括发文用户昵称、用户头像、部分用户简介、发文时间描述、动态正文等。若动态是一条转发动态，卡片会呈现出当前转发动态与被转发动态的嵌套关系。除此之外还会预览动态当前的转发、评论、点赞数量。
右上角的搜索按键是进入“动态博文搜索页”的入口，动态博文搜索页能够根据用户提供的关键字，搜索该平台社区博文动态数据库中含有该关键字的博文内容，又或者是搜索出昵称与包含关键字的用户。如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2014.png)

### 动态博文附属操作

用户无论是在“社区动态博文页”或是“动态博文搜索页”，都可以对显示与屏幕上的动态博文卡片进行点击查看详情。“动态博文详情页”的界面如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2015.png)

这里会显示有关当前动态博文更详尽的内容，包括该条博文的转发、评论、点赞列表，同样适用了分页技术的加持用于显示条目信息。
用户也可以在该页面针对这条博文动态进行转发、评论或点赞操作。在该页面查看“转发”条目时，下方输入框会进入文本“转发”模式，当前输入文本并发送会进入到当前动态博文的转发队列当中；而查看“评论”或“点赞用户”条目时，该输入框会进入文本“评论”模式，此时发送文本信息会进入当前动态博文的评论队列当中。如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2016.png)

该页右上角则是点赞入口，上图中演示了针对该条动态博文点赞前和点赞后的图标显示状态。
当前举例的动态博文是一个多图动态，其现在呈现图片出于缩略图状态，点击就能进入“查看大图页”查看图像的原始画质内容。如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2017.png)

这里能够对查看的大图内容进行放大、缩小的等手势查看。

### 发布动态博文

发布动态博文是适用该在线音乐服务平台用户向右的权力，这也是整个系统中“社交”大功能模块中不可或缺的一部分。用户通过“社区动态博文页”右下角的浮动按钮“写动态”进入“发布动态页”。如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2018.png)

这里是用户发布新动态博文的加工渠道。顶部栏右侧为“草稿箱”入口，若当前页面存在编辑的文字未发送，此时用户退出该页后应用会提示用户是否将未发送文本存储进草稿箱中。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2019.png)

“显示小尾巴”是博文动态发文前设置动态来源的编辑框，这里代表的动态的发送设备状态信息。在默认情况下，此处会被填充为用户手持移动设备的型号又或者是当前应用名。

该页面底栏由左到右的图标按键依次是：

- 选择图片。这里能够选择1~9张图片作为动态博文的附图。

- 热门话题。这里能够为当前动态博文提供时下热门话题的标签。

- @好友。这里能够在动态博文的文本中附属上当前用户所添加的好友昵称。

  ![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2020.png)

- 表情键盘。点击打开表情键盘。该键盘右包括“面部表情”、“肢体动作表情”、“虚构人物”、“动物与植物”、“食物与饮品”、“日常活动”、“旅行与地理”、“物品表情”、“符号标志”等9大表情板块组成，每个板块都包含了含义不同的若干Emoji表情。如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2021.png)

该平台的动态博文支持多图携带发送。点击“发布动态页”下方的“相机”图标按键就能触发图像媒体选择器，如下图所示。用户能够一次性选择最多九张图片发送到社区中。多图添加与上传的实现过程需要多个模块的配合协作完成。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2022.png)

在图片选择之后，触发“发送动态”按键即可将当前编辑的携图动态发送到社区。如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2023.png)

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2024.png)

“草稿箱”是“发布动态博文页”的扩展功能之一。当用户在“发布动态博文页”编辑非空文字但未执行发布而退出该页时，页面会弹出对话框——“是否将未编辑文本存入草稿箱”，这里则是该系统“草稿箱”功能中唯一插入草稿文本的渠道。在“发布动态博文页”点击顶栏右处角图标就能进入“草稿箱”页。如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2025.png)

草稿箱中的草稿条目支持手势操作，单个条目的左右滑动均能将该条草稿从草稿箱列表中移除。若单条草稿的文本量较大，点击该条目能够展开查看全部内容。每个草稿条目的左侧都右一个编辑图标按键，触发该按键后该草稿能够重新回到“社区动态博文发布页”进入到编辑状态。

### 音乐分享

“音乐分享”功能是当前在线音乐服务平台系统中沟通“音乐”与“社交”两大平台的桥梁。用户能够在“搜索页”、“我的库”甚至是其他拥有在线音乐资源信息展示的为止分享音乐内容到社区当中。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2026.png)

这些音乐内容可以是“单曲”、“专辑”、“艺人”、“歌单”其中任意一个单位。而触发分享后会携带选择的音乐单位数据前往“发布动态页”进行下一步的文本编辑。如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2027.png)

对当前使用用户形成关注关系的“粉丝”用户能够在社区中看到用户发布的音乐分享动态，通过点击分享内容反馈响应。若分享内容未单曲，则点击后会立即播放该分享曲目；若分享内容为专辑，则点击后会立即跳转到“专辑详情页”；若分享内容为艺人，则点击后会立即跳转到“艺人详情页”；若分享内容为歌单。则点击后会立即跳转到“歌单详情页”。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2028.png)

该页面在搜索栏没有输入任何搜索关键字的情况下，首先会显示当前用户最近点赞的喜欢曲目，按照点赞事件由近到远的顺序排列，而当点击顶栏右侧的搜索图标输入关键字后，该页就会请求与关键字相关的曲目、专辑、艺人、歌单等相似音乐单位。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2029.png)

当用户确认分享音乐单位并发送该条音乐分享性质的动态博文后，当前用户与关注当前用户的粉丝用户，就能够在“社区动态博文页”刷新浏览到这条音乐分享动态，并再而通过该动态访问到相关页面、相关曲目。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2030.png)

### 用户关注关系

用户在“社区动态博文页”看到的动态博文均是由自己或者自己所关注的用户发布在社区中的动态。而要产生这样的“关注”关系，就需要用户主动查看相关用户的个人主页，从而找到关注功能的入口。
在浏览动态博文卡片时，点击卡片上方的用户头像，即可进入该用户的主页查看其详情信息。也可以在“社区搜索页”通过关键字得到相关搜索结果用户条目，从而进入目标用户的个人主页。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2031.png)

该个人主页能够查看用户的头像、昵称、简介粉丝用户数量以及关注用户等信息，并且能过够查看该用户所发布的所有历史动态博文。动态博文内容列表以全部博文与原创博文区别筛选查看，原创博文的本意为该用户未进行引用转发的动态博文内容。这里还能够查看该用户的自建歌单列表。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2032.png)

显示在用户主页的公开歌单列表中的歌单性质，是由用户自己创建并且公开性设置为对外公开的歌单列表，或是用户自身的默认喜爱歌单列表。
点击用户信息中的“粉丝数”与“关注数”能够查看当前用户正被哪些用户关注并关注着哪些其他用户，这些用户的状态会呈现处与当前自己的关注与被关注关系。这里可以点击用户条目末端的关注操作按键进行快捷的“关注”与“取消关注”操作

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2033.png)

用户之间的关注状态有四种：

- 双方不存在关注关系。这时查看对方用户主页，关注操作按键上显示字样为“+ 关注”。
- 当前使用者用户已关注了对方用户。这时查看对方用户主页，关注操作按键上显示字样为“- 取消关注”。
- 对方用户关注了当前使用者用户。这时查看对方用户主页，关注操作按键上显示字样为“ta 关注了你”
- 若双方用户都相互关注。这时查看对方用户主页，关注操作按键上显示字样为“@ 相互关注”

只有在双方用户都相互关注的这种关注关系情况下，对方用户才能在“社区动态博文页”刷新浏览到自己发布的历史动态博文，而用户自身也才能在“社区动态博文页”刷新浏览到对方发布的历史动态博文。

### 音乐收藏订阅

在该平台应用打开的主页导航栏中，“我的库”入口按键处于最末端的位置。按照绝大多数右手惯用移动设备习惯的用户都能用较为方便的手势打开该功能页。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2034.png)

这里是代表针对用户个性化需求集中设计的功能页，用户在该系统“音乐”大模块中对自身偏好的歌单、艺人、专辑音乐单位进行收藏后的数据出口。这里会显示所有由当前用户收藏的所有音乐单位，并且用户能够通过上方的`Chip`类型筛选需要进行显示的音乐收藏订阅项目。选择列表顶端右侧小图标能够切换显示模式是默认的“列表排列”还是“大图块状排列”。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2035.png)

用户也能够通过右上角的搜索图标检索当前收藏订阅项目内容。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2036.png)

在设计该项目的排列顺序中存在一个“加权算法”，在默认情况下，新加入该用户收藏订阅列表的数据会追加到列表的尾端位置。但通过用户在该在线音乐平台不断地播放曲目，这些产生历史播放记录的行为路径会对这些订阅项目的排列产生影响。

用户也能够通过针对一个收藏订阅项目从左向右滑动的操作手势将该项目手动置顶，这样会使得该项目保持置顶状态。也能够将正在置顶的项目通过由右向左的滑动手势以取消置顶。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2037.png)

这些订阅项目数据来源源自于Room管理维护的SQLite数据库，这些数据会在在曲目操作变动的时候同时进行变更，且也会将变更后的结果同步到服务端当中。该收藏数据会在用户执行登录与登出的时候触发收藏订阅数据的全量更新，这是本地收据库会清空所有数据并接纳储存新数据。

### 歌单操作

歌单操作包括“创建歌单”、“编辑歌单”以及“将曲目收藏到歌单”功能操作。歌单作为集合类曲目单位在该在线音乐平台的设计中是负责灵活音乐传递的重点数据。它能由用户进行编辑，也能够由用户进行分享，与艺人创作专辑这类集合由历史事件决定的不变性数据而言具较灵活的性质。
就此，在该系统设计的初期，就针对用户规划了丰富的歌单操作功能。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2038.png)

“我的库”顶栏处有一加号按键，这里是打开“创建歌单”功能的入口。歌单的创建界面十分简洁，只保留有“歌单标题”、“选择标签”、“歌单描述”、“歌单公开性”四个选项。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2039.png)

其中歌单标签是概括歌单曲目风格性质的标志，该平台用户通过歌单标签索引浏览歌单即是通过该标签进行区分；歌单公开性标识当前歌单是否允许向平台其他用户展示，当该值为false时，其他用户在浏览该用户的歌单列表时不会发现该歌单，该歌单只对创建者与系统管理员可见。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2040.png)

用户在浏览自建歌单时能够在歌单右上角“更多菜单”图标中找到编辑歌单信息的入口。

新创建的歌单时没有任何曲目数据的，这些数据需要用户自行添加收藏。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2041.png)

这里用户可以在弹出的收藏到歌单的对话框中选择需要将当前目标曲目收藏的位置。当对话框中出现在用户执行之前就已经被勾选的歌单项目，则表示当前曲目已经被收藏到该歌单当中了。这时用户也可以将其选择取消勾选表示将该曲目从该歌单中移除收藏。点击确定后保存执行结果。

### Premium会员模块

Premium是该在线音乐服务平台的会员通道身份，用户通过支付开通Premium会员后即可解锁该在线音乐平台中所有的曲目内容。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2042.png)

若一位用户处于非会员的状态，那么在“我的库”中看到的顶栏如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2043.png)

会员图标是处于“未激活”状态。现在这位非会员状态的用户是没有收听付费音频的权力的，若此时点击后缀Premium标识的曲目，则会弹出升级Premium套餐的提示弹窗。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2044.png)

当非会员用户点击“我的库”页面的会员图标或者是“升级到Premium会员”提示弹窗的“现在看看”时，就会跳转到“Premium套餐升级引导页”。如下图所示。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2045.png)

该在线音乐服务平台提供了三种开通Premium会员套餐的方式：

- 通过支付方式，将当前用户套餐升级为Premium个人套餐，解锁该平台所有音频的收听权限。
- 通过支付方式，将当前用户套餐升级为Premium家庭组套餐。当前用户会成为当前开通Premium家庭组的管理员，通过向指定用户分享Premium套餐订单卡号的方式将邀请其到该Premium家庭组套餐中，享用该平台所有音频的收听权限。目前一个家庭组最多能容纳6为成员。
- 通过其他管理员用户分享的订单卡号，通过识别其订单卡号向目标家庭组管理员提出申请。当管理员同意申请后，在该家庭组成员数量没有超过最大限制人数时，当前发送申请用户即可成为该Premium家庭组的一份子，从而享用该平台所有音频的收听权限。

“加入Premium家庭组”的申请方式又细分为两种。一是，通过在该“Premium套餐升级应道页”左后一项“加入Premium家庭组”中订单卡号输入入口，查找到目标Premium家庭组后发送加入申请。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2046.png)

二是，通过目标Premium家庭组管理员或其他组内成员分享家庭组二维码。通过扫码查询Premium家庭组的方式提交申请。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2047.png)

接下来管理员会在“Premium会员信息页”看到家庭组有新的申请。此时管理员点击同意请求后，该请求发出用户就能成功加入到该家庭组的成员行列中。该家庭组所有成员的Premium会员服务期限全部以管理员的服务结束期限为准。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2048.png)

管理员用户仍然能够对已经存在与家庭组中的成员用户进行“移除家庭组”的操作。长按目标家庭组成员卡片，当弹出菜单后选择“从当前家庭组移除”，该页面会弹出一条确认对话框，管理员需要对当前的行为进行确认，确认完成之后才可完成将家庭组成员移除的操作。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2049.png)

### 登录 & 注册

登录与注册是绝大多数系统平台必不可少的功能，该在线音乐服务平台提供了两种注册方案，即“邮箱注册”与“手机号码注册”，邮箱与手机号码格式均由对应的正则表达式进行客户端本地的验证核查，再确认对应邮箱与手机号码符合标准并且服务器确认其在该平台上没有用户使用后即可完成用户的注册。注册之后，用户能够直接使用注册时绑定邮箱或者是手机号码进行登录。

![Untitled](BeeAudioClient%20f729d86d1ad44513b8763d482ef0a19b/Untitled%2050.png)
