package com.chen.beeaudio

import com.chen.beeaudio.model.audio.Tag
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.model.localmodel.Subscribe
import com.chen.beeaudio.utils.TimeUtils.strToDateTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okio.internal.commonAsUtf8ToByteArray
import org.junit.Test

import org.junit.Assert.*
import java.util.Base64
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun time_format_turn_off() {
        var netTime = "2022-12-28T02:39:34+08:00"
        println(strToDateTime(netTime))
    }

    @Test
    fun song_format_to_json() {
        val rawJson = "[share_track]{\"al\":{\"id\":156221935,\"name\":\"水之幻想\",\"pic\":109951168129270910,\"picUrl\":\"https://p2.music.126.net/kVb7tiamBwEEPzR0SlJb3g\\u003d\\u003d/109951168129270915.jpg\"},\"ar\":[{\"id\":12121264,\"name\":\"Ice Paper\"},{\"id\":47192128,\"name\":\"早安\"}],\"dt\":213360,\"fee\":8,\"id\":2005218684,\"name\":\"揽月\",\"noCopyrightRcmd\":\"\",\"privilege_signal\":0,\"source\":\"126.net\",\"usable\":true}"
        val encodeRawJson : String = Base64.getEncoder().encodeToString(rawJson.toByteArray())
        println("Byte : $encodeRawJson")
        val rawString : String = String(Base64.getDecoder().decode(encodeRawJson))
        println("String: $rawString")
        println("String: ${Gson().fromJson(rawString.replace("[share_track]", ""), Track::class.java)}")
    }

    @Test
    fun test_gson_to_subscribe_data() {
        val jsonString = "[]"
        val updateSubscribeDataList = Gson().fromJson<List<Subscribe>>(jsonString, object : TypeToken<List<Subscribe>>() {}.type)
        println(updateSubscribeDataList)
    }

    @Test
    fun test_regex_email() {
        val email = "sample110@163.com"
        val compile = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(.[a-zA-Z0-9_-]+)+\$")
        val matcher = compile.matcher(email)
        println(matcher.matches())

    }
}