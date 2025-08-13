package lk.kdu.ac.mc.sumudustodolist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import lk.kdu.ac.mc.sumudustodolist.data.local.AppDatabase
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoItemEntity
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoListEntity
import lk.kdu.ac.mc.sumudustodolist.data.repository.TodoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import lk.kdu.ac.mc.sumudustodolist.util.ReminderScheduler

//ViewModel for the List Detail screen
//Manages data and operations related to a specific to-do list and its items
class ListDetailViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val repository: TodoRepository
    // Retrieves the listId from navigation arguments
    val listId: Int = savedStateHandle.get<String>("listId")?.toIntOrNull() ?: -1
    val currentList: StateFlow<TodoListEntity?>
    val itemsForList: StateFlow<List<TodoItemEntity>>
    private val _showItemDialog = MutableStateFlow(false)
    val showItemDialog: StateFlow<Boolean> = _showItemDialog.asStateFlow()
    private val _itemToEdit = MutableStateFlow<TodoItemEntity?>(null)
    val itemToEdit: StateFlow<TodoItemEntity?> = _itemToEdit.asStateFlow()
    private val _showDeleteItemConfirmationDialog = MutableStateFlow(false)
    val showDeleteItemConfirmationDialog: StateFlow<Boolean> = _showDeleteItemConfirmationDialog.asStateFlow()
    private val _itemToDelete = MutableStateFlow<TodoItemEntity?>(null)
    val itemToDelete: StateFlow<TodoItemEntity?> = _itemToDelete.asStateFlow()


    init {
        val database = AppDatabase.getDatabase(application)
        repository = TodoRepository(database)

        currentList = if (listId != -1) {
            repository.getListById(listId)
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        } else {
            MutableStateFlow(null)
        }

        itemsForList = if (listId != -1) {
            repository.getItemsForList(listId)
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        } else {
            MutableStateFlow(emptyList())
        }
    }

    //Open dialog to add or edit a to-do item
    fun onOpenItemDialog(item: TodoItemEntity? = null) {
        _itemToEdit.value = item
        _showItemDialog.value = true
    }

    //Close add/edit item dialog and reset the item being edited
    fun onCloseItemDialog() {
        _showItemDialog.value = false
        _itemToEdit.value = null
    }

    //Add a new to-do item or update an existing one
    fun addOrUpdateItem(text: String, description: String?, deadline: Long?, reminderOffset: Long?) { // Updated
        if (listId == -1 || text.isBlank()) return
        viewModelScope.launch {
            val currentItem = _itemToEdit.value
            val reminderScheduler = ReminderScheduler(getApplication())

            if (currentItem != null) {
                val updatedItem = currentItem.copy(
                    text = text,
                    description = description,
                    deadline = deadline,
                    reminderOffsetMillis = reminderOffset
                )
                repository.updateItem(updatedItem)
                // Cancel old reminder and schedule new one if deadline or reminder is changed
                if (currentItem.deadline != updatedItem.deadline || currentItem.reminderOffsetMillis != updatedItem.reminderOffsetMillis) {
                    if (currentItem.deadline != null && currentItem.reminderOffsetMillis != null) {
                        reminderScheduler.cancelReminder(currentItem.id)
                    }
                    if (updatedItem.deadline != null && updatedItem.reminderOffsetMillis != null) {
                        reminderScheduler.scheduleReminder(updatedItem)
                    }
                }
            } else {
                val newItem = TodoItemEntity(
                    listId = listId,
                    text = text,
                    description = description,
                    deadline = deadline,
                    reminderOffsetMillis = reminderOffset
                )
                val newItemId = repository.insertItemAndGetId(newItem)
                val itemWithId = newItem.copy(id = newItemId.toInt())

                if (itemWithId.deadline != null && itemWithId.reminderOffsetMillis != null) {
                    reminderScheduler.scheduleReminder(itemWithId)
                }
            }
            onCloseItemDialog()
        }
    }

    //Request deletion of a to-do item
    fun requestDeleteItem(item: TodoItemEntity) {
        _itemToDelete.value = item
        _showDeleteItemConfirmationDialog.value = true
    }

    //Confirm the deletion of the item currently marked
    fun confirmDeleteItem() {
        _itemToDelete.value?.let { item ->
            viewModelScope.launch {
                repository.deleteItem(item)
                // Cancel reminder
                if (item.deadline != null && item.reminderOffsetMillis != null) {
                    ReminderScheduler(getApplication()).cancelReminder(item.id)
                }
            }
        }
    }

    //Dismiss the delete item confirmation dialog
    fun onDismissDeleteItemConfirmation() {
        _showDeleteItemConfirmationDialog.value = false
        _itemToDelete.value = null
    }

    //Toggle the completion status of a to-do item
    fun toggleItemCompletion(item: TodoItemEntity) {
        viewModelScope.launch {
            val updatedItem = item.copy(isCompleted = !item.isCompleted)
            repository.updateItem(updatedItem)
            val reminderScheduler = ReminderScheduler(getApplication())
            if (updatedItem.isCompleted) {
                // Cancel reminder if completed
                if (item.deadline != null && item.reminderOffsetMillis != null) {
                    reminderScheduler.cancelReminder(item.id)
                }
            } else {
                // Reschedule reminder if not completed, and deadline or reminder is there still
                if (updatedItem.deadline != null && updatedItem.reminderOffsetMillis != null) {
                    reminderScheduler.scheduleReminder(updatedItem)
                }
            }
        }
    }

    //Update the name of the current to-do list
    fun updateListName(newName: String) {
        if (listId != -1 && newName.isNotBlank()) {
            viewModelScope.launch {
                currentList.value?.let {
                    repository.updateList(it.copy(name = newName))
                }
            }
        }
    }

    //Reorder the to-do items within the current list
    //Assistance taken from AI tools
    fun reorderTodoItems(reorderedItems: List<TodoItemEntity>) {
        viewModelScope.launch {
            val itemsToUpdate = reorderedItems.mapIndexed { index, item ->
                item.copy(orderIndex = index)
            }
            repository.updateItemOrder(itemsToUpdate)
        }
    }
}