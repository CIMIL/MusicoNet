import { View, Text, AccessibilityInfo } from 'react-native';
import { React, useState, useContext } from 'react';
import GeneralInfoSection from './(signup)/generalInfo';
import PlayedInstrumentsSection from './(signup)/playedInstruments';
import ProgressBar from '../../components/ProgressBar';
import { KeyboardAvoidingView, Platform, TouchableOpacity } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useNavigation } from '@react-navigation/native';
import { router } from 'expo-router';
import OtherPlatformsSection from './(signup)/otherPlatforms';
import GenresSection from './(signup)/favouriteGenres';
import UploadMusicSection from './(signup)/uploadMusic';
import AccountInfoSection from './(signup)/accountInfo';
import { StatusBar } from 'expo-status-bar';
import { Icon } from 'react-native-elements';
import { AuthContext } from '../../context/authContext';

const signupPage = () => {
  const [step, setstep] = useState(0);
  const { state, getKeyclockUserInfo } = useContext(AuthContext);

  const payload = getKeyclockUserInfo();
  console.log('Payload', payload);

  const [user, setUser] = useState({
    username: '',
    location: '',
    description: '',
    instruments: [],
    genres: [],
    soundcloud: '',
    youtube: '',
    spotify: '',
    appleMusic: '',
    tidal: '',
    amazonMusic: '',
  });

  var stepNames = [
    'General Info',
    'Played Instruments',
    'Genres',
    'Your Music',
    'Other Platforms',
    'Account Info',
  ];

  const navigation = useNavigation();

  const createUser = async (user) => {
    try {
      const response = await fetch('http://204.216.223.231:8080/user/profile/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${state.accessToken}`,
        },
        body: JSON.stringify(user),
      });
    } catch (e) {
      console.log('Error creating user: ', e);
    }
  };

  const handleNext = async () => {
    if (step === 4) {
      createUser(user).then(() => {
        router.push('home');
      });
    } else setstep(step + 1);
  };

  const handlePrevious = () => {
    if (step === 0) {
      router.dismiss();
      //router.push('home');
    } else setstep(step - 1);
  };

  return (
    <>
      <SafeAreaView className="bg-background-default h-full">
        <KeyboardAvoidingView
          behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
          className="flex-1 px-4"
        >
          <View className="flex-row justify-between items-center">
            <Text className="text-text font-pregular text-base mb-2">{stepNames[step]}</Text>
            <Text className="text-text font-pregular text-base mb-2">{step}/4</Text>
          </View>

          <ProgressBar step={step} totalSteps={4} />

          {step === 0 && <PlayedInstrumentsSection user={user} setUser={setUser} />}
          {step === 1 && <GenresSection user={user} setUser={setUser} />}
          {step === 2 && <UploadMusicSection user={user} setUser={setUser} />}
          {step === 3 && <OtherPlatformsSection user={user} setUser={setUser} />}
          {step === 4 && <AccountInfoSection user={user} setUser={setUser} />}

          <View className="flex-row justify-between gap-4 columns-2 mb-4 bg-transparent">
            {step !== 0 ? (
              <TouchableOpacity
                onPress={() => handlePrevious()}
                className="bg-secondary-default rounded-full px-4 h-14 justify-center items-center flex-row "
              >
                <Icon name={'west'} type="material" color="#F0ECF7" />
              </TouchableOpacity>
            ) : (
              <>
                <TouchableOpacity></TouchableOpacity>
              </>
            )}

            <TouchableOpacity
              onPress={() => handleNext()}
              className="bg-secondary-default px-4 rounded-full h-14 justify-center items-center flex-row"
            >
              {step === 5 ? (
                <Text className="text-text font-psemibold text-lg mr-2">Sign up</Text>
              ) : null}
              <Icon name={step === 5 ? 'login' : 'east'} type="material" color={'#F0ECF7'} />
            </TouchableOpacity>
          </View>
        </KeyboardAvoidingView>
      </SafeAreaView>

      <StatusBar style="light" />
    </>
  );
};

export default signupPage;
