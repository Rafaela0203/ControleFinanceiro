package br.edu.utfpr.controlefinanceiro.presentention.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
// NOVO: Import para navegação
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import br.edu.utfpr.controlefinanceiro.R
import br.edu.utfpr.controlefinanceiro.data.repository.CategoryRepository
import br.edu.utfpr.controlefinanceiro.data.repository.TransactionRepository
import br.edu.utfpr.controlefinanceiro.databinding.FragmentDashboardBinding
import br.edu.utfpr.controlefinanceiro.presentention.transactions.TransactionViewModel
import br.edu.utfpr.controlefinanceiro.presentention.transactions.TransactionViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            TransactionRepository(
                FirebaseFirestore.getInstance(),
                FirebaseAuth.getInstance()
            ),
            CategoryRepository(
                FirebaseFirestore.getInstance(),
                FirebaseAuth.getInstance()
            )
        )
    }

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. Observar os dados do ViewModel
        observeFinancialSummary()

        // 3. Recarrega os dados.
        viewModel.loadAndCalculateTransactions()

        // NOVO: Configura o clique do Botão de Ação Flutuante (FAB) [RF02]
        binding.fabAddTransaction.setOnClickListener {
            // Este ID (action_...) será criado no próximo arquivo (nav_graph.xml)
            findNavController().navigate(R.id.action_dashboardFragment_to_transactionRegistrationFragment)
        }
    }

    private fun observeFinancialSummary() {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        // Observa o Saldo Total (RF05)
        viewModel.totalBalance.observe(viewLifecycleOwner) { balance ->
            binding.tvTotalBalance.text = currencyFormat.format(balance)
            val colorRes = if (balance >= 0) R.color.green_positive else R.color.red_negative
            binding.tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
        }

        // Observa a Receita Total (RF05)
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.includeIncome.tvValue.text = currencyFormat.format(income)
            binding.includeIncome.tvLabel.text = "Receitas"
        }

        // Observa a Despesa Total (RF05)
        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.includeExpense.tvValue.text = currencyFormat.format(expense)
            binding.includeExpense.tvLabel.text = "Despesas"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}