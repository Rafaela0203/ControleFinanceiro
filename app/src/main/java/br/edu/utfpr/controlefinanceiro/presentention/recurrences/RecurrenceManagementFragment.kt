package br.edu.utfpr.controlefinanceiro.presentention.recurrences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import br.edu.utfpr.controlefinanceiro.databinding.FragmentRecurrenceManagementBinding
import android.widget.Toast

/**
 * Fragment responsável pelo Gerenciamento de Transações Recorrentes (RF06 / Quadro 10).
 */
class RecurrenceManagementFragment : Fragment() {

    private lateinit var viewModel: RecurrenceViewModel
    private var _binding: FragmentRecurrenceManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecurrenceManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar o ViewModel
        // viewModel = getViewModel()

        // 2. Configurar RecyclerView para listar recorrências
        // setupRecyclerView()

        // 3. Observar dados e mensagens
        observeViewModel()

        // 4. Botão para adicionar nova recorrência
        // binding.fabAddRecurrence.setOnClickListener { showRecurrenceDialog(null) }
    }

    private fun observeViewModel() {
        viewModel.recurrences.observe(viewLifecycleOwner) { recurrences ->
            // Atualizar o adaptador da lista
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // A lógica para o processamento automático de recorrências
    // seria idealmente feita em um WorkManager ou serviço, mas para o TCC,
    // pode ser iniciada ao abrir o Dashboard ou esta tela.
    private fun checkAndProcessDueRecurrences() {
        viewModel.recurrences.value?.forEach { recurrence ->
            viewModel.processRecurrence(recurrence)
        }
    }

    override fun onResume() {
        super.onResume()
        // Chamada para processar recorrências pendentes ao reabrir a tela
        checkAndProcessDueRecurrences()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}