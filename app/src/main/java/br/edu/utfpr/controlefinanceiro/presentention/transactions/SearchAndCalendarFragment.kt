package br.edu.utfpr.controlefinanceiro.presentention.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.utfpr.controlefinanceiro.data.model.Transaction
import br.edu.utfpr.controlefinanceiro.databinding.FragmentSearchAndCalendarBinding
import java.util.Date

/**
 * Fragment responsável pela Busca e Calendário de Transações (RF08 / Quadro 12).
 */
class SearchAndCalendarFragment : Fragment() {

    private lateinit var viewModel: TransactionViewModel

    private var _binding: FragmentSearchAndCalendarBinding? = null
    private val binding get() = _binding!!

    // Adaptador para exibir a lista de resultados (seria o mesmo TransactionAdapter usado no Dashboard)
    // private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchAndCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar o ViewModel
        // viewModel = getViewModel()

        // 2. Configurar a RecyclerView (para mostrar os resultados da busca)
        // setupRecyclerView()

        // 3. Observar resultados
        observeViewModel()

        // 4. Configurar o botão de busca
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        // 5. Configurar o calendário (seria uma biblioteca externa ou componente customizado)
        // setupCalendar()
    }

    private fun observeViewModel() {
        // Observa a lista filtrada para atualizar a UI
        viewModel.filteredTransactions.observe(viewLifecycleOwner) { transactions ->
            // adapter.submitList(transactions)
            binding.tvEmptyState.visibility =
                if (transactions.isEmpty()) View.VISIBLE else View.GONE

            if (transactions.isEmpty() && binding.btnSearch.isPressed) {
                Toast.makeText(context, "Nenhuma transação localizada.", Toast.LENGTH_SHORT).show() // Exceção: Nenhum dado encontrado
            }
        }

        // Observar categorias para popular o Spinner de filtro
        // viewModel.categories.observe(...)
    }

    private fun performSearch() {
        val query = binding.etSearchQuery.text.toString()
        // Recuperar ID da categoria selecionada no Spinner
        // val categoryId = getSelectedCategoryId()

        // Recuperar datas do seletor de calendário/data
        // val startDate: Date? = getStartDate()
        // val endDate: Date? = getEndDate()

        // Chamada ao ViewModel
        viewModel.searchTransactions(query, null, null, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}