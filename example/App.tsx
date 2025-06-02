import { useEvent } from "expo";
import RNMediapipelineModule from "RNMediapipeline";
import { Button, SafeAreaView, ScrollView, Text, View } from "react-native";

export default function App() {
  // const onChangePayload = useEvent(RNMediapipeline, "onChange");

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.container}>
        <Text style={styles.header}>Module API Example</Text>
        <Group name="Constants">{/* <Text>{ExpoSettings.PI}</Text> */}</Group>
        <Group name="Functions">
          <Text>{ExpoSettings.hello()}</Text>
        </Group>
        <Group name="Async functions">
          <Button
            title="Set value"
            onPress={async () => {
              const pol = await ExpoSettings.initialize();
              console.log("model finally loaded ", pol);
            }}
          />
        </Group>
        <Group name="generate">
          <Button
            title="Set value"
            onPress={async () => {
              const pol = await ExpoSettings.generate("Just say Hey");
              console.log("value is ", pol);
            }}
          />
        </Group>
      </ScrollView>
    </SafeAreaView>
  );
}

function Group(props: { name: string; children: React.ReactNode }) {
  return (
    <View style={styles.group}>
      <Text style={styles.groupHeader}>{props.name}</Text>
      {props.children}
    </View>
  );
}

const styles = {
  header: {
    fontSize: 30,
    margin: 20,
  },
  groupHeader: {
    fontSize: 20,
    marginBottom: 20,
  },
  group: {
    margin: 20,
    backgroundColor: "#fff",
    borderRadius: 10,
    padding: 20,
  },
  container: {
    flex: 1,
    backgroundColor: "#eee",
  },
  view: {
    flex: 1,
    height: 200,
  },
};
