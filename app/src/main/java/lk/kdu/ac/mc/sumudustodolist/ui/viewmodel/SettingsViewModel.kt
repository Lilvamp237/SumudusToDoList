package lk.kdu.ac.mc.sumudustodolist.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lk.kdu.ac.mc.sumudustodolist.data.firebase.FirebaseTodoService
import lk.kdu.ac.mc.sumudustodolist.data.local.AppDatabase
import lk.kdu.ac.mc.sumudustodolist.data.repository.TodoRepository

//Sealed class representing the different states of backup and restore operations
sealed class BackupRestoreState {
    object Idle : BackupRestoreState()
    object InProgress : BackupRestoreState()
    data class Success(val message: String) : BackupRestoreState()
    data class Error(val message: String) : BackupRestoreState()
}

//ViewModel for the Settings screen
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository
    private val firebaseService = FirebaseTodoService() //Service for Firebase interactions
    private val auth = FirebaseAuth.getInstance() //Firebase Auth instance for checking login status
    private val _backupRestoreState = MutableStateFlow<BackupRestoreState>(BackupRestoreState.Idle)
    val backupRestoreState: StateFlow<BackupRestoreState> = _backupRestoreState.asStateFlow()
    val isLoggedIn: StateFlow<Boolean> = MutableStateFlow(auth.currentUser != null).also { flow ->
        auth.addAuthStateListener { firebaseAuth -> //Listen to Firebase Auth state changes to update isLoggedIn
            flow.value = firebaseAuth.currentUser != null
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TodoRepository(database)
    }

    //Initiate the backup of all local to-do lists and items to Firebase
    fun backupData() { //Check if user is logged in before proceeding
        if (!firebaseService.isUserLoggedIn()) {
            _backupRestoreState.value = BackupRestoreState.Error("User not logged in.")
            return
        }
        _backupRestoreState.value = BackupRestoreState.InProgress
        viewModelScope.launch {
            try {
                // Fetch all local lists and their items
                val allLists = repository.getAllLists().first() // Get current snapshot
                val allItemsMap = mutableMapOf<Int, List<lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoItemEntity>>()
                allLists.forEach { list ->
                    allItemsMap[list.id] = repository.getItemsForList(list.id).first()
                }
                firebaseService.backupLists(allLists, allItemsMap) // Perform the backup using the Firebase service
                _backupRestoreState.value = BackupRestoreState.Success("Backup successful!")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Backup failed", e)
                _backupRestoreState.value = BackupRestoreState.Error("Backup failed: ${e.message}")
            }
        }
    }

    //Initiate the restoration of data from Firebase to the local DB
    fun restoreData() {
        if (!firebaseService.isUserLoggedIn()) {
            _backupRestoreState.value = BackupRestoreState.Error("User not logged in.")
            return
        }
        _backupRestoreState.value = BackupRestoreState.InProgress
        viewModelScope.launch {
            try {
                //Clear local data and restore
                repository.clearAllData()

                //Fetch data from Firebase
                val restoredData = firebaseService.restoreLists()
                //Insert restored lists and items into the local DB
                restoredData.forEach { (listEntity, itemEntities) ->
                    val newListId = repository.insertList(listEntity)
                    itemEntities.forEach { itemEntity ->
                        //Insert items, associating them with the new local list ID
                        repository.insertItem(itemEntity.copy(listId = newListId.toInt()))
                    }
                }
                _backupRestoreState.value = BackupRestoreState.Success("Restore successful!")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Restore failed", e)
                _backupRestoreState.value = BackupRestoreState.Error("Restore failed: ${e.message}")
            }
        }
    }

    //Resets the backup/restore state to Idle
    fun resetBackupRestoreState() {
        _backupRestoreState.value = BackupRestoreState.Idle
    }
}