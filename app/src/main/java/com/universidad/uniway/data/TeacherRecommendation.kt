package com.universidad.uniway.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class TeacherRecommendation(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val teacherName: String = "",
    val subject: String = "",
    val semester: String = "",
    val year: Int = 0,
    val reference: String = "",
    val rating: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val likeCount: Long = 0,
    val dislikeCount: Long = 0,
    val totalReactions: Long = 0,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val userReaction: String? = null
) : Parcelable