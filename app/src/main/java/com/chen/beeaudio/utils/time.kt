package com.chen.beeaudio.utils

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

/**
 *  一个时间处理的工具类
 *
 */
object TimeUtils {

    enum class Month(val monthValue : Int) {
        Jan(1),
        Feb(2),
        Mar(3),
        Apr(4),
        May(5),
        Jun(6),
        Jul(7),
        Aug(8),
        Sep(9),
        Oct(10),
        Nov(11),
        Dec(12),
    }

    enum class Week(val valueWeek : Int) {
        Sun(7),
        Mon(1),
        Tue(2),
        Wed(3),
        Thu(4),
        Fri(5),
        Sat(6),
    }

    /**
     *  自定义数据类 日期类型
     */
    data class CreatedAtDate(
        var year: Int,
        var month: Month,
        var day: Int,
        var time: CreateAtTime,
        var week: Week?,
    )

    /**
     *  自定义数据类  时间类型
     */
    data class CreateAtTime(
        var hour : Int,
        var minus : Int,
        var second : Int,
    )

    /**
     *  描述博文与现在的时间差
     *  @param  stringCreateTime：目标时间字符串
     *  @return 时间之差描述
     */
    fun descriptionBlogTimeByText(stringCreateTime : String) :String {
        // 时间戳之差
        val subTime = System.currentTimeMillis() - strToDateTime(stringCreateTime).time
        val subTimeDay = subTime/86400000
        if (subTimeDay > 0) {
            return if (subTimeDay < 7) {
                "${subTime/86400000}天前"
            } else if (subTimeDay < 32) {
                "${subTimeDay/7}周前"
            } else if (subTimeDay < 365) {
                "${subTimeDay/30}月前"
            } else {
                "${subTimeDay/365}年前"
            }
        } else {
            // 一天之内
            val subTimeSecond = subTime/1000
            return if (subTimeSecond < 60) {
                "${subTimeSecond}秒前"
            } else if (subTimeSecond < 3600) {
                "${subTimeSecond/60}分钟前"
            } else {
                "${subTimeSecond/3600}小时前"
            }

        }
    }

    /**
     *  从 Weibo.cn 接口传来的字符串时间格式转换为 时间类型
     *  @param  stringTimeParams    :   字符串时间参数
     */
    @SuppressLint("SimpleDateFormat")
    fun strToDateTime(stringTimeParams : String) : Date {
        val timeArray = stringTimeParams
            .replace("-"," ")
            .replace("T", " ")
            .replace("+"," ")
            .split(" ")
//        Log.d("Test", "时间为：${stringTimeParams}")
        try {
            val createAtDate = CreatedAtDate(
                year = timeArray[0].toInt(),
                month = convertMonth(timeArray[1]),
                day = timeArray[2].toInt(),
                time = convertTime(timeArray[3]),
                week = convertWeek("0"),
            )
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("${createAtDate.year}-${createAtDate.month.monthValue}-${createAtDate.day} ${createAtDate.time.hour}:${createAtDate.time.minus}:${createAtDate.time.second}") as Date
        } catch (e : NumberFormatException) {
            try {
                val timeArray = stringTimeParams.split(" ")
//        Log.d("Test", "时间为：${stringTimeParams}")
                val createAtDate = CreatedAtDate(
                    year = timeArray[5].toInt(),
                    month = convertMonth(timeArray[1]),
                    day = timeArray[2].toInt(),
                    time = convertTime(timeArray[3]),
                    week = convertWeek(timeArray[0]),
                )
                return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("${createAtDate.year}-${createAtDate.month.monthValue}-${createAtDate.day} ${createAtDate.time.hour}:${createAtDate.time.minus}:${createAtDate.time.second}") as Date
            } catch (e : IndexOutOfBoundsException) {
                val timeArray = stringTimeParams.split(" ","-","T",":","Z")
                val createAtDate = CreatedAtDate(
                    year = timeArray[0].toInt(),
                    month = convertMonth(timeArray[1]),
                    day = timeArray[2].toInt(),
                    time = convertTime(timeArray[3]),
                    week = convertWeek("0"),
                )
                return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("${createAtDate.year}-${createAtDate.month.monthValue}-${createAtDate.day} ${createAtDate.time.hour}:${createAtDate.time.minus}:${createAtDate.time.second}") as Date
            }
        }

    }

