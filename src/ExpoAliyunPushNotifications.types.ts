export type ExpoAliyunPushModuleEvents = {
  onChange: (params: ChangeEventPayload) => void;
  onNotificationOpened: (params: NotificationOpenedEventPayload) => void;
};

export type ChangeEventPayload = {
  value: string;
  deviceId: string;
};

export type NotificationOpenedEventPayload = {
  message: string;
};
