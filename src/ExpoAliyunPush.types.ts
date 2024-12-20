export type OnLoadEventPayload = {
  url: string;
};

export type ExpoAliyunPushModuleEvents = {
  onChange: (params: ChangeEventPayload) => void;
};

export type ChangeEventPayload = {
  value: string;
};
