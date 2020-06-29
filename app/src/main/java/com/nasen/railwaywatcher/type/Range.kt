package com.nasen.railwaywatcher.type

import android.os.Parcel
import android.os.Parcelable
import com.nasen.railwaywatcher.Quadruple
import com.nasen.railwaywatcher.daysBetween
import java.io.Serializable
import java.util.*

/*
    This is a implementation of non-overlapping range
 */
class Range(var start: Int, var end: Int, var checkDay: Int) : Serializable, Parcelable {
    val inner: TreeMap<Int, Pair<Int, Date?>> = TreeMap()

    init {
        inner[end] = Pair(start, null)
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
        val size = parcel.readInt()
        for (i in 1..size) {
            val key = parcel.readInt()
            val second = parcel.readInt()
            val third = parcel.readString()?.toLong()?.let { Date(it) }
            inner[key] = Pair(second, third)
        }
    }

    fun insert(s: Int, e: Int, date: Date?) {
        // s >= start && e <= end ensured
        // range insertion algorithm
        var ss = s
        var ee = e
        for (it in inner.tailMap(s).asIterable().takeWhile { it.value.first <= e }) {
            if (it.value.first >= s && it.key <= e) {
                // subset of [s, e)
                inner.remove(it.key)
            } else {
                if (it.value.first <= s) {
                    // intersect with [s, e) or next to [s, e) by left
                    if (it.value.second == date) {
                        // combine
                        ss = it.value.first
                        inner.remove(it.key)
                    } else {
                        // divide
                        if (s > it.value.first) {
                            inner[s] = Pair(it.value.first, it.value.second)
                        }
                        inner.remove(it.key)
                    }
                }
                if (it.key >= e) { // it.value.first <= e
                    if (it.value.second == date) {
                        ee = it.key
                        inner.remove(it.key)
                    } else {
                        // divide
                        if (it.key > e) {
                            inner[it.key] = Pair(e, it.value.second)
                        }
                    }
                }
            }
        }
        inner[ee] = Pair(ss, date)
    }

    fun getUncheckedOrLateCount(): Int = inner.filter {
        it.value.second == null || checkDay - daysBetween(it.value.second!!, Date()) <= 5
    }.count()

    fun getUncheckedOrLate(): List<Quadruple<Int, Int, Int, Date?>> = inner.filter {
        it.value.second == null || checkDay - daysBetween(it.value.second!!, Date()) <= 5
    }.map {
        Quadruple(it.value.first, it.key, checkDay, it.value.second)
    }

    fun remove(s: Int, e: Int) {
        insert(s, e, null)
    }

    operator fun compareTo(other: Range): Int = compareValues(this.start, other.start)

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(start)
        dest.writeInt(end)
        dest.writeInt(checkDay)
        dest.writeInt(inner.size)
        for (subRange in inner) {
            dest.writeInt(subRange.key)
            dest.writeInt(subRange.value.first)
            dest.writeString(subRange.value.second?.time.toString())
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Range> {
        override fun createFromParcel(parcel: Parcel): Range {
            return Range(parcel)
        }

        override fun newArray(size: Int): Array<Range?> {
            return arrayOfNulls(size)
        }
    }
}