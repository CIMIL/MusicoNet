import { StatusBar } from 'expo-status-bar';
import { ImageBackground, Text, View } from 'react-native';
import { Redirect, router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import Button from '../components/Button';
import * as WebBrowser from 'expo-web-browser';
import { AuthContext } from '../context/authContext';
import { useContext, useEffect, useState } from 'react';
import { get } from 'react-native/Libraries/TurboModule/TurboModuleRegistry';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as SecureStore from 'expo-secure-store';

export default function App() {
  WebBrowser.maybeCompleteAuthSession();
  const { refreshToken, signOut, state } = useContext(AuthContext);
  const [isLoading, setIsLoading] = useState(false);
  const [shouldRedirect, setShouldRedirect] = useState(false);

  useEffect(() => {
    const checkAndRefreshToken = async () => {
      try {
        console.log('Checking for stored tokens...');
        const accessToken = await AsyncStorage.getItem('accessToken');
        const refToken = await AsyncStorage.getItem('refreshToken');

        console.log('Stored tokens:', {
          accessToken: accessToken ? 'exists' : 'null',
          refToken: refToken ? 'exists' : 'null',
        });

        if (accessToken || refToken) {
          console.log('Attempting to refresh token...');
          const refreshSuccess = await refreshToken();
          if (refreshSuccess) {
            console.log('Token refresh successful');
            setShouldRedirect(true);
          } else {
            console.log('Token refresh failed');
            await signOut(); // This will clear tokens
          }
        } else {
          console.log('No tokens found');
        }
      } catch (error) {
        console.error('Error during token check and refresh:', error);
        await signOut(); // Clear tokens in case of error
      } finally {
        setIsLoading(false);
      }
    };

    checkAndRefreshToken();
  }, [refreshToken, signOut]);

  if (isLoading) {
    return <Text>Loading...</Text>; // Show a loading indicator
  }

  if (shouldRedirect) {
    console.log('Redirecting to home page...');
    return <Redirect href="/home" />;
  }

  console.log('Rendering splash screen...');
  return (
    <>
      <ImageBackground
        source={require('../assets/images/splashscreen.png')}
        className="h-full w-full"
      >
        <SafeAreaView className="h-full bg-background">
          <View className="w-full h-full justify-end items-center px-4 pb-[69px]">
            <Text className="text-4xl font-pbold text-text text-center">
              Discover a new dimension for music collaboration.
            </Text>
            <Button
              title="Enter MusicoNet"
              handlePress={() => {
                router.replace('signin');
                //printTokens();
              }}
              containerStyles="mt-32 w-full bg-opacity-30"
            />
          </View>
        </SafeAreaView>
      </ImageBackground>
      <StatusBar style="light " />
    </>
  );
}
