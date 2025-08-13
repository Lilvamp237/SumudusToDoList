package lk.kdu.ac.mc.sumudustodolist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoItemEntity
import lk.kdu.ac.mc.sumudustodolist.ui.viewmodel.ListDetailViewModel
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex
import kotlinx.collections.immutable.toImmutableList
import androidx.compose.material.icons.filled.Event
import java.util.Calendar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//Composable function for the list detail screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    navController: NavController,
    viewModel: ListDetailViewModel
) {
    //State for the current list being viewed
    val list by viewModel.currentList.collectAsState()
    // State for the original list of items from the ViewModel for drag and drop
    val originalItemsFromVM by viewModel.itemsForList.collectAsState()
    // State for whether the item add/edit dialog is shown
    val showItemDialog by viewModel.showItemDialog.collectAsState()
    // State for the item currently being edited
    val itemToEditFromVM by viewModel.itemToEdit.collectAsState()

    var editingListName by remember { mutableStateOf(false) }
    var listNameText by remember(list) { mutableStateOf(list?.name ?: "") }

    // State for drag-and-drop reordering of items
    var draggedItem by remember { mutableStateOf<TodoItemEntity?>(null) }
    var dragStartIndex by remember { mutableStateOf(-1) }
    var currentDragOffset by remember { mutableStateOf(Offset.Zero) }

    var itemsForDisplay by remember(originalItemsFromVM) { mutableStateOf(originalItemsFromVM.toImmutableList()) }

    val showDeleteItemConfirmation by viewModel.showDeleteItemConfirmationDialog.collectAsState()
    val itemPendingDeletion by viewModel.itemToDelete.collectAsState()

    LaunchedEffect(originalItemsFromVM) {
        if (draggedItem == null) {
            itemsForDisplay = originalItemsFromVM.toImmutableList()
        }
    }

    // Stores the height of each item, used for drag-and-drop calculations
    val itemHeights = remember { mutableMapOf<Int, Float>() }
    // Callback function executed when a drag operation ends or is cancelled
    val onDragEndOrCancel = {
        draggedItem?.let {
            val finalOrderedItems = itemsForDisplay.toList()
            viewModel.reorderTodoItems(finalOrderedItems) // Call ViewModel function for items
        }
        draggedItem = null
        dragStartIndex = -1
        currentDragOffset = Offset.Zero
    }

    //Update the local listNameText state when the list data changes
    LaunchedEffect(list) { // Update listNameText when list changes from DB
        list?.name?.let { listNameText = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (editingListName) {
                        OutlinedTextField(
                            value = listNameText,
                            onValueChange = { listNameText = it },
                            label = { Text("List Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(0.8f) //Prevents overlapping with actions
                        )
                    } else {
                        Text(list?.name ?: "List Details")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (editingListName) { //Show "Save" button during list name editing, otherwise "Edit"
                        Button(onClick = {
                            viewModel.updateListName(listNameText)
                            editingListName = false
                        }) { Text("Save") }
                    } else {
                        list?.let {
                            IconButton(onClick = { editingListName = true }) {
                                Icon(Icons.Filled.Edit, "Edit list name")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            list?.let { //Show FAB only if list is loaded
                FloatingActionButton(onClick = { viewModel.onOpenItemDialog() }) {
                    Icon(Icons.Filled.Add, "Add new item")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (list == null && viewModel.listId != -1) { // List ID is valid but list not loaded yet
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator() // Displayed when list is loading
                }
            } else if (list == null && viewModel.listId == -1) { // Invalid List ID
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: List not found.")
                }
            } else {
                if (itemsForDisplay.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No items in this list yet. Tap '+' to add one!")
                    }
                } else {
                    LazyColumn( // Display items in a LazyColumn with potentially long lists
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(itemsForDisplay, key = { _, item -> item.id }) { index, itemEntry ->
                            val isBeingDragged = draggedItem?.id == itemEntry.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coordinates ->
                                        itemHeights[itemEntry.id] = coordinates.size.height.toFloat()
                                    }
                                    .pointerInput(itemEntry.id, itemsForDisplay.size) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { _ ->
                                                originalItemsFromVM
                                                    .indexOfFirst { it.id == itemEntry.id }
                                                    .takeIf { it != -1 }
                                                    ?.let { foundIndex ->
                                                        draggedItem = itemsForDisplay[foundIndex]
                                                        dragStartIndex = foundIndex
                                                        currentDragOffset = Offset.Zero
                                                    }
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                if (draggedItem == null || dragStartIndex == -1) return@detectDragGesturesAfterLongPress
                                                currentDragOffset += dragAmount

                                                // Reordering
                                                //Assistance taken from AI tools
                                                val currentDraggedItemOriginalY = itemHeights
                                                    .filterKeys { key -> itemsForDisplay.indexOfFirst { it.id == key } < dragStartIndex }
                                                    .values.sum()
                                                val currentDraggedItemCenterY = currentDraggedItemOriginalY + (itemHeights[draggedItem!!.id] ?: 0f) / 2f + currentDragOffset.y

                                                //Determine the target index for reordering based on drag position
                                                var targetIndex = -1
                                                var accumulatedHeight = 0f
                                                for ((idx, itemInLoop) in itemsForDisplay.withIndex()) {
                                                    val itemHeight = itemHeights[itemInLoop.id] ?: 0f
                                                    if (idx == dragStartIndex) {
                                                        accumulatedHeight += itemHeight
                                                        continue
                                                    }
                                                    val itemCenterY = accumulatedHeight + itemHeight / 2f
                                                    if (currentDraggedItemCenterY < itemCenterY) {
                                                        targetIndex = idx
                                                        break
                                                    }
                                                    accumulatedHeight += itemHeight
                                                }
                                                if (targetIndex == -1 && currentDraggedItemCenterY > accumulatedHeight) {
                                                    targetIndex = itemsForDisplay.size -1
                                                }

                                                if (targetIndex != -1 && targetIndex != dragStartIndex) {
                                                    val mutableList = itemsForDisplay.toMutableList()
                                                    val dragged = mutableList.removeAt(dragStartIndex)
                                                    val actualTargetIndex = if (dragStartIndex < targetIndex) targetIndex else targetIndex
                                                    mutableList.add(if (dragStartIndex < actualTargetIndex) actualTargetIndex else actualTargetIndex, dragged)
                                                    itemsForDisplay = mutableList.toImmutableList()
                                                    dragStartIndex = actualTargetIndex
                                                }
                                            },
                                            onDragEnd = onDragEndOrCancel,
                                            onDragCancel = onDragEndOrCancel
                                        )
                                    }
                                    .graphicsLayer {
                                        translationY = if (isBeingDragged) currentDragOffset.y else 0f
                                        alpha = if (isBeingDragged && draggedItem != null) 0.7f else 1.0f
                                        shadowElevation = if (isBeingDragged && draggedItem != null) 8.dp.toPx() else 0f
                                    }
                                    .zIndex(if (isBeingDragged) 1f else 0f)
                            ) {
                                TodoItemRow(
                                    item = itemEntry,
                                    // Disable actions while dragging to prevent unintended interactions
                                    onToggleComplete = { if (draggedItem == null) viewModel.toggleItemCompletion(itemEntry) },
                                    onEdit = { if (draggedItem == null) viewModel.onOpenItemDialog(itemEntry) },
                                    onDelete = { if (draggedItem == null) viewModel.requestDeleteItem(itemEntry) }
                                )
                            }
                            Divider()
                        }
                    }
                }
            }

            // Show item add/edit dialog when requested
            if (showItemDialog) {
                TodoItemDialog(
                    itemToEdit = itemToEditFromVM,
                    onDismiss = { viewModel.onCloseItemDialog() },
                    onConfirm = { itemText, itemDescription, deadline, reminderOffset ->
                        viewModel.addOrUpdateItem(itemText, itemDescription,  deadline, reminderOffset)
                    }
                )
            }
            // Show delete item confirmation dialog when requested
            ConfirmationDialog(
                show = showDeleteItemConfirmation,
                title = "Delete Item",
                message = "Are you sure you want to delete the item \"${itemPendingDeletion?.text ?: ""}\"?",
                onConfirm = { viewModel.confirmDeleteItem() },
                onDismiss = { viewModel.onDismissDeleteItemConfirmation() }
            )
        }
    }
}

//For rendering a single row in the to-do item list
@Composable
fun TodoItemRow(
    item: TodoItemEntity,
    modifier: Modifier = Modifier,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isCompleted,
            onCheckedChange = { onToggleComplete() }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Medium
                ),
            )
            if (!item.description.isNullOrBlank()) {
                Text(
                    text = item.description!!,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        //Action icons for editing and deleting
        IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, "Edit item")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, "Delete item")
        }
    }
}

