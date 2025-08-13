package lk.kdu.ac.mc.sumudustodolist.data.repository

import lk.kdu.ac.mc.sumudustodolist.data.local.AppDatabase
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoItemEntity
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoListEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

//To abstract data operations of lists and items
//Communicate between Room DB and ViewModel
class TodoRepository(private val db: AppDatabase) {
    private val todoListDao = db.todoListDao() //DAO for to-do list operations
    private val todoItemDao = db.todoItemDao() //DAO for to-do item operations

    // List operations
    fun getAllLists(): Flow<List<TodoListEntity>> = todoListDao.getAllLists()
    fun getListById(listId: Int): Flow<TodoListEntity?> = todoListDao.getListById(listId)

    suspend fun updateListOrder(lists: List<TodoListEntity>) {
        todoListDao.updateListOrder(lists)
    }
    suspend fun updateItemOrder(items: List<TodoItemEntity>) {
        todoItemDao.updateItemOrder(items)
    }

    suspend fun insertList(list: TodoListEntity): Long {
        val maxOrder = db.todoListDao().getAllLists().firstOrNull()?.maxOfOrNull { it.orderIndex } ?: -1
        val newList = list.copy(orderIndex = maxOrder + 1)
        return todoListDao.insertList(newList)
    }

    suspend fun updateList(list: TodoListEntity) = todoListDao.updateList(list)
    suspend fun deleteList(list: TodoListEntity) {
        todoListDao.deleteList(list)
    }

    // Item operations
    fun getItemsForList(listId: Int): Flow<List<TodoItemEntity>> = todoItemDao.getItemsForList(listId)
    suspend fun insertItem(item: TodoItemEntity) {
        insertItemAndGetId(item)
    }

    suspend fun updateItem(item: TodoItemEntity) = todoItemDao.updateItem(item)
    suspend fun deleteItem(item: TodoItemEntity) = todoItemDao.deleteItem(item)


    fun searchLists(query: String): Flow<List<TodoListEntity>> {
        return if (query.isBlank()) {
            todoListDao.getAllLists()
        } else {
            todoListDao.searchListsWithItems(query)
        }
    }

    suspend fun insertItemAndGetId(item: TodoItemEntity): Long {
        val maxOrder = db.todoItemDao().getItemsForList(item.listId).firstOrNull()?.maxOfOrNull { it.orderIndex } ?: -1
        val newItem = item.copy(orderIndex = maxOrder + 1)
        return todoItemDao.insertItem(newItem)
    }

    //Clears all data from items and lists
    suspend fun clearAllData() {
        db.todoItemDao().clearAllItems()
        db.todoListDao().clearAllLists()
    }
}