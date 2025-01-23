package com.github.bqluan.aliyunpush

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import android.util.Log
import androidx.core.app.NotificationCompat
import com.alibaba.sdk.android.push.AliyunMessageIntentService
import com.alibaba.sdk.android.push.MessageReceiver
import com.alibaba.sdk.android.push.notification.CPushMessage

/**
 * 通过Service 处理下发通知和消息
 */
class MyMessageIntentService: AliyunMessageIntentService() {
    private val TAG = "MyMessageIntentService"
    private val NOTIFICATION_ID = 1

    // 点击通知回调。点击通知会回调该方法。
    override fun onNotificationOpened(context: Context?, title: String?, summary: String?, extraMap: String?) {
        Log.d(TAG, "onNotificationOpened  title: $title, summary: $summary, extraMap: $extraMap")
    }

    // 删除通知的回调。删除通知时会回调该方法。
    override fun onNotificationRemoved(p0: Context?, messageId: String?) {
        Log.d(TAG, "onNotificationRemoved messageId: $messageId")
    }

    // 推送通知到达回调。SDK收到通知后，回调该方法，可获取到并处理通知相关的参数。
    override fun onNotification(
        context: Context?,
        title: String?,
        summary: String?,
        extraMap: MutableMap<String, String>?
    ) {
        context?.let {
            Log.d(TAG, "onNotification  title: $title, summary: $summary, extraMap: $extraMap")
            // Toast.makeText(it, "service 收到了通知", Toast.LENGTH_SHORT).show()// 显示通知
            showNotification(context, title, summary)
        }
    }

    // 收到推送消息的回调，可以在这里处理推送消息。
    override fun onMessage(context: Context?, message: CPushMessage?) {
        context?.apply {
            message?.let {
                Log.d(TAG, "onMessage  title: ${it.title}, content: ${it.content}, messageId: ${it.messageId}")
            }
        }
    }

    // 点击无跳转逻辑通知的回调，点击无跳转逻辑（open=4）通知时回调该方法（v2.3.2及以上版本支持）。
    override fun onNotificationClickedWithNoAction(
        p0: Context?,
        p1: String?,
        p2: String?,
        p3: String?
    ) {

    }

    // 通知在应用内回调，该方法仅在showNotificationNow返回false时才会被回调，且此时不调用onNotification，此时需要您自己处理通知逻辑。
    override fun onNotificationReceivedInApp(
        p0: Context?,
        p1: String?,
        p2: String?,
        p3: MutableMap<String, String>?,
        p4: Int,
        p5: String?,
        p6: String?
    ) {

    }

    // 用通知通道显示通知
    private fun showNotification(context: Context, title: String?, content: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 获取应用的主 Activity
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)
        val mainActivityClass = launchIntent?.component?.className?.let {
            try {
                Class.forName(it)
            } catch (e: ClassNotFoundException) {
                null
            }
        }

        // 创建 PendingIntent
        val pendingIntent = if (mainActivityClass != null) {
            val intent = Intent(context, mainActivityClass)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            // 如果无法获取 MainActivity，使用默认启动 Intent
            PendingIntent.getActivity(
                context,
                0,
                packageManager.getLaunchIntentForPackage(context.packageName),
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        Log.d(TAG, "showNotification  channelId: ${ExpoAliyunPushNotificationsModule.CHANNEL_ID}")

        // 创建通知
        val notification = NotificationCompat.Builder(context, ExpoAliyunPushNotificationsModule.CHANNEL_ID)
            .setSmallIcon(getAppIcon(context))
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getAppIcon(context: Context): Int {
        try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            return applicationInfo.icon
        } catch (e: PackageManager.NameNotFoundException) {
            // 如果获取失败，返回一个默认图标资源ID
            return android.R.drawable.ic_dialog_info
        }
    }
}
