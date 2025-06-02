
- bindings for expo-mediapipe-llm-inference
- text only

# `react-native-mediapipe-llm-inference`

React Native bindings for MediaPipe LLM Inference.

## Usage

```javascript
import RNMediapipelineModule from "RNMediapipeline";

// Initialize and use the LLM

await llm.initialize();
const response = await llm.generate('Hello, LLM!');
console.log(response);
```

> Note: Model path hardcoded right now

## License

MIT
