import { NativeModule, requireNativeModule } from "expo";

import { ExpoSettingsModuleEvents } from "./ExpoSettings.types";

declare class ExpoSettingsModule extends NativeModule<ExpoSettingsModuleEvents> {
  hello(): string;
  initialize(): Promise<boolean>;
  generate(prompt: string): Promise<string>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoSettingsModule>("ExpoSettings");
