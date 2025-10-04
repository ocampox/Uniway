package com.universidad.uniway.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class StudentTeacher(
    val id: String = "",
    val studentId: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val teacherEmail: String = "",
    val subject: String = "",
    val semester: String = "",
    val year: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Date = Date()
) : Parcelable
