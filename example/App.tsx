import React, { useState, useEffect, useCallback } from 'react';
import {
  StyleSheet,
  View,
  Text,
  TextInput,
  Button,
  ScrollView,
  ActivityIndicator,
  NativeModules, // Don't forget this import for NativeModules
  NativeEventEmitter, // And this one for event listeners
} from 'react-native';

// Get your native module
import RNMediapipelineModule from "RNMediapipeline";

export default function App() {
  const [status, setStatus] = useState('initializing'); // Start in initializing state
  const [statusMessage, setStatusMessage] = useState('Initializing LLM...');
  const [prompt, setPrompt] = useState('Write a poem about a cat.');
  const [response, setResponse] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);

  // Use useEffect to run initialization and set up listeners ONLY ONCE
  useEffect(() => {
    console.log("Starting LLM initialization and setting up listeners...");

    // Initialize the native module
    RNMediapipelineModule.initialize();

    // Create a NativeEventEmitter instance for your module
    // This is the correct way to listen to events emitted from Native Modules
    // const eventEmitter = new NativeEventEmitter(RNMediapipelineModule);

    const subscription2 = RNMediapipelineModule.addListener('onLLMReady', () => {
      console.log('LLM is ready!');
      setStatus('ready');
    });
  }, []); // The empty array [] means this effect runs only once after the initial render

  const handleGenerate = useCallback(async () => {
    if (status !== 'ready' || isGenerating) return;

    setIsGenerating(true);
    setResponse('Generating...');
    try {
      const result = await RNMediapipelineModule.generate(prompt);
      setResponse(result);
    } catch (e) {
      console.error(e);
      setResponse(`Error: ${e.message}`);
      setStatus('error'); // Set status to error on generation failure
      setStatusMessage(`Generation Error: ${e.message}`);
    } finally {
      setIsGenerating(false);
    }
  }, [prompt, status, isGenerating]);

  const renderStatus = () => {
    if (status === 'ready') {
      return <Text style={styles.statusReady}>{statusMessage}</Text>;
    }
    if (status === 'error') {
      return <Text style={styles.statusError}>{statusMessage}</Text>;
    }
    return (
      <View style={styles.statusContainer}>
        <ActivityIndicator size="small" color="#007AFF" />
        <Text style={styles.statusLoading}>{statusMessage}</Text>
      </View>
    );
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>RN MediaPipe LLM</Text>
      {renderStatus()}

      <TextInput
        style={styles.input}
        value={prompt}
        onChangeText={setPrompt}
        placeholder="Enter your prompt"
        multiline
        editable={status === 'ready' && !isGenerating} // Disable input while initializing or generating
      />

      <Button
        title={isGenerating ? 'Generating...' : 'Generate Response'}
        onPress={handleGenerate}
        disabled={status !== 'ready' || isGenerating}
      />

      {response && (
        <View style={styles.responseContainer}>
          <Text style={styles.responseTitle}>Response:</Text>
          <Text style={styles.responseText}>{response}</Text>
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 20,
  },
  statusContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 10,
    marginBottom: 10,
  },
  statusLoading: {
    marginLeft: 10,
    fontSize: 16,
    color: '#333',
  },
  statusReady: {
    fontSize: 16,
    color: 'green',
    textAlign: 'center',
    marginBottom: 10,
  },
  statusError: {
    fontSize: 16,
    color: 'red',
    textAlign: 'center',
    marginBottom: 10,
  },
  input: {
    backgroundColor: 'white',
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 8,
    padding: 15,
    fontSize: 16,
    minHeight: 100,
    textAlignVertical: 'top',
    marginBottom: 20,
  },
  responseContainer: {
    marginTop: 20,
    padding: 15,
    backgroundColor: 'white',
    borderRadius: 8,
    borderColor: '#e0e0e0',
    borderWidth: 1,
  },
  responseTitle: {
    fontWeight: 'bold',
    marginBottom: 5,
  },
  responseText: {
    fontSize: 16,
  },
});
