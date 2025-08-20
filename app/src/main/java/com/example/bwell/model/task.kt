package com.example.bwell.model

import android.os.Parcel
import android.os.Parcelable

data class Task(
    val title: String,
    val description: String,
    val dueAtMillis: Long,   // store time as epoch millis
    val area: String,
    val urgency: Int         // 1..5 (or whatever scale you prefer)
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeLong(dueAtMillis)
        parcel.writeString(area)
        parcel.writeInt(urgency)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel) = Task(parcel)
        override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)
    }
}
