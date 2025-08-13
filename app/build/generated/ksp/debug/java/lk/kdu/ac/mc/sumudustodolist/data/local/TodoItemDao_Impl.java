package lk.kdu.ac.mc.sumudustodolist.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoItemEntity;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TodoItemDao_Impl implements TodoItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TodoItemEntity> __insertionAdapterOfTodoItemEntity;

  private final EntityDeletionOrUpdateAdapter<TodoItemEntity> __deletionAdapterOfTodoItemEntity;

  private final EntityDeletionOrUpdateAdapter<TodoItemEntity> __updateAdapterOfTodoItemEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteItemsByListId;

  private final SharedSQLiteStatement __preparedStmtOfClearAllItems;

  public TodoItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTodoItemEntity = new EntityInsertionAdapter<TodoItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `todo_items` (`id`,`listId`,`text`,`description`,`isCompleted`,`createdAt`,`orderIndex`,`deadline`,`reminderOffsetMillis`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TodoItemEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getListId());
        statement.bindString(3, entity.getText());
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getOrderIndex());
        if (entity.getDeadline() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getDeadline());
        }
        if (entity.getReminderOffsetMillis() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getReminderOffsetMillis());
        }
      }
    };
    this.__deletionAdapterOfTodoItemEntity = new EntityDeletionOrUpdateAdapter<TodoItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `todo_items` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TodoItemEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTodoItemEntity = new EntityDeletionOrUpdateAdapter<TodoItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `todo_items` SET `id` = ?,`listId` = ?,`text` = ?,`description` = ?,`isCompleted` = ?,`createdAt` = ?,`orderIndex` = ?,`deadline` = ?,`reminderOffsetMillis` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TodoItemEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getListId());
        statement.bindString(3, entity.getText());
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getOrderIndex());
        if (entity.getDeadline() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getDeadline());
        }
        if (entity.getReminderOffsetMillis() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getReminderOffsetMillis());
        }
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteItemsByListId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM todo_items WHERE listId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllItems = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM todo_items";
        return _query;
      }
    };
  }

  @Override
  public Object insertItem(final TodoItemEntity item,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTodoItemEntity.insertAndReturnId(item);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteItem(final TodoItemEntity item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTodoItemEntity.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateItem(final TodoItemEntity item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTodoItemEntity.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateItemOrder(final List<TodoItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTodoItemEntity.handleMultiple(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteItemsByListId(final int listId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteItemsByListId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, listId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteItemsByListId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllItems(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllItems.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAllItems.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TodoItemEntity>> getItemsForList(final int listId) {
    final String _sql = "SELECT * FROM todo_items WHERE listId = ? ORDER BY orderIndex ASC, createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, listId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"todo_items"}, new Callable<List<TodoItemEntity>>() {
      @Override
      @NonNull
      public List<TodoItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfListId = CursorUtil.getColumnIndexOrThrow(_cursor, "listId");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfOrderIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "orderIndex");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfReminderOffsetMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderOffsetMillis");
          final List<TodoItemEntity> _result = new ArrayList<TodoItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TodoItemEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpListId;
            _tmpListId = _cursor.getInt(_cursorIndexOfListId);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final int _tmpOrderIndex;
            _tmpOrderIndex = _cursor.getInt(_cursorIndexOfOrderIndex);
            final Long _tmpDeadline;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmpDeadline = null;
            } else {
              _tmpDeadline = _cursor.getLong(_cursorIndexOfDeadline);
            }
            final Long _tmpReminderOffsetMillis;
            if (_cursor.isNull(_cursorIndexOfReminderOffsetMillis)) {
              _tmpReminderOffsetMillis = null;
            } else {
              _tmpReminderOffsetMillis = _cursor.getLong(_cursorIndexOfReminderOffsetMillis);
            }
            _item = new TodoItemEntity(_tmpId,_tmpListId,_tmpText,_tmpDescription,_tmpIsCompleted,_tmpCreatedAt,_tmpOrderIndex,_tmpDeadline,_tmpReminderOffsetMillis);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
