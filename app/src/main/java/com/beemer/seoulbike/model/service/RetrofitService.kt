package com.beemer.seoulbike.model.service

import com.beemer.seoulbike.BuildConfig
import com.beemer.seoulbike.model.api.AuthApi
import com.beemer.seoulbike.model.data.UserData
import com.beemer.seoulbike.model.dto.TokenDto
import com.beemer.seoulbike.model.repository.DataStoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitService @Inject constructor(private val dataStoreRepository: DataStoreRepository) {
    companion object {
        private const val TIMEOUT_SECONDS = 5L
    }

    private val baseClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val baseRetrofit: Retrofit = createRetrofit(baseClient)

    private val authRetrofitInstance: Retrofit by lazy {
        val interceptorClient = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(RequestInterceptor(dataStoreRepository))
            .addInterceptor(ResponseInterceptor(dataStoreRepository, baseRetrofit.create(AuthApi::class.java)))
            .build()

        createRetrofit(interceptorClient)
    }

    private fun createRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getRetrofit(): Retrofit = baseRetrofit

    fun getAuthRetrofit(): Retrofit = authRetrofitInstance
}

class RequestInterceptor @Inject constructor(private val dataStoreRepository: DataStoreRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking { dataStoreRepository.getAccessToken().first() }

        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(request)
    }
}

class ResponseInterceptor @Inject constructor(private val dataStoreRepository: DataStoreRepository, private val authApi: AuthApi) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val responseBody = response.body?.string() ?: ""

        if (response.code == 401) {
            val errorMessage = JSONObject(responseBody).getString("message")

            if (errorMessage == "JWT 토큰이 만료되었습니다.") {
                val accessToken = runBlocking { dataStoreRepository.getAccessToken().first() }
                val refreshToken = runBlocking { dataStoreRepository.getRefreshToken().first() }

                if (accessToken != null && refreshToken != null) {
                    authApi.reissueAccessToken(TokenDto(accessToken, refreshToken)).execute().run {
                        if (isSuccessful) {
                            body()?.accessToken?.let { newAccessToken ->
                                runBlocking {
                                    dataStoreRepository.saveAccessToken(newAccessToken)
                                    UserData.accessToken = newAccessToken
                                }

                                val newRequest = chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer $newAccessToken")
                                    .build()

                                return chain.proceed(newRequest)
                            }
                        } else {
                            runBlocking {
                                dataStoreRepository.clearAccessToken()
                                dataStoreRepository.clearRefreshToken()
                            }
                        }
                    }
                }
            }
        }

        return response.newBuilder()
            .body(responseBody.toResponseBody(response.body?.contentType()))
            .build()
    }
}
