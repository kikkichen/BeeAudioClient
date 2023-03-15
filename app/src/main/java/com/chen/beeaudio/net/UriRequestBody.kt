package com.chen.beeaudio.net

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink

class UriRequestBody(val context: Context, val uri: Uri) : RequestBody() {
    override fun contentType(): MediaType =
        (context.contentResolver.getType(uri) ?: "multipart/form-data").toMediaType()

    @SuppressLint("Recycle")
    override fun writeTo(sink: BufferedSink) {
        val ips = context.contentResolver.openInputStream(uri)
        ips?.let {
            sink.writeAll(Buffer().readFrom(it, contentLength()))
        }
    }

    @SuppressLint("Recycle")
    override fun contentLength(): Long = context.contentResolver.openFileDescriptor(uri, "r")?.statSize?:-1L
}

/** 生成上传文件的 MultipartBody.Part 参数
 *  @param  context 上下文参数
 *  @param  name    参数名
 *  @param  fileName    文件名
 *  @param  uri     Uri字段
 */
fun getPart(context: Context, filename: String, extendName: String, uri: Uri): MultipartBody.Part{
    return MultipartBody.Part.createFormData(filename, extendName, UriRequestBody(context, uri))
}