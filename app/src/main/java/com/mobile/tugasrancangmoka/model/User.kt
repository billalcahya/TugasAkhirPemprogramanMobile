package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class UserListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<User>
)

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String
) {
    val initial: String
        get() = fullName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
}

data class UserResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)