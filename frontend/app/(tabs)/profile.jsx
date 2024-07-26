import React, { useContext, useEffect, useState } from 'react';
import { View, ScrollView, Text, Image, TouchableOpacity, StyleSheet } from 'react-native';
import { Icon } from 'react-native-elements';
import { styled } from 'nativewind';
import TextandIconChip from '../../components/TextandIconChip';
import { SafeAreaView } from 'react-native-safe-area-context';
import { BlurView } from 'expo-blur';
import { router } from 'expo-router';
import { AuthContext } from '../../context/authContext';
import { StatusBar } from 'expo-status-bar';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { blank_profile } from '../../assets/images/blank_propic.png';

const Profile = () => {
  const accessToken = AsyncStorage.getItem('accessToken');
  const [profilePicUrl, setProfilePicUrl] = useState('');
  const { state, signOut } = useContext(AuthContext);

  const user = {
    profileImage: profilePicUrl,
    username: state.userInfo?.username,
    name: state.userInfo?.username,
    location: state.userInfo?.location || 'Undefined',
    description: state.userInfo?.description || 'No description provided',
    initialGenres: state.userInfo?.genres,
    initialInstruments: state.userInfo?.instruments,
    initialAuditionTrack: {
      id: '1',
      title: 'Serenata punk',
      artist: 'Artist Name',
      artwork: 'https://picsum.photos/300',
      url: '../../assets/audio.mp3',
    },
    platforms: {
      spotify: state.userInfo?.spotify,
      soundcloud: state.userInfo?.soundcloud,
      youtube: state.userInfo?.youtube,
      tidal: state.userInfo?.tidal,
      appleMusic: state.userInfo?.appleMusic,
      amazonMusic: state.userInfo?.amazonMusic,
    },
  };

  const handleDeleteTrack = (index) => {
    setAuditionTracks((prevTracks) => prevTracks.filter((_, i) => i !== index));
  };

  const fetchProfilePic = async () => {
    try {
      const response = await fetch('http://204.216.223.231:8080/user/profile/picture', {
        method: 'GET',
        headers: {
          'Content-Type': 'image/png',
          Authorization: `Bearer ${state.accessToken}`,
        },
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.blob();
      if (!data) {
        throw new Error('Failed to create blob from response');
      }
      const imageUrl = URL.createObjectURL(data);
      setProfilePicUrl(imageUrl);
    } catch (e) {
      console.log('Error fetching profile picture: ', e);
    }
  };

  useEffect(() => {
    console.log('User data', state.userInfo);
    fetchProfilePic();

    return () => {
      if (profilePicUrl) {
        URL.revokeObjectURL(profilePicUrl);
      }
    };
  }, []);

  const [auditionTracks, setAuditionTracks] = useState(user.initialAuditionTrack);

  return (
    <ScrollView className="bg-background-default">
      <SafeAreaView className=" bg-background-default">
        <View className="relative">
          {/* <Image source={blank_profile} className="w-full h-56 rounded-[30px]" /> */}
          <Image
            source={require('../../assets/images/blank_propic.png')}
            className="w-full h-56 rounded-[30px]"
          />

          <TouchableOpacity className="absolute top-2 right-2 ">
            <BlurView
              intensity={100}
              className="p-4 rounded-full overflow-hidden bg-background-default"
              experimentalBlurMethod="true"
            >
              <Icon name="edit" type="material" color="#F0ECF7" />
            </BlurView>
          </TouchableOpacity>
          <TouchableOpacity className="absolute bottom-2 right-2">
            <BlurView
              intensity={100}
              className="p-4 rounded-full overflow-hidden bg-grayopacity50"
              experimentalBlurMethod="true"
            >
              <Icon name="flip-camera-ios" type="material" color="#F0ECF7" />
            </BlurView>
          </TouchableOpacity>
          <BlurView
            intensity={100}
            className="absolute p-4 rounded-full overflow-hidden bottom-2 left-2"
            experimentalBlurMethod="true"
          >
            <Text className="text-text font-psemibold">@{user.username}</Text>
          </BlurView>
        </View>
        <View className="p-4 ">
          <View className="flex-row justify-between items-center mb-2 ">
            <Text className="text-text text-3xl font-pbold mr-2">{user.name}</Text>
            <View className="flex-row items-center">
              <Icon name="location-on" type="material" color="#fff" />
              <Text className="text-text text-base font-psemibold ml-1">{user.location}</Text>
            </View>
          </View>
          <Text className="text-text text-xl font-pbold mb-1">Description</Text>
          <Text className="text-text mb-4 text-base font-pregular">{user.description}</Text>
          <Text className="text-text font-pbold text-xl mb-2">Played genres</Text>
          <ScrollView
            horizontal={true}
            showsHorizontalScrollIndicator={false}
            className="flex-row mb-4 overflow-visible"
          >
            {user.initialGenres?.map((genre, index) => (
              <TextandIconChip key={index} text={genre} textStyle={'font-pregular text-base'} />
            ))}
          </ScrollView>
          <Text className="text-text text-xl font-pbold mb-2">Played instruments</Text>
          <ScrollView
            className="flex-row mb-4 overflow-visible"
            horizontal={true}
            showsHorizontalScrollIndicator={false}
          >
            {user.initialInstruments?.map((instrument, index) => (
              <TextandIconChip
                key={index}
                text={instrument}
                textStyle={'font-pregular text-base'}
              />
            ))}
          </ScrollView>
          <Text className="text-text text-xl font-pbold mb-2">Audition tracks</Text>
          <ScrollView
            className="flex-row overflow-visible"
            horizontal
            showsHorizontalScrollIndicator={false}
          >
            <View className="flex-row flex-wrap bg-secondary-opacity50 p-3 rounded-3xl mr-2">
              <Image
                source={{ uri: user.initialAuditionTrack.artwork }}
                className="w-16 h-16 rounded-xl mr-2"
              />
              <View className="flex-col min-w-[180px]">
                <Text className="text-text text-lg mb-2 ml-1 font-psemibold">Serenata punk</Text>
                <View className="flex-row items-center">
                  <TouchableOpacity>
                    <Icon name="play-arrow" type="material" color="#fff" />
                  </TouchableOpacity>
                  <View className="mx-2 flex-1 bg-secondary-opacity50 h-2 rounded-full">
                    <View
                      className="absolute top-0 left-0 h-full bg-accent rounded-full"
                      style={{ width: '50%' }}
                    />
                  </View>
                </View>
              </View>
            </View>
            {/* <ProfileAudioCard track={initialAuditionTrack} onDelete={handleDeleteTrack} /> */}
          </ScrollView>
          <Text className="text-text text-xl font-pbold mb-2 mt-4">Other platforms</Text>
          <View className="flex-row mb-4">
            {user.platforms.spotify && (
              <TouchableOpacity className="mr-2">
                <Image
                  source={require('../../assets/images/platformIcons/spotify.png')}
                  className="w-14 h-14 rounded-full"
                />
              </TouchableOpacity>
            )}
            {user.platforms.tidal && (
              <TouchableOpacity className="mr-2">
                <Image
                  source={require('../../assets/images/platformIcons/tidal.jpeg')}
                  className="w-14 h-14 rounded-full"
                />
              </TouchableOpacity>
            )}
            {user.platforms.appleMusic && (
              <TouchableOpacity className="mr-2">
                <Image
                  source={require('../../assets/images/platformIcons/applemusic.jpg')}
                  className="w-14 h-14 rounded-full"
                />
              </TouchableOpacity>
            )}
            {user.platforms.amazonMusic && (
              <TouchableOpacity className="mr-2">
                <Image
                  source={require('../../assets/images/platformIcons/amazonmusic.png')}
                  className="w-14 h-14 rounded-full"
                />
              </TouchableOpacity>
            )}
            {user.platforms.soundcloud && (
              <TouchableOpacity className="mr-2">
                <Image
                  source={require('../../assets/images/platformIcons/soundcloud.png')}
                  className="w-14 h-14 rounded-full"
                />
              </TouchableOpacity>
            )}
            {user.platforms.youtube && (
              <TouchableOpacity>
                <Image
                  source={require('../../assets/images/platformIcons/youtube.png')}
                  className="w-14 h-14 rounded-full"
                />
              </TouchableOpacity>
            )}
            {!user.platforms.spotify &&
              !user.platforms.tidal &&
              !user.platforms.appleMusic &&
              !user.platforms.amazonMusic &&
              !user.platforms.soundcloud &&
              !user.platforms.youtube && (
                <Text className="text-text text-base font-pmedium italic">
                  No platforms available
                </Text>
              )}
          </View>
          <TouchableOpacity className="bg-gray-400 py-3 rounded-full flex-row justify-center items-center mt-4">
            <Text className="text-white font-pbold mr-2">Edit profile</Text>
            <Icon name="edit" type="material" color="white" />
          </TouchableOpacity>
          <TouchableOpacity
            className="bg-red-700 py-3 rounded-full mt-4 flex-row justify-center items-center"
            onPress={() => {
              signOut();
              state.isSignedIn = false;
              router.replace('');
            }}
          >
            <Text className="text-white font-pbold mr-2">Log out</Text>
            <Icon name="logout" type="material" color="white" />
          </TouchableOpacity>
        </View>
      </SafeAreaView>

      <StatusBar style="light" />
    </ScrollView>
  );
};

export default styled(Profile);
