package lk.kdu.ac.mc.sumudustodolist.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

//A list of items in the local DB
@Entity(tableName = "todo_lists")
data class TodoListEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    val createdAt: Long = System.currentTimeMillis(), // For sorting
    var orderIndex: Int = 0
)