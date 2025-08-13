package lk.kdu.ac.mc.sumudustodolist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import lk.kdu.ac.mc.sumudustodolist.data.local.AppDatabase
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoListEntity
import lk.kdu.ac.mc.sumudustodolist.data.repository.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

//ViewModel for the Lists Overview screen
//Manages data and operations related to displaying, searching, and managing to-do lists
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ListsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val displayedLists: StateFlow<List<TodoListEntity>>
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()
    private val _listToEdit = MutableStateFlow<TodoListEntity?>(null)
    val listToEdit: StateFlow<TodoListEntity?> = _listToEdit.asStateFlow()
    private val _showDeleteListConfirmationDialog = MutableStateFlow(false)
    val showDeleteListConfirmationDialog: StateFlow<Boolean> = _showDeleteListConfirmationDialog.asStateFlow()
    private val _listToDelete = MutableStateFlow<TodoListEntity?>(null)
    val listToDelete: StateFlow<TodoListEntity?> = _listToDelete.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TodoRepository(database)

        // React to search query changes
        displayedLists = _searchQuery
            .debounce(300)
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.getAllLists()
                } else {
                    repository.searchLists(query)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )
    }

    //Update the search query
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    //Open the dialog for adding or editing a to-do list
    fun onOpenDialog(list: TodoListEntity? = null) {
        _listToEdit.value = list
        _showDialog.value = true
    }

    //Close the add/edit list dialog and reset the list being edited
    fun onCloseDialog() {
        _showDialog.value = false
        _listToEdit.value = null
    }

    //Add a new to-do list or update an existing one
    fun addOrUpdateList(listName: String) {
        viewModelScope.launch {
            if (listName.isNotBlank()) {
                val currentList = _listToEdit.value
                if (currentList != null) {
                    repository.updateList(currentList.copy(name = listName))
                } else {
                    repository.insertList(TodoListEntity(name = listName))
                }
                onCloseDialog()
            }
        }
    }

    //Request deletion of a to-do list
    fun requestDeleteList(list: TodoListEntity) {
        _listToDelete.value = list
        _showDeleteListConfirmationDialog.value = true
    }

    //Confirm the deletion of the list currently marked
    fun confirmDeleteList() {
        _listToDelete.value?.let { list ->
            viewModelScope.launch {
                repository.deleteList(list)
            }
        }
    }

    //Dismiss the delete list confirmation dialog
    fun onDismissDeleteListConfirmation() {
        _showDeleteListConfirmationDialog.value = false
        _listToDelete.value = null // Clear the targeted list
    }

    //Reorder the to-do lists
    //Assistance taken from AI tools
    fun reorderLists(reorderedLists: List<TodoListEntity>) {
        viewModelScope.launch {
            val listsToUpdate = reorderedLists.mapIndexed { index, list ->
                list.copy(orderIndex = index)
            }
            repository.updateListOrder(listsToUpdate)
        }
    }
}