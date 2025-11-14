package br.edu.utfpr.controlefinanceiro.presentention.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.controlefinanceiro.MainActivity
import br.edu.utfpr.controlefinanceiro.R
import br.edu.utfpr.controlefinanceiro.data.model.User
import br.edu.utfpr.controlefinanceiro.databinding.ActivityAuthBinding // NOVO: Usa o 'ActivityAuthBinding'
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.Executor

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    private lateinit var firestore: FirebaseFirestore

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialização
        auth = Firebase.auth
        credentialManager = CredentialManager.create(this)
        firestore = FirebaseFirestore.getInstance()

        // Configura a lógica de biometria
        setupBiometricLogic()

        // --- Configuração dos Listeners ---

        // 1. Botão Login com Google
        binding.btnLoginGoogle.setOnClickListener {
            btLoginOnClick()
        }

        // 2. Botão Login com E-mail
        binding.btnLoginEmail.setOnClickListener {
            // Navega para a tela de Registro/Login com formulário
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        // 3. Botão Biometria
        binding.btnLoginBiometric.setOnClickListener {
            showBiometricPrompt()
        }

        // 4. Botão "Continuar sem login"
        binding.tvContinueWithoutLogin.setOnClickListener {
            Log.d("Auth", "Continuando sem login")
            goToMainActivity()
        }
    }

    override fun onStart() {
        super.onStart()
        // Se o usuário já está logado, oferece biometria.
        // Se não, mostra a tela de login padrão.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("Auth", "Usuário logado, verificando biometria.")
            checkBiometricSupportAndShowButton()
        } else {
            Log.d("Auth", "Usuário não logado, mostrando login padrão.")
            showStandardLoginUI()
        }
    }

    // --- Lógica de Login (Sem alteração) ---

    private fun btLoginOnClick() {
        launchCredentialManager()
    }

    private fun launchCredentialManager(){
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try{
                val result = credentialManager.getCredential(
                    context = this@AuthActivity,
                    request = request
                )
                handleSignIn(result.credential)
            }catch (e: Exception){
                Log.e("Auth", "Erro ao recuperar credenciais: ${e.localizedMessage}")
                Toast.makeText(
                    this@AuthActivity,
                    "Falha no login: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w("Auth", "A credencial não é do tipo ID do Google.")
            Toast.makeText(this, "Falha na credencial.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "signInWithCredential:success")
                    checkAndCreateProfile(auth.currentUser)
                } else {
                    Log.w( "Erro", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Falha na autenticação.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAndCreateProfile(user: com.google.firebase.auth.FirebaseUser?) {
        user ?: return goToMainActivity()

        firestore.collection("users").document(user.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.exists()) {
                    val newUser = User(
                        id = user.uid,
                        name = user.displayName ?: user.email?.substringBefore('@') ?: "Usuário Google",
                        email = user.email ?: "",
                        creationDate = Date()
                    )
                    firestore.collection("users").document(user.uid).set(newUser)
                        .addOnSuccessListener { goToMainActivity() }
                        .addOnFailureListener { e ->
                            Log.e("Auth", "Falha ao salvar perfil: ${e.message}")
                            goToMainActivity()
                        }
                } else {
                    goToMainActivity()
                }
            }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // --- Lógica de Biometria (Atualizada para o novo layout) ---

    private fun setupBiometricLogic() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e("Biometric", "Erro: $errorCode :: $errString")
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                        showStandardLoginUI()
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d("Biometric", "Autenticação bem-sucedida!")
                    goToMainActivity()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w("Biometric", "Falha na autenticação.")
                    Toast.makeText(applicationContext, "Digital não reconhecida.", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login Biométrico")
            .setSubtitle("Use sua digital para entrar no app")
            .setNegativeButtonText("Usar outra conta")
            .build()
    }

    private fun checkBiometricSupportAndShowButton() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("Biometric", "Biometria disponível.")
                showBiometricLoginUI()
                showBiometricPrompt()
            }
            else -> {
                Log.e("Biometric", "Biometria não suportada.")
                goToMainActivity()
            }
        }
    }

    private fun showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Mostra a UI de login padrão (Logo, Títulos, Botões Google/Email)
     */
    private fun showStandardLoginUI() {
        binding.ivLogoAuth.visibility = View.VISIBLE
        binding.tvTitleEquilibra.visibility = View.VISIBLE
        binding.tvSubtitleEquilibra.visibility = View.VISIBLE
        binding.btnLoginGoogle.visibility = View.VISIBLE
        binding.btnLoginEmail.visibility = View.VISIBLE
        binding.tvContinueWithoutLogin.visibility = View.VISIBLE

        binding.btnLoginBiometric.visibility = View.GONE
        binding.tvBiometricLabel.visibility = View.GONE
    }

    /**
     * Mostra apenas a UI de login biométrico
     */
    private fun showBiometricLoginUI() {
        binding.ivLogoAuth.visibility = View.GONE
        binding.tvTitleEquilibra.visibility = View.GONE
        binding.tvSubtitleEquilibra.visibility = View.GONE
        binding.btnLoginGoogle.visibility = View.GONE
        binding.btnLoginEmail.visibility = View.GONE
        binding.tvContinueWithoutLogin.visibility = View.GONE

        binding.btnLoginBiometric.visibility = View.VISIBLE
        binding.tvBiometricLabel.visibility = View.VISIBLE
    }
}