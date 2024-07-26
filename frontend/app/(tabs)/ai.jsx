import { View, Text, TouchableOpacity, Animated, Image } from 'react-native';
import React, { useEffect, useState, useContext } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
//import osc from 'expo-osc';
import { Icon } from 'react-native-elements';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as StompJS from '@stomp/stompjs';
import { TextDecoder, TextEncoder } from 'text-encoding';
import AudioUpload from '../../components/AudioUpload';
import { AuthContext } from '../../context/authContext';
import { Audio } from 'expo-av';
import AnimatedLoader from 'react-native-animated-loader';

global.TextDecoder = global.TextDecoder || TextDecoder;
global.TextEncoder = global.TextEncoder || TextEncoder;

const Ai = () => {
  //const [stompClient, setStompClient] = useState(null);
  const [isRecording, setIsRecording] = useState(false);
  const [micRecording, setMicRecording] = useState();
  const [permissionResponse, requestPermission] = Audio.usePermissions();
  const [sound, setSound] = useState();
  const [recordingUri, setRecordingUri] = useState(null);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [analysisResults, setAnalysisResults] = useState(false);
  const [analysisType, setAnalysisType] = useState('');
  const [queryResult, setQueryResult] = useState('');

  const { state } = useContext(AuthContext);
  const songAnalysis = {
    genres: ['rock', 'classic rock', 'blues'],
    mood: 'chillout',
    bpm: 119,
    danceability: 1.0476913,
  };

  const formattedString = `Genres: ${songAnalysis.genres.join(', ')}
Mood: ${songAnalysis.mood}
BPM: ${songAnalysis.bpm}
Danceability: ${songAnalysis.danceability.toFixed(2)}`;

  const result = [
    {
      amazonMusic: null,
      appleMusic: null,
      birthdate: [2000, 4, 10],
      description:
        'Lead vocalist of Led Zeppelin, known for his powerful voice and charismatic performances.',
      firstName: null,
      genres: ['Rock'],
      instruments: ['Trumpet'],
      profilePicturePath: null,
      requestID: null,
      soundcloud: null,
      spotify: null,
      surname: null,
      tidal: null,
      userId: 'test_19c5dfd5-1cbf-4020-aa40-6f511b5a201e',
      username: 'Robert Plant',
      youtube: null,
    },
  ];

  useEffect(() => {
    return sound
      ? () => {
          console.log('Unloading Sound');
          sound.unloadAsync();
        }
      : undefined;
  }, [sound]);

  async function startRecording() {
    setAnalysisType('mic');
    if (sound) {
      console.log('Stopping and unloading existing recording...');
      setSound(undefined); // Assicurati di resettare lo stato
    }
    setMicRecording(true);
    try {
      if (permissionResponse.status !== 'granted') {
        console.log('Requesting permission..');
        await requestPermission();
      }
      await Audio.setAudioModeAsync({
        allowsRecordingIOS: true,
        playsInSilentModeIOS: true,
      });

      console.log('Starting recording..');
      const { recording } = await Audio.Recording.createAsync(
        Audio.RecordingOptionsPresets.HIGH_QUALITY
      );
      setMicRecording(recording);
      console.log('Recording started');
    } catch (err) {
      console.error('Failed to start recording', err);
    }
  }

  async function stopRecording() {
    setAnalysisType('');
    setMicRecording(false);
    console.log('Stopping recording..');
    setMicRecording(undefined);
    await micRecording.stopAndUnloadAsync();
    await Audio.setAudioModeAsync({
      allowsRecordingIOS: false,
    });
    const uri = micRecording.getURI();
    const { sound } = await Audio.Sound.createAsync({ uri });
    setSound(sound);
    setRecordingUri(uri);
    console.log('Recording stopped and stored at', uri);
  }

  async function playSound() {
    const { sound } = await Audio.Sound.createAsync({ uri: recordingUri });
    console.log('Loading Sound');
    await sound.playAsync();
    console.log('Sound played Sound');
    setRecordingUri(null);
  }

  let client, ws;

  function connectOk() {
    client.subscribe('/user/queue/analysis/result', function (message) {
      console.log('Received: ' + message.body);
      setResults(message.body);
    });
    client.subscribe('/user/queue/query/result', function (message) {
      console.log('Received: ' + message.body);
      setQueryResult(message.body);
    });

    // client.subscribe('/queue/test', function (message) {
    //   console.log('RICEVUTO: ' + JSON.stringify(message.body));
    //   setQueryResult(message.body);
    // });

    client.publish({ destination: '/test', body: 'Hello, STOMP' });
  }

  const connect = async () => {
    ws = new WebSocket('ws://204.216.223.231:8080/analysis', [], {
      headers: {
        Authorization: `Bearer ${state.accessToken}`,
      },
    });

    client = new StompJS.Client({
      webSocketFactory: () => ws,
      debug: (msg) => {
        console.log(msg);
      },
      reconnectDelay: null,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });
    client.onConnect = connectOk;
    client.onStompError = function (frame) {
      console.log('Broker reported error: ' + frame.headers['message']);
      console.log('Additional details: ' + frame.body);
    };
    client.onWebSocketClose = function (evt) {
      console.log('Websocket closed!');
      console.log(evt);
    };
    client.activate();
  };

  const disconnect = async () => {
    try {
      if (client && client.connected) {
        console.log('Disconnecting STOMP client...');
        await client.deactivate();
        console.log('STOMP client disconnected successfully');
      } else {
        console.log('STOMP client is not connected');
      }

      if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
        console.log('Closing WebSocket connection...');
        ws.close();
        console.log('WebSocket connection closed');
      } else {
        console.log('WebSocket is not open');
      }
    } catch (error) {
      console.error('Error during disconnect:', error);
    }
  };
  const sendStart = async () => {
    setAnalysisType('si');
    setAnalysisResults(false);
    setIsAnalyzing(false);
    setIsRecording(true);
    let value = '';
    try {
      value = await AsyncStorage.getItem('clientId');
      if (value !== null) {
        const parts = value.split('/');
        clientId = parts[1];
        //osc.sendMessage(`/${clientId}/`, [true]);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const sendStop = () => {
    setAnalysisType('');
    //osc.sendMessage(`/${clientId}/`, [false]);
    setIsRecording(false);
    setIsAnalyzing(true);
    setTimeout(() => {
      setIsAnalyzing(false);
      setAnalysisResults(true);
    }, 4000);
  };

  const [fadeAnim] = useState(new Animated.Value(0)); // Inizialmente invisibile

  useEffect(() => {
    if (analysisResults) {
      // Fai partire l'animazione quando analysisResults è true
      Animated.timing(fadeAnim, {
        toValue: 1, // Fai diventare la sezione completamente visibile
        duration: 700, // Durata dell'animazione
        useNativeDriver: true, // Utilizza il driver nativo per le prestazioni
      }).start();
    }
  }, [analysisResults, fadeAnim]);

  return (
    <SafeAreaView className="p-4 bg-background-default h-full">
      <Text className="text-text text-3xl font-pbold mb-4">Music Insight </Text>
      {analysisType !== 'mic' ? (
        !isRecording ? (
          <TouchableOpacity
            onPress={sendStart}
            className="bg-secondary-opacity50 p-4 rounded-2xl mb-4 flex-row justify-between items-center"
          >
            <Text className="text-white font-pmedium">Record on Smart Instrument</Text>
            <Icon name="radio-button-checked" type="material" color="white" />
          </TouchableOpacity>
        ) : (
          <TouchableOpacity
            onPress={sendStop}
            className="bg-primary-default p-4 rounded-2xl mb-4 flex-row justify-between items-center"
          >
            <Text className="text-white font-pmedium">Stop recording on Smart Instument</Text>
            <Icon name="stop-circle" type="material" color="white" />
          </TouchableOpacity>
        )
      ) : null}

      {analysisType === '' ? (
        <>
          <AudioUpload />

          {
            //replace true with false to hide debug buttons
            true && (
              <View className="flex-row">
                <TouchableOpacity
                  className="bg-primary-default p-2 rounded-2xl mt-2 flex-row justify-between items-center"
                  onPress={() => {
                    connect();
                  }}
                >
                  <Text className="text-text">Socket connect</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  className="bg-primary-default p-2 rounded-2xl mt-2 ml-2 flex-row justify-between items-center"
                  onPress={() => {
                    disconnect();
                  }}
                >
                  <Text className="text-text">Socket disconnect</Text>
                </TouchableOpacity>
              </View>
            )
          }
        </>
      ) : null}

      {analysisType !== 'si' ? (
        !micRecording ? (
          <TouchableOpacity
            onPress={() => {
              startRecording();
              setIsAnalyzing(false);
              setAnalysisResults(false);
            }}
            className="bg-secondary-opacity50 p-4 rounded-2xl my-4 flex-row justify-between items-center mt-4"
          >
            <Text className="text-white font-pmedium">Start Mic Recording</Text>
            <Icon name="mic" type="material" color="white" />
          </TouchableOpacity>
        ) : (
          <TouchableOpacity
            onPress={
              //setMicRecording(false)
              stopRecording
            }
            className="bg-primary-default p-4 rounded-2xl my-4 flex-row justify-between items-center mt-4"
          >
            <Text className="text-white font-pmedium">Stop Mic Recording</Text>
            <Icon name="mic-off" type="material" color="white" />
          </TouchableOpacity>
        )
      ) : null}

      {recordingUri ? (
        <TouchableOpacity
          onPress={() => {
            playSound();
            setIsAnalyzing(true);
            setTimeout(() => {
              setIsAnalyzing(false);
              setAnalysisResults(true);
            }, 4000);
          }}
          className="bg-secondary-opacity50 p-4 rounded-2xl my-4 flex-row justify-between items-center mt-4"
        >
          <Text className="text-white font-pmedium">Replay and Analyze Recording</Text>
          <Icon name="play-arrow" type="material" color="white" />
        </TouchableOpacity>
      ) : null}

      {/* <V/.. */}

      {isAnalyzing && !analysisResults && (
        <AnimatedLoader
          visible={true}
          source={require('../../assets/analyze_animation.json')}
          animationStyle={{ width: 170, height: 170 }}
          speed={1}
        >
          <Text className="text-text font-semibold text-lg">Analyzing...</Text>
        </AnimatedLoader>
      )}
      {analysisResults && (
        <Animated.View
          style={{
            opacity: fadeAnim, // Collega l'opacità allo stato dell'animazione
          }}
        >
          <Text className="text-text text-lg font-pbold mt-4">Analysis results</Text>
          <View className="bg-secondary-opacity25 p-4 rounded-2xl mt-2">
            <Text className="text-text text-base font-pmedium">{formattedString}</Text>
          </View>
        </Animated.View>
      )}
      {!queryResult &&
        //replace results with queryResult when soket is ok
        result.map((result, index) => (
          <View
            key={index}
            className="bg-primary-opacity25 rounded-2xl p-4 mb-4 flex-row items-center"
          >
            <View className="mr-4">
              <Image
                source={require('../../assets/images/blank_propic.png')}
                className="h-16 w-16 rounded-xl"
              />
            </View>
            <View className="">
              <Text className="text-text font-pbold text-lg">{result.username}</Text>
              <Text className="text-text font-pregular text-sm">{result.genres.join(', ')}</Text>
              <Text className="text-text font-pregular text-sm">
                {result.instruments.join(', ')}
              </Text>
            </View>
          </View>
        ))}
    </SafeAreaView>
  );
};

export default Ai;
