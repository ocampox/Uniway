package com.universidad.uniway.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.universidad.uniway.ui.forum.ForumViewModel
import com.universidad.uniway.ui.login.LoginViewModel
import com.universidad.uniway.ui.profile.ProfileViewModel
import com.universidad.uniway.ui.register.RegisterViewModel
import com.universidad.uniway.ui.forgotpassword.ForgotPasswordViewModel
import com.universidad.uniway.ui.resetpassword.ResetPasswordViewModel
import com.universidad.uniway.ui.teacherlist.TeacherListViewModel
import com.universidad.uniway.ui.addteacher.AddTeacherViewModel
import com.universidad.uniway.ui.teacher.AllRecommendationsViewModel
import com.universidad.uniway.ui.teacher.MyRecommendationsViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(context) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(context) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(context) as T
            }
            modelClass.isAssignableFrom(ForumViewModel::class.java) -> {
                ForumViewModel(context) as T
            }
            modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java) -> {
                ForgotPasswordViewModel(context) as T
            }
                modelClass.isAssignableFrom(ResetPasswordViewModel::class.java) -> {
                    ResetPasswordViewModel(context) as T
                }
                modelClass.isAssignableFrom(TeacherListViewModel::class.java) -> {
                    TeacherListViewModel(context) as T
                }
                modelClass.isAssignableFrom(AddTeacherViewModel::class.java) -> {
                    AddTeacherViewModel(context) as T
                }
                modelClass.isAssignableFrom(AllRecommendationsViewModel::class.java) -> {
                    AllRecommendationsViewModel(context) as T
                }
                modelClass.isAssignableFrom(MyRecommendationsViewModel::class.java) -> {
                    MyRecommendationsViewModel(context) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
