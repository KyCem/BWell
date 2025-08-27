package com.example.bwell.model

import android.os.Parcel
import android.os.Parcelable

data class LeaderBoardItem(
    val uid: String = "",
    val name: String,
    val score: Long,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(name)
        parcel.writeLong(score)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel) = Task(parcel)
        override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)
    }
}
