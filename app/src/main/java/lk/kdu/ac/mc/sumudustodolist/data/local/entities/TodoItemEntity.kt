package lk.kdu.ac.mc.sumudustodolist.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

//Single items details in the local DB
@Entity(
    tableName = "todo_items",
    foreignKeys = [ForeignKey(
        entity = TodoListEntity::class,
        parentColumns = ["id"],
        childColumns = ["listId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TodoItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listId: Int,
    var text: String,
    var description: String? = null,
    var isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(), // For sorting
    var orderIndex: Int = 0,
    var deadline: Long? = null,
    var reminderOffsetMillis: Long? = null
)