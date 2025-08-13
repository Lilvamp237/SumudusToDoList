package lk.kdu.ac.mc.sumudustodolist.data.firebase.models

//The item details passed onto Firebase
data class FirebaseTodoItem(
    val localId: Int = 0,
    var text: String = "",
    var description: String? = null,
    var isCompleted: Boolean = false,
    val createdAt: Long = 0L,
    var orderIndex: Int = 0
) {
    // Constructor with no arguments
    constructor() : this(0, "", null, false, 0L, 0)
}