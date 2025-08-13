package lk.kdu.ac.mc.sumudustodolist.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import lk.kdu.ac.mc.sumudustodolist.MainActivity
import lk.kdu.ac.mc.sumudustodolist.R

//A BroadcastReceiver responsible for receiving reminder intents scheduled by ReminderScheduler
//and displaying a system notification to the user
class ReminderBroadcastReceiver : BroadcastReceiver() {

    companion object {
        // Action string for the reminder intent
        const val ACTION_REMINDER = "lk.kdu.ac.mc.sumudustodolist.ACTION_REMINDER"
        // Keys for extras in the reminder intent
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_ITEM_TEXT = "extra_item_text"
        // Notification channel ID for Android Oreo (API 26) and above
        private const val CHANNEL_ID = "todo_reminder_channel"
        // Prefix for notification IDs to ensure uniqueness per item
        private const val NOTIFICATION_ID_PREFIX = 1000 // To ensure notification IDs are not the same
    }

    //Called when the BroadcastReceiver is receiving an Intent broadcast
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Received action: ${intent.action}")
        if (intent.action == ACTION_REMINDER) { // Check if the received action matches the expected reminder action
            val itemId = intent.getIntExtra(EXTRA_ITEM_ID, -1)
            val itemText = intent.getStringExtra(EXTRA_ITEM_TEXT) ?: "Upcoming To-Do Task"

            if (itemId != -1) {
                Log.d("ReminderReceiver", "Showing reminder for item ID: $itemId, Text: $itemText")
                showNotification(context, itemId, "To-Do Reminder", itemText)
            } else {
                Log.w("ReminderReceiver", "Invalid item ID received.")
            }
        }
    }

    //Creates and displays a system notification for the to-do item reminder
    private fun showNotification(context: Context, itemId: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android Oreo and above
        //Assistance taken from AI tools and sources
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "To-Do Reminders",
                NotificationManager.IMPORTANCE_HIGH // Set importance to high for reminders
            ).apply {
                description = "Channel for To-Do item reminders" // User-visible description
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open the app when notification is tapped
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_item_id", itemId)
        }
        val pendingOpenAppIntent = PendingIntent.getActivity(
            context,
            itemId + 2000,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        //Assistance taken from AI tools and sources
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.todo_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen
            .setAutoCancel(true) // Dismiss notification when tapped
            .setContentIntent(pendingOpenAppIntent) // Set the action when notification is tapped

        notificationManager.notify(NOTIFICATION_ID_PREFIX + itemId, notificationBuilder.build())
    }
}