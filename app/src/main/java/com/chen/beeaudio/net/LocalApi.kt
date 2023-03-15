package com.chen.beeaudio.net

import com.chen.beeaudio.model.audio.*
import com.chen.beeaudio.model.blog.*
import com.chen.beeaudio.model.history.HistoryData
import com.chen.beeaudio.model.net.AuthRegister
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.model.net.SubscribeResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface LocalApi {
    /** 请求歌单详细信息（不包含全部歌曲目录，但包含全部歌曲ID集合）
     *  @param  playListId  目标请求歌单ID
     */
    @GET("playlist/detail")
    suspend fun getPlayListDetail(
        @Query("playlist_id") playListId : Long
    ) : ResponseBody<PlayList>

    /** 请求歌单全部歌曲 (带分页)
     *  @param  playListId  目标请求歌单ID
     *  @param  page    请求页码
     *  @param  size    单页条目容量
     *
     */
    @GET("playlist/songs")
    suspend fun getPlayListTrackItems(
        @Query("playlist_id") playListId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ) : ResponseBody<List<Track>>

    /** 请求热门歌单
     *
     */
    @GET("/top/playlists")
    suspend fun getHotPlayListCollection() : ResponseBody<List<PlayList>>

    /** 请求歌单Tag集合
     *
     */
    @GET("/tags/hot")
    suspend fun getHotAndAllTags() : ResponseBody<HotAndAllTags>

    /** 根据索引标签获取指定Tag歌单列表 (分页)
     *  @param  cat  歌单索引标签
     *  @param  page    请求页码
     *  @param  size    单页条目容量
     *
     */
    @GET("/top/catplaylists")
    suspend fun getTagPlayListCollection(
        @Query("cat") cat: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ) : ResponseBody<List<PlayList>>

    /** 根据专辑ID请求详细的专辑信息
     *  @param albumId  请求目的专辑的ID
     */
    @GET("/album/detail")
    suspend fun getAlbumDetail(
        @Query("album_id") albumId : Long,
    ) : ResponseBody<AlbumDetail>

    /** 根据艺人ID请求详细的艺人信息
     *  @param  artistId    请求目的艺人ID
     */
    @GET("/ar/detail")
    suspend fun getArtistDetail(
        @Query("artist_id") artistId : Long
    ) : ResponseBody<Artist>

    /** 根据艺人ID请求曲目列表 - 分页
     *  @param  artistId    目标查询艺人ID
     *  @param  page    请求页码
     *  @param  size    单页条目容量
     */
    @GET("/ar/songs")
    suspend fun getCurrentArtistTracks(
        @Query("artist_id") artistId : Long,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ) : ResponseBody<List<Track>>

    /** 根据艺人ID请求专辑列表 - 分页
     *  @param  artistId    目标查询艺人ID
     *  @param  page    请求页码
     *  @param  size    单页条目容量
     */
    @GET("/ar/albums")
    suspend fun getCurrentArtistAlbums(
        @Query("artist_id") artistId : Long,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ) : ResponseBody<List<Album>>

    /** 根据曲目ID， 请求该目的曲目的详细信息
     *  @param  trackIds 目的曲目ID
     */
    @GET("/songs/detail")
    suspend fun getTrackDetail(
        @Query("song_ids") trackIds: String
    ) : ResponseBody<List<Track>>

    /** 根据曲目ID， 请求该复数曲目的详细信息
     *  @param  trackIds 曲目ID集合
     */
    @GET("/songs/detail")
    suspend fun getMultiTrackDetail(
        @Query("song_ids") trackIds: List<Long>
    ) : ResponseBody<List<Track>>

    /** 依据曲目ID列表， 请求该列表中所有的曲目的Url
     *  @param  trackIds 目的曲目ID
     */
    @GET("/play/url")
    suspend fun getTracksUrlInfo(
        @Query("song_ids") trackIds: String
    ) : ResponseBody<List<TrackFile>>

    /** 依据曲目ID列表， 请求该列表中所有的曲目的Url
     *  @param  trackIds 曲目ID集合
     */
    @GET("/play/url")
    suspend fun getMultiTracksUrlInfo(
        @Query("song_ids") trackIds: List<Long>
    ) : ResponseBody<List<TrackFile>>

    /** 查询我的历史播放记录
     *  @param  uid 当前查询历史记录用户ID
     *  @param  page    请求播放历史记录页数
     *  @param  size    请求单页容量大小
     */
    @GET("/history/browser")
    suspend fun browserMyHistoryData(
        @Query("uid") uid : Long,
        @Query("page") page : Int,
        @Query("size") size : Int
    ) : ResponseBody<List<HistoryData>>

    /** 添加一条历史记录
     *  @param  uid 当前新增历史记录条目用户ID
     *  @param  songId  添加历史播放记录的曲目ID
     */
    @FormUrlEncoded
    @POST("/history/update")
    suspend fun addMyHistoryItem(
        @Field("uid") uid : Long,
        @Field("sid") songId : Long
    ) : ResponseBody<Boolean>

    /** 清空我的历史播放记录
     *  @param  uid 请求清空历史播放记录的用户ID
     */
    @FormUrlEncoded
    @POST("/history/clear")
    suspend fun clearMyPlayHistory(
        @Field("uid") uid : Long
    ) : ResponseBody<Boolean>

    /** 通过邮箱 注册新用户
     *  @param  email   邮箱字符串字段
     *  @param  password    密码字段
     */
    @FormUrlEncoded
    @POST("/user/register/email")
    suspend fun registerNewUserByEmail(
        @Field("email") email : String,
        @Field("password") password : String
    ) : ResponseBody<AuthRegister>

    /** 验证Token - 用于知晓当前账户是否过期
     *
     */
    @GET("/token/verify")
    suspend fun verifyAccountToken(
        @Query("token") token : String
    ) : ResponseBody<Long>


    /** 获取当前用户关注博文动态
     *  @param  userId  当前目标用户Id
     *  @param  page    请求页码
     *  @param  size    请求单页大小
     */
    @GET("/blog/subscribe")
    suspend fun getMyFollowBlogsV2(
        @Query("user_id") userId : Long,
        @Query("page") page : Int,
        @Query("size") size : Int
    ) : ResponseBody<List<RequestBlog>>

    /** 获取用户关注列表
     *  @param  userId  查询目标用户ID
     */
    @GET("/user/focus")
    suspend fun getMyFocus(
        @Query("user_id") userId : Long,
        @Query("my_id") myId : Long
    ) : ResponseBody<List<SimpleUser>>

    /** 获取用户粉丝列表
     *  @param  userId  查询目标用户ID
     */
    @GET("/user/fans")
    suspend fun getMyFans(
        @Query("user_id") userId : Long,
        @Query("my_id") myId : Long
    ) : ResponseBody<List<SimpleUser>>

    /** 获取用户默认喜爱歌单
     *  @param  userId  目标默认喜爱歌单查询所属用户的ID
     */
    @GET("/user/favorite")
    suspend fun getUserFavoritePlayListId(
        @Query("user_id") userId : Long
    ) : ResponseBody<Long>

    /** 获取目标博文的详细内容
     *  @param  blogId  目标博文动态ID
     */
    @GET("/blog/detail")
    suspend fun getTargetBlogDetail(
        @Query("blog_id") blogId : Long
    ) : ResponseBody<RequestBlog>

    /** 获取目标博文的转发列表
     *  @param  blogId  目标博文动态ID
     *  @param  page    请求页码
     *  @param  size    请求单页大小
     *  @param  isReport    当前动态是否为转发博文标识
     */
    @GET("/blog/reports")
    suspend fun getBlogRetweetedList(
        @Query("blog_id") blogId : Long,
        @Query("page") page : Int,
        @Query("size") size : Int,
        @Query("is_report") isReport : Boolean
    ) : ResponseBody<List<Retweeted>>

    /** 获取目标博文的评论列表
     *  @param  blogId  目标博文动态ID
     *  @param  page    请求页码
     *  @param  size    请求单页大小
     */
    @GET("/blog/comments")
    suspend fun getBlogCommentList(
        @Query("blog_id") blogId : Long,
        @Query("page") page : Int,
        @Query("size") size : Int
    ) : ResponseBody<List<Comment>>

    /** 获取目标博文的评论列表
     *  @param  blogId  目标博文动态ID
     *  @param  page    请求页码
     *  @param  size    请求单页大小
     */
    @GET("/blog/attitudes")
    suspend fun getBlogAttitudeList(
        @Query("blog_id") blogId : Long,
        @Query("page") page : Int,
        @Query("size") size : Int
    ) : ResponseBody<List<Attitude>>

    /** 获取用户的详细信息
     *  @param  userId  目标查询用户ID
     */
    @GET("/user/detail")
    suspend fun getUserDetail(
        @Query("user_id") userId : Long
    ) : ResponseBody<RequestUserDetail>

    /** 查询目标用户的关注、粉丝、互粉数量
     *  @param  userId  目标查询用户ID
     */
    @GET("/user/count")
    suspend fun getCountUser(
        @Query("user_id") userId : Long
    ) : ResponseBody<SimpleUserCount>

    /** 查询目标用户是否为Premium会员
     *  @param  userId  目标查询用户ID
     */
    @GET("/user/ispremium")
    suspend fun getIsPremium(
        @Query("user_id") userId : Long
    ) : ResponseBody<Premium>

    /** 查询当前用户与目标用户的关系
     *  @param  myUid   当前用户ID
     *  @param  targetUid   关系查询目标用户ID
     */
    @GET("/user/relative")
    suspend fun getTargetUserRelative(
        @Query("my_uid") myUid : Long,
        @Query("target_uid") targetUid : Long
    ) : ResponseBody<Int>

    /** 查询目标用户历史博文
     *  @param  targetUserId    目标用户ID
     *  @param  page    请求页码
     *  @param  size    请求单页大小
     *  @param  isOriginal  是否请求原创博文而动态
     */
    @GET("/user/blogs")
    suspend fun getTargetUserBlogs(
        @Query("user_id") targetUserId : Long,
        @Query("page") page : Int,
        @Query("size") size : Int,
        @Query("isOri") isOriginal : Boolean
    ) : ResponseBody<List<RequestBlog>>

    /** 查询用户公开自建歌单列表
     *  @param  uid 查询目标用户ID
     */
    @GET("/user/playlist/access")
    suspend fun accessUserCreatedPlaylistCollection(
        @Query("uid") uid : Long
    ) : ResponseBody<List<PlayList>>

    /** 关注、取消关注目标用户
     *  @param  myUid   当前用户ID
     *  @param  targetUid   目标用户ID
     */
    @GET("/user/do/follow")
    suspend fun dealWithFollowAction(
        @Query("my_uid") myUid : Long,
        @Query("target_uid") targetUid : Long
    ) : ResponseBody<Boolean>

    /** 关键字搜索博文动态
     *  @param  keyword 关键字字符串
     *  @param  page    分页请求页码
     *  @param  size    请求单页容量大小
     */
    @GET("/search/blogs")
    suspend fun searchBlogByKeywords(
        @Query("keyword") keyword : String,
        @Query("page") page : Int = 1,
        @Query("size") size : Int = 20,
    ) : ResponseBody<List<RequestBlog>>

    /** 关键字搜索用户
     *  @param  keyword 关键字字符串
     *  @param  page    分页请求页码
     *  @param  size    请求单页容量大小
     */
    @GET("/search/users")
    suspend fun searchUserByKeywords(
        @Query("keyword") keyword : String,
        @Query("page") page : Int = 1,
        @Query("size") size : Int = 20
    ) : ResponseBody<List<RequestUser>>

    /** 上传博文动态随行图片
     *  @param  uid     发布博文动态用户ID
     *  @param  name    文件名
     *  @param  upload  待上传文件本体
     */
    @Multipart
    @POST("/blog/sent/img")
    suspend fun uploadBlogImageFile(
        @Part("uid") uid : Long,
        @Part("name") name : String,
        @Part upload : MultipartBody.Part
    ) : ResponseBody<String>

    /** 发送一条博文动态
     *  @param  uid     发布博文动态用户ID
     *  @param  text    博文动态正文
     *  @param  source  发布来源
     *  @param  picUrls 图片文件名集合
     */
    @FormUrlEncoded
    @POST("/blog/sent")
    suspend fun sentBlog(
        @Field("uid") uid : Long,
        @Field("text") text : String,
        @Field("source") source : String = "BeeAudio 客户端",
        @Field("pic_urls") picUrls : List<String>,
        @Field("media_data") mediaData : String,
    ) : ResponseBody<String>

    /** 转发一条博文
     *  @param  uid     转发博文动态用户ID
     *  @param  text    转发博文动态文本
     *  @param  source  发布来源
     *  @param  retweetedId 转发博文ID
     */
    @FormUrlEncoded
    @POST("/blog/retweeted/do")
    suspend fun retweetedBlog(
        @Field("uid") uid : Long,
        @Field("text") text : String,
        @Field("source") source : String = "BeeAudio 客户端",
        @Field("retweeted_id") retweetedId : Long,
    ) : ResponseBody<String>

    /** 评论一条博文
     *  @param  uid     评论博文动态用户ID
     *  @param  text    评论博文动态文本
     *  @param  source  发布来源
     *  @param  rootId  针对评论ID (一般为被评论博文ID)
     *  @param  bid     博文ID
     */
    @FormUrlEncoded
    @POST("/blog/comment/do")
    suspend fun commentBlog(
        @Field("uid") uid : Long,
        @Field("text") text : String,
        @Field("source") source : String = "BeeAudio 客户端",
        @Field("root_id") rootId : Long,
        @Field("bid") bid : Long,
    ) : ResponseBody<String>

    /** 为目标博文点赞
     *  @param  uid 点赞用户ID
     *  @param  source  点赞来源
     *  @param  bid 点赞博文ID
     */
    @FormUrlEncoded
    @POST("/blog/attitude/do")
    suspend fun attitudeBlog(
        @Field("uid") uid : Long,
        @Field("source") source : String,
        @Field("bid") bid : Long
    ) : ResponseBody<Boolean>

    /** 查询是否对当前动态博文点赞
     *  @param  uid     当前查询用户ID
     *  @param  bid     查询目标动态博文对象ID
     */
    @GET("/blog/attitude/check")
    suspend fun isAttitudeCheck(
        @Query("uid") uid : Long,
        @Query("bid") bid : Long,
    ) : ResponseBody<Boolean>

    /** 同步我的音频订阅项目
     *  @param  uid     当前查询用户ID
     *  @param  data    订阅数据json
     */
    @FormUrlEncoded
    @POST("/my/audio/sync_subscribe")
    suspend fun syncMySubscribeData(
        @Field("uid") uid : Long,
        @Field("data") data : String,
    ) : ResponseBody<Boolean>

    /** 获取我的音频订阅项目
     *  @param  uid     当前查询用户ID
     */
    @GET("/my/audio/subscribe")
    suspend fun getMySubscribeData(
        @Query("uid") uid : Long,
    ) : ResponseBody<SubscribeResponse>

    /** 向自建歌单中添加曲目
     *  @param  uid 当前执行用户ID
     *  @param  pid 执行目标歌单ID
     *  @param  sid 添加目标曲目ID
     */
    @FormUrlEncoded
    @POST("/playlist/insert")
    suspend fun addSongIntoMyCreatedPlaylist(
        @Field("uid") uid : Long,
        @Field("pid") pid : Long,
        @Field("sid") sid : Long,
    ) : ResponseBody<Boolean>

    /** 从我的自建歌单中 添加/删除 曲目 (可多个曲目)
     *  @param  uid 当前执行用户ID
     *  @param  sid 目标曲目ID
     *  @param  addPlayListIds  将曲目添加到目标自建歌单的 自建歌单ID列表
     *  @param  removePlayListIds  将曲目从目标自建歌单移除的 自建歌单ID列表
     */
    @FormUrlEncoded
    @POST("/my/playlist/change")
    suspend fun editSongFromMyMultipleCreatedPlaylist(
        @Field("uid") uid : Long,
        @Field("sid") sid : Long,
        @Field("add_pids") addPlayListIds : List<Long>,
        @Field("remove_pids") removePlayListIds : List<Long>,
    ) : ResponseBody<Boolean>

    /** 从我的自建歌单中批量 删除 曲目
     *  @param  uid 当前执行用户ID
     *  @param  pid 执行的目标自建歌单ID
     *  @param  songIds 执行目标曲目ID列表
     */
    @FormUrlEncoded
    @POST("/my/playlist/remove")
    suspend fun batchRemoveSongFromMyCreatedPlaylist(
        @Field("uid") uid : Long,
        @Field("pid") pid : Long,
        @Field("song_ids") songIds : List<Long>
    ) : ResponseBody<Boolean>

    /** 新建一个自建歌单
     *  @param  uid     当前执行用户ID
     *  @param  name    创建新歌单标题名字
     *  @param  description 歌单描述文本
     *  @param  tags    歌单属性标签TAG
     *  @param  public  歌单对外可见性
     */
    @FormUrlEncoded
    @POST("/my/playlist/creator")
    suspend fun createNewMyPlaylist(
        @Field("uid") uid : Long,
        @Field("name") name : String,
        @Field("description") description : String,
        @Field("tags") tags : String,
        @Field("public") public : Boolean = false,
    ) : ResponseBody<Long>

    /** 删除我的自建歌单
     *  @param  uid 执行用户ID
     *  @param  pid 目标删除歌单ID
     */
    @FormUrlEncoded
    @POST("/my/playlist/delete")
    suspend fun deleteMyPlaylist(
        @Field("uid") uid : Long,
        @Field("pid") pid : Long,
    ) : ResponseBody<Boolean>

    /** 编辑更新我的自建歌单信息
     *  @param  uid     当前执行用户ID
     *  @param  pid     编辑目标自建歌单ID
     *  @param  name    创建新歌单标题名字
     *  @param  description 歌单描述文本
     *  @param  tags    歌单属性标签TAG
     *  @param  public  歌单对外可见性
     */
    @FormUrlEncoded
    @POST("/my/playlist/update")
    suspend fun editorMyCreatedPlayListInfo(
        @Field("uid") uid : Long,
        @Field("pid") pid: Long,
        @Field("name") name : String,
        @Field("description") description : String,
        @Field("tags") tags : String,
        @Field("public") public : Boolean = false,
    ) : ResponseBody<PlayList>


    /** 上传歌单封面
     *  @param  uid 执行用户ID
     *  @param  pid 上传封面的目标歌单ID
     *  @param  upload  上传图片本体
     */
    @Multipart
    @POST("/playlist/cover/upload")
    suspend fun uploadPlaylistCover(
        @Part("uid") uid : Long,
        @Part("pid") pid: Long,
        @Part upload : MultipartBody.Part
    ) : ResponseBody<Boolean>

    /** 上传用户头像
     *  @param  uid     上传头像用户ID
     *  @param  upload  上传图片本体
     */
    @Multipart
    @POST("/my/avatar/upload")
    suspend fun uploadUserAvatar(
        @Part("uid") uid : Long,
        @Part upload : MultipartBody.Part
    ) : ResponseBody<Boolean>

    /** 修改用户基本信息
     *  @param  uid     修改目标用户ID
     *  @param  name    修改目标用户昵称
     *  @param  description 用户描述信息
     *  @param  email   邮箱
     *  @param  phone   电话号码
     */
    @FormUrlEncoded
    @POST("/my/info/update")
    suspend fun editUserDetail(
        @Field("uid") uid : Long,
        @Field("name") name : String,
        @Field("description") description : String,
        @Field("email") email : String = "",
        @Field("phone") phone : String = ""
    ) : ResponseBody<Boolean>

    /** 通过用户ID 修改用户密码
     *  @param  uid 目标修改密码用户ID
     *  @param  originalPassword    原密码
     *  @param  newPassword 新密码
     */
    @FormUrlEncoded
    @POST("/user/password/modifier/id")
    suspend fun modifierUserPassword(
        @Field("uid") uid : Long,
        @Field("original_password") originalPassword : String,
        @Field("new_password") newPassword : String
    ) : ResponseBody<Boolean>

    /** 通过用户邮箱 修改用户密码
     *  @param  email 目标修改密码用户绑定邮箱
     *  @param  originalPassword    原密码
     *  @param  newPassword 新密码
     */
    @FormUrlEncoded
    @POST("/user/password/modifier/email")
    suspend fun modifierUserPasswordByEmail(
        @Field("email") email : String,
        @Field("original_password") originalPassword : String,
        @Field("new_password") newPassword : String
    ) : ResponseBody<Boolean>
}