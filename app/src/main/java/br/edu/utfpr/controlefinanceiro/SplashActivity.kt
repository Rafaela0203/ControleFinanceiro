package br.edu.utfpr.controlefinanceiro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.controlefinanceiro.presentention.auth.AuthActivity // ATUALIZADO

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // Inicia a AuthActivity
            val intent = Intent(this, AuthActivity::class.java) // ATUALIZADO
            startActivity(intent)

            finish()
        }, 2000)
    }
}