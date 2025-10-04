package com.universidad.uniway

import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.universidad.uniway.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar transición suave
        setupSmoothTransition()
    }

    private fun setupSmoothTransition() {
        // Animación de entrada suave
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 800
        fadeIn.fillAfter = true
        
        binding.root.startAnimation(fadeIn)
        
        // Configurar la barra de estado
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
    }
}
