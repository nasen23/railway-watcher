package com.nasen.railwaywatcher

import java.util.*

data class Quadruple<T1, T2, T3, T4>(val t1: T1, val t2: T2, val t3: T3, val t4: T4)
data class Quintuple<T1, T2, T3, T4, T5>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5
) {
    fun set5(new: T5): Quintuple<T1, T2, T3, T4, T5> = Quintuple(t1, t2, t3, t4, new)
}

fun daysBetween(d1: Date, d2: Date): Long = (d2.time - d1.time) / 1000 / 60 / 60 / 24
