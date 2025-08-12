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
    Events("onChange","onNotificationOpened")

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
  }
    func initPushSdk() {
        CloudPushSDK.setLogLevel(MPLogLevel.info);
        // SDK初始化
        CloudPushSDK.start(withAppkey: "335582740", appSecret: "") { res in
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
                print("推送权限状态: \(granted ? "已授权" : "被拒绝")")
            }
        }
    }

    @objc func onMessageReceived(_ notification: Notification) {
        guard let data = notification.object as? [String: Any],
              let title = data["title"] as? String,
              let content = data["content"] as? String else {
            return
        }
         print("收到自定义消息")
         EventManager.shared.sendEvent("onNotificationOpened", ["title": title,"content": content])


    }
}
