package com.chen.beeaudio.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.chen.beeaudio.proto.TokenModel
import com.chen.beeaudio.proto.TokenModelSerializer
import kotlinx.coroutines.flow.Flow

class DataStoreManager(
    val context : Context
) {
    private val Context.tokenDataStore: DataStore<TokenModel> by dataStore(
        fileName = "data.proto",
        serializer = TokenModelSerializer
    )

    suspend fun saveData(
        accessToken: String,
        expiresIn: Long,
        refreshToken: String,
        scope: String,
        tokenType: String
    ) {
        context.tokenDataStore.updateData { preference ->
            preference.toBuilder()
                .setAccessToken(accessToken)
                .setExpiresIn(expiresIn)
                .setRefreshToken(refreshToken)
                .setScope(scope)
                .setTokenType(tokenType)
                .build()
        }
    }

    val tokenModel: Flow<TokenModel> = context.tokenDataStore.data
}