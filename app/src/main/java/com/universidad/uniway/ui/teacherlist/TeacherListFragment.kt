package com.universidad.uniway.ui.teacherlist

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
import com.universidad.uniway.databinding.FragmentTeacherListBinding
import com.universidad.uniway.ui.ViewModelFactory
import com.universidad.uniway.ui.teacher.TeacherRecommendationAdapter

class TeacherListFragment : Fragment() {

    private var _binding: FragmentTeacherListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TeacherListViewModel
    private lateinit var adapter: TeacherRecommendationAdapter
    private var allTeachers: List<TeacherRecommendation> = emptyList()
    private var filteredTeachers: List<TeacherRecommendation> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[TeacherListViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        setupSubjectFilter()
        observeViewModel()

        // Cargar profesores del estudiante
        viewModel.loadStudentTeachers()
    }

    private fun setupRecyclerView() {
        adapter = TeacherRecommendationAdapter(
            onRemoveClick = { recommendation ->
                showDeleteConfirmationDialog(recommendation)
            },
            showOwnActions = true // Mostrar botón eliminar para propias recomendaciones
        )

        binding.recyclerViewTeachers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TeacherListFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTeacher.setOnClickListener {
            findNavController().navigate(R.id.action_teacher_list_to_add_teacher)
        }

        binding.imageViewClearFilter.setOnClickListener {
            clearFilter()
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

    private fun updateSubjectFilter(teachers: List<TeacherRecommendation>) {
        // Obtener todas las materias únicas
        val subjects = teachers.map { it.subject }.distinct().sorted()
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
        filteredTeachers = if (subject == "Todas las materias") {
            binding.imageViewClearFilter.visibility = View.GONE
            allTeachers
        } else {
            binding.imageViewClearFilter.visibility = View.VISIBLE
            allTeachers.filter { it.subject == subject }
        }
        
        updateRecyclerView(filteredTeachers)
    }

    private fun clearFilter() {
        binding.spinnerSubjectFilter.setSelection(0)
        binding.imageViewClearFilter.visibility = View.GONE
        filteredTeachers = allTeachers
        updateRecyclerView(filteredTeachers)
    }

    private fun updateRecyclerView(teachers: List<TeacherRecommendation>) {
        if (teachers.isEmpty()) {
            binding.recyclerViewTeachers.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewTeachers.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            adapter.submitList(teachers)
        }
    }

    private fun observeViewModel() {
        viewModel.studentTeachers.observe(viewLifecycleOwner) { teachers ->
            android.util.Log.d("TeacherListFragment", "Received ${teachers.size} teachers")
            allTeachers = teachers
            filteredTeachers = teachers
            
            // Actualizar el filtro de materias
            updateSubjectFilter(teachers)
            
            // Mostrar los profesores
            updateRecyclerView(filteredTeachers)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            android.util.Log.d("TeacherListFragment", "Loading state: $isLoading")
            // TODO: Mostrar/ocultar loading indicator
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            android.util.Log.d("TeacherListFragment", "Error message: $message")
            if (message.isNotEmpty()) {
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(recommendation: TeacherRecommendation) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_confirmation, null)
        val messageText = dialogView.findViewById<android.widget.TextView>(R.id.textMessage)
        val buttonCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonCancel)
        val buttonDelete = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonDelete)
        
        messageText.text = "¿Estás seguro de que deseas eliminar la recomendación del profesor ${recommendation.teacherName}?\n\nEsta acción no se puede deshacer."
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        buttonDelete.setOnClickListener {
            dialog.dismiss()
            viewModel.removeTeacher(recommendation)
        }
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
