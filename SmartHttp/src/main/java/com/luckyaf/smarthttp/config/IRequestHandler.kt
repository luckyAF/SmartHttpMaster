package com.luckyaf.smarthttp.config;

import com.luckyaf.smarthttp.request.CommonRequest
import okhttp3.Request
import okhttp3.Response
import java.lang.Exception
import java.lang.reflect.Type

/**
 * 类描述：
 *
 * @author Created by luckyAF on 2021/10/26
 */
interface IRequestHandler {
    /**
     * 请求开始
     *
     * @param request           请求接口对象
     * @param builder       请求构建对象
     * @return              返回新的请求对象
     */
    fun requestStart(
        request: CommonRequest,
        builder: Request.Builder
    ): Request {
        return builder.build()
    }

    /**
     * 请求成功时回调
     *
     * @param request           请求接口对象
     * @param response      响应对象
     * @param type          解析类型
     * @return              返回结果
     *
     * @throws Exception    如果抛出则回调失败
     */
    @Throws(Exception::class)
    fun requestSucceed(
        request: CommonRequest,
        response: Response,
        type: Type
    ): Any

    /**
     * 请求失败
     *
     * @param request           请求接口对象
     * @param e             错误对象
     * @return              错误对象
     */
    fun requestFail(
                    request: CommonRequest,
                    e: Exception):
            Exception

}
