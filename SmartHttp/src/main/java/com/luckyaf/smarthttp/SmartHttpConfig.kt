package com.luckyaf.smarthttp

import com.luckyaf.smarthttp.config.DefaultParamsConverter
import com.luckyaf.smarthttp.config.IParamsConverter
import com.luckyaf.smarthttp.config.IRequestHandler
import kotlinx.coroutines.handleCoroutineException
import okhttp3.OkHttpClient
import java.util.HashMap

/**
 * 类描述：
 * @author Created by luckyAF on 2021/10/26
 *
 */
class SmartHttpConfig private constructor(client: OkHttpClient) {
    /** OkHttp 客户端  */
    private val mClient: OkHttpClient = client

    /** 通用参数  */
    private var mParams: HashMap<String, Any>

    /** 通用请求头  */
    private var mHeaders: HashMap<String, String>

    private var mRequestHandler: IRequestHandler?=null

    private var mParamsConverter:IParamsConverter?=null

    /** 重试次数  */
    private var mRetryCount = 0

    /** 重试时间  */
    private var mRetryTime: Long = 2000

    fun setRetryCount(count:Int):SmartHttpConfig{
        mRetryCount = count
        return this
    }
    fun setRetryTime(time:Long):SmartHttpConfig{
        mRetryTime = time
        return this
    }

    fun setParams(params: HashMap<String, Any>?): SmartHttpConfig {
        var params = params
        if (params == null) {
            params = HashMap()
        }
        mParams = params
        return this
    }

    fun setHeaders(headers: HashMap<String, String>?): SmartHttpConfig {
        var headers = headers
        if (headers == null) {
            headers = HashMap()
        }
        mHeaders = headers
        return this
    }
    fun setRequestHandler(handler:IRequestHandler):SmartHttpConfig{
        mRequestHandler = handler
        return this
    }
    fun setParamsConverter(converter:IParamsConverter):SmartHttpConfig{
        mParamsConverter = converter
        return this
    }

    fun addHeader(key: String?, value: String?): SmartHttpConfig {
        if (key != null && value != null) {
            mHeaders[key] = value
        }
        return this
    }

    fun removeHeader(key: String?): SmartHttpConfig {
        if (key != null) {
            mHeaders.remove(key)
        }
        return this
    }

    fun addParam(key: String?, value: String?): SmartHttpConfig {
        if (key != null && value != null) {
            mParams[key] = value
        }
        return this
    }

    fun removeParam(key: String?): SmartHttpConfig {
        if (key != null) {
            mParams.remove(key)
        }
        return this
    }

    fun initialize() {
        if(mRequestHandler==null){
            throw IllegalArgumentException("The object being processed by the request cannot be empty")
        }
        if(mParamsConverter==null){
            mParamsConverter = DefaultParamsConverter()
        }
        setInstance(this)
    }

    companion object {
        @Volatile
        private var sConfig: SmartHttpConfig? = null

        // 当前没有初始化配置
        val instance: SmartHttpConfig
            get() {
                checkNotNull(sConfig) {
                    // 当前没有初始化配置
                    "You haven't initialized the configuration yet"
                }
                return sConfig!!
            }

        private fun setInstance(config: SmartHttpConfig) {
            sConfig = config
        }

        fun with(client: OkHttpClient): SmartHttpConfig {
            return SmartHttpConfig(client)
        }
        fun getClient(): OkHttpClient {
            return instance.mClient
        }

        fun getParams(): HashMap<String, Any> {
            return instance.mParams
        }

        fun getHeaders(): HashMap<String, String> {
            return instance.mHeaders
        }

        fun getRequestHandler():IRequestHandler{
            return instance.mRequestHandler!!
        }
        fun getParamsConverter():IParamsConverter{
            return instance.mParamsConverter!!
        }
        fun getRetryCount():Int{
            return instance.mRetryCount
        }
        fun getRetryTime():Long{
            return instance.mRetryTime
        }
    }

    init {
        mParams = HashMap()
        mHeaders = HashMap()
    }
}
