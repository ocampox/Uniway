package com.universidad.uniway.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.universidad.uniway.R
import com.universidad.uniway.data.TeacherRecommendation
import com.universidad.uniway.databinding.FragmentAllRecommendationsBinding
import com.universidad.uniway.ui.ViewModelFactory

class AllRecommendationsFragment : Fragment() {

    private var _binding: FragmentAllRecommendationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AllRecommendationsViewModel
    private lateinit var adapter: TeacherRecommendationAdapter
    private var allRecommendations: List<TeacherRecommendation> = emptyList()
    private var filteredRecommendations: List<TeacherRecommendation> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllRecommendationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[AllRecommendationsViewModel::class.java]

        setupRecyclerView()
        setupSubjectFilter()
        setupClickListeners()
        observeViewModel()

        // Cargar todas las recomendaciones
        viewModel.loadAllRecommendations()
    }

    private fun setupRecyclerView() {
        adapter = TeacherRecommendationAdapter(
            onRemoveClick = { recommendation ->
                android.util.Log.d("AllRecommendationsFragment", "Remove clicked for: ${recommendation.teacherName}")
                // Verificar si es recomendación propia o si es administrador
                val currentUserId = com.universidad.uniway.network.TokenManager(requireContext()).getUserId()
                val userRole = com.universidad.uniway.network.TokenManager(requireContext()).getUserRole()
                
                if (recommendation.studentId == currentUserId || userRole == "ADMINISTRATION") {
                    showDeleteConfirmationDialog(recommendation)
                } else {
                    android.widget.Toast.makeText(requireContext(), "No tienes permisos para eliminar esta recomendación", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            onLikeClick = { recommendation ->
                android.util.Log.d("AllRecommendationsFragment", "Like clicked for: ${recommendation.teacherName}")
                // Verificar si es recomendación propia
                val currentUserId = com.universidad.uniway.network.TokenManager(requireContext()).getUserId()
                if (recommendation.studentId == currentUserId) {
                    android.widget.Toast.makeText(requireContext(), "No puedes dar like a tus propias recomendaciones", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Dando like a ${recommendation.teacherName}", android.widget.Toast.LENGTH_SHORT).show()
                    viewModel.likeRecommendation(recommendation)
                }
            },
            onDislikeClick = { recommendation ->
                android.util.Log.d("AllRecommendationsFragment", "Dislike clicked for: ${recommendation.teacherName}")
                // Verificar si es recomendación propia
                val currentUserId = com.universidad.uniway.network.TokenManager(requireContext()).getUserId()
                if (recommendation.studentId == currentUserId) {
                    android.widget.Toast.makeText(requireContext(), "No puedes dar dislike a tus propias recomendaciones", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Dando dislike a ${recommendation.teacherName}", android.widget.Toast.LENGTH_SHORT).show()
                    viewModel.dislikeRecommendation(recommendation)
                }
            },
            showOwnActions = true, // Mostrar botón eliminar, se controlará la visibilidad en el adaptador
            getCurrentUserId = { com.universidad.uniway.network.TokenManager(requireContext()).getUserId() },
            getUserRole = { com.universidad.uniway.network.TokenManager(requireContext()).getUserRole() }
        )

        binding.recyclerViewAllRecommendations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AllRecommendationsFragment.adapter
        }
    }

    private fun setupSubjectFilter() {
        // Configurar el spinner inicialmente vacío
        val initialAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("Todas las materias")
        )
        initialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubjectFilter.adapter = initialAdapter

        binding.spinnerSubjectFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSubject = parent?.getItemAtPosition(position) as String
                filterBySubject(selectedSubject)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddRecommendation.setOnClickListener {
            findNavController().navigate(R.id.action_all_recommendations_to_add_teacher)
        }

        binding.imageViewClearFilter.setOnClickListener {
            clearFilter()
        }
        
        // TEMPORAL: Long press para forzar recarga (debugging)
        binding.fabAddRecommendation.setOnLongClickListener {
            android.widget.Toast.makeText(requireContext(), "Forzando recarga de recomendaciones...", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.forceReloadRecommendations()
            true
        }
    }

    private fun observeViewModel() {
        viewModel.allRecommendations.observe(viewLifecycleOwner) { recommendations ->
            allRecommendations = recommendations
            filteredRecommendations = recommendations
            
            // Actualizar el filtro de materias
            updateSubjectFilter(recommendations)
            
            // Mostrar las recomendaciones
            updateRecyclerView(filteredRecommendations)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Mostrar/ocultar loading indicator
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSubjectFilter(recommendations: List<TeacherRecommendation>) {
        // Obtener todas las materias únicas
        val subjects = recommendations.map { it.subject }.distinct().sorted()
        val filterOptions = mutableListOf("Todas las materias")
        filterOptions.addAll(subjects)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filterOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubjectFilter.adapter = adapter
    }

    private fun filterBySubject(subject: String) {
        filteredRecommendations = if (subject == "Todas las materias") {
            binding.imageViewClearFilter.visibility = View.GONE
            allRecommendations
        } else {
            binding.imageViewClearFilter.visibility = View.VISIBLE
            allRecommendations.filter { it.subject == subject }
        }
        
        updateRecyclerView(filteredRecommendations)
    }

    private fun clearFilter() {
        binding.spinnerSubjectFilter.setSelection(0)
        binding.imageViewClearFilter.visibility = View.GONE
        filteredRecommendations = allRecommendations
        updateRecyclerView(filteredRecommendations)
    }

    private fun updateRecyclerView(recommendations: List<TeacherRecommendation>) {
        if (recommendations.isEmpty()) {
            binding.recyclerViewAllRecommendations.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewAllRecommendations.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            adapter.submitList(recommendations)
        }
    }

    private fun showDeleteConfirmationDialog(recommendation: TeacherRecommendation) {
        val dialogView = layoutInflater.inflate(com.universidad.uniway.R.layout.dialog_delete_confirmation, null)
        val messageText = dialogView.findViewById<android.widget.TextView>(com.universidad.uniway.R.id.textMessage)
        val buttonCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(com.universidad.uniway.R.id.buttonCancel)
        val buttonDelete = dialogView.findViewById<com.google.android.material.button.MaterialButton>(com.universidad.uniway.R.id.buttonDelete)
        
        messageText.text = "¿Estás seguro de que deseas eliminar la recomendación de ${recommendation.teacherName}?\n\nEsta acción no se puede deshacer."
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        buttonDelete.setOnClickListener {
            dialog.dismiss()
            viewModel.deleteRecommendation(recommendation)
            android.widget.Toast.makeText(requireContext(), "Recomendación eliminada exitosamente", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}