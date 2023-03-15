package com.chen.beeaudio.model.audio

import com.google.gson.annotations.SerializedName

data class Tag(
    @SerializedName("name")
    val name : String,
    @SerializedName("category")
    val category : Int
)

data class HotAndAllTags(
    @SerializedName("hot")
    val hotTags : List<Tag>,
    @SerializedName("all")
    val allTags : List<Tag>,
)