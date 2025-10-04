package com.universidad.uniway.ui.forum

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.universidad.uniway.R
import com.universidad.uniway.data.Post
import com.universidad.uniway.data.PostType

class EditPostDialog : DialogFragment() {

    private lateinit var post: Post
    private var onEditPost: ((String, PostType) -> Unit)? = null
    
    // Views
    private lateinit var editTextContent: android.widget.EditText
    private lateinit var spinnerPostType: android.widget.Spinner
    private lateinit var buttonCancel: android.widget.Button
    private lateinit var buttonSave: android.widget.Button

    companion object {
        private const val ARG_POST = "post"
        
        fun newInstance(post: Post, onEditPost: (String, PostType) -> Unit): EditPostDialog {
            val dialog = EditPostDialog()
            dialog.onEditPost = onEditPost
            
            val args = Bundle()
            args.putParcelable(ARG_POST, post)
            dialog.arguments = args
            
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        post = arguments?.getParcelable(ARG_POST) ?: throw IllegalArgumentException("Post is required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Usar el layout simplificado que garantiza que los textos se muestren
        return inflater.inflate(R.layout.dialog_edit_post_simple, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicializar views
        editTextContent = view.findViewById(R.id.editTextContent)
        spinnerPostType = view.findViewById(R.id.spinnerPostType)
        buttonCancel = view.findViewById(R.id.buttonCancel)
        buttonSave = view.findViewById(R.id.buttonSave)
        
        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // Configurar el contenido actual del post
        editTextContent.setText(post.content)
        
        // Configurar el spinner de tipos de post
        val postTypes = PostType.values().map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, postTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPostType.adapter = adapter
        
        // Seleccionar el tipo actual del post
        val currentTypeIndex = PostType.values().indexOf(post.postType)
        spinnerPostType.setSelection(currentTypeIndex)
    }

    private fun setupClickListeners() {
        buttonCancel.setOnClickListener {
            dismiss()
        }
        
        buttonSave.setOnClickListener {
            val content = editTextContent.text.toString().trim()
            
            if (content.isEmpty()) {
                editTextContent.error = "El contenido no puede estar vacío"
                return@setOnClickListener
            }
            
            if (content.length < 10) {
                editTextContent.error = "El contenido debe tener al menos 10 caracteres"
                return@setOnClickListener
            }
            
            val selectedPostType = PostType.values()[spinnerPostType.selectedItemPosition]
            
            onEditPost?.invoke(content, selectedPostType)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        // Configurar el tamaño del diálogo
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // No hay binding que limpiar
    }
}