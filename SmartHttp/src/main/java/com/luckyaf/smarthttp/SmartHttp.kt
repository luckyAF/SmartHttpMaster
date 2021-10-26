package com.luckyaf.smarthttp

import com.luckyaf.smarthttp.request.CommonRequest
/**
 * 类描述：
 * @author Created by luckyAF on 2021/10/26
 *
 */
object SmartHttp {
    fun cancelTag(tag: Any?) {
        if (null == tag) {
            return
        } else {
            val queuedCalls = SmartHttpConfig.getClient().dispatcher.queuedCalls()
            for (call in queuedCalls) {
                if (tag === call.request().tag()) {
                    call.cancel()
                }
            }
            val runningCalls = SmartHttpConfig.getClient().dispatcher.runningCalls()
            for (call in runningCalls) {
                if (tag === call.request().tag()) {
                    call.cancel()
                }
            }
        }
    }

    fun cancelAll() {
        val queuedCalls = SmartHttpConfig.getClient().dispatcher.queuedCalls()
        for (call in queuedCalls) {
            call.cancel()
        }
        val runningCalls = SmartHttpConfig.getClient().dispatcher.runningCalls()
        for (call in runningCalls) {
            call.cancel()
        }
    }

    fun get(): CommonRequest {
        return CommonRequest.get()
    }

    fun post(): CommonRequest {
        return CommonRequest.post()
    }

    fun head(): CommonRequest {
        return CommonRequest.head()
    }

    fun patch(): CommonRequest {
        return CommonRequest.patch()
    }

    fun put(): CommonRequest {
        return CommonRequest.put()
    }

    fun delete(): CommonRequest {
        return CommonRequest.delete()
    }
}