//Composable function for the dialog used to add or edit a TodoItemEntity
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItemDialog(
    itemToEdit: TodoItemEntity?,
    onDismiss: () -> Unit,
    onConfirm: (text: String, description: String?, deadline: Long?, reminderOffset: Long?) -> Unit
) {
    //Item's text and description input fields
    var text by remember(itemToEdit) { mutableStateOf(itemToEdit?.text ?: "") }
    var description by remember(itemToEdit) { mutableStateOf(itemToEdit?.description ?: "") }

    // State of Deadline
    val initialDeadline = itemToEdit?.deadline
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDeadline,
        selectableDates = object : SelectableDates { //Can only select today or future dates
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        }
    )
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val calendarForTimePicker = Calendar.getInstance()
    initialDeadline?.let { calendarForTimePicker.timeInMillis = it } //Initialize time picker with existing deadline time
    val timePickerState = rememberTimePickerState(
        initialHour = calendarForTimePicker.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendarForTimePicker.get(Calendar.MINUTE),
        is24Hour = false
    )
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var selectedDeadlineDateTime by remember(itemToEdit?.deadline) { mutableStateOf(itemToEdit?.deadline) }

    //Reminder options
    val reminderOptions = remember { // Use remember for the map if it's static
        mapOf(
            "No reminder" to null,
            "At time of event" to 0L,
            "5 minutes before" to 5 * 60 * 1000L,
            "15 minutes before" to 15 * 60 * 1000L,
            "30 minutes before" to 30 * 60 * 1000L,
            "1 hour before" to 60 * 60 * 1000L,
            "2 hours before" to 2 * 60 * 60 * 1000L,
            "1 day before" to 24 * 60 * 60 * 1000L
        )
    }
    var selectedReminderOffsetKey by remember(itemToEdit?.reminderOffsetMillis) {
        mutableStateOf(
            // Initialize with existing reminder or "No reminder"
            reminderOptions.entries.find { it.value == itemToEdit?.reminderOffsetMillis }?.key ?: "No reminder"
        )
    }
    var reminderDropdownExpanded by remember { mutableStateOf(false) }

    // DatePicker Dialog foe deaadline selection
    //Assistance taken from AI tools and sources
    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        datePickerState.selectedDateMillis?.let {
                            val cal = Calendar.getInstance().apply { timeInMillis = it }
                            val currentDeadlineCal = Calendar.getInstance()
                            selectedDeadlineDateTime?.let { currentDeadlineCal.timeInMillis = it }

                            cal.set(Calendar.HOUR_OF_DAY, currentDeadlineCal.get(Calendar.HOUR_OF_DAY))
                            cal.set(Calendar.MINUTE, currentDeadlineCal.get(Calendar.MINUTE))

                            selectedDeadlineDateTime = cal.timeInMillis
                            showTimePickerDialog = true
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePicker Dialog for deadline selection
    //Assistance taken from AI tools and sources
    if (showTimePickerDialog) {
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = { Text("Select Time") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTimePickerDialog = false
                        val calendar = Calendar.getInstance()
                        val baseDateMillis = datePickerState.selectedDateMillis ?: selectedDeadlineDateTime ?: System.currentTimeMillis()
                        calendar.timeInMillis = baseDateMillis

                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        selectedDeadlineDateTime = calendar.timeInMillis
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) { Text("Cancel") }
            }
        )
    }
    //Main AlertDialog for adding/editing item details
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (itemToEdit == null) "Add New Item" else "Edit Item") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Item Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Deadline", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { showDatePickerDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Event, "Set Deadline Date/Time", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            selectedDeadlineDateTime?.let {
                                SimpleDateFormat("EEE, MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(it))
                            } ?: "Set Deadline"
                        )
                    }
                    if (selectedDeadlineDateTime != null) {
                        IconButton(onClick = {
                            selectedDeadlineDateTime = null
                        }) {
                            Icon(Icons.Filled.Close, "Clear Deadline")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Picker
                Text("Reminder", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = reminderDropdownExpanded,
                    onExpandedChange = { reminderDropdownExpanded = !reminderDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedReminderOffsetKey,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Remind me") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = reminderDropdownExpanded,
                        onDismissRequest = { reminderDropdownExpanded = false }
                    ) {
                        reminderOptions.forEach { (label, _) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedReminderOffsetKey = label
                                    reminderDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                //Confirm only if item title is not blank
                if (text.isNotBlank()) {
                    onConfirm(text, description.ifBlank { null }, selectedDeadlineDateTime, reminderOptions[selectedReminderOffsetKey])
                }
            }) { Text(if (itemToEdit == null) "Add" else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}