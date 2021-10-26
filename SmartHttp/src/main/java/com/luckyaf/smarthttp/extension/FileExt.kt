package com.luckyaf.smarthttp.extension

import java.io.File
import java.net.URLConnection
import java.util.*

/**
 * 类描述：
 * @author Created by luckyAF on 2021/10/26
 *
 */
fun File.mediaType(): String {
    return URLConnection.getFileNameMap().getContentTypeFor(name) ?: when (extension.lowercase(
        Locale.getDefault()
    )) {
        "json" -> "application/json"
        "js" -> "application/javascript"
        "apk" -> "application/vnd.android.package-archive"
        "md" -> "text/x-markdown"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
    }
}