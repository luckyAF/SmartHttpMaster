package com.luckyaf.smarthttp.config

import org.json.JSONException
import org.json.JSONObject
import org.json.JSONArray

import okhttp3.RequestBody
import java.io.File
import java.io.InputStream
import java.lang.reflect.Field


/**
 * 类描述：
 * @author Created by luckyAF on 2021/10/26
 *
 */
class DefaultParamsConverter : IParamsConverter {

    override fun paramsToJsonObject(value: Any): Any? {
        return if (value is List<*>) {
            // 如果这是一个 List 参数
            listToJsonArray(value as List<*>?)
        } else if (value is Map<*, *>) {
            // 如果这是一个 Map 参数
            mapToJsonObject(value as Map<*, *>?)
        } else if (isBeanType(value)) {
            // 如果这是一个 Bean 参数
            mapToJsonObject(beanToHashMap(value)))
        } else {
            // 如果这是一个普通的参数
            value
        }
    }

     fun paramsToJsonObject(map: Map<String, Any>): JSONObject {
        val jsonObject = JSONObject()
        if (map.isEmpty()) {
            return jsonObject
        }

        val keySet: Set<*> = map.keys
        for (key in keySet) {
            val value = map[key]
            if (isEmpty(value)) {
                continue
            }
            try {
                if (value is List<*>) {
                    jsonObject.put(key.toString(), listToJsonArray(value as List<*>?))
                } else if (value is Map<*, *>) {
                    jsonObject.put(key.toString(), mapToJsonObject(value as Map<*, *>?))
                } else if (isBeanType(value)) {
                    jsonObject.put(key.toString(), mapToJsonObject(beanToHashMap(value)))
                } else {
                    jsonObject.put(key.toString(), value)
                }
            } catch (e: JSONException) {

            }
        }
        return jsonObject
    }


    /**
     * 将 Bean 类转成 HashMap 对象
     */
    fun beanToHashMap(data: Any?): HashMap<String, Any?>? {
        if (data == null) {
            return null
        }
        val fields: Array<Field> = data.javaClass.declaredFields
        val result: HashMap<String, Any?> = HashMap(fields.size)
        for (field in fields) {
            // 允许访问私有字段
            field.setAccessible(true)
            try {
                // 获取字段的对象
                val value: Any = field.get(data)
                val key = field.name

                if (value is List<*>) {
                    result[key] = listToJsonArray(value)
                } else if (value is Map<*, *>) {
                    result[key] = mapToJsonObject(value)
                } else if (isBeanType(value)) {
                    result[key] = beanToHashMap(value)
                } else {
                    result[key] = value
                }
            } catch (e: IllegalAccessException) {
            }
        }
        return result
    }

    /**
     * 将 List 集合转 JsonArray 对象
     */
    fun listToJsonArray(list: List<*>?): JSONArray? {
        val jsonArray = JSONArray()
        if (list == null || list.isEmpty()) {
            return jsonArray
        }
        for (value in list) {
            if (isEmpty(value)) {
                continue
            }
            if (value is List<*>) {
                jsonArray.put(listToJsonArray(value as List<*>?))
            } else if (value is Map<*, *>) {
                jsonArray.put(mapToJsonObject(value as Map<*, *>?))
            } else if (isBeanType(value)) {
                jsonArray.put(mapToJsonObject(beanToHashMap(value)))
            } else {
                jsonArray.put(value)
            }
        }
        return jsonArray
    }

    /**
     * 将 Map 集合转成 JsonObject 对象
     */
    fun mapToJsonObject(map: Map<*, *>?): JSONObject? {
        val jsonObject = JSONObject()
        if (map == null || map.isEmpty()) {
            return jsonObject
        }
        val keySet = map.keys
        for (key in keySet) {
            val value = map[key]
            if (isEmpty(value)) {
                continue
            }
            try {
                if (value is List<*>) {
                    jsonObject.put(key.toString(), listToJsonArray(value as List<*>?))
                } else if (value is Map<*, *>) {
                    jsonObject.put(key.toString(), mapToJsonObject(value as Map<*, *>?))
                } else if (isBeanType(value)) {
                    jsonObject.put(key.toString(), mapToJsonObject(beanToHashMap(value)))
                } else {
                    jsonObject.put(key.toString(), value)
                }
            } catch (e: JSONException) {
            }
        }
        return jsonObject
    }


    /**
     * 判断对象或者集合是否为空
     */
    private fun isEmpty(data: Any?): Boolean {
        if (data == null) {
            return true
        }
        return if (data is List<*> && data.isEmpty()) {
            true
        } else data is Map<*, *> && data.isEmpty()
    }

    /**
     * 判断对象是否为 Bean 类
     */
    private fun isBeanType(data: Any?): Boolean {
        return if (data == null) {
            false
        } else !(data is Number || data is CharSequence || data is Boolean ||
                data is File || data is InputStream || data is RequestBody ||
                data is Char || data is JSONObject || data is JSONArray)
        // Number：Long、Integer、Short、Double、Float、Byte
        // CharSequence：String、StringBuilder、StringBuilder
    }


}