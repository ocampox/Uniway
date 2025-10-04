package com.universidad.uniway.ui.addteacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.universidad.uniway.R
import com.universidad.uniway.databinding.FragmentAddTeacherBinding
import com.universidad.uniway.ui.ViewModelFactory
import java.util.Calendar

class AddTeacherFragment : Fragment() {

    private var _binding: FragmentAddTeacherBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddTeacherViewModel
    private var currentRating: Int = 0
    private lateinit var stars: List<android.widget.ImageView>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTeacherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[AddTeacherViewModel::class.java]

        setupClickListeners()
        setupRatingStars()
        observeViewModel()
        setDefaultValues()
    }

    private fun setupClickListeners() {
        binding.buttonAddTeacher.setOnClickListener {
            addTeacher()
        }

        // Botón "Ver Mis Profesores" eliminado
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.buttonAddTeacher.isEnabled = !isLoading
            binding.buttonAddTeacher.text = if (isLoading) "Publicando..." else "Publicar Recomendación"
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                clearForm()
                // Navegar de vuelta a la lista de recomendaciones
                findNavController().navigateUp()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupRatingStars() {
        stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )
        
        // Configurar click listeners para cada estrella
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                setRating(index + 1)
            }
        }
        
        // Configurar rating inicial
        setRating(0)
    }
    
    private fun setRating(rating: Int) {
        currentRating = rating
        
        // Actualizar visual de las estrellas
        stars.forEachIndexed { index, star ->
            if (index < rating) {
                // Estrella activa (llena)
                star.setImageResource(R.drawable.ic_star_filled)
                star.imageTintList = androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.star_active)
            } else {
                // Estrella inactiva (vacía)
                star.setImageResource(R.drawable.ic_star_outline)
                star.imageTintList = androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.star_inactive)
            }
        }
        
        // Actualizar texto descriptivo
        val description = when (rating) {
            0 -> "Toca las estrellas para calificar"
            1 -> "⭐ Muy malo"
            2 -> "⭐⭐ Malo"
            3 -> "⭐⭐⭐ Regular"
            4 -> "⭐⭐⭐⭐ Bueno"
            5 -> "⭐⭐⭐⭐⭐ Excelente"
            else -> "Calificación inválida"
        }
        
        binding.textRatingDescription.text = description
    }

    private fun setDefaultValues() {
        // Establecer semestre por defecto
        binding.editTextSemester.setText("2024-2")
    }

    private fun addTeacher() {
        val teacherName = binding.editTextTeacherName.text.toString().trim()
        val subject = binding.editTextSubject.text.toString().trim()
        val semester = binding.editTextSemester.text.toString().trim()
        val reference = binding.editTextReference.text.toString().trim()

        // Validaciones
        if (teacherName.isEmpty()) {
            binding.editTextTeacherName.error = "Ingresa el nombre completo del profesor"
            return
        }

        if (teacherName.length < 5) {
            binding.editTextTeacherName.error = "Ingresa el nombre completo (mínimo 5 caracteres)"
            return
        }

        if (subject.isEmpty()) {
            binding.editTextSubject.error = "Ingresa la materia que cursaste"
            return
        }

        if (semester.isEmpty()) {
            binding.editTextSemester.error = "Ingresa el semestre cursado"
            return
        }

        if (reference.isEmpty()) {
            binding.editTextReference.error = "Escribe tu referencia y recomendación"
            return
        }

        if (reference.length < 20) {
            binding.editTextReference.error = "La referencia debe ser más detallada (mínimo 20 caracteres)"
            return
        }

        if (currentRating == 0) {
            Toast.makeText(requireContext(), "Por favor, califica al profesor con estrellas", Toast.LENGTH_SHORT).show()
            return
        }

        // Limpiar errores
        binding.editTextTeacherName.error = null
        binding.editTextSubject.error = null
        binding.editTextSemester.error = null
        binding.editTextReference.error = null

        // Publicar recomendación con rating
        viewModel.addTeacherRecommendation(teacherName, subject, semester, reference, currentRating)
    }

    private fun clearForm() {
        binding.editTextTeacherName.text?.clear()
        binding.editTextSubject.text?.clear()
        binding.editTextReference.text?.clear()
        setRating(0) // Limpiar rating
        // Mantener semestre para facilitar agregar múltiples recomendaciones
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
