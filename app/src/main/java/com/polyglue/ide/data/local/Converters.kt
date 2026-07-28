package com.polyglue.ide.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value == null) return emptyList()
        return gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String = gson.toJson(list ?: emptyList<String>())

    @TypeConverter
    fun fromFloatList(value: String?): List<Float> {
        if (value == null) return emptyList()
        return gson.fromJson(value, object : TypeToken<List<Float>>() {}.type)
    }

    @TypeConverter
    fun toFloatList(list: List<Float>?): String = gson.toJson(list ?: emptyList<Float>())

    @TypeConverter
    fun fromMap(value: String?): Map<String, String> {
        if (value == null) return emptyMap()
        return gson.fromJson(value, object : TypeToken<Map<String, String>>() {}.type)
    }

    @TypeConverter
    fun toMap(map: Map<String, String>?): String = gson.toJson(map ?: emptyMap<String, String>())

    @TypeConverter
    fun fromJsonObject(value: String?): com.google.gson.JsonObject {
        if (value == null) return com.google.gson.JsonObject()
        return gson.fromJson(value, com.google.gson.JsonObject::class.java)
    }

    @TypeConverter
    fun toJsonObject(obj: com.google.gson.JsonObject?): String = gson.toJson(obj ?: com.google.gson.JsonObject())
}
