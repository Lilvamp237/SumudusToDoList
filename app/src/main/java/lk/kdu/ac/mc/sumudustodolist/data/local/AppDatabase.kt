package lk.kdu.ac.mc.sumudustodolist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoItemEntity
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoListEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

//Room DB class to define entities and give access to DAOs
@Database(entities = [TodoListEntity::class, TodoItemEntity::class], version = 4, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoListDao(): TodoListDao //Access to TodoListDao
    abstract fun todoItemDao(): TodoItemDao //Access to TodoItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) { //Adds a 'description' column to the 'todo_items' table
                database.execSQL("ALTER TABLE todo_items ADD COLUMN description TEXT")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) { //Adds 'orderIndex' columns to both tables and initializes them
                database.execSQL("ALTER TABLE todo_lists ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE todo_items ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")

                database.execSQL("""
                    UPDATE todo_lists
                    SET orderIndex = (
                        SELECT COUNT(*)
                        FROM todo_lists tl_inner
                        WHERE tl_inner.createdAt <= todo_lists.createdAt
                    ) - 1
                    WHERE EXISTS (SELECT 1 FROM todo_lists)
                """)
                database.execSQL("""
                    UPDATE todo_items
                    SET orderIndex = (
                        SELECT COUNT(*)
                        FROM todo_items ti_inner
                        WHERE ti_inner.listId = todo_items.listId AND ti_inner.createdAt <= todo_items.createdAt
                    ) - 1
                    WHERE EXISTS (SELECT 1 FROM todo_items WHERE todo_items.listId = todo_items.listId)
                """)
            }
        }
        //Assistance taken from AI tools
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) { //Adds 'deadline' and 'reminderOffsetMillis' columns to the 'todo_items' table
                database.execSQL("ALTER TABLE todo_items ADD COLUMN deadline INTEGER") // SQLite INTEGER can store Long
                database.execSQL("ALTER TABLE todo_items ADD COLUMN reminderOffsetMillis INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}