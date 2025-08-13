package lk.kdu.ac.mc.sumudustodolist.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile TodoListDao _todoListDao;

  private volatile TodoItemDao _todoItemDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `todo_lists` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `orderIndex` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `todo_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `listId` INTEGER NOT NULL, `text` TEXT NOT NULL, `description` TEXT, `isCompleted` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `orderIndex` INTEGER NOT NULL, `deadline` INTEGER, `reminderOffsetMillis` INTEGER, FOREIGN KEY(`listId`) REFERENCES `todo_lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6fc1c47615c0051de35f0dc8d9e0e5a1')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `todo_lists`");
        db.execSQL("DROP TABLE IF EXISTS `todo_items`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTodoLists = new HashMap<String, TableInfo.Column>(4);
        _columnsTodoLists.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTodoLists.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTodoLists.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTodoLists.put("orderIndex", new TableInfo.Column("orderIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTodoLists = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTodoLists = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTodoLists = new TableInfo("todo_lists", _columnsTodoLists, _foreignKeysTodoLists, _indicesTodoLists);
        final TableInfo _existingTodoLists = TableInfo.read(db, "todo_lists");
        if (!_infoTodoLists.equals(_existingTodoLists)) {
          return new RoomOpenHelper.ValidationResult(false, "todo_lists(lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoListEntity).\n"
                  + " Expected:\n" + _infoTodoLists + "\n"
                  + " Found:\n" + _existingTodoLists);
        }
        final HashMap<String, TableInfo.Column> _columnsTodoItems = new HashMap<String, TableInfo.Column>(9);
        _columnsTodoItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTodoItems.put("listId", new TableInfo.Column("listId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTodoItems.put("text", new TableInfo.Column("text", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTodoItems.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTodoItems.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTodoItems.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTodoItems.put("orderIndex", new TableInfo.Column("orderIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTodoItems.put("deadline", new TableInfo.Column("deadline", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTodoItems.put("reminderOffsetMillis", new TableInfo.Column("reminderOffsetMillis", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTodoItems = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTodoItems.add(new TableInfo.ForeignKey("todo_lists", "CASCADE", "NO ACTION", Arrays.asList("listId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTodoItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTodoItems = new TableInfo("todo_items", _columnsTodoItems, _foreignKeysTodoItems, _indicesTodoItems);
        final TableInfo _existingTodoItems = TableInfo.read(db, "todo_items");
        if (!_infoTodoItems.equals(_existingTodoItems)) {
          return new RoomOpenHelper.ValidationResult(false, "todo_items(lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoItemEntity).\n"
                  + " Expected:\n" + _infoTodoItems + "\n"
                  + " Found:\n" + _existingTodoItems);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6fc1c47615c0051de35f0dc8d9e0e5a1", "cb86a1cb74dbcc73cb19ae48a2343b51");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "todo_lists","todo_items");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `todo_lists`");
      _db.execSQL("DELETE FROM `todo_items`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TodoListDao.class, TodoListDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TodoItemDao.class, TodoItemDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TodoListDao todoListDao() {
    if (_todoListDao != null) {
      return _todoListDao;
    } else {
      synchronized(this) {
        if(_todoListDao == null) {
          _todoListDao = new TodoListDao_Impl(this);
        }
        return _todoListDao;
      }
    }
  }

  @Override
  public TodoItemDao todoItemDao() {
    if (_todoItemDao != null) {
      return _todoItemDao;
    } else {
      synchronized(this) {
        if(_todoItemDao == null) {
          _todoItemDao = new TodoItemDao_Impl(this);
        }
        return _todoItemDao;
      }
    }
  }
}
