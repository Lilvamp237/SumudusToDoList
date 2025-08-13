// e.g., in util/ReminderScheduler.kt
package lk.kdu.ac.mc.sumudustodolist.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import lk.kdu.ac.mc.sumudustodolist.data.local.entities.TodoItemEntity

//Utility class responsible for scheduling and canceling reminders for TodoItemEntity instances
//using Android's AlarmManager
class ReminderScheduler(private val context: Context) {
    // Get an instance of AlarmManager system service
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    //Schedules a reminder for the given to-do item
    fun scheduleReminder(item: TodoItemEntity) {
        if (item.deadline == null || item.reminderOffsetMillis == null) {
            Log.d("ReminderScheduler", "Cannot schedule reminder for item ${item.id}, deadline or offset is null.")
            return
        }
        // Do not schedule if the item is already completed
        if (item.isCompleted) {
            Log.d("ReminderScheduler", "Item ${item.id} is completed, not scheduling reminder.")
            return
        }

        // Calculate the exact time for the reminder
        val reminderTime = item.deadline!! - item.reminderOffsetMillis!!

        // Schedule if reminder time is in the future
        if (reminderTime <= System.currentTimeMillis()) {
            Log.d("ReminderScheduler", "Reminder time for item ${item.id} is in the past. Not scheduling.")
            return
        }

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ReminderBroadcastReceiver.ACTION_REMINDER
            putExtra(ReminderBroadcastReceiver.EXTRA_ITEM_ID, item.id)
            putExtra(ReminderBroadcastReceiver.EXTRA_ITEM_TEXT, item.text)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //Assistance taken from AI tools and sources
        try { // Check for permission to schedule exact alarms on Android S (API 31) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w("ReminderScheduler", "Cannot schedule exact alarms. Fallback needed for item ${item.id}.")
                return
            }
            // Schedule an exact alarm that wakes up the device
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
            Log.d("ReminderScheduler", "Reminder scheduled for item ${item.id} at $reminderTime")
        } catch (se: SecurityException) {
            Log.e("ReminderScheduler", "SecurityException while scheduling exact alarm for item ${item.id}", se)
            // Can happen if SCHEDULE_EXACT_ALARM is not granted on Android 12 or upper
        }
    }

    //Cancel any scheduled reminder for the given to-do item ID
    fun cancelReminder(itemId: Int) {
        // Recreate the same Intent that was used for scheduling
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ReminderBroadcastReceiver.ACTION_REMINDER
            // We only need the ID to find the matching PendingIntent
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            itemId, // Must be the same request code used for scheduling
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // Use FLAG_NO_CREATE to check if it exists
        )

        // If the PendingIntent exists, cancel the alarm and the PendingIntent itself
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel() // Also cancel the PendingIntent itself
            Log.d("ReminderScheduler", "Reminder cancelled for item $itemId")
        } else {
            Log.d("ReminderScheduler", "No reminder found to cancel for item $itemId")
        }
    }
}