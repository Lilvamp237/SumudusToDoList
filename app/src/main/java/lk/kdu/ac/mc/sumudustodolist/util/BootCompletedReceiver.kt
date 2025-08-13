package lk.kdu.ac.mc.sumudustodolist.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lk.kdu.ac.mc.sumudustodolist.data.local.AppDatabase
import lk.kdu.ac.mc.sumudustodolist.data.repository.TodoRepository

//A BroadcastReceiver that listens for the `ACTION_BOOT_COMPLETED` intent
//When the device finishes booting, this receiver is triggered to reschedule
class BootCompletedReceiver : BroadcastReceiver() {
    //Coroutine scope for performing background operations like database access, reminder scheduling
    //SupervisorJob ensures that if one child coroutine fails, others are not affected
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //Called when the BroadcastReceiver is receiving an Intent broadcast
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the received action is ACTION_BOOT_COMPLETED
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device boot completed. Rescheduling reminders.")
            scope.launch {
                //Obtain instances of the database, repository, and reminder scheduler
                val database = AppDatabase.getDatabase(context.applicationContext)
                val repository = TodoRepository(database)
                val reminderScheduler = ReminderScheduler(context.applicationContext)

                try {
                    // Fetch all lists and their corresponding items from the repository
                    val allLists = repository.getAllLists().first()
                    allLists.forEach { list ->
                        val items = repository.getItemsForList(list.id).first()
                        items.forEach { item -> //Reschedule reminder only if item is not completed, has a deadline,
                            // a reminder offset, and the reminder time is in the future
                            if (!item.isCompleted && item.deadline != null && item.reminderOffsetMillis != null) {
                                val reminderTime = item.deadline!! - item.reminderOffsetMillis!!
                                if (reminderTime > System.currentTimeMillis()) {
                                    reminderScheduler.scheduleReminder(item)
                                }
                            }
                        }
                    }
                    Log.d("BootReceiver", "Reminders rescheduled.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling reminders", e)
                }
            }
        }
    }
}