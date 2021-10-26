package com.luckyaf.smarthttp.request

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.luckyaf.smarthttp.SmartHttpConfig
import com.luckyaf.smarthttp.download.DownloadInterceptor
import com.luckyaf.smarthttp.download.ReadCallback
import com.luckyaf.smarthttp.executor.AppExecutors
import com.luckyaf.smarthttp.extension.mediaType
import com.luckyaf.smarthttp.listener.OnDownloadListener
import com.luckyaf.smarthttp.model.HttpMethod
import com.luckyaf.smarthttp.utils.FileIOUtil
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * 类描述：
 * @author Created by luckyAF on 2021/10/26
 *
 */
data class DownloadRequest(
    @HttpMethod var method: String = HttpMethod.GET,
    private var mTag: Any? = null,
    private var mUrl: String = "",
    private var mHeaders: ArrayList<Pair<String, String>> = arrayListOf(),
    private var mParams: ArrayList<Pair<String, Any>> = arrayListOf()
) {



    fun tag(tag: Any): DownloadRequest {
        mTag = tag
        return this
    }
    /**
     * 主线程的handler
     */
    private val mainThreadHandler by lazy {
        Handler(Looper.getMainLooper())
    }
    private val appExecutors by lazy {
        AppExecutors()
    }
    private var mTotalByte: Long = 0
    private val hasCompleted = AtomicBoolean(false)
    private val notifyInterval = 100
    private val tempReadSize = AtomicLong(0)
    private val currentSize = AtomicLong(0)
    private val speedBuffer: ArrayList<Long> = ArrayList(4)


    fun url(url: String): DownloadRequest {
        mUrl = url
        // 如果 tag为空 则设置tag为当前url
        mTag = mTag ?: mUrl
        return this
    }

    private var saveFile = File("temp.txt")

    fun asFile(dir: String, name: String): DownloadRequest {
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

    fun headers(vararg headers: Pair<String, Any>): DownloadRequest {
        headers.forEach { this.mHeaders.add(Pair(it.first, "${it.second}")) }
        return this
    }

    fun headers(map: Map<String, Any>): DownloadRequest {
        map.forEach { this.mHeaders.add(Pair(it.key, "${it.value}")) }
        return this
    }

    fun header(key: String, value: Any): DownloadRequest {
        this.mHeaders.add(Pair(key, "$value"))
        return this
    }


    fun params(vararg params: Pair<String, Any>): DownloadRequest {
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

    fun params(map: Map<String, Any>): DownloadRequest {
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
    private var usingJson = true
    fun useJson(use: Boolean): DownloadRequest {
        usingJson = use
        return this
    }
    private var backInOldThread = false
    fun backInOld(inOld: Boolean): DownloadRequest {
        this.backInOldThread = inOld
        return this
    }

    fun param(key: String, value: Any): DownloadRequest {
        this.mParams.add(Pair(key, value))
        return this
    }

    private var mOnDownloadListener: OnDownloadListener?=null
    fun  downloadListener(listener: OnDownloadListener) :DownloadRequest{
        mOnDownloadListener = listener
        return this
    }

    fun startDownload() {
        val req = buildRequest()
        if (null == req) {
            commonCallback {
                mOnDownloadListener?.onError(saveFile, IOException(errorMessage))
            }
            return
        }
        val client = SmartHttpConfig.getClient()
            .newBuilder().addNetworkInterceptor(DownloadInterceptor(object : ReadCallback {
                override fun callTotalSize(size: Long) {
                    mTotalByte = size
                }
                override fun read(size: Long) {
                    tempReadSize.getAndAdd(size)
                }
            })).build()

        appExecutors.runOnNetIoThread {
            mOnDownloadListener?.onStart(saveFile)
            sendProgress()
            var randomAccessFile: RandomAccessFile? = null
            var responseStream: InputStream? = null
            var inputStream: BufferedInputStream? = null
            try {
                val response = client.newCall(req).execute()
                val downloadResponseBody = response.body
                //start downloading
                //start downloading
                randomAccessFile = RandomAccessFile(saveFile, "rw")
                randomAccessFile!!.seek(0)
                responseStream = downloadResponseBody!!.byteStream()
                inputStream = BufferedInputStream(responseStream, 8 * 1024)
                //缓冲数组8kB
                //缓冲数组8kB
                val buffer = ByteArray(1024 * 8)

                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    randomAccessFile!!.write(buffer, 0, len)
                }
                inputStream.close()
                sendProgress()
                commonCallback {
                    mOnDownloadListener?.onComplete(saveFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                commonCallback {
                    mOnDownloadListener?.onError(saveFile,e)
                    mOnDownloadListener?.onEnd(saveFile)

                }
            } finally {
                //关闭IO流
                FileIOUtil.closeQuietly(randomAccessFile)
                FileIOUtil.closeQuietly(inputStream)
                FileIOUtil.closeQuietly(responseStream)
            }
        }

    }

    private var errorMessage:String = ""
    fun buildRequest(): Request? {
        return try {
            when (method) {
                HttpMethod.GET -> buildGetRequest()
                HttpMethod.POST -> buildPostRequest()
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
    private fun urlParams(): String {
        val queryParams =
            if (mParams.isEmpty()) ""
            else "?" + mParams.joinToString(separator = "&", transform = {
                "${it.first}=${it.second}"
            })

        return "$mUrl$queryParams"
    }
    private fun buildPostRequest(): Request {
        return bodyBuilder().post(buildRequestBody()).tag(mTag!!).build()
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

    private fun sendProgress() {
        if (hasCompleted.get()) {
            return
        }
        mainThreadHandler.post {
            var speed = tempReadSize.get() * 1000 / notifyInterval
            speed = bufferSpeed(speed)
            currentSize.getAndAdd(tempReadSize.get())
            tempReadSize.set(0)
            val fraction = currentSize.get() * 1.0f / mTotalByte
            mOnDownloadListener?.onProgress(saveFile,currentSize.get(), mTotalByte, fraction, speed)
            if (currentSize.get() == mTotalByte) {
                hasCompleted.set(true)
            }
        }
        mainThreadHandler.postDelayed({ sendProgress() }, notifyInterval.toLong())
    }

    /**
     * 平滑网速，避免抖动过大
     */
    private fun bufferSpeed(speed: Long): Long {
        speedBuffer.add(speed)
        if (speedBuffer.size > 3) {
            speedBuffer.removeAt(0)
        }
        var sum: Long = 0
        for (speedTemp in speedBuffer) {
            sum += speedTemp
        }
        return sum / speedBuffer.size
    }


    private fun getJsonString(): String {
        val jsonObject = JsonObject()
        mParams.forEach {
            jsonObject.add(it.first, Gson().toJsonTree(it.second))
        }
        return Gson().toJson(jsonObject)
    }

   private fun commonCallback(f: () -> Unit) {
        if (backInOldThread) {
            f.invoke()
        } else {
            mainThreadHandler.post(f)
        }
    }



}