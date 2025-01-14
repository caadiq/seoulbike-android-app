package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.beemer.seoulbike.model.dto.TokenDto
import com.beemer.seoulbike.model.dto.UserInfoDto
import com.beemer.seoulbike.model.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : BaseViewModel() {
    private val _reissueAllTokens = MutableLiveData<TokenDto>()
    val reissueAllTokens: LiveData<TokenDto> = _reissueAllTokens

    private val _user = MutableLiveData<UserInfoDto>()
    val user: LiveData<UserInfoDto> = _user

    fun reissueAllTokens(dto: TokenDto) {
        execute(
            call = { repository.reissueAllTokens(dto) },
            onSuccess = { _reissueAllTokens.value = it }
        )
    }

    fun getUser() {
        execute(
            call = { repository.getUser() },
            onSuccess = { _user.value = it }
        )
    }
}