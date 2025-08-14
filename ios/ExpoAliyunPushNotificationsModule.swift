import ExpoModulesCore
import CloudPushSDK
import UserNotifications
import UIKit

public class ExpoAliyunPushNotificationsModule: Module {

  public func definition() -> ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoAliyunPushNotifications')` in JavaScript.
    OnCreate {
      // 阿里云推送初始化代码
      EventManager.shared.setModule(self)
      initPushSdk()
      setupAPNs()
      NotificationCenter.default.addObserver(self,
                                             selector: #selector(onMessageReceived(_:)),
                                             name: NSNotification.Name("CCPDidReceiveMessageNotification"),
                                             object: nil)
    }
    Name("ExpoAliyunPushNotifications")

    // Sets constant properties on the module. Can take a dictionary or a closure that returns a dictionary.
    Constants([
      "PI": Double.pi
    ])

    // Defines event names that the module can send to JavaScript.
    Events("onChange", "onNotificationOpened")

    // Defines a JavaScript synchronous function that runs the native code on the JavaScript thread.
    Function("hello") {
      return "Hello world! 👋"
    }
    // TODO
    Function("getDeviceId") {
      CloudPushSDK.getDeviceId()
      return CloudPushSDK.getDeviceId()
    }

    // Defines a JavaScript function that always returns a Promise and whose native code
    // is by default dispatched on the different thread than the JavaScript runtime runs on.
    AsyncFunction("setValueAsync") { (value: String) in
      // Send an event to JavaScript.
      self.sendEvent("onChange", [
        "value": value
      ])
    }
    Function("setIconBadgeNumberAsync") { (value: Int) in
       UIApplication.shared.applicationIconBadgeNumber = value
    }
  }

  func initPushSdk() {
    CloudPushSDK.setLogLevel(MPLogLevel.info)
    guard let appKey = Bundle.main.object(forInfoDictionaryKey: "Ali_Push_App_Key") as? String,
          let appSecret = Bundle.main.object(forInfoDictionaryKey: "Ali_Push_App_Secret") as? String else {

          return
    }

    // SDK初始化
    CloudPushSDK.start(withAppkey: appKey, appSecret: appSecret) { res in
      if res.success {
        print("SDK初始化成功 | DeviceID: \(CloudPushSDK.getDeviceId() ?? "N/A")")
      } else {
        print("初始化失败: \(res.error?.localizedDescription ?? "未知错误")")
      }
    }
  }

  // 请求用户授权
  func setupAPNs() {
    let center = UNUserNotificationCenter.current()
    center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
      DispatchQueue.main.async {
        if granted {
          UIApplication.shared.registerForRemoteNotifications()
        }
      }
    }
  }

  @objc func onMessageReceived(_ notification: Notification) {
    guard var data = notification.object as? [String: Any],
    let title = data["title"] as? String,
    let content = data["content"] as? String else {
      return
    }
    data["custom_message"] = "denden_ali_msg"
    do {
      if let contentData = content.data(using: .utf8) {
        let jsonObject = try JSONSerialization.jsonObject(with: contentData, options: [])
        if let contentDict = jsonObject as? [String: Any],

        let subtitle = contentDict["subtitle"] as? String,
        let type = contentDict["type"] as? String {
          print("type: \(type)")
          if type == "Conversation" {
            EventManager.shared.sendEvent("onNotificationOpened", data)
          } else {
            showSystemNotification(title: title, body: subtitle, userInfo: contentDict)
          }
            }
      }
        } catch {

        }
  }

  private func showSystemNotification(title: String, body: String, userInfo: [String: Any]) {
    let content = UNMutableNotificationContent()
    content.title = title
    content.body = body
    content.sound = UNNotificationSound.default
    content.userInfo = userInfo
    let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)

    let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)

    UNUserNotificationCenter.current().add(request) { error in
      if let error = error {
        print("添加通知失败: \(error)")
      } else {
        //UIApplication.shared.applicationIconBadgeNumber = UIApplication.shared.applicationIconBadgeNumber + 1
      }
    }
  }
}
