package com.example.core.network

import android.content.Context
import com.example.core.config.ApiConfig
import com.example.core.security.SecureStorage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class AuthHeaderInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val session = SecureStorage(context).getSession()
        if (session != null && session.token.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer ${session.token}")
        }
        requestBuilder.addHeader("Accept", "application/json")
        requestBuilder.addHeader("User-Agent", "MediBridge-Android/1.0")
        return chain.proceed(requestBuilder.build())
    }
}

object RetrofitClientProvider {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun getOkHttpClient(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(AuthHeaderInterceptor(context))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun createRetrofit(context: Context, customUrl: String? = null): Retrofit? {
        val baseUrl = if (!customUrl.isNullOrBlank()) {
            customUrl
        } else {
            ApiConfig.getBaseUrl(context)
        }

        if (baseUrl.isBlank()) {
            return null
        }

        val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return try {
            Retrofit.Builder()
                .baseUrl(formattedUrl)
                .client(getOkHttpClient(context))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
        } catch (e: Exception) {
            null
        }
    }

    inline fun <reified T> createService(context: Context, customEndpointKey: String? = null): T? {
        val customUrl = customEndpointKey?.let { ApiConfig.getCustomEndpoint(context, it) }
        val retrofit = createRetrofit(context, customUrl) ?: return null
        return try {
            retrofit.create(T::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun <T> safeApiCall(
        serviceName: String,
        apiCall: suspend () -> retrofit2.Response<T>
    ): ApiResult<T> {
        return try {
            val response = apiCall()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = when (response.code()) {
                    400 -> "Bad request. Please verify your inputs."
                    401 -> "Session expired. Please log in again."
                    403 -> "Access forbidden for this account."
                    404 -> "$serviceName record was not found."
                    409 -> "Conflict occurred. The slot or order already exists."
                    429 -> "Too many requests. Please wait a moment."
                    500, 502, 503 -> "$serviceName server is temporarily unavailable."
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                ApiResult.HttpError(response.code(), message, errorBody)
            }
        } catch (e: IOException) {
            ApiResult.NetworkError("Network connection failed. Please check your internet.")
        } catch (e: Exception) {
            ApiResult.HttpError(-1, e.localizedMessage ?: "Unexpected error connecting to $serviceName")
        }
    }
}
