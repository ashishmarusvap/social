package com.social
import android.os.Parcel
import android.os.Parcelable

data class SocialLoginModel(
    val id: String,
    val name: String,
    val email: String? = "",
    val imgUrl: String?,
    val loginType: String
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString().toString(),
        source.readString().toString(),
        source.readString().toString(),
        source.readString().toString(),
        source.readString().toString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(imgUrl)
        writeString(loginType)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SocialLoginModel> =
            object : Parcelable.Creator<SocialLoginModel> {
                override fun createFromParcel(source: Parcel): SocialLoginModel =
                    SocialLoginModel(source)
                override fun newArray(size: Int): Array<SocialLoginModel?> = arrayOfNulls(size)
            }
    }
}