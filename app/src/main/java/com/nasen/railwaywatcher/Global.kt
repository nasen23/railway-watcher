package com.nasen.railwaywatcher

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.nasen.railwaywatcher.type.Railway
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

object Global {
    lateinit var railways: MutableLiveData<MutableList<Railway>>
    lateinit var context: Context

    fun init(context: Context) {
        this.context = context
        val inner = deserializeRailways()
        railways = MutableLiveData(inner)
    }

    fun add(new: Railway) {
        railways.value?.add(new)
        serializeRailways()
    }

    fun remove(idx: Int) {
        railways.value?.removeAt(idx)
        serializeRailways()
    }

    fun get(idx: Int): Railway = railways.value!![idx]

    fun addRecord(idx: Int, start: Int, end: Int, date: Date) {
        railways.value?.get(idx)?.insertRecord(start, end, date)
        serializeRailways()
    }

    fun removeRecord(idx: Int, start: Int, end: Int) {
        railways.value?.get(idx)?.removeRecord(start, end)
        serializeRailways()
    }

    fun setCheckPeriod(idx1: Int, idx2: Int, new: Int) {
        railways.value?.get(idx1)?.ranges?.get(idx2)?.checkDay = new
        serializeRailways()
    }

    private fun serializeRailways() {
        ObjectOutputStream(context.openFileOutput("railway.dat", Activity.MODE_PRIVATE)).use {
            it.writeObject(railways.value)
        }
    }

    private fun deserializeRailways(): MutableList<Railway> {
        try {
            val ois = ObjectInputStream(context.openFileInput("railway.dat"))
            val res = ois.readObject() as List<*>
            return if (res.isNotEmpty()) {
                @Suppress("UNCHECKED_CAST")
                res as MutableList<Railway>
            } else {
                mutableListOf()
            }
        } catch (e: Exception) {
            return mutableListOf()
        }
    }
}