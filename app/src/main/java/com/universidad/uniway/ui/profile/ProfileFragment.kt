package com.universidad.uniway.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.universidad.uniway.R
import com.universidad.uniway.data.User
import com.universidad.uniway.data.UserRole
import com.universidad.uniway.databinding.FragmentProfileBinding
import com.universidad.uniway.ui.ViewModelFactory

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(requireContext()))[ProfileViewModel::class.java]
    }
    
    // Launcher para seleccionar imagen
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                // Aquí puedes procesar la imagen seleccionada
                Toast.makeText(context, "Imagen del carnet seleccionada", Toast.LENGTH_SHORT).show()
                // En una aplicación real, aquí subirías la imagen al servidor
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
        loadUserProfile()
    }
    
    override fun onResume() {
        super.onResume()
        // Recargar datos del usuario cuando el fragment se vuelve visible
        loadUserProfile()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner, Observer { user ->
            user?.let { updateUI(it) }
        })

        viewModel.updateResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is ProfileUpdateResult.Success -> {
                    Toast.makeText(context, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                }
                is ProfileUpdateResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.buttonUpdateProfile.isEnabled = !isLoading
            binding.buttonUpdateProfile.text = if (isLoading) "Actualizando..." else "Actualizar Perfil"
        })
    }

    private fun setupClickListeners() {
        binding.buttonUpdateProfile.setOnClickListener {
            updateProfile()
        }
        
        binding.buttonAttachCard.setOnClickListener {
            selectCardImage()
        }
        
        binding.buttonLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserProfile() {
        // Limpiar datos de usuario anterior antes de cargar el nuevo
        viewModel.clearOldUserData()
        // Cargar perfil del usuario actual
        viewModel.loadCurrentUser()
    }

    private fun updateUI(user: User) {
        android.util.Log.d("ProfileFragment", "Updating UI with user: ${user.fullName} (${user.email})")
        android.util.Log.d("ProfileFragment", "User data - ID: ${user.id}, Name: ${user.fullName}, Email: ${user.email}, Phone: ${user.phone}, Address: ${user.address}")
        
        // Verificar que el usuario tiene datos válidos
        if (user.fullName.isBlank() || user.email.isBlank()) {
            android.util.Log.w("ProfileFragment", "User data is incomplete, not updating UI")
            return
        }
        
        // Verificar que el email del usuario coincide con el usuario logueado
        val tokenManager = com.universidad.uniway.network.TokenManager(requireContext())
        val loggedEmail = tokenManager.getUserEmail()
        if (loggedEmail != null && user.email != loggedEmail) {
            android.util.Log.w("ProfileFragment", "User email mismatch: User=${user.email}, Logged=${loggedEmail}")
            return
        }
        
        // Extraer el primer nombre para el saludo
        val firstName = user.fullName.split(" ").firstOrNull() ?: user.fullName
        binding.textViewWelcome.text = "¡Bienvenido $firstName!"
        
        binding.editTextFullName.setText(user.fullName)
        binding.editTextEmail.setText(user.email)
        binding.editTextPhone.setText(user.phone)
        binding.editTextAddress.setText(user.address)
        
        // Actualizar información del carnet
        binding.textViewStudentName.text = user.fullName
        binding.textViewStudentId.text = "ID: ${user.studentId}"
        binding.textViewProgram.text = user.program
        
        android.util.Log.d("ProfileFragment", "UI updated successfully for user: ${user.email}")
    }

    private fun updateProfile() {
        val fullName = binding.editTextFullName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val address = binding.editTextAddress.text.toString().trim()

        if (fullName.isBlank() || email.isBlank()) {
            Toast.makeText(context, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateProfile(fullName, email, phone, address)
    }
    
    private fun selectCardImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }
    
    private fun logout() {
        // Limpiar datos de sesión
        val tokenManager = com.universidad.uniway.network.TokenManager(requireContext())
        tokenManager.clearAllData()
        
        // Limpiar usuario actual
        val userRepository = com.universidad.uniway.data.UserRepository(requireContext())
        userRepository.logout()
        
        // Redirigir al login
        val intent = Intent(requireContext(), com.universidad.uniway.AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

