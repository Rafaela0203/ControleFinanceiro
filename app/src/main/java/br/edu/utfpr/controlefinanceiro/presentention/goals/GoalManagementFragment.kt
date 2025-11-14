package br.edu.utfpr.controlefinanceiro.presentention.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
// NOVO: Imports necessários
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.utfpr.controlefinanceiro.data.model.FinancialGoal
import br.edu.utfpr.controlefinanceiro.data.repository.FinancialGoalRepository
import br.edu.utfpr.controlefinanceiro.data.repository.TransactionRepository
import br.edu.utfpr.controlefinanceiro.databinding.FragmentGoalManagementBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GoalManagementFragment : Fragment() {

    // NOVO: Corrigido a inicialização do ViewModel
    // Usamos 'activityViewModels' para que ele seja compartilhado com outros fragments
    // (como o TransactionRegistrationFragment, que precisa chamar o checkGoalAlerts)
    private val viewModel: GoalViewModel by activityViewModels {
        GoalViewModelFactory(
            FinancialGoalRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance()),
            TransactionRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
        )
    }

    private var _binding: FragmentGoalManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: GoalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. A inicialização do ViewModel agora é feita acima

        // 2. Configurar RecyclerView e Adapter
        setupRecyclerView()

        // 3. Observar dados (AGORA VAI FUNCIONAR)
        observeViewModel()

        // 4. Botão para adicionar nova meta
        binding.fabAddGoal.setOnClickListener {
            showGoalDialog(null)
        }
    }

    private fun setupRecyclerView() {
        // Adaptador precisaria de um GoalAdapter, semelhante ao CategoryAdapter
        // adapter = GoalAdapter(...)
        binding.rvGoals.layoutManager = LinearLayoutManager(context)
        // binding.rvGoals.adapter = adapter
    }

    private fun observeViewModel() {
        // Esta linha não vai mais travar
        viewModel.goals.observe(viewLifecycleOwner) { goals ->
            // adapter.submitList(goals)
            binding.tvEmptyState.visibility = if (goals.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showGoalDialog(goal: FinancialGoal?) {
        // Lógica de UI para o diálogo...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}