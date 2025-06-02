import { NativeModule, requireNativeModule } from "expo";

import { RNMediapipelineModuleEvents } from "./RNMediapipeline.types";

declare class RNMediapipelineModule extends NativeModule<RNMediapipelineModuleEvents> {

    // prints hello world
  hello(): string;
  initialize(): Promise<boolean>;
  generate(prompt: string): Promise<string>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<RNMediapipelineModule>("ReactNativeMediapipe");
