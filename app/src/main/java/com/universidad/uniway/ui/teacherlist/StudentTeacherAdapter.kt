package com.universidad.uniway.ui.teacherlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.universidad.uniway.R
import com.universidad.uniway.data.StudentTeacher
import java.text.SimpleDateFormat
import java.util.*

class StudentTeacherAdapter(
    private val onRemoveClick: (StudentTeacher) -> Unit,
    private val onLikeClick: ((StudentTeacher) -> Unit)? = null,
    private val onDislikeClick: ((StudentTeacher) -> Unit)? = null,
    private val showOwnActions: Boolean = false
) : ListAdapter<StudentTeacher, StudentTeacherAdapter.ViewHolder>(DiffCallback()) {

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

        fun bind(studentTeacher: StudentTeacher) {
            textTeacherName.text = studentTeacher.teacherName.ifEmpty { "Profesor" }
            textRecommendedBy.text = "Recomendado por: Estudiante" // TODO: Obtener nombre real del estudiante
            textSubject.text = studentTeacher.subject
            textSemesterYear.text = "${studentTeacher.semester} (${studentTeacher.year})"
            textRating.text = "⭐ N/A" // TODO: Obtener rating real del profesor
            textReference.text = "Excelente profesor, muy claro en sus explicaciones y siempre dispuesto a ayudar." // TODO: Obtener referencia real
            
            // Formatear fecha
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            textViewDate.text = "Publicado: ${dateFormat.format(studentTeacher.createdAt)}"
            
            // Configurar contadores (por ahora valores mock)
            textViewLikeCount.text = "0" // TODO: Obtener likes reales
            textViewDislikeCount.text = "0" // TODO: Obtener dislikes reales
            
            // Configurar visibilidad de botones según el contexto
            buttonRemove.visibility = if (showOwnActions) View.VISIBLE else View.GONE
            
            // Configurar click listeners
            buttonRemove.setOnClickListener {
                onRemoveClick(studentTeacher)
            }
            
            // Configurar likes/dislikes si están disponibles
            onLikeClick?.let { likeCallback ->
                layoutLike.setOnClickListener {
                    likeCallback(studentTeacher)
                }
            }
            
            onDislikeClick?.let { dislikeCallback ->
                layoutDislike.setOnClickListener {
                    dislikeCallback(studentTeacher)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<StudentTeacher>() {
        override fun areItemsTheSame(oldItem: StudentTeacher, newItem: StudentTeacher): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: StudentTeacher, newItem: StudentTeacher): Boolean {
            return oldItem == newItem
        }
    }
}
