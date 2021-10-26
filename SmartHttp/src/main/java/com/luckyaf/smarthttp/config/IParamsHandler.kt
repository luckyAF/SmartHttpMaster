package com.luckyaf.smarthttp.config

import org.json.JSONObject

/**
 * 类描述：
 * @author Created by luckyAF on 2021/10/26
 *
 */
interface IParamsConverter {
    fun paramsToJsonObject(data:Any): Any?
}