    /**
     *  日期格式转换为自定义日期格式
     *  @param  targetDate  :   目标日期时间
     *  @return
     */
    fun dateToStringTime(targetDate : Date) : CreatedAtDate {
        val timeArray = targetDate.toString().split(" ")

        return CreatedAtDate(
            year = timeArray[5].toInt(),
            month = convertMonth(timeArray[1]),
            day = timeArray[2].toInt(),
            time = convertTime(timeArray[3]),
            week = convertWeek(timeArray[0]),
        )
    }

    /**
     *  转换星期字符串为自定义星期时间类型
     *  @param  stringWeek  :   星期字符串
     *  @return
     */
    private fun convertWeek(stringWeek : String) :Week {
        return when(stringWeek) {
            "Sun"   ->  Week.Sun
            "7"     ->  Week.Sun
            "Mon"   ->  Week.Mon
            "1"     ->  Week.Mon
            "Tue"   ->  Week.Tue
            "2"     ->  Week.Tue
            "Wed"   ->  Week.Wed
            "3"     ->  Week.Wed
            "Thu"   ->  Week.Thu
            "4"     ->  Week.Thu
            "Fri"   ->  Week.Fri
            "5"     ->  Week.Fri
            "Sat"   ->  Week.Sat
            "6"     ->  Week.Sat
            else    ->  Week.Sun
        }
    }

    /**
     *  转换月份字符串为自定义月份时间类型
     *  @param  stringMonth :   月份字符串
     *  @return
     */
    private fun convertMonth(stringMonth : String) :Month {
        return when(stringMonth) {
            "Jan"   ->  Month.Jan
            "1"     ->  Month.Jan
            "01"    ->  Month.Jan
            "Feb"   ->  Month.Feb
            "2"     ->  Month.Feb
            "02"    ->  Month.Feb
            "Mar"   ->  Month.Mar
            "3"     ->  Month.Mar
            "03"    ->  Month.Mar
            "Apr"   ->  Month.Apr
            "4"     ->  Month.Apr
            "04"    ->  Month.Apr
            "May"   ->  Month.May
            "5"     ->  Month.May
            "05"    ->  Month.May
            "Jun"   ->  Month.Jun
            "6"     ->  Month.Jun
            "06"    ->  Month.Jun
            "Jul"   ->  Month.Jul
            "7"     ->  Month.Jul
            "07"    ->  Month.Jul
            "Aug"   ->  Month.Aug
            "8"     ->  Month.Aug
            "08"    ->  Month.Aug
            "Sep"   ->  Month.Sep
            "9"     ->  Month.Sep
            "09"    ->  Month.Sep
            "Oct"   ->  Month.Oct
            "10"    ->  Month.Oct
            "Nov"   ->  Month.Nov
            "11"    ->  Month.Nov
            "Dec"   ->  Month.Dec
            "12"    ->  Month.Dec
            else    ->  Month.Jan
        }
    }

    /**
     *  转换时间字符串为 时间整型数组
     *  @param  stringTime  :   时间字符串
     *  @return
     */
    private fun convertTime(stringTime : String) :CreateAtTime{
        val (h, m, s) = stringTime.replace("Z","").split(":")
        return CreateAtTime(
            h.toInt(),
            m.toInt(),
            s.toInt()
        )
    }

    /**
     *  转换日期字符串为 日期整型数组
     *  @param  stringDate  :   日期人字符串
     *  @return
     */
    private fun convertDate(stringDate : String) : Array<Int> {
        val (y, m, d) = stringDate.split("-")
        return arrayOf(y.toInt(), m.toInt(), d.toInt())
    }

    /* 获取当前日期时间 */
    @SuppressLint("SimpleDateFormat")
    fun getCurrentTime() : String {
        val c = Calendar.getInstance()
        return if (Build.VERSION.SDK_INT >= 24) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        } else {
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            val second = c.get(Calendar.SECOND)
            "$year-$month-$day ${hour.let { if (it.toString().length<=1) "0${hour}" else hour.toString() }}:${minute.let { if (it.toString().length<=1) "0${minute}" else minute.toString() }}:${second.let { if (it.toString().length<=1) "0${second}" else second.toString() }}"
        }
    }

    /**
     *  Weibo.cn 字符时间格式 准换为 时间戳
     */
    fun convertStrToLongTimeUnit(createAtTime: String) : Long{
        /* 有效的发布时间 - 返回其值为 时间戳 的Long类型*/
        return if (createAtTime.isNotBlank()) {
            strToDateTime(createAtTime).time
        } else {
            /* 无效的发布时间 - 返回值为 0 的 Long 类型*/
            0
        }
    }
}