package com.mobile.tugasrancangmoka.model

data class LoginResponse(
    val status: String,
    val data: LoginData?
)

data class LoginData(
    val token: String,
    val expiresIn: Int,
    val user: UserProfile
)

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)