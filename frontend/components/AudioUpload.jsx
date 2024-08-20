import React, { useState, useContext } from 'react';
import { View, Button, Text, ActivityIndicator, TouchableOpacity } from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import * as FileSystem from 'expo-file-system';
import axios from 'axios';
import { color } from 'react-native-elements/dist/helpers';
import { Icon } from 'react-native-elements';
import { LinearGradient } from 'expo-linear-gradient';
import { AuthContext } from '../context/authContext';

const AudioUpload = () => {
  const [uploading, setUploading] = useState(false);
  const [uploadStatus, setUploadStatus] = useState('');
  const { state } = useContext(AuthContext);

  const pickAudioFile = async () => {
    console.log('Picking audio file...');
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: 'audio/*',
      });

      if (result.assets) {
        console.log('Result: ', result.assets[0]);
        let { uri, name, size, mimeType } = result.assets[0];
        const file = await fetch(uri).then((response) => response.blob());
        console.log('File: ', name);
        const new_name = name + '.mp3';
        const formData = new FormData();
        formData.append('file', {
          uri: uri,
          name: new_name,
          type: mimeType,
        });
        await uploadAudio(formData);
      }
    } catch (error) {
      console.log('Error picking audio file: ', error);
    }
  };

  const uploadAudio = async (formData) => {
    setUploading(true);
    console.log('Uploading audio file...');
    console.log('Data: ', formData);

    axios
      .post('http://192.168.188.28:8080/audio/analysis', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          Authorization: `Bearer ${state.accessToken}`,
        },
      })
      .then((response) => {
        console.log(response);
        console.log('Upload response: ', response.data);
        setUploadStatus('success');
        setUploading(false);
      })
      .catch((error) => {
        // //console.error(error);
        setUploading(false);
      })
      .finally(() => {
        setUploading(false);
      });
  };
  return (
    <View className="">
      {!uploading ? (
        <TouchableOpacity
          onPress={pickAudioFile}
          className="w-full p-4 rounded-2xl flex-row justify-between items-center bg-secondary-opacity50"
        >
          <Text className="text-text font-pmedium ">Upload file to analyze</Text>
          <Icon name="upload" type="material" color="white" />
        </TouchableOpacity>
      ) : (
        <View className="flex-row justify-between items-center w-full p-4 rounded-2xl bg-primary-default">
          <Text className="text-text font-pmedium">Uploading...</Text>
          <ActivityIndicator size="small" color="#FFFFFF" />
        </View>
      )}
      {uploadStatus ? (console.log(uploadStatus), setUploadStatus('')) : null}
    </View>
  );
};

export default AudioUpload;
