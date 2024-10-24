package org.videolan.resources.opensubtitles

import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.videolan.resources.AppContextProvider
import org.videolan.resources.BuildConfig
import org.videolan.resources.util.ConnectivityInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


private const val BASE_URL = "https://api.opensubtitles.com/api/v1/"
private const val USER_AGENT = "VLSub v0.9"

private fun buildClient() = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .addInterceptor(UserAgentInterceptor(USER_AGENT))
                .addInterceptor(ConnectivityInterceptor(AppContextProvider.appContext))
            .addInterceptor(CurlInterceptor(object : Logger {
                override fun log(message: String) {
                    Log.v("Ok2Curl", message)
                }
            }))
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .build())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(IOpenSubtitleService::class.java)

private class UserAgentInterceptor(val userAgent: String): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val userAgentRequest: Request = request.newBuilder()
            .header("User-Agent", userAgent)
            .header("Api-Key", BuildConfig.VLC_OPEN_SUBTITLES_API_KEY)
            .build()
        return chain.proceed(userAgentRequest)
    }
}

interface OpenSubtitleClient {
    companion object {
        val instance: IOpenSubtitleService by lazy { buildClient() }
        fun getDownloadLink(id:Int) = "${BASE_URL}download?file_id=$id"
    }
}
