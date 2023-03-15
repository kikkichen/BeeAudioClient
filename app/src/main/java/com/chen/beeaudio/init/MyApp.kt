package com.chen.beeaudio.init

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

const val ACCESS_TOKEN = "2.00lgUztDZZ8CwD06439dcfc0022dYb"
const val REMOTE_SERVER_URL = "https://api.weibo.com"
const val LOCAL_SERVER_URL = "http://po.shigure.space:8099"
const val AUTH_SERVER_URL = "http://po.shigure.space:9096"

const val AUTH_CLIENT_USERNAME = "test_client_1"
const val AUTH_CLIENT_PASSWORD = "test_secret_1"

const val WEIBO_LARGE_IMAGE_URL_PREFIX = "http://wx2.sinaimg.cn/large"
const val WEIBO_MIDDLE_IMAGE_URL_PREFIX = "http://wx2.sinaimg.cn/bmiddle"
const val WEIBO_THUMBNAIL_IMAGE_URL_PREFIX = "http://wx2.sinaimg.cn/thumbnail"

const val LOCAL_SERVER_THUMBNAIL_PREFIX = "http://po.shigure.space:8099/blog/thumbnail"
const val LOCAL_SERVER_LARGE_PREFIX = "http://po.shigure.space:8099/blog/large"

const val LOCAL_ROOM_DATABASE_NAME = "blog_database"
const val LOCAL_ROOM_BLOG_TABLE = "blog_table"
const val LOCAL_ROOM_RETWEETED_BLOG_TABLE = "retweeted_blog_table"
const val LOCAL_ROOM_PICTURE_URL_GROUP_TABLE = "pic_group_table"
const val LOCAL_ROOM_USER_TABLE = "user_table"
const val LOCAL_ROOM_BLOG_REMOTE_KEY_TABLE = "blog_remote_key_table"
const val LOCAL_ROOM_TRACK_SUMMARY_TABLE = "track_summary_table"
const val LOCAL_ROOM_SUBSCRIBE_TABLE = "subscribe_table"

/* 草稿数据库名称 */
const val DRAFT_DATABASE_NAME = "blog_draft_database"

/* 云析API  */
const val CLOUD_CONN_URL = "http://api.a20safe.com/api.php/"
const val CLOUD_CONN_KEY = "7d06a110e9e20a684e02934549db1d3d"

/* FastMock */
const val FAST_MOCK_URL = "https://www.fastmock.site/"

// Paging Value
const val BLOGS_PER_PAGE_SIZE = 20
const val BLOGS_INITIAL_SIZE = 20

@HiltAndroidApp
class MyApp : Application() {
}