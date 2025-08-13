package lk.kdu.ac.mc.sumudustodolist.data.local

import androidx.room.*
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoItemEntity
import kotlinx.coroutines.flow.Flow

//Data Access Object (DAO) for TodoItemEntity
//Methods to interact with the 'todo_lists' table in the DB
@Dao
interface TodoItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: TodoItemEntity): Long

    @Update
    suspend fun updateItem(item: TodoItemEntity)

    @Delete
    suspend fun deleteItem(item: TodoItemEntity)

    @Query("SELECT * FROM todo_items WHERE listId = :listId ORDER BY orderIndex ASC, createdAt ASC") // Initial sort by orderIndex
    fun getItemsForList(listId: Int): Flow<List<TodoItemEntity>>

    @Query("DELETE FROM todo_items WHERE listId = :listId")
    suspend fun deleteItemsByListId(listId: Int)

    @Update(entity = TodoItemEntity::class)
    suspend fun updateItemOrder(items: List<TodoItemEntity>)

    @Query("DELETE FROM todo_items")
    suspend fun clearAllItems()
}
