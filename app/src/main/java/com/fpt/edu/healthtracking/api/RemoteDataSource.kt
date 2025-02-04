package com.fpt.edu.healthtracking.api

import TokenAuthenticator
import com.fpt.edu.healthtracking.data.UserPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RemoteDataSource(
    private val userPreferences: UserPreferences? = null,
    private val authApi: AuthApi? = null
) {

    companion object{
        private const val BASE_URL_Localhost = "http:/10.0.2.2:5163/api/"
        private const val BASE_URL_Localhost1 = "http:/192.168.100.208:5163/api/"
        private const val BASE_URL = "https://healthtrack-hydtdue4ede8b5fp.southeastasia-01.azurewebsites.net/api/"
        private const val BASE_URL1= "https://full-flying-lion.ngrok-free.app/api/"
    }
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .apply {
            if (userPreferences != null && authApi != null) {
                authenticator(TokenAuthenticator(userPreferences, authApi))
            }
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .hostnameVerifier { _, _ -> true }
        .build()

    fun<Api> buildApi(
        api: Class<Api>,
    ) : Api{
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }
}