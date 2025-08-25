package com.example.bwell.model

import android.os.Parcel
import android.os.Parcelable

data class DietEntry(
    val name: String,
    val calories: Int,
    val partOfDay: String // "Morning" | "Afternoon" | "Evening"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "Morning"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(calories)
        parcel.writeString(partOfDay)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<DietEntry> {
        override fun createFromParcel(parcel: Parcel) = DietEntry(parcel)
        override fun newArray(size: Int): Array<DietEntry?> = arrayOfNulls(size)
    }
}
