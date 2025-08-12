import Foundation
import ExpoModulesCore

class EventManager {
    static let shared = EventManager()
    private var module: Module?

    private init() {}

    func setModule(_ module: Module) {
        self.module = module
    }

    func sendEvent(_ name: String, _ payload: [String: Any]) {
        (module as? Module)?.sendEvent(name, payload)
    }
}
