package com.universidad.uniway.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Teacher(
    val id: String = "",
    val fullName: String = "",
    val institutionalEmail: String = "",
    val subjects: List<String> = emptyList(),
    val advisoriesAvailable: Boolean = false,
    val virtualWorkshopsAvailable: Boolean = false,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0
) : Parcelable

data class TeacherReview(
    val id: String = "",
    val teacherId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val timestamp: Date = Date()
)



