import { NativeModule, requireNativeModule } from "expo";
import { RNMediapipelineModuleEvents } from "./RNMediapipeline.types";

declare class RNMediapipelineModule extends NativeModule<RNMediapipelineModuleEvents> {
    // prints hello world
    hello(): string;

    // Initialize the LLM (now with better state management)
    initialize(): Promise<boolean>;

    // Generate text using the LLM
    generate(prompt: string): Promise<string>;

    // Check if LLM is ready to use
    isReady(): boolean;

}

// This call loads the native module object from the JSI.
export default requireNativeModule<RNMediapipelineModule>("ReactNativeMediapipe");
