
# `react-native-mediapipe-llm-inference`

React Native bindings for MediaPipe LLM Inference.
On device Inference 

## Usage

```javascript
import RNMediapipelineModule from "RNMediapipeline";

// Initialize and use the LLM


RNMediapipelineModule.initialize();

const subscription2 = RNMediapipelineModule.addListener('onLLMReady', () => {
    console.log('LLM is ready!');
    setStatus('ready');
});
}, []); 


const result = await RNMediapipelineModule.generate(prompt);
console.log(response);
```

> Note: Model path hardcoded right now
- Do not Await the initialization, blocks the UI thread.
- Event driven, use listeners for the onLLMReady event 
- text generation only, working on multimodal support 

## License

MIT
