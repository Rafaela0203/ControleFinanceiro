package br.edu.utfpr.controlefinanceiro.presentention.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.controlefinanceiro.data.model.Category
import br.edu.utfpr.controlefinanceiro.data.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // NOVO: Adiciona a LiveData 'loading'
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadCategories() {
        _loading.postValue(true) // Mostra o loading
        viewModelScope.launch {
            try {
                val categoryList = repository.getAllUserCategories()
                _categories.postValue(categoryList)
            } catch (e: Exception) {
                _message.postValue("Erro ao carregar categorias: ${e.message}")
            } finally {
                _loading.postValue(false) // Esconde o loading
            }
        }
    }

    /**
     * Salva uma NOVA categoria.
     * Agora ela cria o objeto Category completo.
     */
    fun saveCategory(name: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                // MODIFICADO: Cria o objeto Category completo
                val newCategory = Category(
                    name = name,
                    type = "DESPESA", // Assume DESPESA por padrão
                    icon = null // Começa sem ícone
                )
                repository.saveCategory(newCategory) // Passa o objeto
                _message.postValue("Categoria salva com sucesso.")
                loadCategories() // Recarrega a lista
            } catch (e: Exception) {
                _message.postValue("Falha ao salvar: ${e.message}")
                _loading.postValue(false)
            }
        }
    }

    // NOVO: Adiciona a função 'updateCategory'
    fun updateCategory(category: Category) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                repository.updateCategory(category)
                _message.postValue("Categoria atualizada.")
                loadCategories() // Recarrega a lista
            } catch (e: Exception) {
                _message.postValue("Erro ao atualizar: ${e.message}")
                _loading.postValue(false)
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                repository.deleteCategory(categoryId)
                _message.postValue("Categoria excluída.")
                loadCategories() // Recarrega a lista
            } catch (e: Exception) {
                _message.postValue("Erro ao excluir: ${e.message}")
                _loading.postValue(false)
            }
        }
    }
}