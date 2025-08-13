import ExpoModulesCore
import CloudPushSDK
import UserNotifications
import UIKit

// 实现 AppDelegate 订阅者协议
public class AliyunPushAppDelegateSubscriber: ExpoAppDelegateSubscriber, UNUserNotificationCenterDelegate, UIApplicationDelegate  {


  public func application(_ application: UIApplication,
                          didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    CloudPushSDK.registerDevice(deviceToken) { result in
      print("DeviceToken 上报" + (result.success ? "成功" : "失败"))
    }
  }
  public func userNotificationCenter(_ center: UNUserNotificationCenter,
                                     willPresent notification: UNNotification,
                                     withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
    print("收到前台通知回调")
    handleUserInfo(userInfo: notification.request.content.userInfo)

    // 设置通知展示方式
    completionHandler([.alert, .sound])
    //completionHandler(.newData)
  }

  public func userNotificationCenter(_ center: UNUserNotificationCenter,
                                     didReceive response: UNNotificationResponse,
                                     withCompletionHandler completionHandler: @escaping () -> Void) {
    print("收到点击通知回调")
    handleUserInfo(userInfo: response.notification.request.content.userInfo)
    completionHandler()
  }

  public func application(_ application: UIApplication,
                          didReceiveRemoteNotification userInfo: [AnyHashable : Any],
                          fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
    print("收到静默通知回调")
    handleUserInfo(userInfo: userInfo)
    completionHandler(.newData)
  }
  public func handleUserInfo(userInfo: [AnyHashable : Any]) {
    // 通过字典获取通知携带的自定义kv，例如：
    // let customValue = userInfo["customKey"] as? String

    guard let aps = userInfo["aps"] as? [String: Any],
    let alert = aps["alert"] as? [String: String],
    let title = alert["title"],
    let body = alert["body"] else {
      return
    }
    EventManager.shared.sendEvent("onNotificationOpened", userInfo as? [String: Any] ?? [:])
    // 通知点击上报
    CloudPushSDK.sendNotificationAck(userInfo)
  }


}
