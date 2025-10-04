package com.universidad.uniway.ui.teacher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.universidad.uniway.R
import com.universidad.uniway.data.TeacherRecommendation
import java.text.SimpleDateFormat
import java.util.*

class TeacherRecommendationAdapter(
    private val onRemoveClick: ((TeacherRecommendation) -> Unit)? = null,
    private val onLikeClick: ((TeacherRecommendation) -> Unit)? = null,
    private val onDislikeClick: ((TeacherRecommendation) -> Unit)? = null,
    private val showOwnActions: Boolean = false,
    private val getCurrentUserId: (() -> String?)? = null,
    private val getUserRole: (() -> String?)? = null
) : ListAdapter<TeacherRecommendation, TeacherRecommendationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_teacher, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTeacherName: TextView = itemView.findViewById(R.id.textTeacherName)
        private val textRecommendedBy: TextView = itemView.findViewById(R.id.textRecommendedBy)
        private val textSubject: TextView = itemView.findViewById(R.id.textSubject)
        private val textSemesterYear: TextView = itemView.findViewById(R.id.textSemesterYear)
        private val textRating: TextView = itemView.findViewById(R.id.textRating)
        private val textReference: TextView = itemView.findViewById(R.id.textReference)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        
        // Interacciones
        private val layoutLike: LinearLayout = itemView.findViewById(R.id.layoutLike)
        private val layoutDislike: LinearLayout = itemView.findViewById(R.id.layoutDislike)
        private val imageViewLike: ImageView = itemView.findViewById(R.id.imageViewLike)
        private val imageViewDislike: ImageView = itemView.findViewById(R.id.imageViewDislike)
        private val textViewLikeCount: TextView = itemView.findViewById(R.id.textViewLikeCount)
        private val textViewDislikeCount: TextView = itemView.findViewById(R.id.textViewDislikeCount)
        
        // Botones
        private val buttonRemove: MaterialButton = itemView.findViewById(R.id.buttonRemove)

        fun bind(recommendation: TeacherRecommendation) {
            textTeacherName.text = recommendation.teacherName.ifEmpty { "Profesor" }
            textRecommendedBy.text = "Recomendado por: ${recommendation.studentName.ifEmpty { "Estudiante" }}"
            textSubject.text = recommendation.subject
            textSemesterYear.text = "${recommendation.semester} (${recommendation.year})"
            
            // Mostrar rating con emojis de estrellas sobre fondo azul
            val ratingText = if (recommendation.rating > 0) {
                val stars = "⭐".repeat(recommendation.rating)
                "$stars (${recommendation.rating}/5)"
            } else {
                "⭐ Sin calificación"
            }
            textRating.text = ratingText
            
            textReference.text = recommendation.reference.ifEmpty { "Sin referencia disponible" }
            
            // Formatear fecha
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            textViewDate.text = "Publicado: ${dateFormat.format(recommendation.createdAt)}"
            
            // Configurar contadores de likes/dislikes
            textViewLikeCount.text = recommendation.likeCount.toString()
            textViewDislikeCount.text = recommendation.dislikeCount.toString()
            
            // Configurar estado visual de likes/dislikes
            updateLikeDislikeState(recommendation)
            
            // Configurar visibilidad de botones según el contexto y permisos
            val currentUserId = getCurrentUserId?.invoke()
            val userRole = getUserRole?.invoke()
            val canDelete = (recommendation.studentId == currentUserId) || (userRole == "ADMINISTRATION")
            
            buttonRemove.visibility = if (showOwnActions && canDelete && onRemoveClick != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
            
            // Configurar click listeners
            onRemoveClick?.let { removeCallback ->
                buttonRemove.setOnClickListener {
                    android.util.Log.d("TeacherRecommendationAdapter", "Remove button clicked for: ${recommendation.teacherName}")
                    removeCallback(recommendation)
                }
            }
            
            // Configurar likes/dislikes si están disponibles
            onLikeClick?.let { likeCallback ->
                layoutLike.setOnClickListener {
                    android.util.Log.d("TeacherRecommendationAdapter", "Like layout clicked for: ${recommendation.teacherName}")
                    likeCallback(recommendation)
                }
            }
            
            onDislikeClick?.let { dislikeCallback ->
                layoutDislike.setOnClickListener {
                    android.util.Log.d("TeacherRecommendationAdapter", "Dislike layout clicked for: ${recommendation.teacherName}")
                    dislikeCallback(recommendation)
                }
            }
        }
        
        private fun updateLikeDislikeState(recommendation: TeacherRecommendation) {
            val context = itemView.context
            
            // Configurar like
            if (recommendation.isLiked) {
                imageViewLike.setColorFilter(ContextCompat.getColor(context, R.color.like_active))
                textViewLikeCount.setTextColor(ContextCompat.getColor(context, R.color.like_active))
            } else {
                imageViewLike.setColorFilter(ContextCompat.getColor(context, R.color.like_inactive))
                textViewLikeCount.setTextColor(ContextCompat.getColor(context, R.color.like_inactive))
            }
            
            // Configurar dislike
            if (recommendation.isDisliked) {
                imageViewDislike.setColorFilter(ContextCompat.getColor(context, R.color.dislike_active))
                textViewDislikeCount.setTextColor(ContextCompat.getColor(context, R.color.dislike_active))
            } else {
                imageViewDislike.setColorFilter(ContextCompat.getColor(context, R.color.dislike_inactive))
                textViewDislikeCount.setTextColor(ContextCompat.getColor(context, R.color.dislike_inactive))
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TeacherRecommendation>() {
        override fun areItemsTheSame(oldItem: TeacherRecommendation, newItem: TeacherRecommendation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TeacherRecommendation, newItem: TeacherRecommendation): Boolean {
            return oldItem == newItem
        }
    }
}