package lk.kdu.ac.mc.sumudustodolist.data.local

import androidx.room.*
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoListEntity
import kotlinx.coroutines.flow.Flow

//Data Access Object (DAO) for TodoListEntity
//Methods to interact with the 'todo_lists' table in the DB
@Dao
interface TodoListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TodoListEntity): Long // Returns the new rowId

    @Update
    suspend fun updateList(list: TodoListEntity)

    @Delete
    suspend fun deleteList(list: TodoListEntity)

    @Query("SELECT * FROM todo_lists ORDER BY orderIndex ASC, createdAt ASC") // Initial sort by orderIndex, later by createdAt
    fun getAllLists(): Flow<List<TodoListEntity>>

    @Query("SELECT * FROM todo_lists WHERE id = :listId")
    fun getListById(listId: Int): Flow<TodoListEntity?>

    @Query("""
        SELECT DISTINCT tl.*
        FROM todo_lists tl
        LEFT JOIN todo_items ti ON tl.id = ti.listId
        WHERE tl.name LIKE '%' || :query || '%'
           OR ti.text LIKE '%' || :query || '%'
           OR ti.description LIKE '%' || :query || '%'
        ORDER BY tl.createdAt DESC
    """)
    fun searchListsWithItems(query: String): Flow<List<TodoListEntity>>

    @Update(entity = TodoListEntity::class)
    suspend fun updateListOrder(lists: List<TodoListEntity>)

    @Query("DELETE FROM todo_lists")
    suspend fun clearAllLists()
}