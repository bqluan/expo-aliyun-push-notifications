package com.github.bqluan.aliyunpush

object EventManager {
    private var moduleInstance: ExpoAliyunPushNotificationsModule? = null

    fun setModule(module: ExpoAliyunPushNotificationsModule) {
        moduleInstance = module
    }

    fun sendEvent(eventName: String, message: String?) {
        moduleInstance?.let { module ->
            message?.let {
                module.notifyPushReceived(message)
            }
        }
    }
}
