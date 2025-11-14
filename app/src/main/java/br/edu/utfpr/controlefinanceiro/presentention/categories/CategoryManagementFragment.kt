package br.edu.utfpr.controlefinanceiro.presentention.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.utfpr.controlefinanceiro.R
import br.edu.utfpr.controlefinanceiro.data.model.Category
import br.edu.utfpr.controlefinanceiro.data.repository.CategoryRepository
import br.edu.utfpr.controlefinanceiro.databinding.DialogAddCategoryBinding
import br.edu.utfpr.controlefinanceiro.databinding.FragmentCategoryManagementBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CategoryManagementFragment : Fragment() {

    private val viewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(
            CategoryRepository(
                FirebaseFirestore.getInstance(),
                FirebaseAuth.getInstance()
            )
        )
    }

    private var _binding: FragmentCategoryManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var myCategoriesAdapter: CategoryAdapter
    private lateinit var suggestedCategoryAdapter: SuggestedCategoryAdapter

    private var allMyCategories: List<Category> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMyCategoriesRecyclerView()
        setupSuggestedRecyclerView()
        observeViewModel()

        binding.fabAddCustomCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        viewModel.loadCategories()
    }

    private fun setupMyCategoriesRecyclerView() {
        myCategoriesAdapter = CategoryAdapter(
            onEditClick = { category ->
                showAddCategoryDialog(category)
            },
            onDeleteClick = { category ->
                category.id?.let { id ->
                    viewModel.deleteCategory(id)
                } ?: Toast.makeText(context, "Erro: Categoria sem ID.", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvMyCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyCategories.adapter = myCategoriesAdapter
    }

    private fun setupSuggestedRecyclerView() {
        val suggestions = resources.getStringArray(R.array.suggested_categories).toList()

        suggestedCategoryAdapter = SuggestedCategoryAdapter(suggestions) { categoryName ->
            val alreadyExists = allMyCategories.any { it.name.equals(categoryName, ignoreCase = true) }

            if (alreadyExists) {
                Toast.makeText(context, "'$categoryName' já está na sua lista.", Toast.LENGTH_SHORT).show()
            } else {
                // AGORA VAI FUNCIONAR (só passa o nome)
                viewModel.saveCategory(categoryName)
            }
        }
        binding.rvSuggestedCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSuggestedCategories.adapter = suggestedCategoryAdapter
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            allMyCategories = categories
            myCategoriesAdapter.submitList(categories)
            binding.tvEmptyState.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
        }

        // AGORA VAI FUNCIONAR
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddCategoryDialog(category: Category? = null) {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        val editText = dialogBinding.etCategoryName

        val title = if (category == null) "Nova Categoria" else "Editar Categoria"
        editText.setText(category?.name ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(dialogBinding.root)
            .setPositiveButton("Salvar") { dialog, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    if (category == null) {
                        // Salva nova (só passa o nome)
                        viewModel.saveCategory(name)
                    } else {
                        // AGORA VAI FUNCIONAR
                        // Cria uma cópia do objeto com o nome atualizado
                        val updatedCategory = category.copy(name = name)
                        viewModel.updateCategory(updatedCategory)
                    }
                } else {
                    Toast.makeText(context, "O nome não pode estar vazio.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
