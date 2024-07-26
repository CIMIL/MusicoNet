import React, { useState, useEffect, useContext } from 'react';
import { View, Text, Modal, TouchableOpacity, ScrollView, TextInput } from 'react-native';
import SectionedMultiSelect from 'react-native-sectioned-multi-select';
import { Icon } from 'react-native-elements';
import { AuthContext } from '../context/authContext';

const FilterModal = ({ isVisible, onClose, onApplyFilters }) => {
  const [minAge, setMinAge] = useState('18');
  const [maxAge, setMaxAge] = useState('100');
  const [selectedGenres, setSelectedGenres] = useState([]);
  const [selectedInstruments, setSelectedInstruments] = useState([]);
  const [multiInstrumentalismLevel, setMultiInstrumentalismLevel] = useState('0');
  const [selectedGender, setSelectedGender] = useState([]);

  const [items, setItems] = useState([
    {
      name: 'Genres',
      id: 0,
      children: [],
    },
    {
      name: 'Instruments',
      id: 1,
      children: [],
    },
    {
      name: 'Gender',
      id: 2,
      children: [
        { name: 'Male', id: 'male' },
        { name: 'Female', id: 'female' },
        { name: 'Non-binary', id: 'non-binary' },
        { name: 'Other', id: 'other' },
      ],
    },
  ]);

  const { state } = useContext(AuthContext);

  const fetchGenres = async () => {
    try {
      const response = await fetch('http://204.216.223.231:8080/user/data/genres', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${state.accessToken}`,
        },
      });
      const data = await response.json();
      const genres = data.map((genre, index) => ({ name: genre, id: `genre-${index}-${genre}` }));
      setItems((prevItems) => [
        {
          ...prevItems[0],
          children: genres,
        },
        ...prevItems.slice(1),
      ]);
    } catch (e) {
      console.log('Error fetching genres: ', e);
    }
  };

  const fetchInstruments = async () => {
    try {
      const response = await fetch('http://204.216.223.231:8080/user/data/instruments', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${state.accessToken}`,
        },
      });
      const data = await response.json();
      const instrumentChildren = data.map((instrument, index) => ({
        name: instrument,
        id: `instrument-${index}-${instrument}`, // This creates a unique id
      }));
      setItems((prevItems) => [
        prevItems[0],
        {
          ...prevItems[1],
          children: instrumentChildren,
        },
        ...prevItems.slice(2),
      ]);
    } catch (e) {
      console.log('Error fetching instruments: ', e);
    }
  };

  useEffect(() => {
    if (isVisible) {
      fetchGenres();
      fetchInstruments();
    }
  }, [isVisible]);

  const handleMinAgeChange = (text) => {
    const newMinAge = parseInt(text) || 0;
    setMinAge(text);
    if (newMinAge > parseInt(maxAge)) {
      setMaxAge(text);
    }
  };

  const handleMaxAgeChange = (text) => {
    const newMaxAge = parseInt(text) || 0;
    setMaxAge(text);
    if (newMaxAge < parseInt(minAge)) {
      setMinAge(text);
    }
  };

  const handleSelectChange = (selectedItems) => {
    const genres = selectedItems.filter((id) => items[0].children.some((child) => child.id === id));
    const instruments = selectedItems.filter((id) =>
      items[1].children.some((child) => child.id === id)
    );
    const gender = selectedItems.filter((id) => items[2].children.some((child) => child.id === id));

    setSelectedGenres(genres);
    setSelectedInstruments(instruments);
    setSelectedGender(gender);
  };

  const handleApply = () => {
    onApplyFilters({
      minAge: parseInt(minAge),
      maxAge: parseInt(maxAge),
      genres: selectedGenres,
      instruments: selectedInstruments,
      multiInstrumentalismLevel: parseInt(multiInstrumentalismLevel),
      gender: selectedGender[0], // Assuming single selection for gender
    });
    onClose();
  };

  return (
    <Modal animationType="slide" transparent={true} visible={isVisible} onRequestClose={onClose}>
      <View className="flex-1 justify-end">
        <View className="bg-background-default rounded-t-3xl p-5 h-5/6">
          <ScrollView>
            <Text className="text-text text-2xl font-pbold mb-4">Filters</Text>

            <View className="flex-row justify-between">
              <View className="flex-1 mr-2">
                <Text className="text-text font-pregular text-base">Min Age:</Text>
                <TextInput
                  className="bg-secondary-opacity50 text-text text-base font-pmedium pl-4 py-2 rounded-2xl mt-2"
                  value={minAge}
                  onChangeText={handleMinAgeChange}
                  keyboardType="numeric"
                />
              </View>
              <View className="flex-1 ml-2">
                <Text className="text-text font-pregular text-base">Max Age:</Text>
                <TextInput
                  className="bg-secondary-opacity50 text-text text-base font-pmedium pl-4 py-2 rounded-2xl mt-2"
                  value={maxAge}
                  onChangeText={handleMaxAgeChange}
                  keyboardType="numeric"
                />
              </View>
            </View>

            <Text className="text-text text-base mt-4 mb-2 font-pregular">
              Genres, Instruments, and Gender
            </Text>
            <SectionedMultiSelect
              IconRenderer={Icon}
              items={items}
              uniqueKey="id"
              subKey="children"
              selectText="Select options..."
              showDropDowns={true}
              readOnlyHeadings={true}
              onSelectedItemsChange={handleSelectChange}
              selectedItems={[...selectedGenres, ...selectedInstruments, ...selectedGender]}
              styles={{
                container: { backgroundColor: '#1D1B21', borderRadius: 15 },
                selectToggle: {
                  backgroundColor: '#281b46',
                  padding: 10,
                  borderRadius: 15,
                  marginBottom: 10,
                },
                selectToggleText: { color: '#F0ECF7' },
                item: { backgroundColor: '#1D1B21' },
                itemText: { color: '#F0ECF7', fontSize: 16, padding: 10 },
                selectedItem: { backgroundColor: '#281b46' },
                selectedSubItem: { backgroundColor: '#281b46' },
                separator: { backgroundColor: '#1D1B21' },
                subItemText: {
                  color: '#F0ECF7',
                  paddingLeft: 10,
                  paddingVertical: 4,
                  marginVertical: 2,
                },
                subItem: { backgroundColor: '#1D1B21' },
                searchBar: { backgroundColor: '#1D1B21' },
                searchTextInput: { color: '#F0ECF7' },
                chipContainer: { backgroundColor: '#1D1B21', borderColor: '#F0ECF7' },
                chipText: { color: '#F0ECF7' },
                confirmText: { color: '#F0ECF7' },
              }}
            />

            <Text className="text-text mt-4 mb-2 font-pmedium text-base">
              Multi-instrumentalism Level
            </Text>
            <TextInput
              className="bg-secondary-opacity50 text-text text-base pl-4 py-2 rounded-2xl font-pregular"
              value={multiInstrumentalismLevel}
              onChangeText={setMultiInstrumentalismLevel}
              keyboardType="numeric"
            />
          </ScrollView>

          <View className="flex-row justify-between mt-4">
            <TouchableOpacity onPress={onClose} className="py-3 px-5 font-pregular">
              <Text className="text-text text-base font-pmedium">Cancel</Text>
            </TouchableOpacity>
            <TouchableOpacity
              onPress={handleApply}
              className="bg-primary-default py-3 px-5 rounded-2xl"
            >
              <Text className="text-text text-base font-psemibold">Apply Filters</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
};

export default FilterModal;
