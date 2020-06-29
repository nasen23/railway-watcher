package com.nasen.railwaywatcher

import java.util.*

data class Quadruple<T1, T2, T3, T4>(val t1: T1, val t2: T2, val t3: T3, val t4: T4)

fun daysBetween(d1: Date, d2: Date): Long {
    // Assume d1 < d2
    val diff = d2.time - d1.time
    return diff / 1000 / 60 / 60 / 24
}
