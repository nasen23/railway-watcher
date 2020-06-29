package com.nasen.railwaywatcher

import com.nasen.railwaywatcher.type.Range
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RangeTest {
    @Test
    fun insert() {
        val range = Range(0, 100, 30)
        assertEquals(range.inner, mutableMapOf(100 to Pair(0, null)))
        range.insert(0, 100, null)
        assertEquals(range.inner, mutableMapOf(100 to Pair(0, null)))
        range.insert(50, 100, null)
        assertEquals(range.inner, mutableMapOf(100 to Pair(0, null)))
        val date1 = Date()
        range.insert(0, 100, date1)
        assertEquals(range.inner, mutableMapOf(100 to Pair(0, date1)))
        val date2 = Date(date1.time + 4000)
        assert(date2.after(date1))
        range.insert(50, 100, date2)
        assertEquals(range.inner, mutableMapOf(50 to Pair(0, date1), 100 to Pair(50, date2)))
        val date3 = Date(date1.time - 4000)
        assert(date3.before(date1))
        range.insert(25, 75, date3)
        assertEquals(
            mutableMapOf(
                25 to Pair(0, date1),
                100 to Pair(75, date2),
                75 to Pair(25, date3)
            ), range.inner
        )
        // combine
        range.insert(75, 100, date3)
        assertEquals(mutableMapOf(25 to Pair(0, date1), 100 to Pair(25, date3)), range.inner)
        range.insert(0, 100, date1)
        assertEquals(mutableMapOf(100 to Pair(0, date1)), range.inner)
        range.insert(50, 75, null)
        assertEquals(
            mutableMapOf(
                50 to Pair(0, date1),
                75 to Pair(50, null),
                100 to Pair(75, date1)
            ), range.inner
        )
        range.insert(0, 100, null)
        range.insert(0, 20, date1)
        assertEquals(mutableMapOf(20 to Pair(0, date1), 100 to Pair(20, null)), range.inner)
        range.insert(80, 100, date2)
        assertEquals(
            mutableMapOf(
                20 to Pair(0, date1),
                80 to Pair(20, null),
                100 to Pair(80, date2)
            ), range.inner
        )
    }
}