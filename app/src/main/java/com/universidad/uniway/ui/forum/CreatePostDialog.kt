package com.universidad.uniway.ui.forum

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.universidad.uniway.R
import com.universidad.uniway.data.PostPriority
import com.universidad.uniway.data.PostType
import com.universidad.uniway.databinding.DialogCreatePostBinding

class CreatePostDialog : DialogFragment() {

    private var _binding: DialogCreatePostBinding? = null
    private val binding get() = _binding!!
    
    private var onPostCreated: ((String, PostType, PostPriority) -> Unit)? = null

    companion object {
        fun newInstance(onPostCreated: (String, PostType, PostPriority) -> Unit): CreatePostDialog {
            return CreatePostDialog().apply {
                this.onPostCreated = onPostCreated
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCreatePostBinding.inflate(layoutInflater)
        
        setupViews()
        setupClickListeners()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupViews() {
        // Configurar radio buttons por defecto
        binding.radioGeneral.isChecked = true
        
        // Configurar prioridad por defecto
        binding.radioNormal.isChecked = true
        
        // Mostrar/ocultar prioridad según el tipo de post
        setupPostTypeListener()
    }

    private fun setupPostTypeListener() {
        binding.radioGroupPostType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioGeneral -> {
                    binding.layoutPriority.visibility = View.GONE
                }
                R.id.radioNews, R.id.radioAlert -> {
                    binding.layoutPriority.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonCreate.setOnClickListener {
            createPost()
        }
    }

    private fun createPost() {
        val content = binding.editTextContent.text.toString().trim()
        
        // Validaciones
        if (content.isBlank()) {
            Toast.makeText(context, "Por favor escribe el contenido de tu publicación", Toast.LENGTH_SHORT).show()
            return
        }

        if (content.length < 10) {
            Toast.makeText(context, "El contenido debe tener al menos 10 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener tipo de post
        val postType = when (binding.radioGroupPostType.checkedRadioButtonId) {
            R.id.radioGeneral -> PostType.GENERAL
            R.id.radioNews -> PostType.NEWS
            R.id.radioAlert -> PostType.ALERT
            else -> PostType.GENERAL
        }

        // Obtener prioridad
        val priority = when (binding.radioGroupPriority.checkedRadioButtonId) {
            R.id.radioNormal -> PostPriority.NORMAL
            R.id.radioHigh -> PostPriority.HIGH
            R.id.radioUrgent -> PostPriority.URGENT
            else -> PostPriority.NORMAL
        }

        // Llamar callback
        onPostCreated?.invoke(content, postType, priority)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}









