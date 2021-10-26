/**
 * @author haizhuo
 * @desciption 第三方依赖包
 */
object ThirdPart {
    //顶部SnackBar
    const val topSnackBar = "com.github.PengHaiZhuo:TSnackBar:1.1.1"

    //网路请求库retrofit
    val retrofit = Retrofit

    //沉浸式状态栏
    const val immersionbar = "com.gyf.immersionbar:immersionbar:2.3.3"

    // 下拉刷新
    const val SmartRefreshLayout = "com.scwang.smartrefresh:SmartRefreshLayout:1.1.0"

    // walle
    const val walle = "com.meituan.android.walle:library:1.1.6"

    const val androidUtilCode = "com.blankj:utilcodex:1.30.6"

    //rxjava
    const val rxjava2 = "io.reactivex.rxjava2:rxjava:2.2.14"
    const val rxandroid = "io.reactivex.rxjava2:rxandroid:2.1.1"





}
object Retrofit {
    private const val retrofit_version = "2.8.1"
    const val retrofit = "com.squareup.retrofit2:retrofit:$retrofit_version"
    const val convertGson = "com.squareup.retrofit2:converter-gson:$retrofit_version"
    const val adapterRxjava = "com.squareup.retrofit2:adapter-rxjava2:$retrofit_version"
}


//okhttp
object OkHttp {
    private const val okhttp_version = "4.8.0"
    const val okhttp = "com.squareup.okhttp3:okhttp:$okhttp_version"
    const val urlConnection = "com.squareup.okhttp3:okhttp-urlconnection:$okhttp_version"
    const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$okhttp_version"
}

//图片加载框架
object Glide {
    private const val glide_version = "4.11.0"
    const val glide = "com.github.bumptech.glide:glide:$glide_version"
    const val compiler = "com.github.bumptech.glide:compiler:$glide_version"
}
//
object kunminx {
    const val unpeekLivedata = "com.kunminx.arch:unpeek-livedata:7.2.0-beta1"
    const val smoothNavigation = "com.kunminx.archi:smooth-navigation:3.3.2-beta5"
    const val strictDatabinding = "com.kunminx.archi:strict-databinding:3.6.2-beta1"
    const val domain = "com.kunminx.archi:domain:1.1.0"
}
