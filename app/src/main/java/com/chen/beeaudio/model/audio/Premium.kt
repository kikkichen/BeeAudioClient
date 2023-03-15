package com.chen.beeaudio.model.audio

import com.chen.beeaudio.model.blog.RequestUser
import com.google.gson.annotations.SerializedName

data class Premium(
    @SerializedName("card_id") val card_id: String,
    @SerializedName("card_type") val card_type: Int,
    @SerializedName("server_expired") val server_expired: String,
    @SerializedName("server_in") val server_in: String,
    @SerializedName("uid") val uid: Long
)

data class FamilyPremium(
    @SerializedName("numbers") val numbers : List<RequestUser>,
    @SerializedName("summarize") val summarize : Premium
)

data class FamilyPremiumNumbers(
    @SerializedName("apply_numbers") val applyNumbers : List<RequestUser>,
    @SerializedName("formal_numbers") val formalNumbers : List<RequestUser>
)