package com.chen.beeaudio.proto

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

object TokenModelSerializer : Serializer<TokenModel>{
    override val defaultValue: TokenModel
        get() = TokenModel.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): TokenModel {
        return try {
            TokenModel.parseFrom(input)
        } catch (exception : Exception) {
            TokenModel.getDefaultInstance()
        }
    }

    override suspend fun writeTo(t: TokenModel, output: OutputStream) {
        t.writeTo(output)
    }
}