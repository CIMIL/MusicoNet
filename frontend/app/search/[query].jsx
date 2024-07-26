import { View, Text, StatusBar, ScrollView, TouchableOpacity } from 'react-native';
import React, { useContext, useEffect, useState } from 'react';
import { router, useLocalSearchParams } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import ExpoStatusBar from 'expo-status-bar/build/ExpoStatusBar';
import { AuthContext } from '../../context/authContext';
import { Icon } from 'react-native-elements';

const Search = () => {
  const { query } = useLocalSearchParams();
  const searchData = JSON.parse(query);
  const { state } = useContext(AuthContext);
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(true);

  const searchBody = {
    minAge: searchData?.minAge,
    maxAge: searchData?.maxAge,
    genres: searchData?.genres,
    instruments: searchData?.instruments,
    multi_instrumentalism_level: searchData?.multiInstrumentalismLevel,
    gender: searchData?.gender,
  };

  const extractLastWord = (str) => {
    const parts = str.split('-');
    return parts[parts.length - 1];
  };

  const processGenres = (genres) => {
    return genres.map(extractLastWord);
  };

  const processInstruments = (instruments) => {
    return instruments.map(extractLastWord);
  };

  const fetchSearch = async () => {
    try {
      const response = await fetch('http://204.216.223.231:8080/user/search', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${state.accessToken}`,
        },
        body: JSON.stringify({
          minAge: searchBody.minAge || 18,
          maxAge: searchBody.maxAge || 100,
          genres: processGenres(searchBody.genres),
          instruments: processInstruments(searchBody.instruments),
          multi_instrumentalism_level: searchBody.multi_instrumentalism_level,
          gender: searchBody.gender,
        }),
      });

      // Gestisci la risposta qui
      if (response.ok) {
        const data = await response.json();
        setSearchResults(data);
      } else {
        console.error('Errore nella risposta:', response.status, response.statusText);
      }
    } catch (e) {
      console.log('Error fetching instruments: ', e);
    }
  };

  useEffect(() => {
    fetchSearch();
    setSearching(false);
    console.log('Search data:', searchBody);
  }, []);

  return (
    <>
      <SafeAreaView className="bg-background-default h-full">
        <ScrollView className="p-4">
          <View className="flex-row items-center mb-4">
            <TouchableOpacity onPress={() => router.back()}>
              <Icon name="arrow-back" type="material" color="#fff" />
            </TouchableOpacity>
            <Text className="text-text font-pbold text-2xl ml-4">Search Results</Text>
          </View>
          {searching ? (
            <Text className="text-text font-pregular">Searching...</Text>
          ) : (
            searchResults.map((result, index) => (
              <View key={index} className="bg-background-secondary p-4 mb-4">
                <Text className="text-text font-pbold text-lg">{result.username}</Text>
                <Text className="text-text font-pregular text-sm">{result.genres.join(', ')}</Text>
                <Text className="text-text font-pregular text-sm">
                  {result.instruments.join(', ')}
                </Text>
              </View>
            ))
          )}
          {/* Here you would typically fetch and display search results based on the query and filters */}
        </ScrollView>
      </SafeAreaView>
      <ExpoStatusBar style="light" />
    </>
  );
};

export default Search;
