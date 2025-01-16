package com.beemer.seoulbike.view.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.beemer.seoulbike.databinding.ActivitySigninBinding
import com.beemer.seoulbike.model.data.UserData
import com.beemer.seoulbike.model.dto.SignInRequestDto
import com.beemer.seoulbike.viewmodel.AuthViewModel
import com.beemer.seoulbike.viewmodel.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySigninBinding.inflate(layoutInflater) }

    private val authViewModel by viewModels<AuthViewModel>()
    private val dataStoreViewModel by viewModels<DataStoreViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupView()
        setupViewModel()
    }

    private fun setupView() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.editEmail.text?.trim().toString()
            val password = binding.editPassword.text?.trim().toString()

            if (email.isEmpty()) {
                DefaultDialog(
                    message = "이메일을 입력해주세요.",
                    canCancel = false,
                    onConfirm = {}
                ).show(supportFragmentManager, "DefaultDialog")
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                DefaultDialog(
                    message = "비밀번호를 입력해주세요.",
                    canCancel = false,
                    onConfirm = {}
                ).show(supportFragmentManager, "DefaultDialog")
                return@setOnClickListener
            }

            authViewModel.signIn(
                SignInRequestDto(
                    email = email,
                    password = password
                )
            )
        }
    }

    private fun setupViewModel() {
        authViewModel.apply {
            signIn.apply {
                response.observe(this@SignInActivity) { user ->
                    UserData.apply {
                        isLoggedIn = true
                        email = user.userInfo.email
                        nickname = user.userInfo.nickname
                        socialType = user.userInfo.socialType
                        createdDate = user.userInfo.createdDate
                        accessToken = user.token.accessToken
                        refreshToken = user.token.refreshToken
                    }

                    user.token.accessToken?.let { dataStoreViewModel.saveAccessToken(it) }
                    user.token.refreshToken?.let { dataStoreViewModel.saveRefreshToken(it) }

                    finish()
                }

                errorMessage.observe(this@SignInActivity) { message ->
                    if (!message.isNullOrEmpty()) {
                        DefaultDialog(
                            message = message,
                            onConfirm = {}
                        ).show(supportFragmentManager, "DefaultDialog")
                    }
                }
            }
        }
    }
}