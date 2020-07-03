package com.nasen.railwaywatcher.type

import com.nasen.railwaywatcher.Quintuple
import java.util.*

class Railway(
    val name: String,
    val start: Int,
    val end: Int,
    val ranges: List<Range>
) {

    fun insertRecord(s: Int, e: Int, date: Date?, up: Boolean?) {
        ranges.find { it.start <= s && it.end >= e }?.insertRecord(s, e, date, up)
    }

    fun getUncheckedOrLateCount(): Int = ranges.sumBy { it.getUncheckedOrLateCount() }

    fun getUncheckedOrLate(): List<Quintuple<Int, Int, Int, Date?, Boolean?>> = ranges.flatMap {
        it.getUncheckedOrLate()
    }

    fun getAll(): List<Quintuple<Int, Int, Int, Date?, Boolean?>> = ranges.flatMap { it.getAll() }

    fun removeRecord(s: Int, e: Int, up: Boolean?) {
        insertRecord(s, e, null, up)
    }
}