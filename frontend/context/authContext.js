import React, { createContext, useEffect, useMemo, useReducer } from 'react';
import { useAuthRequest, useAutoDiscovery } from 'expo-auth-session';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { get } from 'react-native/Libraries/TurboModule/TurboModuleRegistry';

const initialState = {
  isSignedIn: false,
  accessToken: null,
  idToken: null,
  refreshToken: null,
  userInfo: null,
};

const AuthContext = createContext({
  state: initialState,
  signIn: () => {},
  signOut: () => {},
  refreshToken: async () => {},
  getUserProfile: async () => {},
  getKeyclockUserInfo: async () => {},
});

const AuthProvider = ({ children }) => {
  const discovery = useAutoDiscovery(process.env.EXPO_PUBLIC_KEYCLOAK_URL);
  const redirectUri = 'myapp://signin';
  const [request, response, promptAsync] = useAuthRequest(
    {
      clientId: process.env.EXPO_PUBLIC_KEYCLOAK_CLIENT_ID,
      redirectUri: redirectUri,
      scopes: ['openid', 'profile'],
    },
    discovery
  );

  const [authState, dispatch] = useReducer((previousState, action) => {
    switch (action.type) {
      case 'SIGN_IN':
        return {
          ...previousState,
          isSignedIn: true,
          accessToken: action.payload.access_token,
          idToken: action.payload.id_token,
          refreshToken: action.payload.refresh_token,
        };
      case 'USER_INFO':
        return {
          ...previousState,
          userInfo: action.payload,
        };
      case 'KEYCLOCK_USER_INFO':
        return {
          ...previousState,
          keyclockUserInfo: action.payload,
        };
      case 'SIGN_OUT':
        return {
          initialState,
        };
    }
  }, initialState);

  const storeTokens = async (accessToken, refreshToken) => {
    try {
      await AsyncStorage.setItem('accessToken', accessToken);
      await AsyncStorage.setItem('refreshToken', refreshToken);
      console.log('Tokens stored successfully');
    } catch (error) {
      console.error('Error storing tokens:', error);
    }
  };

  const getToken = async ({ code, codeVerifier, redirectUri }) => {
    try {
      const formData = {
        grant_type: 'authorization_code',
        client_id: process.env.EXPO_PUBLIC_KEYCLOAK_CLIENT_ID,
        code: code,
        code_verifier: codeVerifier,
        redirect_uri: redirectUri,
        scope: 'openid offline_access',
      };
      const formBody = [];
      for (const property in formData) {
        var encodedKey = encodeURIComponent(property);
        var encodedValue = encodeURIComponent(formData[property]);
        formBody.push(encodedKey + '=' + encodedValue);
      }
      console.log('BODDDY', formBody);
      const response = await fetch(
        `${process.env.EXPO_PUBLIC_KEYCLOAK_URL}/protocol/openid-connect/token`,
        {
          method: 'POST',
          headers: {
            Accept: 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: formBody.join('&'),
        }
      );
      if (response.ok) {
        const payload = await response.json();
        console.log('TOKEN ', payload);
        await storeTokens(payload.access_token, payload.refresh_token);
        // @ts-ignore
        dispatch({ type: 'SIGN_IN', payload });
      }
    } catch (e) {
      console.warn('Error in getToken:', e);
    }
  };

  const authContext = useMemo(
    () => ({
      state: authState,
      signIn: async () => {
        try {
          console.log('Prompting for authentication...');
          const result = await promptAsync();

          if (result.type === 'success') {
            const { code } = result.params;
            console.log('Obtaining token...');
            await getToken({
              code,
              codeVerifier: request?.codeVerifier,
              redirectUri,
            });
            await authContext.getUserProfile();
            await authContext.getKeyclockUserInfo();
            return true;
          }
          console.log('Sign-in unsuccessful');
          return false;
        } catch (error) {
          console.error('Error during sign-in:', error);
          return false;
        }
      },
      signOut: async () => {
        try {
          const idToken = authState.idToken;
          await fetch(
            `${process.env.EXPO_PUBLIC_KEYCLOAK_URL}/protocol/openid-connect/logout?id_token_hint=${idToken}`
          );
          await AsyncStorage.removeItem('accessToken');
          await AsyncStorage.removeItem('refreshToken');
          dispatch({ type: 'SIGN_OUT' });
          console.log('Signed out and tokens cleared');
        } catch (e) {
          console.warn('Error in signOut:', e);
        }
      },
      refreshToken: async () => {
        try {
          console.log('Refreshing token...');
          const rfToken = await AsyncStorage.getItem('refreshToken');
          if (!rfToken) {
            console.log('No refresh token found');
            return false;
          }

          const response = await fetch(
            `${process.env.EXPO_PUBLIC_KEYCLOAK_URL}/protocol/openid-connect/token`,
            {
              method: 'POST',
              headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
              },
              body: new URLSearchParams({
                grant_type: 'refresh_token',
                client_id: process.env.EXPO_PUBLIC_KEYCLOAK_CLIENT_ID,
                refresh_token: rfToken,
              }).toString(),
            }
          );

          if (response.ok) {
            const payload = await response.json();
            await storeTokens(payload.access_token, payload.refresh_token);
            dispatch({ type: 'SIGN_IN', payload });
            return true;
          }
          console.log('Token refresh failed');
          return false;
        } catch (error) {
          console.error('Error refreshing token:', error);
          return false;
        }
      },
      getUserProfile: async () => {
        try {
          const accessToken = await AsyncStorage.getItem('accessToken');
          const formattedAccessToken = accessToken?.replace(/^"(.*)"$/, '$1');
          const response = await fetch('http://204.216.223.231:8080/user/profile/get', {
            method: 'GET',
            headers: {
              'Content-Type': 'application/json',
              Authorization: 'Bearer ' + formattedAccessToken,
            },
          });
          if (response.ok) {
            const data = await response.json();
            dispatch({ type: 'USER_INFO', payload: data });
            return { exists: true, data };
          } else {
            return { exists: false };
          }
        } catch (error) {
          console.error('Error fetching profile:', error);
          return { exists: false, error };
        }
      },
      getKeyclockUserInfo: async () => {
        try {
          const accessToken = authState.accessToken;
          console.log(accessToken);
          const response = await fetch(
            `${process.env.EXPO_PUBLIC_KEYCLOAK_URL}/protocol/openid-connect/userinfo`,
            {
              method: 'GET',
              headers: {
                Authorization: 'Bearer ' + accessToken,
                ContentType: 'application/json',
              },
            }
          );
          if (response.ok) {
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
              const payload = await response.json();
              console.log('Keycloak user info:', payload);
              dispatch({ type: 'KEYCLOAK_USER_INFO', payload });
              return payload;
            } else {
              console.error('Expected JSON response, but got:', contentType);
              throw new Error('Invalid response format');
            }
          } else {
            const errorText = await response.text();
            console.error('Error response:', errorText);
            throw new Error(`HTTP error! status: ${response.status}, details: ${errorText}`);
          }
        } catch (e) {
          console.warn(e);
        }
      },
    }),
    [authState, promptAsync, request?.codeVerifier]
  );

  useEffect(() => {
    if (response?.type === 'success') {
      const { code } = response.params;
      getToken({
        code,
        codeVerifier: request?.codeVerifier,
        redirectUri,
      });
    } else if (response?.type === 'error') {
      console.warn('Authentication error: ', response.error);
    } else if (response?.type === 'cancel') {
      console.log('Authentication dismissed');
      setTimeout(() => {
        promptAsync();
      }, 500);
    }
  }, [dispatch, redirectUri, request?.codeVerifier, response]);

  useEffect(() => {
    if (authState.isSignedIn) {
      authContext.getKeyclockUserInfo();
      authContext.getUserProfile();
    }
  }, [authState.accessToken, authState.isSignedIn, dispatch]);

  return <AuthContext.Provider value={authContext}>{children}</AuthContext.Provider>;
};

export { AuthContext, AuthProvider };
