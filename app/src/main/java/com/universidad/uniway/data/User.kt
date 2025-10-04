package com.universidad.uniway.data

data class User(
    val id: String = "",
    val email: String,
    val password: String,
    val role: UserRole,
    val fullName: String = "",
    val studentId: String = "",
    val program: String = "",
    val profileImage: String = "",
    val phone: String = "",
    val address: String = ""
)

enum class UserRole {
    STUDENT,
    ADMINISTRATION
}
