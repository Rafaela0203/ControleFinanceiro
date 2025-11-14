package br.edu.utfpr.controlefinanceiro.presentention.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.controlefinanceiro.data.model.Category
import br.edu.utfpr.controlefinanceiro.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, onEditClick, onDeleteClick)
    }

    class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            category: Category,
            onEditClick: (Category) -> Unit,
            onDeleteClick: (Category) -> Unit
        ) {
            binding.tvCategoryName.text = category.name
            binding.tvCategoryType.text = category.type

            // Define a cor de fundo do tipo (exemplo, você pode usar um se/when)
            // binding.tvCategoryType.setBackgroundColor(...)

            binding.btnEdit.setOnClickListener { onEditClick(category) }
            binding.btnDelete.setOnClickListener { onDeleteClick(category) }

            // Configuração do ícone seria feita aqui, baseada em category.icon
        }
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
}