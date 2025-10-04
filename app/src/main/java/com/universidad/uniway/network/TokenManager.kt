package com.universidad.uniway.network

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "uniway_tokens", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val TOKEN_KEY = "auth_token"
        private const val USER_ID_KEY = "user_id"
        private const val USER_EMAIL_KEY = "user_email"
        private const val USER_NAME_KEY = "user_name"
        private const val USER_ROLE_KEY = "user_role"
        private const val REMEMBER_ME_KEY = "remember_me"
    }
    
    // ==================== TOKEN ====================
    
    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }
    
    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }
    
    fun clearToken() {
        prefs.edit().remove(TOKEN_KEY).apply()
    }
    
    fun hasToken(): Boolean {
        return !getToken().isNullOrEmpty()
    }
    
    // ==================== USUARIO ====================
    
    fun saveUserInfo(userId: String, email: String, fullName: String, role: String) {
        prefs.edit()
            .putString(USER_ID_KEY, userId)
            .putString(USER_EMAIL_KEY, email)
            .putString(USER_NAME_KEY, fullName)
            .putString(USER_ROLE_KEY, role)
            .apply()
    }
    
    fun getUserId(): String? {
        return prefs.getString(USER_ID_KEY, null)
    }
    
    fun getUserEmail(): String? {
        return prefs.getString(USER_EMAIL_KEY, null)
    }
    
    fun getUserName(): String? {
        return prefs.getString(USER_NAME_KEY, null)
    }
    
    fun getUserRole(): String? {
        return prefs.getString(USER_ROLE_KEY, null)
    }
    
    // ==================== SESIÓN ====================
    
    fun saveAuthData(authResponse: AuthResponse) {
        saveToken(authResponse.token)
        saveUserInfo(
            userId = authResponse.user.id,
            email = authResponse.user.email,
            fullName = authResponse.user.fullName,
            role = authResponse.user.role
        )
    }

    fun setRememberMe(enabled: Boolean) {
        prefs.edit().putBoolean(REMEMBER_ME_KEY, enabled).apply()
    }

    fun isRememberMe(): Boolean {
        return prefs.getBoolean(REMEMBER_ME_KEY, false)
    }
    
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
    
    fun isLoggedIn(): Boolean {
        return hasToken() && !getUserId().isNullOrEmpty()
    }
}