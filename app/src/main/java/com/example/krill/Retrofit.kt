package com.example.krill

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class KeepCookieJar : CookieJar {
    private val cookies = mutableListOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
        if (url.host() == "lobste.rs")
            cookies.addAll(cookies)
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        if (url.host() == "lobste.rs")
            return ArrayList(cookies)
        return ArrayList()
    }
}

object RetrofitClient {
    val Api : LobstersApi by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val logger = HttpLoggingInterceptor()
        logger.setLevel(HttpLoggingInterceptor.Level.HEADERS)
        val client = OkHttpClient.Builder()
            .cookieJar(KeepCookieJar())
            .addInterceptor(logger)
        client.followRedirects(false)
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client.build())
            .build()
        return@lazy retrofit.create(LobstersApi::class.java)
    }
}