package com.nasen.railwaywatcher.type

import com.nasen.railwaywatcher.Quintuple
import com.nasen.railwaywatcher.daysBetween
import java.util.*

/*
    This is a implementation of non-overlapping range
 */
abstract class Range(var start: Int, var end: Int, var checkDay: Int) {
    abstract fun insertRecord(s: Int, e: Int, date: Date?, up: Boolean?)
    abstract fun removeRecord(s: Int, e: Int, up: Boolean?)
    abstract fun getAll(): List<Quintuple<Int, Int, Int, Date?, Boolean?>>
    abstract fun getUncheckedOrLate(): List<Quintuple<Int, Int, Int, Date?, Boolean?>>
    abstract fun getUncheckedOrLateCount(): Int
}

class SingleRange(start: Int, end: Int, checkDay: Int) : Range(start, end, checkDay) {
    val inner: TreeMap<Int, Pair<Int, Date?>> = TreeMap()

    init {
        inner[end] = Pair(start, null)
    }

    override fun insertRecord(s: Int, e: Int, date: Date?, up: Boolean?) {
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

    override fun getAll(): List<Quintuple<Int, Int, Int, Date?, Boolean?>> = inner.map {
        val dir: Boolean? = null
        Quintuple(it.value.first, it.key, checkDay, it.value.second, dir)
    }

    override fun getUncheckedOrLate(): List<Quintuple<Int, Int, Int, Date?, Boolean?>> =
        inner.filter {
            it.value.second?.let { d -> daysBetween(d, Date()) > checkDay - 10 } ?: true
        }.map {
            val dir: Boolean? = null
            Quintuple(it.value.first, it.key, checkDay, it.value.second, dir)
        }

    override fun getUncheckedOrLateCount(): Int = inner.filter {
        it.value.second?.let { d -> daysBetween(d, Date()) > checkDay - 10 } ?: true
    }.count()

    override fun removeRecord(s: Int, e: Int, up: Boolean?) {
        insertRecord(s, e, null, null)
    }

    operator fun compareTo(other: Range): Int = compareValues(this.start, other.start)

}

class DoubleRange(start: Int, end: Int, checkDay: Int) : Range(start, end, checkDay) {
    var r1 = SingleRange(start, end, checkDay)
    var r2 = SingleRange(start, end, checkDay)

    override fun insertRecord(s: Int, e: Int, date: Date?, up: Boolean?) {
        if (up == null) {
            r1.insertRecord(s, e, date, null)
            r2.insertRecord(s, e, date, null)
        } else if (up) {
            r1.insertRecord(s, e, date, null)
        } else {
            r2.insertRecord(s, e, date, null)
        }
    }

    override fun removeRecord(s: Int, e: Int, up: Boolean?) {
        if (up == null) {
            r1.insertRecord(s, e, null, null)
            r2.insertRecord(s, e, null, null)
        } else if (up) {
            r1.insertRecord(s, e, null, null)
        } else {
            r2.insertRecord(s, e, null, null)
        }
    }

    override fun getAll(): List<Quintuple<Int, Int, Int, Date?, Boolean?>> =
        r1.getAll().map { it.set5(true) } + r2.getAll().map { it.set5(false) }

    override fun getUncheckedOrLate(): List<Quintuple<Int, Int, Int, Date?, Boolean?>> =
        r1.getUncheckedOrLate().map { it.set5(true) } + r2.getUncheckedOrLate()
            .map { it.set5(false) }

    override fun getUncheckedOrLateCount(): Int =
        r1.getUncheckedOrLateCount() + r2.getUncheckedOrLateCount()

}