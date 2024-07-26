import { View, Text, TouchableOpacity } from 'react-native';
import React, { useEffect, useState } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import AsyncStorage from '@react-native-async-storage/async-storage';

const Chats = () => {
  const printTokens = async () => {
    const acTk = await AsyncStorage.getItem('accessToken');
    const rfTk = await AsyncStorage.getItem('refreshToken');
    console.log('actk: ', acTk);
    console.log('rftk: ', rfTk);
  };
  return (
    <SafeAreaView className="p-4 bg-background-default h-full">
      <Text className="text-text text-3xl font-pbold">Chat</Text>

      <TouchableOpacity onPress={printTokens}>
        <Text className="text-text">PROVA</Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
};

export default Chats;
