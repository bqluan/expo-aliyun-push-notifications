import { NativeModule, requireNativeModule } from "expo";

import { ExpoAliyunPushModuleEvents } from "./ExpoAliyunPushNotifications.types";

declare class ExpoAliyunPushModule extends NativeModule<ExpoAliyunPushModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
  init(): Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoAliyunPushModule>("ExpoAliyunPushNotifications");
