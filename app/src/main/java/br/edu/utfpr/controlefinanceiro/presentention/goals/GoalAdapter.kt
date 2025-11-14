package br.edu.utfpr.controlefinanceiro.presentention.goals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.controlefinanceiro.data.model.FinancialGoal
import br.edu.utfpr.controlefinanceiro.databinding.ItemGoalBinding // Binding para o item de layout
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter para exibir a lista de Metas Financeiras (RF04).
 * Usa ListAdapter para melhor desempenho.
 */
class GoalAdapter(
    private val onEditClick: (FinancialGoal) -> Unit,
    private val onDeleteClick: (FinancialGoal) -> Unit
) : ListAdapter<FinancialGoal, GoalAdapter.GoalViewHolder>(GoalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = getItem(position)
        holder.bind(goal, onEditClick, onDeleteClick)
    }

    class GoalViewHolder(private val binding: ItemGoalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(
            goal: FinancialGoal,
            onEditClick: (FinancialGoal) -> Unit,
            onDeleteClick: (FinancialGoal) -> Unit
        ) {
            // Define o nome e o tipo da meta
            binding.tvGoalName.text = goal.name
            binding.tvGoalType.text = if (goal.type == "ECONOMIA") "Economia" else "Limite de Gastos"

            // Exibe os valores formatados
            binding.tvTargetValue.text = currencyFormat.format(goal.targetValue)
            binding.tvSavedValue.text = currencyFormat.format(goal.savedValue)

            // Exibe a data limite
            binding.tvDeadline.text = "Prazo: ${dateFormat.format(goal.deadline)}"

            // Calcula e exibe o progresso
            val progress = ((goal.savedValue / goal.targetValue) * 100).toInt()
            binding.progressBar.progress = progress
            binding.tvProgressPercentage.text = "$progress%"

            // Define os Listeners para os botões de ação
            binding.btnEdit.setOnClickListener { onEditClick(goal) }
            binding.btnDelete.setOnClickListener { onDeleteClick(goal) }
        }
    }
}

/**
 * Utilitário de Callback para otimizar a atualização do RecyclerView.
 */
class GoalDiffCallback : DiffUtil.ItemCallback<FinancialGoal>() {
    override fun areItemsTheSame(oldItem: FinancialGoal, newItem: FinancialGoal): Boolean {
        // Assegura que itens com o mesmo ID são considerados o mesmo item
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FinancialGoal, newItem: FinancialGoal): Boolean {
        // Verifica se o conteúdo do item mudou (data classes ajudam aqui)
        return oldItem == newItem
    }
}