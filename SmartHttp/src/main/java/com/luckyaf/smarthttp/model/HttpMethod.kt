package com.luckyaf.smarthttp.model;

import androidx.annotation.StringDef

/**
 * 类描述：
 * @author Created by luckyAF on 2021/10/26
 *
 */
@StringDef(HttpMethod.GET,HttpMethod.HEAD,HttpMethod.POST,
   HttpMethod.DELETE,HttpMethod.PUT,HttpMethod.PATCH)
@Retention(AnnotationRetention.SOURCE)
annotation class  HttpMethod {
   companion object {
      const val GET = "GET"
      const val HEAD = "HEAD"
      const val POST = "POST"
      const val DELETE = "DELETE"
      const val PUT = "put"
      const val PATCH = "patch"
      const val UPLOAD = "POST"
      const val DOWNLOAD = "GET"
   }
}