package com.luckyaf.smarthttp.request

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleObserver
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.luckyaf.smarthttp.SmartHttpConfig
import com.luckyaf.smarthttp.download.DownloadInterceptor
import com.luckyaf.smarthttp.download.ReadCallback
import com.luckyaf.smarthttp.executor.AppExecutors
import com.luckyaf.smarthttp.extension.mediaType
import com.luckyaf.smarthttp.model.HttpMethod
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import okhttp3.FormBody

import okhttp3.MultipartBody

import okhttp3.RequestBody
import com.luckyaf.smarthttp.listener.HttpCallback
import com.luckyaf.smarthttp.listener.OnDownloadListener
import com.luckyaf.smarthttp.model.CallProxy
import com.luckyaf.smarthttp.utils.FileIOUtil
import okhttp3.RequestBody.Companion.toRequestBody


/**
 * 类描述：通用请求
 * @author Created by luckyAF on 2019/4/26
 *
 */
data class CommonRequest(
    @HttpMethod var method: String = HttpMethod.GET,
    private var mTag: Any? = null,
    private var mUrl: String = "",
    private var mHeaders: ArrayList<Pair<String, String>> = arrayListOf(),
    private var mParams: ArrayList<Pair<String, Any>> = arrayListOf()
) : LifecycleObserver {
    companion object {
        fun get() = CommonRequest(HttpMethod.GET)
        fun post() = CommonRequest(HttpMethod.POST)
        fun head() = CommonRequest(HttpMethod.HEAD)
        fun patch() = CommonRequest(HttpMethod.PATCH)
        fun put() = CommonRequest(HttpMethod.PUT)
        fun delete() = CommonRequest(HttpMethod.DELETE)
    }


    private val appExecutors by lazy {
        AppExecutors()
    }
    /**
     * 主线程的handler
     */
    private val mainThreadHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    /** 请求执行代理类  */
    private val mCallProxy: CallProxy? = null
    public  var errorMessage: String = ""

    fun tag(tag: Any): CommonRequest {
        mTag = tag
        return this
    }


    fun url(url: String): CommonRequest {
        mUrl = url
        // 如果 tag为空 则设置tag为当前url
        mTag = mTag ?: mUrl
        return this
    }

    private var saveFile = File("temp.txt")

    fun asFile(dir: String, name: String): CommonRequest {
        val filePath = File(dir)
        if (!filePath.exists()) {
            filePath.mkdirs()
        }
        saveFile = File(dir, name)
        if (saveFile.exists()) {
            saveFile.delete()
        }
        return this

    }

    fun headers(vararg headers: Pair<String, Any>): CommonRequest {
        headers.forEach { this.mHeaders.add(Pair(it.first, "${it.second}")) }
        return this
    }

    fun headers(map: Map<String, Any>): CommonRequest {
        map.forEach { this.mHeaders.add(Pair(it.key, "${it.value}")) }
        return this
    }

    fun header(key: String, value: Any): CommonRequest {
        this.mHeaders.add(Pair(key, "$value"))
        return this
    }

    private var usingJson = true
    fun useJson(use: Boolean): CommonRequest {
        usingJson = use
        return this
    }

    private var backInOldThread = false
    fun backInOld(inOld: Boolean): CommonRequest {
        this.backInOldThread = inOld
        return this
    }


    fun params(vararg params: Pair<String, Any>): CommonRequest {
        params.forEach {
            this.mParams.add(
                Pair(
                    it.first,
                    if (it.second is File) it.second else "${it.second}"
                )
            )
        }
        return this
    }

    fun params(map: Map<String, Any>): CommonRequest {
        map.forEach {
            this.mParams.add(
                Pair(
                    it.key,
                    it.value
                )
            )
        }
        return this
    }

    fun param(key: String, value: Any): CommonRequest {
        this.mParams.add(Pair(key, value))
        return this
    }


    fun getUrl() = mUrl
    fun tag() = mTag
    private fun params() = mParams


    fun buildRequest(): Request? {
        return try {
            when (method) {
                HttpMethod.GET -> buildGetRequest()
                HttpMethod.HEAD -> buildHeadRequest()
                HttpMethod.POST -> buildPostRequest()
                HttpMethod.DELETE -> buildDeleteRequest()
                HttpMethod.PUT -> buildPutRequest()
                HttpMethod.PATCH -> buildPatchRequest()
                else -> buildPostRequest()
            }

        } catch (e: Exception) {
            errorMessage = e.message ?: "Request build onError"
            null
        }
    }


    private fun buildGetRequest(): Request {
        return Request.Builder().url(urlParams())
            .apply {
                SmartHttpConfig.getHeaders().forEach {
                    addHeader(it.key, it.value)
                }
                mHeaders.forEach {
                    addHeader(it.first, it.second)
                }
            }
            .get()
            .tag(mTag!!)
            .build()
    }

    private fun buildHeadRequest(): Request {
        return Request.Builder().url(urlParams())
            .apply {
                SmartHttpConfig.getHeaders().forEach { addHeader(it.key, it.value) }
                mHeaders.forEach { addHeader(it.first, it.second) }
            }
            .head().tag(mTag!!).build()
    }

    private fun buildPostRequest(): Request {
        return bodyBuilder().post(buildRequestBody()).tag(mTag!!).build()
    }

    private fun buildPutRequest(): Request {
        return bodyBuilder().put(buildRequestBody()).tag(mTag!!).build()
    }

    private fun buildDeleteRequest(): Request {
        return bodyBuilder().delete(buildRequestBody()).tag(mTag!!).build()
    }

    private fun buildPatchRequest(): Request {
        return bodyBuilder().patch(buildRequestBody()).tag(mTag!!).build()
    }


    private fun bodyBuilder(): Request.Builder {
        return Request.Builder().url(mUrl)
            .apply {
                SmartHttpConfig.getHeaders().forEach { addHeader(it.key, it.value) }
                mHeaders.forEach { addHeader(it.first, it.second) }
            }
    }


    private fun buildRequestBody(): RequestBody {
        return when {
            usingJson -> {
                getJsonString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            }
            isMultiPart() -> {
                // multipart/form-data
                val builder = MultipartBody.Builder()
                mParams.forEach {
                    if (it.second is String) {
                        builder.addFormDataPart(it.first, it.second as String)
                    } else if (it.second is File) {
                        val file = it.second as File
                        builder.addFormDataPart(
                            it.first,
                            file.name,
                            file.asRequestBody(file.mediaType().toMediaTypeOrNull())
                        )
                    }
                }
                builder.setType(MultipartBody.FORM).build()
            }
            else -> {
                // form-data url-encoded
                val builder = FormBody.Builder()
                mParams.forEach {
                    builder.add(it.first, "${it.second}")
                }
                builder.build()
            }
        }

    }


    private fun isMultiPart() = mParams.any { it.second is File }

    //private fun isMultiPart() = true

    private fun urlParams(): String {
        val queryParams =
            if (params().isEmpty()) ""
            else "?" + mParams.joinToString(separator = "&", transform = {
                "${it.first}=${it.second}"
            })

        return "$mUrl$queryParams"
    }


    inline fun <reified T> request(callback: HttpCallback<T>) {
        val req = buildRequest()
        if (null == req) {
            commonCallback {
                callback.onFail(IOException(errorMessage))
            }
            return
        }
        SmartHttpConfig.getClient().newCall(req).apply {
            enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val exception = SmartHttpConfig.getRequestHandler().requestFail(
                        this@CommonRequest, e
                    )
                    commonCallback {
                        callback.onFail(exception)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val result = SmartHttpConfig.getRequestHandler().requestSucceed(
                        this@CommonRequest, response, T::class.java
                    )
                    commonCallback {
                        callback.onSucceed(result as T)
                    }
                }
            })
        }
    }

    inline fun <reified T> defferedRequest(): Deferred<T?> {
        val req = buildRequest()
        val deferred = CompletableDeferred<T?>()
        if (null == req) {
            deferred.completeExceptionally(IOException(errorMessage))
            return deferred
        }
        val call = SmartHttpConfig.getClient().newCall(req)
        deferred.invokeOnCompletion {
            if (deferred.isCancelled) {
                call.cancel()
            }
        }
        try {
            val response = call.execute()
            val result = SmartHttpConfig.getRequestHandler().requestSucceed(
                this@CommonRequest, response, T::class.java
            )
            deferred.complete(result as T)
        } catch (e: Exception) {
            val exception = SmartHttpConfig.getRequestHandler().requestFail(
                this@CommonRequest, e
            )
            deferred.completeExceptionally(exception)
        }
        return deferred
    }

    suspend inline fun <reified T> suspendRequest(): T? {
        val req = buildRequest()
        val deferred = CompletableDeferred<T?>()
        if (null == req) {
            deferred.completeExceptionally(Exception(errorMessage))
            return deferred.await()
        }
        val call = SmartHttpConfig.getClient().newCall(req)
        deferred.invokeOnCompletion {
            if (deferred.isCancelled) {
                call.cancel()
            }
        }
        try {
            val response = call.execute()
            val result = SmartHttpConfig.getRequestHandler().requestSucceed(
                this@CommonRequest, response, T::class.java
            )
            deferred.complete(result as T)
        } catch (e: Exception) {
            val exception = SmartHttpConfig.getRequestHandler().requestFail(
                this@CommonRequest, e
            )
            deferred.completeExceptionally(exception)

        }
        return deferred.await()
    }

    fun commonCallback(f: () -> Unit) {
        if (backInOldThread) {
            f.invoke()
        } else {
            mainThreadHandler.post(f)
        }
    }


    private fun getJsonString(): String {
        val jsonObject = JsonObject()
        mParams.forEach {
            jsonObject.add(it.first, Gson().toJsonTree(it.second))
        }
        return Gson().toJson(jsonObject)
    }


}