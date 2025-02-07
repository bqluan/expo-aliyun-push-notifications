package com.github.bqluan.aliyunpush

import android.app.Application
import android.os.Build
import android.app.NotificationManager
import android.app.NotificationChannel
import android.graphics.Color
import android.util.Log
import android.content.Context
import android.text.TextUtils
import com.alibaba.sdk.android.push.CloudPushService
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.alibaba.sdk.android.push.noonesdk.PushInitConfig
/*import com.alibaba.sdk.android.push.HonorRegister
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister
import com.alibaba.sdk.android.push.register.MeizuRegister
import com.alibaba.sdk.android.push.register.MiPushRegister
import com.alibaba.sdk.android.push.register.OppoRegister
import com.alibaba.sdk.android.push.register.VivoRegister*/
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import android.content.pm.PackageManager
import java.net.URL

class ExpoAliyunPushNotificationsModule : Module() {
    init {
        EventManager.setModule(this)
    }
  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  override fun definition() = ModuleDefinition {

    OnCreate() {
        initPushSdk()
    }
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoAliyunPushNotifications')` in JavaScript.
    Name("ExpoAliyunPushNotifications")

    // Sets constant properties on the module. Can take a dictionary or a closure that returns a dictionary.
    Constants(
      "PI" to Math.PI
    )

    // Defines event names that the module can send to JavaScript.
    Events("onChange")

    // Defines a JavaScript synchronous function that runs the native code on the JavaScript thread.
    Function("hello") {
      "Hello world! 👋"
    }

    // 点击通知事件
    Events(NOTIFICATION_OPENED_EVENT)

    Function("getApiKey") {
      val applicationInfo = appContext?.reactContext?.packageManager?.getApplicationInfo(appContext?.reactContext?.packageName.toString(), PackageManager.GET_META_DATA)
      val metaData = applicationInfo?.metaData
      return@Function when (val value = metaData?.get("Ali_Push_App_Key")) {
          is String -> value
          is Int -> value.toString()
          else -> null
      }
    }

    Function("getDeviceId") {
        val service = PushServiceFactory.getCloudPushService()
        val deviceId = service?.deviceId
        Log.d(TAG, "Device ID: $deviceId")
        return@Function deviceId
    }

    // Defines a JavaScript function that always returns a Promise and whose native code
    // is by default dispatched on the different thread than the JavaScript runtime runs on.
    AsyncFunction("setValueAsync") { value: String ->
      // Send an event to JavaScript.
      sendEvent("onChange", mapOf(
        "value" to value
      ))
    }

    AsyncFunction("init") { value: String ->
      // Send an event to JavaScript.
      sendEvent("onChange", mapOf(
        "value" to value
      ))
    }
  }

    private fun initPushSdk() {
        val applicationInfo = appContext?.reactContext?.packageManager?.getApplicationInfo(appContext?.reactContext?.packageName.toString(), PackageManager.GET_META_DATA)

        val metaData = applicationInfo?.metaData
        val appKey = when (val value = metaData?.get("Ali_Push_App_Key")) {
            is String -> value
            is Int -> value.toString()
            else -> null
        }

        val appSecret = when (val value = metaData?.get("Ali_Push_App_Secret")) {
            is String -> value
            is Int -> value.toString()
            else -> null
        }

        val app = appContext.reactContext?.applicationContext as Application
        PushServiceFactory.init(PushInitConfig.Builder()
            .application(app)
            .appKey(appKey)
            .appSecret(appSecret)
            .build())
        val service = PushServiceFactory.getCloudPushService()
        service.setDebug(true)
        service.setLogLevel(CloudPushService.LOG_DEBUG)
        service.setPushIntentService(MyMessageIntentService::class.java)
        createNotificationChannel()
        /*initOthers()*/
        service.register(app, object : CommonCallback {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "Device registered successfully: $result")
                val deviceId = service?.deviceId
                Log.d(TAG, "Device ID: $deviceId")
            }

            override fun onFailed(code: String?, message: String?) {
                Log.e(TAG, "Device registration failed: code=$code, message=$message")
            }
        })
    }

    /**
     * 初始化通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 使用 appContext.reactContext 获取 Context
            val context = appContext.reactContext ?: return
            val mNotificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val mChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            // 配置通知渠道的属性
            mChannel.description = CHANNEL_DESC
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true)
            mChannel.setVibrationPattern(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel)
        }
    }

    /*private fun initOthers() {
        val app = appContext.reactContext?.applicationContext as Application
        HuaWeiRegister.register(app) // 接入华为辅助推送
        HonorRegister.register(app)  // 荣耀推送
        MiPushRegister.register(app, getMetaValue("Xiaomi_App_Id"), getMetaValue("Xiaomi_App_Key")) // 初始化小米辅助推送
        VivoRegister.registerAsync(appContext.reactContext) // 接入vivo辅助推送
        OppoRegister.registerAsync(appContext.reactContext, getMetaValue("Oppo_App_Key"), getMetaValue("Oppo_App_Secret")) // OPPO辅助推送
        MeizuRegister.registerAsync(appContext.reactContext, getMetaValue("Meizu_App_Id"), getMetaValue("Meizu_App_Key")) // 接入魅族辅助推送
    }

    private fun getMetaValue(name: String): String? {
        val applicationInfo = appContext?.reactContext?.packageManager?.getApplicationInfo(appContext?.reactContext?.packageName.toString(), PackageManager.GET_META_DATA)
        val metaData = applicationInfo?.metaData
        return when (val value = metaData?.get(name)) {
            is String -> value
            is Int -> value.toString()
            else -> null
        }
    }*/

    companion object {
        private const val NOTIFICATION_OPENED_EVENT = "onNotificationOpened"
        const val TAG = "ExpoAliyunPushNotifications"
        const val CHANNEL_ID = "dendenmushi_aliyun_push_channel"
        const val CHANNEL_NAME = "dendenmushi_with_aliyun_push_channel"
        const val CHANNEL_DESC = "dendenmushi app notification"
    }
}
