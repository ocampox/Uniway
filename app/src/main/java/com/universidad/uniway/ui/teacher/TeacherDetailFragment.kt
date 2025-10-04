package com.universidad.uniway.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.universidad.uniway.R
import com.universidad.uniway.data.Teacher
import com.universidad.uniway.data.TeacherReview
import com.universidad.uniway.network.ApiRepository
import com.universidad.uniway.databinding.FragmentTeacherDetailBinding
import kotlinx.coroutines.launch

class TeacherDetailFragment : Fragment() {

    private var _binding: FragmentTeacherDetailBinding? = null
    private val binding get() = _binding!!

    private val apiRepository = ApiRepository()
    private lateinit var reviewsAdapter: TeacherReviewAdapter

    private var teacher: Teacher? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teacher = arguments?.getParcelable("teacher")

        setupRecycler()
        bindTeacher()
        loadReviews()

        binding.buttonSendReview.setOnClickListener { sendReview() }
        binding.ratingBar.setOnRatingBarChangeListener { _: RatingBar, _: Float, fromUser: Boolean ->
            if (fromUser) {
                // hold local rating until sending
            }
        }
    }

    private fun setupRecycler() {
        reviewsAdapter = TeacherReviewAdapter()
        binding.recyclerReviews.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reviewsAdapter
        }
    }

    private fun bindTeacher() {
        teacher?.let { t ->
            binding.textTeacherName.text = t.fullName
            binding.textTeacherEmail.text = t.institutionalEmail
            binding.ratingBar.rating = t.averageRating.toFloat()
            val availability = buildString {
                append(if (t.virtualWorkshopsAvailable) "Talleres virtuales disponibles" else "Talleres no disponibles")
                append("\n")
                append(if (t.advisoriesAvailable) "Asesorías virtuales disponibles" else "Asesorías virtuales no disponibles")
            }
            binding.textAvailability.text = availability

            binding.chipsSubjects.removeAllViews()
            t.subjects.forEach { subject ->
                val tv = TextView(requireContext())
                tv.text = "\uD83D\uDCD1 $subject"
                tv.setPadding(8, 6, 8, 6)
                binding.chipsSubjects.addView(tv)
            }
        }
    }

    private fun loadReviews() {
        // TODO: Implementar con el nuevo sistema de recomendaciones
        // Por ahora, mostrar mensaje de que no hay reviews disponibles
        Toast.makeText(context, "Sistema de reviews temporalmente deshabilitado", Toast.LENGTH_SHORT).show()
    }

    private fun sendReview() {
        val t = teacher ?: return
        val comment = binding.editReview.text.toString().trim()
        val rating = binding.ratingBar.rating.toInt()
        if (rating < 0 || rating > 5) {
            Toast.makeText(context, "La calificación debe ser de 0 a 5", Toast.LENGTH_SHORT).show()
            return
        }
        if (comment.isEmpty()) {
            Toast.makeText(context, "Escribe tu comentario", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: obtener el id del usuario actual desde persistencia/token
        val authorId = "demo-author-id"

        // TODO: Implementar con el nuevo sistema de recomendaciones
        Toast.makeText(context, "Sistema de reviews temporalmente deshabilitado", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private class TeacherReviewAdapter : RecyclerView.Adapter<TeacherReviewViewHolder>() {
    private val items = mutableListOf<TeacherReview>()

    fun submit(list: List<TeacherReview>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return TeacherReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeacherReviewViewHolder, position: Int) {
        val item = items[position]
        holder.author.text = item.authorName
        holder.content.text = item.comment
        holder.time.text = "★ ${item.rating}"
    }

    override fun getItemCount(): Int = items.size
}

private class TeacherReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val author: TextView = view.findViewById(R.id.textViewCommentAuthor)
    val content: TextView = view.findViewById(R.id.textViewCommentContent)
    val time: TextView = view.findViewById(R.id.textViewCommentTime)
}



