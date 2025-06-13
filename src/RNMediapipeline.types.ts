export type OnLoadEventPayload = {
  url: string;
};

export type ChangeEventPayload = {
  value: string;
};

export type RNMediapipelineModuleEvents = {
  // Existing events
  onChange: (params: ChangeEventPayload) => void;

  // LLM-related events added in the Kotlin update
  onLLMResponse: (response: string) => void;
  onLLMError: (error: { error: string }) => void;
  onLLMReady: () => void;
  onLLMInitializing: () => void;
};
