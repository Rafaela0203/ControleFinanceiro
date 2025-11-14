//package br.edu.utfpr.controlefinanceiro.presentention.categories
//
//import android.content.Context
//import android.content.res.ColorStateList
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import br.edu.utfpr.controlefinanceiro.R
//import br.edu.utfpr.controlefinanceiro.data.model.Category
//import br.edu.utfpr.controlefinanceiro.databinding.ItemCategoryGridBinding
//
//// Define um 'callback' para o clique, que passará a Categoria selecionada
//typealias OnCategoryClickListener = (Category) -> Unit
//
//class CategoryGridAdapter(
//    private val listener: OnCategoryClickListener
//) : ListAdapter<Category, CategoryGridAdapter.CategoryViewHolder>(CategoryDiffCallback()) {
//
//    private var selectedPosition = RecyclerView.NO_POSITION
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
//        val binding = ItemCategoryGridBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return CategoryViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
//        holder.bind(getItem(position), position)
//    }
//
//    inner class CategoryViewHolder(private val binding: ItemCategoryGridBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(category: Category, position: Int) {
//            val context = binding.root.context
//
//            // Define o nome da categoria
//            binding.tvCategoryName.text = category.name
//
//            // Lógica para destacar o item selecionado (como no protótipo)
//            if (position == selectedPosition) {
//                // Estado: Selecionado
//                binding.iconBackground.backgroundTintList =
//                    ColorStateList.valueOf(getThemeColor(context, R.attr.colorPrimary))
//                binding.ivCategoryIcon.imageTintList =
//                    ColorStateList.valueOf(getThemeColor(context, R.attr.colorOnPrimary))
//                binding.tvCategoryName.setTextColor(getThemeColor(context, R.attr.colorPrimary))
//            } else {
//                // Estado: Padrão (Não selecionado)
//                binding.iconBackground.backgroundTintList =
//                    ColorStateList.valueOf(getThemeColor(context, R.attr.colorSurface))
//                binding.ivCategoryIcon.imageTintList =
//                    ColorStateList.valueOf(getThemeColor(context, R.attr.colorTextSecondary))
//                binding.tvCategoryName.setTextColor(getThemeColor(context, R.attr.colorTextSecondary))
//            }
//
//            // (Opcional: Carregar ícone real da categoria)
//            // Por enquanto, usamos um ícone padrão:
//            binding.ivCategoryIcon.setImageResource(R.drawable.ic_default_category)
//
//            // Define o clique
//            binding.root.setOnClickListener {
//                if (selectedPosition != position) {
//                    val oldPosition = selectedPosition
//                    selectedPosition = position
//
//                    // Atualiza (redesenha) o item que foi desmarcado
//                    if (oldPosition != RecyclerView.NO_POSITION) {
//                        notifyItemChanged(oldPosition)
//                    }
//                    // Atualiza (redesenha) o item que foi marcado
//                    notifyItemChanged(selectedPosition)
//
//                    // Envia a categoria selecionada de volta para o Fragment
//                    listener(category)
//                }
//            }
//        }
//
//        // Helper para buscar as cores do Tema (Ex: ?attr/colorPrimary)
//        private fun getThemeColor(context: Context, attr: Int): Int {
//            val typedValue = android.util.TypedValue()
//            context.theme.resolveAttribute(attr, typedValue, true)
//            return ContextCompat.getColor(context, typedValue.resourceId)
//        }
//    }
//}
//
//// Classe 'DiffUtil' para o ListAdapter saber o que mudou
//class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
//    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
//        return oldItem.id == newItem.id
//    }
//
//    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
//        return oldItem == newItem
//    }
//}