//package br.edu.utfpr.controlefinanceiro.presentention.transactions
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import br.edu.utfpr.controlefinanceiro.data.model.Category
//import br.edu.utfpr.controlefinanceiro.data.repository.CategoryRepository
//import br.edu.utfpr.controlefinanceiro.data.repository.FinancialGoalRepository // Corrigido do passo anterior
//import br.edu.utfpr.controlefinanceiro.data.repository.TransactionRepository
//import br.edu.utfpr.controlefinanceiro.databinding.FragmentTransactionRegistrationBinding
//import br.edu.utfpr.controlefinanceiro.presentention.goals.GoalViewModel
//import br.edu.utfpr.controlefinanceiro.presentention.goals.GoalViewModelFactory // Corrigido do passo anterior
//import com.google.android.material.datepicker.MaterialDatePicker
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//class TransactionRegistrationFragment : Fragment() {
//
//    private val viewModel: TransactionViewModel by activityViewModels {
//        TransactionViewModelFactory(
//            TransactionRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance()),
//            CategoryRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
//        )
//    }
//
//    private val goalViewModel: GoalViewModel by activityViewModels {
//        GoalViewModelFactory(
//            FinancialGoalRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance()),
//            TransactionRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
//        )
//    }
//
//    private var _binding: FragmentTransactionRegistrationBinding? = null
//    private val binding get() = _binding!!
//
//    private var selectedDate: Date = Date()
//    private var availableCategories: List<Category> = emptyList()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentTransactionRegistrationBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // 1. Configurar o campo de Data
//        setupDateField()
//
//        // 2. Observar categorias e mensagens
//        observeViewModel()
//
//        // 3. Configurar botão de salvar
//        binding.btnSaveTransaction.setOnClickListener {
//            saveTransaction()
//        }
//
//        // NOVO: Pede ao ViewModel para carregar as categorias
//        // Isso irá disparar o observador em 'observeViewModel'
//        viewModel.loadCategories()
//    }
//
//    private fun setupDateField() {
//        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//        binding.etDate.setText(dateFormat.format(selectedDate))
//
//        binding.etDate.setOnClickListener {
//            val datePicker = MaterialDatePicker.Builder.datePicker()
//                .setTitleText("Selecione a data")
//                .setSelection(selectedDate.time)
//                .build()
//
//            datePicker.addOnPositiveButtonClickListener { timeInMilliSeconds ->
//                selectedDate = Date(timeInMilliSeconds)
//                binding.etDate.setText(dateFormat.format(selectedDate))
//            }
//            datePicker.show(parentFragmentManager, "DATE_PICKER")
//        }
//    }
//
//    private fun observeViewModel() {
//        // Observa a lista de categorias e preenche o Spinner
//        viewModel.categories.observe(viewLifecycleOwner) { categories ->
//            availableCategories = categories
//            val categoryNames = categories.map { it.name }
//
//            val adapter = ArrayAdapter(
//                requireContext(),
//                android.R.layout.simple_spinner_item,
//                categoryNames
//            )
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
////            binding.spinnerCategory.adapter = adapter
//        }
//
//        // Observa mensagens de status
//        viewModel.message.observe(viewLifecycleOwner) { message ->
//            if (message.isNotEmpty()) {
//                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//
//                if (message.contains("sucesso", ignoreCase = true)) {
//                    // Dispara a verificação de alertas
//                    goalViewModel.checkGoalAlerts(requireContext())
//
//                    // Lógica para fechar (voltar para a tela anterior)
//                    parentFragmentManager.popBackStack()
//                }
//            }
//        }
//    }
//
//    private fun saveTransaction() {
//        val valueString = binding.etValue.text.toString()
//        val description = binding.etDescription.text.toString()
//
//        if (valueString.isBlank() || description.isBlank()) {
//            Toast.makeText(context, "Preencha o valor e a descrição.", Toast.LENGTH_SHORT).show()
//            return
//        }
//        val value = valueString.toDoubleOrNull()
//        if (value == null || value <= 0) {
//            Toast.makeText(context, "Valor inválido.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // NOVO: Validação de Categoria (necessário se a lista estiver vazia)
//        if (availableCategories.isEmpty()) {
//            Toast.makeText(context, "Nenhuma categoria cadastrada. Vá para a tela de 'Categorias' e adicione uma.", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        val type = if (binding.rbExpense.isChecked) "DESPESA" else "RECEITA"
//        val selectedCategoryIndex = binding.spinnerCategory.selectedItemPosition
//
//        if (selectedCategoryIndex == -1 || selectedCategoryIndex >= availableCategories.size) {
//            Toast.makeText(context, "Selecione uma categoria válida.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val categoryId = availableCategories[selectedCategoryIndex].id ?: run {
//            Toast.makeText(context, "Erro: Categoria sem ID.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        viewModel.saveTransaction(
//            value = value,
//            description = description,
//            type = type,
//            categoryId = categoryId,
//            date = selectedDate
//        )
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}