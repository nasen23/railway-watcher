package com.nasen.railwaywatcher

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nasen.railwaywatcher.helper.RuntimeTypeAdapterFactory
import com.nasen.railwaywatcher.type.DoubleRange
import com.nasen.railwaywatcher.type.Railway
import com.nasen.railwaywatcher.type.Range
import com.nasen.railwaywatcher.type.SingleRange
import java.io.Reader
import java.util.*

inline fun <reified T> Gson.fromJson(reader: Reader): T =
    fromJson<T>(reader, object : TypeToken<T>() {}.type)

object Global {
    lateinit var gson: Gson
    lateinit var railways: MutableLiveData<MutableList<Railway>>
    lateinit var context: Context

    fun init(context: Context) {
        this.context = context
        val adapter = RuntimeTypeAdapterFactory.of(Range::class.java)
            .registerSubtype(SingleRange::class.java)
            .registerSubtype(DoubleRange::class.java)
        gson = GsonBuilder().registerTypeAdapterFactory(adapter).create()
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

    fun addRecord(idx: Int, start: Int, end: Int, date: Date, up: Boolean?) {
        railways.value?.get(idx)?.insertRecord(start, end, date, up)
        serializeRailways()
    }

    fun removeRecord(idx: Int, start: Int, end: Int, up: Boolean?) {
        railways.value?.get(idx)?.removeRecord(start, end, up)
        serializeRailways()
    }

    fun setCheckPeriod(idx1: Int, idx2: Int, new: Int) {
        railways.value?.get(idx1)?.ranges?.get(idx2)?.checkDay = new
        serializeRailways()
    }

    private fun serializeRailways() {
        context.openFileOutput("railway.json", Context.MODE_PRIVATE).bufferedWriter().use {
            val json = gson.toJson(railways.value!!)
            it.write(json)
            it.flush()
        }
    }

    private fun deserializeRailways(): MutableList<Railway> =
        try {
            context.openFileInput("railway.json").bufferedReader().let {
                gson.fromJson(it)
            }
        } catch (ignored: Exception) {
            mutableListOf()
        }
}