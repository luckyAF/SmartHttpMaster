package com.luckyaf.smarthttp.model

import androidx.annotation.StringDef

/**
 * 类描述：
 * @author Created by luckyAF on 2021/10/26
 *
 */

@StringDef(BodyType.FORM,BodyType.JSON)
@Retention(AnnotationRetention.SOURCE)
annotation class  BodyType {
    companion object {
        const val FORM = "FORM"
        const val JSON = "JSON"

    }
}