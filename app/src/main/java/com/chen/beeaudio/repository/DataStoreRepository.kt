package com.chen.beeaudio.repository

import com.chen.beeaudio.proto.TokenModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataStoreRepository @Inject constructor(
    val dataStoreManager: DataStoreManager
) {
    suspend fun saveData(
        accessToken: String,
        expiresIn: Long,
        refreshToken: String,
        scope: String,
        tokenType: String
    ) {
        dataStoreManager.saveData(accessToken, expiresIn, refreshToken, scope, tokenType)
    }

    val tokenModel: Flow<TokenModel> = dataStoreManager.tokenModel
}