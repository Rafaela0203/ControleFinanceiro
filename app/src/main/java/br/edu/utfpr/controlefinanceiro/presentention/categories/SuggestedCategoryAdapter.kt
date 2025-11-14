package br.edu.utfpr.controlefinanceiro.presentention.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.controlefinanceiro.databinding.ItemSuggestedCategoryBinding

class SuggestedCategoryAdapter(
    private val suggestions: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SuggestedCategoryAdapter.SuggestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemSuggestedCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val categoryName = suggestions[position]
        holder.bind(categoryName)
    }

    override fun getItemCount() = suggestions.size

    inner class SuggestionViewHolder(private val binding: ItemSuggestedCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryName: String) {
            binding.tvCategoryName.text = categoryName
            binding.root.setOnClickListener {
                onItemClick(categoryName)
            }
        }
    }
}