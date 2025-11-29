package com.apptolast.login.data.model

data class AuthResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val userId: String? = null
)