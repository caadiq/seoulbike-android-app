package com.beemer.seoulbike.viewmodel

import com.beemer.seoulbike.model.dto.SignInRequestDto
import com.beemer.seoulbike.model.dto.SignInResponseDto
import com.beemer.seoulbike.model.dto.TokenDto
import com.beemer.seoulbike.model.dto.UserInfoDto
import com.beemer.seoulbike.model.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : BaseViewModel() {
    val reissueAllTokens = Request<TokenDto>()
    val user = Request<UserInfoDto>()
    val signIn = Request<SignInResponseDto>()
    val signOut = Request<Unit>()

    fun reissueAllTokens(dto: TokenDto) {
        execute(
            call = { repository.reissueAllTokens(dto) },
            onSuccess = { reissueAllTokens.response.value = it },
            errorCode = reissueAllTokens.errorCode,
            errorMessage = reissueAllTokens.errorMessage
        )
    }

    fun getUser() {
        execute(
            call = { repository.getUser() },
            onSuccess = { user.response.value = it },
            errorCode = user.errorCode,
            errorMessage = user.errorMessage
        )
    }

    fun signIn(dto: SignInRequestDto) {
        execute(
            call = { repository.signIn(dto) },
            onSuccess = { signIn.response.value = it },
            errorCode = signIn.errorCode,
            errorMessage = signIn.errorMessage
        )
    }

    fun signOut() {
        execute(
            call = { repository.signOut() },
            onSuccess = { signOut.response.value = it },
            errorCode = signOut.errorCode,
            errorMessage = signOut.errorMessage
        )
    }
}