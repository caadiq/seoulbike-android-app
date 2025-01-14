package com.beemer.seoulbike.model.api

import com.beemer.seoulbike.model.dto.EmailDto
import com.beemer.seoulbike.model.dto.SignInRequestDto
import com.beemer.seoulbike.model.dto.SignInResponseDto
import com.beemer.seoulbike.model.dto.SignUpDto
import com.beemer.seoulbike.model.dto.TokenDto
import com.beemer.seoulbike.model.dto.UserInfoDto
import com.beemer.seoulbike.model.dto.VerifyCodeDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/seoulbike/auth/send-signup-verification-email")
    fun sendSignUpVerificationEmail(
        @Body dto: EmailDto
    ): Call<Unit>

    @POST("/api/seoulbike/auth/verify-code-signup")
    fun verifyCodeSignUp(
        @Body dto: VerifyCodeDto
    ): Call<Unit>

    @POST("/api/seoulbike/auth/signup")
    fun signUp(
        @Body dto: SignUpDto
    ): Call<Unit>

    @POST("/api/seoulbike/auth/signin")
    fun signIn(
        @Body dto: SignInRequestDto
    ): Call<SignInResponseDto>

    @POST("/api/seoulbike/auth/signout")
    fun signOut(): Call<Unit>

    @GET("/api/seoulbike/user")
    fun getUser(): Call<UserInfoDto>

    @POST("/api/seoulbike/auth/reissue-access-token")
    fun reissueAccessToken(
        @Body dto: TokenDto
    ): Call<TokenDto>

    @POST("/api/seoulbike/auth/reissue-refresh-token")
    fun reissueRefreshToken(
        @Body dto: TokenDto
    ): Call<TokenDto>

    @POST("/api/seoulbike/auth/reissue-all-tokens")
    fun reissueAllTokens(
        @Body dto: TokenDto
    ): Call<TokenDto>
}