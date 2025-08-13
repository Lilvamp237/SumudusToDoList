package lk.kdu.ac.mc.sumudustodolist.data.firebase.models

//Format of lists paased to the Firebase
data class FirebaseTodoList(
    val localId: Int = 0, // Store the original Room ID for mapping during restore
    var name: String = "",
    val createdAt: Long = 0L,
    var orderIndex: Int = 0,
    val items: List<FirebaseTodoItem> = emptyList() // Embed items within the list document
) {
    // Constructor with no arguments
    constructor() : this(0, "", 0L, 0, emptyList())
}
