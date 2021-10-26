package com.luckyaf.smarthttp.body

import androidx.annotation.NonNull
import com.luckyaf.smarthttp.model.ContentType
import okhttp3.MediaType
import okio.BufferedSink

import org.json.JSONArray

import org.json.JSONObject

import okhttp3.RequestBody
import okio.IOException


/**
 * 类描述：
 * @author Created by luckyAF on 2021/10/26
 *
 */
class JsonBody : RequestBody {
    /** Json 数据  */
    private val mJson: String

    /** 字节数组  */
    private val mBytes: ByteArray

    constructor(map: Map<*, *>?) : this(JSONObject(map)) {}
    constructor(list: List<*>?) : this(JSONArray(list)) {}
    constructor(jsonObject: JSONObject) {
        mJson = jsonObject.toString()
        mBytes = mJson.toByteArray()
    }

    constructor(jsonArray: JSONArray) {
        mJson = jsonArray.toString()
        mBytes = mJson.toByteArray()
    }

    constructor(json: String) {
        mJson = json
        mBytes = mJson.toByteArray()
    }

    override fun contentType(): MediaType {
        return ContentType.JSON
    }

    override fun contentLength(): Long {
        // 需要注意：这里需要用字节数组的长度来计算
        return mBytes.size.toLong()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        sink.write(mBytes, 0, mBytes.size)
    }

    /**
     * 获取 Json 字符串
     */
    fun getJson(): String {
        return mJson
    }

    @NonNull
    override fun toString(): String {
        return mJson
    }
}