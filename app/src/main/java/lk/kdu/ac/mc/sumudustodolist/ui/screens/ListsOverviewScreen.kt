package lk.kdu.ac.mc.sumudustodolist.ui.screens

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoListEntity
import lk.kdu.ac.mc.sumudustodolist.ui.navigation.Screen
import lk.kdu.ac.mc.sumudustodolist.ui.viewmodel.ListsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.collections.immutable.toImmutableList
import androidx.compose.foundation.clickable
import android.util.Log
import androidx.compose.material.icons.filled.Person
import lk.kdu.ac.mc.sumudustodolist.ui.viewmodel.AuthViewModel

//Composable function for the Lists Overview screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsOverviewScreen(
    navController: NavController,
    viewModel: ListsViewModel,
    authViewModel: AuthViewModel
) {
    // Use originalLists from ViewModel for reordering
    val originalListsFromVM by viewModel.displayedLists.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val listToEdit by viewModel.listToEdit.collectAsState()
    var draggedList by remember { mutableStateOf<TodoListEntity?>(null) }
    var dragStartIndex by remember { mutableStateOf(-1) }
    var currentDragOffset by remember { mutableStateOf(Offset.Zero) }
    var listForDisplay by remember(originalListsFromVM) { mutableStateOf(originalListsFromVM.toImmutableList()) }

    LaunchedEffect(originalListsFromVM) {
        if (draggedList == null) {
            listForDisplay = originalListsFromVM.toImmutableList()
        }
    }

    val density = LocalDensity.current
    val itemHeights = remember { mutableMapOf<Int, Float>() }

    //Callback function executed when a drag operation ends or is cancelled
    val onDragEndOrCancel = {
        draggedList?.let {
            val finalOrderedList = listForDisplay.toList()
            viewModel.reorderLists(finalOrderedList)
        }
        // Reset drag state
        draggedList = null
        dragStartIndex = -1
        currentDragOffset = Offset.Zero
    }
    // State for delete list confirmation dialog
    val showDeleteConfirmation by viewModel.showDeleteListConfirmationDialog.collectAsState()
    val listPendingDeletion by viewModel.listToDelete.collectAsState()
    // State for the TopAppBar options menu
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Todo Lists") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                showMenu = false
                                navController.navigate(Screen.Profile.route)
                            },
                            leadingIcon = { Icon(Icons.Filled.Person, "Profile") }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                showMenu = false
                                navController.navigate(Screen.Settings.route)
                            },
                            leadingIcon = { Icon(Icons.Filled.Settings, "Settings") }
                        )
                    }
                }
            ) },

        floatingActionButton = {
            FloatingActionButton(onClick = {
                Log.d("ListsOverview", "FAB clicked!") // Add this
                viewModel.onOpenDialog() // Opens the dialog to create a new list
            }

            ) {
                Icon(Icons.Filled.Add, "Create new list")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            OutlinedTextField( //Search input field.
                value = searchQuery,
                onValueChange = {
                    if (draggedList == null) {
                        viewModel.onSearchQueryChanged(it)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Search lists or items...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                trailingIcon = { // Show clear button if search query is not empty
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors()
            )

            if (listForDisplay.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.isNotEmpty() && originalListsFromVM.isEmpty()) "No lists found matching your search."
                        else if (originalListsFromVM.isEmpty()) "No lists yet. Tap '+' to create one!"
                        else "No lists to display."
                    )
                }
            } else {
                LazyColumn( // Display lists in a LazyColumn
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(listForDisplay, key = { _, item -> item.id }) { index, listEntry ->
                        val isBeingDragged = draggedList?.id == listEntry.id

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    itemHeights[listEntry.id] = coordinates.size.height.toFloat()
                                }
                                .pointerInput(listEntry.id, listForDisplay.size) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { offset ->
                                            originalListsFromVM
                                                .indexOfFirst { it.id == listEntry.id }
                                                .takeIf { it != -1 }
                                                ?.let { foundIndex ->
                                                    draggedList = listForDisplay[foundIndex]
                                                    dragStartIndex = foundIndex
                                                    currentDragOffset = Offset.Zero
                                                }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            if (draggedList == null || dragStartIndex == -1) return@detectDragGesturesAfterLongPress
                                            currentDragOffset += dragAmount

                                            val currentDraggedItemOriginalY = itemHeights
                                                .filterKeys { key -> listForDisplay.indexOfFirst { it.id == key } < dragStartIndex }
                                                .values.sum()


                                            val currentDraggedItemCenterY = currentDraggedItemOriginalY + (itemHeights[draggedList!!.id] ?: 0f) / 2f + currentDragOffset.y

                                            //Determine the target index for reordering
                                            var targetIndex = -1
                                            var accumulatedHeight = 0f
                                            for ((idx, item) in listForDisplay.withIndex()) {
                                                val itemHeight = itemHeights[item.id] ?: 0f
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
                                                targetIndex = listForDisplay.size -1
                                            }


                                            if (targetIndex != -1 && targetIndex != dragStartIndex) {
                                                val mutableList = listForDisplay.toMutableList()
                                                val dragged = mutableList.removeAt(dragStartIndex)
                                                val actualTargetIndex = if (dragStartIndex < targetIndex) targetIndex else targetIndex
                                                mutableList.add(if (dragStartIndex < actualTargetIndex) actualTargetIndex else actualTargetIndex, dragged)

                                                listForDisplay = mutableList.toImmutableList()
                                                dragStartIndex = actualTargetIndex
                                            }
                                        },
                                        onDragEnd = onDragEndOrCancel,
                                        onDragCancel = onDragEndOrCancel
                                    )
                                }
                                .graphicsLayer {
                                    translationY = if (isBeingDragged) currentDragOffset.y else 0f
                                    alpha = if (isBeingDragged && draggedList != null) 0.7f else 1.0f
                                    shadowElevation = if (isBeingDragged && draggedList != null) 8.dp.toPx() else 0f
                                }
                                .zIndex(if (isBeingDragged) 1f else 0f)
                        ) {
                            ListItemRow(
                                list = listEntry,
                                onClick = {
                                    if (draggedList == null) {
                                        navController.navigate(Screen.ListDetail.createRoute(listEntry.id))
                                    }
                                },
                                onEdit = {
                                    if (draggedList == null) viewModel.onOpenDialog(listEntry)
                                },
                                onDelete = {
                                    if (draggedList == null) viewModel.requestDeleteList(listEntry)
                                }
                            )
                        }
                        Divider()
                    }
                }
            }

            //Show list creation/edit dialog if requested
            if (showDialog) {
                ListCreationDialog(
                    listToEdit = listToEdit,
                    onDismiss = { viewModel.onCloseDialog() },
                    onConfirm = { listName ->
                        viewModel.addOrUpdateList(listName)
                    }
                )
            }
            // Show delete list confirmation dialog if requested
            ConfirmationDialog(
                show = showDeleteConfirmation,
                title = "Delete List",
                message = "Are you sure you want to delete the list \"${listPendingDeletion?.name ?: ""}\"? All its items will also be deleted.",
                onConfirm = { viewModel.confirmDeleteList() },
                onDismiss = { viewModel.onDismissDeleteListConfirmation() }
            )
        }
    }
}

//Composable function for rendering a single row in the to-do list overview
@Composable
fun ListItemRow(
    list: TodoListEntity,
    modifier: Modifier = Modifier, // Added modifier
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Date formatter for displaying the creation date
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(list.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Created: ${dateFormat.format(Date(list.createdAt))}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row { // Row for action icons
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, "Edit list name")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "Delete list")
                }
            }
        }
    }
}

//Composable function for the dialog used to create a new to-do list or edit an existing one
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListCreationDialog(
    listToEdit: TodoListEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    //List name input field state
    var text by remember(listToEdit) { mutableStateOf(listToEdit?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (listToEdit == null) "Create New List" else "Edit List Name")
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("List Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text)
                    }
                }
            ) {
                Text(if (listToEdit == null) "Create" else "Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}