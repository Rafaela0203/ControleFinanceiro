package br.edu.utfpr.controlefinanceiro.presentention.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import br.edu.utfpr.controlefinanceiro.MainActivity
import br.edu.utfpr.controlefinanceiro.data.model.User
import br.edu.utfpr.controlefinanceiro.databinding.ActivityRegistrationBinding
import java.util.Date

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRegisterButton()
        setupBackToLoginLink()
    }

    private fun setupRegisterButton() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmailReg.text.toString()
            val password = binding.etPasswordReg.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validação de Senha (RNF03)
            if (password.length < 6) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)

            // 1. Cria o usuário no Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { authTask ->
                    if (authTask.isSuccessful) {
                        val firebaseUser = authTask.result?.user
                        firebaseUser?.let {
                            // 2. Salva o perfil no Firestore (RNF02)
                            saveUserProfile(it.uid, name, email)
                        }
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Falha no Cadastro: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun saveUserProfile(uid: String, name: String, email: String) {
        val newUser = User(
            id = uid,
            name = name,
            email = email,
            creationDate = Date()
        )

        firestore.collection("users").document(uid).set(newUser)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Cadastro concluído! Bem-vindo(a)!", Toast.LENGTH_SHORT).show()
                goToMainActivity()
            }
            .addOnFailureListener { e ->
                // Reverte: Deleta o usuário criado no Auth se falhar no Firestore
                auth.currentUser?.delete()
                showLoading(false)
                Toast.makeText(this, "Erro ao salvar perfil. Tente novamente.", Toast.LENGTH_LONG).show()
            }
    }

    // --- Utilidades ---

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
    }

    private fun setupBackToLoginLink() {
        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}