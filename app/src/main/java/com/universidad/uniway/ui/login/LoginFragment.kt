package com.universidad.uniway.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.universidad.uniway.R
import com.universidad.uniway.databinding.FragmentLoginBinding
import com.universidad.uniway.ui.ViewModelFactory
import android.content.Intent
import com.universidad.uniway.MainActivity
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(requireContext()))[LoginViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupAnimations()
        setupObservers()
        setupClickListeners()

        // Auto-login si Recordarme está activo y hay token
        val tokenManager = com.universidad.uniway.network.TokenManager(requireContext())
        if (tokenManager.isRememberMe() && tokenManager.isLoggedIn()) {
            val intent = android.content.Intent(requireContext(), com.universidad.uniway.MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun setupAnimations() {
        // Animación de entrada suave para los elementos
        val fadeIn = android.view.animation.AlphaAnimation(0f, 1f)
        fadeIn.duration = 600
        fadeIn.fillAfter = true
        
        val slideUp = android.view.animation.TranslateAnimation(
            0f, 0f, 100f, 0f
        )
        slideUp.duration = 600
        slideUp.fillAfter = true
        
        val animationSet = android.view.animation.AnimationSet(false)
        animationSet.addAnimation(fadeIn)
        animationSet.addAnimation(slideUp)
        
        binding.root.startAnimation(animationSet)
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is LoginResult.Success -> {
                    Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    // Navegar a MainActivity con navegación inferior
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                is LoginResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.buttonLogin.isEnabled = !isLoading
            binding.buttonLogin.text = if (isLoading) "Iniciando sesión..." else "Iniciar Sesión"
        })
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            loginUser()
        }

        binding.textViewGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.textViewForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot_password)
        }
    }

    private fun loginUser() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString()
        val remember = binding.checkRememberMe.isChecked
        viewModel.loginUser(email, password, remember)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
