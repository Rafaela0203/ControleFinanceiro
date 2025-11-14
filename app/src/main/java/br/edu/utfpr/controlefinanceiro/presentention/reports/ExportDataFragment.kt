package br.edu.utfpr.controlefinanceiro.presentention.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.edu.utfpr.controlefinanceiro.databinding.FragmentExportDataBinding
import br.edu.utfpr.controlefinanceiro.presentention.transactions.TransactionViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment responsável pela Exportação de Dados (RF09 / Quadro 13).
 */
class ExportDataFragment : Fragment() {

    // O ViewModel que contém a lógica de exportação
    private lateinit var viewModel: TransactionViewModel

    private var _binding: FragmentExportDataBinding? = null
    private val binding get() = _binding!!

    // Variáveis de estado para o período de exportação
    private var startDate: Date? = null
    private var endDate: Date? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar o ViewModel (Assuma que a injeção foi configurada)
        // viewModel = getViewModel()

        // 2. Configurar o seletor de formato
        setupFormatSpinner()

        // 3. Configurar seletores de data
        setupDatePickers()

        // 4. Observar o resultado da exportação
        observeViewModel()

        // 5. Configurar o botão de exportar
        binding.btnExport.setOnClickListener {
            initiateExport()
        }
    }

    private fun setupFormatSpinner() {
        // Assume que o array 'export_formats' está definido em strings.xml
        val formats = resources.getStringArray(br.edu.utfpr.controlefinanceiro.R.array.export_formats)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, formats)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFormat.adapter = adapter
    }

    private fun setupDatePickers() {
        // Configura o seletor para a data inicial
        binding.etStartDate.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                binding.etStartDate.setText(dateFormat.format(date))
            }
        }

        // Configura o seletor para a data final
        binding.etEndDate.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                binding.etEndDate.setText(dateFormat.format(date))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecione a Data")
            .build()

        datePicker.addOnPositiveButtonClickListener { timeInMilliSeconds ->
            onDateSelected(Date(timeInMilliSeconds))
        }
        datePicker.show(parentFragmentManager, "DATE_PICKER_EXPORT")
    }

    private fun observeViewModel() {
        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        }

        // Lógica que reage ao caminho do arquivo gerado
        viewModel.exportFileUri.observe(viewLifecycleOwner) { filePath ->
            if (filePath != null) {
                // [PENDENTE]: Implementação da chamada nativa do Android para download/compartilhamento
                Toast.makeText(context, "Relatório gerado! Pronto para download.", Toast.LENGTH_LONG).show()
                binding.progressBar.visibility = View.GONE
                viewModel.clearExportFileUri() // Limpa o estado

                // Exemplo de como abrir o arquivo (Requer permissões e tratamento de URI)
                // val fileUri = FileProvider.getUriForFile(...)
                // val intent = Intent(Intent.ACTION_VIEW).setDataAndType(fileUri, "application/pdf")
                // startActivity(intent)
            }
        }
    }

    private fun initiateExport() {
        val format = binding.spinnerFormat.selectedItem.toString()

        // Validação (Quadro 13: Exceção - Período inválido selecionado)
        if (startDate != null && endDate != null && startDate!!.after(endDate)) {
            Toast.makeText(context, "Erro: A data inicial não pode ser posterior à data final.", Toast.LENGTH_LONG).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        // Chamada ao ViewModel para iniciar a exportação
        viewModel.exportTransactions(format, startDate, endDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}