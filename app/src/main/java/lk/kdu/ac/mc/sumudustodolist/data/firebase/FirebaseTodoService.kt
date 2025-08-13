package lk.kdu.ac.mc.sumudustodolist.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoItemEntity
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoListEntity
import lk.kdu.ac.mc.sumudustodolist.data.firebase.models.FirebaseTodoList
import lk.kdu.ac.mc.sumudustodolist.data.firebase.models.FirebaseTodoItem

//Communication class with the FIrebase
class FirebaseTodoService {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    //Gets user details from DB if logged in
    private fun currentUserCollection() = auth.currentUser?.uid?.let { userId ->
        firestore.collection("users").document(userId).collection("todoLists")
    } ?: throw IllegalStateException("User not logged in for Firebase operation")

    //Function to backup local lists to the DB
    suspend fun backupLists(todoLists: List<TodoListEntity>, todoItemsMap: Map<Int, List<TodoItemEntity>>) {
        val userListsCollection = currentUserCollection()
        val batch = firestore.batch()

        todoLists.forEach { listEntity ->
            val itemsForThisList = todoItemsMap[listEntity.id] ?: emptyList()
            val firebaseItems = itemsForThisList.map { itemEntity ->
                FirebaseTodoItem(
                    localId = itemEntity.id,
                    text = itemEntity.text,
                    description = itemEntity.description,
                    isCompleted = itemEntity.isCompleted,
                    createdAt = itemEntity.createdAt,
                    orderIndex = itemEntity.orderIndex
                )
            }
            val firebaseList = FirebaseTodoList(
                localId = listEntity.id,
                name = listEntity.name,
                createdAt = listEntity.createdAt,
                orderIndex = listEntity.orderIndex,
                items = firebaseItems
            )
            val docRef = userListsCollection.document(listEntity.id.toString())
            batch.set(docRef, firebaseList)
        }
        batch.commit().await()
    }

    //Function to get list details from DB to restore locally
    suspend fun restoreLists(): List<Pair<TodoListEntity, List<TodoItemEntity>>> {
        val userListsCollection = currentUserCollection()
        val firebaseListsData = mutableListOf<Pair<TodoListEntity, List<TodoItemEntity>>>()

        val querySnapshot = userListsCollection.get().await()
        for (document in querySnapshot.documents) {
            val firebaseList = document.toObject<FirebaseTodoList>()
            firebaseList?.let { fbList ->
                val todoListEntity = TodoListEntity(
                    name = fbList.name,
                    createdAt = fbList.createdAt,
                    orderIndex = fbList.orderIndex
                )
                val todoItemEntities = fbList.items.map { fbItem ->
                    TodoItemEntity(
                        listId = 0,
                        text = fbItem.text,
                        description = fbItem.description,
                        isCompleted = fbItem.isCompleted,
                        createdAt = fbItem.createdAt,
                        orderIndex = fbItem.orderIndex
                    )
                }
                firebaseListsData.add(Pair(todoListEntity, todoItemEntities))
            }
        }
        return firebaseListsData
    }

    // Helper to check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}