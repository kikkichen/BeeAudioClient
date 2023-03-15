package com.chen.beeaudio.net

import com.chen.beeaudio.model.audio.FamilyPremium
import com.chen.beeaudio.model.audio.FamilyPremiumNumbers
import com.chen.beeaudio.model.audio.Premium
import com.chen.beeaudio.model.net.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LocalPremiumApi {
    /** 获取用户进20次Premium订单记录
     *  @param  uid 查询目标用户ID
     */
    @GET("/my/premium/order")
    suspend fun getRecentPremiumOrder(
        @Query("uid") uid : Long
    ) : ResponseBody<List<Premium>>

    /** 同意加入家庭组的申请 - 管理员
     *  @param  uid 当前执行操作的管理员用户ID
     *  @param  targetUserId    目标同意审核的加入用户ID
     *  @param  cardId  Premium家庭组卡号
     */
    @FormUrlEncoded
    @POST("/user/premium/apply")
    suspend fun agreeUserJoinInPremiumGroup(
        @Field("uid") uid : Long,
        @Field("target_id") targetUserId : Long,
        @Field("card_id") cardId : String
    ) : ResponseBody<Boolean>

    /** 通过卡号查询Premium套餐概要信息
     *  @param  cardId  Premium 套餐卡号
     */
    @GET("/user/premium/select")
    suspend fun getPremiumCardInfo(
        @Query("card_id") cardId : String
    ) : ResponseBody<FamilyPremium>

    /** 查询 Premium 家庭组成员
     *  @param  uid 当前用户ID - 用于验证当前用户是否为其家庭组管理员身份
     *  @param  cardId  Premium 套餐卡号
     */
    @GET("/user/premium/family/numbers")
    suspend fun getPremiumFamilyNumbers(
        @Query("uid") uid : Long,
        @Query("card_id") cardId : String
    ) : ResponseBody<FamilyPremiumNumbers>

    /** 查询目标用户是否为Premium会员
     *  @param  userId  目标查询用户ID
     */
    @GET("/user/ispremium")
    suspend fun getIsPremium(
        @Query("user_id") userId : Long
    ) : ResponseBody<Premium>

    /** 升级为 Premium 个人套餐会员
     *  @param  uid 待套餐升级用户ID
     */
    @FormUrlEncoded
    @POST("/user/premium/person/upgrade")
    suspend fun upgradeToPremiumPerson(
        @Field("uid") uid : Long
    ) : ResponseBody<String>

    /** 开通为 Premium 家庭组套餐
     *  @param  uid 待套餐升级用户ID
     */
    @FormUrlEncoded
    @POST("/user/premium/family/upgrade")
    suspend fun upgradeToPremiumFamily(
        @Field("uid") uid : Long
    ) : ResponseBody<String>

    /** 不同意加入家庭组的申请
     *  @param  uid 当前执行管理员用户ID
     *  @param  targetId    目标处理用户ID
     *  @param  cardId      Premium 套餐卡号ID
     */
    @FormUrlEncoded
    @POST("/user/premium/unapply")
    suspend fun disagreeUserJoinInPremiumGroup(
        @Field("uid") uid : Long,
        @Field("target_id") targetId : Long,
        @Field("card_id") cardId : String
    ) : ResponseBody<Boolean>

    /** 提交加入目标Premium家庭组的申请
     *  @param  targetId    当前执行申请用户ID
     *  @param  cardId      Premium 套餐卡号ID
     */
    @FormUrlEncoded
    @POST("/user/premium/family/apply")
    suspend fun postJoinInPremiumGroup(
        @Field("target_id") targetId : Long,
        @Field("card_id") cardId : String
    ) : ResponseBody<Boolean>
}