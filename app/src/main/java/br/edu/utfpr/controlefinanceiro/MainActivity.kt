package br.edu.utfpr.controlefinanceiro

// Imports para Permissões e Notificações
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import br.edu.utfpr.controlefinanceiro.utils.NotificationHelper

// Imports Padrão
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.controlefinanceiro.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

// Imports para Navegação (Jetpack)
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    val bd = Firebase.firestore

    // Lançador para o pedido de permissão de notificação
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Notification", "Permissão de notificação concedida.")
        } else {
            Log.w("Notification", "Permissão de notificação negada.")
            Toast.makeText(this, "Alertas de metas podem não funcionar.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Inicializar autenticação
        auth = Firebase.auth
        credentialManager = CredentialManager.create(this)

        // 2. Configurar o botão Sair
        binding.btSair.setOnClickListener {
            signOut()
        }

        // 3. Configurar Notificações (RF07)
        NotificationHelper.createNotificationChannel(this)
        askNotificationPermission()

        // 4. Configurar a Navegação (Jetpack)
        // Encontra o "contêiner" de fragments no XML
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Encontra o menu inferior no XML
        val bottomNavView = binding.bottomNavView

        // Conecta o menu inferior ao 'navController'
        // Isso faz a mágica de trocar os fragments ao clicar nos ícones
        NavigationUI.setupWithNavController(bottomNavView, navController)
    }

    /**
     * Pede a permissão POST_NOTIFICATIONS (Obrigatória no Android 13+)
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // Lança o pedido de permissão
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Desloga o usuário do Firebase e do Credential Manager
     */
    private fun signOut() {
        auth.signOut()

        lifecycleScope.launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                Log.d("Logout", "Limpando as credenciais do usuário")
                Toast.makeText(this@MainActivity, "Logout realizado.", Toast.LENGTH_SHORT).show()
                navigateToLoginScreen()
            } catch (e: ClearCredentialException) {
                Log.e("Logout", "Não foi possível limpar as credenciais: ${e.localizedMessage}")
                navigateToLoginScreen()
            } catch (e: Exception) {
                Log.e("Logout", "Erro desconhecido no logout: ${e.localizedMessage}")
                navigateToLoginScreen()
            }
        }
    }

    /**
     * Navega de volta para a tela de Login e limpa a pilha de Activities
     */
    private fun navigateToLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}