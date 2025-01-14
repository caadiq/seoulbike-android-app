package com.beemer.seoulbike.model.repository

import com.beemer.seoulbike.model.api.AuthApi
import com.beemer.seoulbike.model.di.AuthRetrofit
import com.beemer.seoulbike.model.di.BasicRetrofit
import com.beemer.seoulbike.model.dto.EmailDto
import com.beemer.seoulbike.model.dto.SignInRequestDto
import com.beemer.seoulbike.model.dto.SignInResponseDto
import com.beemer.seoulbike.model.dto.SignUpDto
import com.beemer.seoulbike.model.dto.TokenDto
import com.beemer.seoulbike.model.dto.UserInfoDto
import com.beemer.seoulbike.model.dto.VerifyCodeDto
import com.beemer.seoulbike.model.utils.RetrofitUtil
import retrofit2.Retrofit
import javax.inject.Inject

class AuthRepository @Inject constructor(@BasicRetrofit retrofit: Retrofit, @AuthRetrofit retrofitAuth: Retrofit) {
    private val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    private val authApiAuth: AuthApi = retrofitAuth.create(AuthApi::class.java)

    suspend fun sendSignUpVerificationEmail(dto: EmailDto): RetrofitUtil.Results<Unit> {
        return RetrofitUtil.call(authApi.sendSignUpVerificationEmail(dto))
    }

    suspend fun verifyCodeSignUp(dto: VerifyCodeDto): RetrofitUtil.Results<Unit> {
        return RetrofitUtil.call(authApi.verifyCodeSignUp(dto))
    }

    suspend fun signUp(dto: SignUpDto): RetrofitUtil.Results<Unit> {
        return RetrofitUtil.call(authApi.signUp(dto))
    }

    suspend fun signIn(dto: SignInRequestDto): RetrofitUtil.Results<SignInResponseDto> {
        return RetrofitUtil.call(authApi.signIn(dto))
    }

    suspend fun signOut(): RetrofitUtil.Results<Unit> {
        return RetrofitUtil.call(authApiAuth.signOut())
    }

    suspend fun getUser(): RetrofitUtil.Results<UserInfoDto> {
        return RetrofitUtil.call(authApiAuth.getUser())
    }

    suspend fun reissueAccessToken(dto: TokenDto): RetrofitUtil.Results<TokenDto> {
        return RetrofitUtil.call(authApi.reissueAccessToken(dto))
    }

    suspend fun reissueRefreshToken(dto: TokenDto): RetrofitUtil.Results<TokenDto> {
        return RetrofitUtil.call(authApiAuth.reissueRefreshToken(dto))
    }

    suspend fun reissueAllTokens(dto: TokenDto): RetrofitUtil.Results<TokenDto> {
        return RetrofitUtil.call(authApi.reissueAllTokens(dto))
    }
}