package com.universidad.uniway.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserRepository(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("uniway_users", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveUser(user: User) {
        val users = getAllUsers().toMutableList()
        // Verificar si el usuario ya existe (por email)
        val existingUserIndex = users.indexOfFirst { it.email == user.email }
        if (existingUserIndex != -1) {
            users[existingUserIndex] = user
        } else {
            users.add(user)
        }
        
        val usersJson = gson.toJson(users)
        prefs.edit().putString("users", usersJson).apply()
    }
    
    fun getUserByEmail(email: String): User? {
        val users = getAllUsers()
        return users.find { it.email == email }
    }
    
    fun getAllUsers(): List<User> {
        val usersJson = prefs.getString("users", null)
        return if (usersJson != null) {
            val type = object : TypeToken<List<User>>() {}.type
            gson.fromJson(usersJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun getCurrentUser(): User? {
        val currentUserId = prefs.getString("current_user_id", null)
        return if (currentUserId != null) {
            val users = getAllUsers()
            users.find { it.id == currentUserId }
        } else {
            null
        }
    }
    
    fun setCurrentUser(user: User) {
        prefs.edit().putString("current_user_id", user.id).apply()
    }
    
    fun logout() {
        prefs.edit().remove("current_user_id").apply()
    }
}









