package com.nasen.railwaywatcher.type

import android.os.Parcel
import android.os.Parcelable
import com.nasen.railwaywatcher.Quadruple
import com.nasen.railwaywatcher.RangeOutOfBounds
import java.io.Serializable
import java.util.*

class Railway(
    val from: String,
    val to: String,
    val start: Int,
    val end: Int,
    val ranges: List<Range>
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.createTypedArrayList(Range)!!
    ) {
    }

    fun insertRecord(s: Int, e: Int, date: Date) {
        val range = ranges.find { range -> range.start <= s && range.end >= e }
        range ?: throw RangeOutOfBounds(s, e)
        range.insert(s, e, date)
    }

    fun getUncheckedOrLate(): List<Quadruple<Int, Int, Int, Date?>> =
        ranges.flatMap { range -> range.getUncheckedOrLate() }

    fun removeRecord(s: Int, e: Int) {
        val range = ranges.find { range -> range.start <= s && range.end >= e }
        range?.remove(s, e)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(from)
        parcel.writeString(to)
        parcel.writeInt(start)
        parcel.writeInt(end)
        parcel.writeTypedList(ranges)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Railway> {
        override fun createFromParcel(parcel: Parcel): Railway {
            return Railway(parcel)
        }

        override fun newArray(size: Int): Array<Railway?> {
            return arrayOfNulls(size)
        }
    }
}