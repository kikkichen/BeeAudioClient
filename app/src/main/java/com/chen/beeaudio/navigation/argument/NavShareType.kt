package com.chen.beeaudio.navigation.argument

import android.os.Bundle
import androidx.navigation.NavType
import com.google.gson.Gson

class NavShareType : NavType<ShareType>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): ShareType? {
        return bundle.getParcelable(key)
    }

    override fun parseValue(value: String): ShareType {
        return Gson().fromJson(value , ShareType::class.java)
    }

    override fun put(bundle: Bundle, key: String, value: ShareType) {
        bundle.putParcelable(key, value)
    }
}