package com.chen.beeaudio.navigation.argument

import android.os.Parcel
import android.os.Parcelable

data class ShareType(val content: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: ""
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(content)
    }

    companion object CREATOR : Parcelable.Creator<ShareType> {
        override fun createFromParcel(parcel: Parcel): ShareType {
            return ShareType(parcel)
        }

        override fun newArray(size: Int): Array<ShareType?> {
            return arrayOfNulls(size)
        }
    }

}
