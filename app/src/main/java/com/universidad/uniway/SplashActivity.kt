package com.universidad.uniway

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import com.universidad.uniway.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashDuration = 3000L // 3 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ocultar la barra de estado para pantalla completa
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupAnimations()
        startSplashSequence()
    }

    private fun setupAnimations() {
        // Verificar si existe el logo personalizado
        checkForCustomLogo()
        
        // Animación de entrada del logo
        val logoScaleX = ObjectAnimator.ofFloat(binding.imageViewLogo, "scaleX", 0.3f, 1.0f)
        val logoScaleY = ObjectAnimator.ofFloat(binding.imageViewLogo, "scaleY", 0.3f, 1.0f)
        val logoAlpha = ObjectAnimator.ofFloat(binding.imageViewLogo, "alpha", 0f, 1f)
        
        logoScaleX.duration = 1200
        logoScaleY.duration = 1200
        logoAlpha.duration = 1200
        
        logoScaleX.interpolator = AccelerateDecelerateInterpolator()
        logoScaleY.interpolator = AccelerateDecelerateInterpolator()
        logoAlpha.interpolator = AccelerateDecelerateInterpolator()
        
        val logoAnimatorSet = AnimatorSet()
        logoAnimatorSet.playTogether(logoScaleX, logoScaleY, logoAlpha)
        
        // Animación de entrada del eslogan (con delay)
        val sloganAlpha = ObjectAnimator.ofFloat(binding.textViewSlogan, "alpha", 0f, 1f)
        val sloganTranslationY = ObjectAnimator.ofFloat(binding.textViewSlogan, "translationY", 50f, 0f)
        
        sloganAlpha.duration = 800
        sloganTranslationY.duration = 800
        sloganAlpha.startDelay = 800
        sloganTranslationY.startDelay = 800
        
        sloganAlpha.interpolator = AccelerateDecelerateInterpolator()
        sloganTranslationY.interpolator = AccelerateDecelerateInterpolator()
        
        val sloganAnimatorSet = AnimatorSet()
        sloganAnimatorSet.playTogether(sloganAlpha, sloganTranslationY)
        
        // Ejecutar animaciones
        logoAnimatorSet.start()
        sloganAnimatorSet.start()
        
        // Mostrar indicador de carga después de un delay
        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressBar.visibility = View.VISIBLE
            val progressAlpha = ObjectAnimator.ofFloat(binding.progressBar, "alpha", 0f, 1f)
            progressAlpha.duration = 500
            progressAlpha.start()
        }, 1800)
    }
    
    private fun checkForCustomLogo() {
        try {
            // Intentar cargar el logo personalizado
            val logoResource = resources.getIdentifier("logo_uniway", "drawable", packageName)
            if (logoResource != 0) {
                binding.imageViewLogo.setImageResource(logoResource)
            }
        } catch (e: Exception) {
            // Si no se encuentra el logo personalizado, usar el placeholder
            // El placeholder ya está configurado en el XML
        }
    }

    private fun startSplashSequence() {
        Handler(Looper.getMainLooper()).postDelayed({
            // Animación de salida
            val fadeOut = AlphaAnimation(1f, 0f)
            fadeOut.duration = 500
            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                
                override fun onAnimationEnd(animation: Animation?) {
                    // Navegar a AuthActivity
                    val intent = Intent(this@SplashActivity, AuthActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
                
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            
            binding.root.startAnimation(fadeOut)
        }, splashDuration)
    }

    override fun onBackPressed() {
        // No permitir volver atrás desde el splash
        // No hacer nada
    }
}
