@file:JvmName("Global")

package com.isanechek.imagehandler

import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import coil.size.PixelSize
import java.io.File
import kotlin.random.Random


const val DATABASE_EMPTY = "database_empty"
const val NOT_FIND_TO_SAVE = "not_find_to_save"

const val PROGRESS_STATE_CHECK_PRIVATE_FOLDER = "check.private.folder"
const val PROGRESS_STATE_LOAD_DATA_FROM_DB = "load.data.from.database"
const val PROGRESS_STATE_EXECUTE_FILES_SIZE = "execute.files.size"
const val PROGRESS_STATE_CHECK_EXISTS_FILE = "check.exists.file"
const val PROGRESS_STATE_SAVE_FILE = "save.file"
const val PROGRESS_STATE_SAVE_FILE_DONE = "save.file.done"
const val PROGRESS_STATE_SAVE_FILE_FAIL = "save.file.fail"
const val PROGRESS_STATE_ADD_SIZE_IN_GALLEY = "gallery.add.size"


@JvmField val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
    "Verbose WorkManager Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications whenever work starts"
@JvmField val NOTIFICATION_TITLE: CharSequence = "WorkRequest Starting"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 1


const val EMPTY_VALUE = ""

const val PRIVATE_APP_FOLDER_NAME = "imagehandler_cache"
const val PUBLIC_APP_FOLDER_NAME = "ImageHandler"

typealias _layout = R.layout
typealias _id = R.id
typealias _style= R.style
typealias _drawable = R.drawable
typealias _anim = R.anim
typealias _color = R.color
typealias _dimen = R.dimen

infix fun ViewGroup.inflate(layoutResId: Int): View =
    LayoutInflater.from(this.context).inflate(layoutResId, this, false)

fun View.onClick(function: () -> Unit) {
    setOnClickListener {
        function()
    }
}

inline fun debugLog(message: () -> String) {
    Log.e("DEBUG", message())
}

fun hasMinimumSdk(minimumSdk: Int): Boolean = Build.VERSION.SDK_INT >= minimumSdk

fun String.toFile(): File = File(this)

internal fun Cursor.doWhile(action: () -> Unit) {
    this.use {
        if (this.moveToFirst()) {
            do {
                action()
            } while (this.moveToNext())
        }
    }
}

fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> = map {
    if (block(it)) newValue else it
}

fun Context.getDisplaySize(): PixelSize =
    resources.displayMetrics.run { PixelSize(widthPixels, heightPixels) }

fun Int.dp(context: Context): Float = this * context.resources.displayMetrics.density

@ColorInt
fun randomColor(): Int =
    Color.argb(128, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

//const val TLS_1_2 = "TLSv1.2"
//val TLS_1_2_ONLY = arrayOf(TLS_1_2)
//
//private val CONNECTION_SPEC_TLS_1_2_ONLY = run {
//    ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//        .tlsVersions(TlsVersion.TLS_1_2)
//        .build()
//}
//
///**
// * Force the [OkHttpClient] to only accept TLS 1.2 connections.
// *
// * This enables TLS 1.2 support on API 16+.
// */
//@RequiresApi(16)
//fun OkHttpClient.Builder.forceTls12(): OkHttpClient.Builder {
//    // TLS 1.2 is enabled by default on API 21 and above.
//    if (SDK_INT >= 21) return this
//
//    try {
//        val sslContext = SSLContext.getInstance(TLS_1_2)
//        val trustManager = getDefaultTrustManager()
//        sslContext.init(null, null, null)
//        val socketFactory = Tls12SocketFactory(sslContext.socketFactory)
//
//        // If we don't find the X509TrustManager, let OkHttp try to find it.
//        if (trustManager != null) {
//            sslSocketFactory(socketFactory, trustManager)
//        } else {
//            @Suppress("DEPRECATION")
//            sslSocketFactory(socketFactory)
//        }
//
//        connectionSpecs(listOf(CONNECTION_SPEC_TLS_1_2_ONLY))
//    } catch (_: Exception) {}
//
//    return this
//}
//
//inline fun <reified R : Any> Array<*>.findInstance(): R? = find { it is R } as R?
//
///** Find and initialize the default trust manager. */
//private fun getDefaultTrustManager(): X509TrustManager? {
//    val algorithm = TrustManagerFactory.getDefaultAlgorithm()
//    val factory = TrustManagerFactory.getInstance(algorithm)
//    factory.init(null as KeyStore?)
//    return factory.trustManagers.findInstance()
//}

fun calculateNoOfColumns(
    context: Context,
    columnWidthDp: Float
): Int { // For example columnWidthdp=180
    val displayMetrics = context.resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
    return (screenWidthDp / columnWidthDp + 0.5).toInt()
}

fun Int.toDp() = (this / Resources.getSystem().displayMetrics.density)