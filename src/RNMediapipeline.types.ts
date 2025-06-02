export type OnLoadEventPayload = {
  url: string;
};

export type RNMediapipelineModuleEvents = {
  onChange: (params: ChangeEventPayload) => void;
};

export type ChangeEventPayload = {
  value: string;
};

