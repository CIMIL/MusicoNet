import React, { useEffect, useState, useContext } from 'react';
import { View, Text, ScrollView, TouchableOpacity } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import UserCard from '../../components/UserCard';
import { SearchBar, Icon } from 'react-native-elements';
import { usePathname, router } from 'expo-router';
import { AuthContext } from '../../context/authContext';
import FilterModal from '../../components/FilterModal';

const users = [
  {
    name: 'Christina Joe',
    username: 'christina.joe',
    profilePicture: 'https://i.pravatar.cc/150?u=christina.joe',
    location: 'New York',
    age: 25,
    mainInstrument: 'piano',
  },
  {
    name: 'Angela Rossi',
    username: 'angyrossi76',
    profilePicture: 'https://i.pravatar.cc/150?u=angyrossi76',
    location: 'Los Angeles',
    age: 48,
    mainInstrument: 'headphones',
  },
  {
    name: 'Luke Ferris',
    username: 'ferris97',
    profilePicture: 'https://i.pravatar.cc/150?u=ferris97',
    location: 'Chicago',
    age: 27,
    mainInstrument: 'mic-external-on',
  },
];

const Home = () => {
  const [showSearch, setShowSearch] = useState(false);
  const [search, setSearch] = useState('');
  const pathname = usePathname();
  const [query, setQuery] = useState('');
  const [showFilterModal, setShowFilterModal] = useState(false);
  const [filters, setFilters] = useState({});

  const updateSearch = (search) => {
    setSearch(search);
    setQuery(search);
  };

  const handleApplyFilters = (newFilters) => {
    setFilters(newFilters);
    console.log('Applied filters:', newFilters);
  };

  const navigateToSearch = () => {
    const searchParams = {
      query: query,
      ...filters,
    };
    if (pathname.startsWith('/search')) {
      router.setParams(JSON.stringify(searchParams));
    } else {
      router.push({
        pathname: `/search/${JSON.stringify(searchParams)}`,
      });
    }
  };

  return (
    <SafeAreaView className="bg-background-default px-4">
      <View className="flex-row justify-between items-center">
        <Text className="text-text text-3xl font-pbold">Feed</Text>
        <TouchableOpacity onPress={() => setShowSearch(!showSearch)}>
          <Icon name={showSearch ? 'close' : 'search'} type="material" color="#fff" />
        </TouchableOpacity>
      </View>
      {showSearch && (
        <View className="flex-row items-center">
          <SearchBar
            placeholder="Search..."
            onChangeText={updateSearch}
            value={search}
            containerStyle={{
              backgroundColor: 'transparent',
              borderWidth: 0,
              width: '90%',
            }}
            inputContainerStyle={{ backgroundColor: '#333', borderRadius: 20 }}
            inputStyle={{ color: '#fff' }}
            placeholderTextColor="#999"
            searchIcon={{ color: '#fff', style: { marginLeft: 8 } }}
            clearIcon={{ color: '#fff' }}
            autoCapitalize={false}
            autoCorrect={false}
            onCancel={() => {
              setQuery('');
              setSearch('');
            }}
            onSubmitEditing={navigateToSearch}
          />
          <TouchableOpacity onPress={() => setShowFilterModal(true)}>
            <Icon name="tune" type="material" color="#F0ECF7" />
          </TouchableOpacity>
        </View>
      )}
      <ScrollView>
        <View className="mt-4">
          {users.map((user, index) => (
            <UserCard key={index} user={user} />
          ))}
        </View>
      </ScrollView>
      <FilterModal
        isVisible={showFilterModal}
        onClose={() => setShowFilterModal(false)}
        onApplyFilters={handleApplyFilters}
      />
    </SafeAreaView>
  );
};

export default Home;
