package com.nasen.railwaywatcher

class RangeOutOfBounds(val start: Int, val end: Int) : Exception()
class SplitPointOutOfBounds(val pos: Int) : Exception()