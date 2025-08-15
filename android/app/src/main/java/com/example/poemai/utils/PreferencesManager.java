package com.example.poemai.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferencesManager {
    private static final String TAG = "PreferencesManager";
    private static final String PREF_NAME = "PoemAIPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_API_KEY = "api_key";

    private SharedPreferences sharedPreferences;

    public PreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthToken(String token, Long userId) {
        Log.d(TAG, "保存token: " + token);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putLong(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getToken() {
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        Log.d(TAG, "获取token: " + token);
        return token;
    }

    public Long getUserId() {
        long userId = sharedPreferences.getLong(KEY_USER_ID, -1);
        Log.d(TAG, "获取userId: " + userId);
        return userId;
    }

    public void clearAuthToken() {
        Log.d(TAG, "清除认证信息");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.apply();
    }

    public void saveApiKey(String apiKey) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_API_KEY, apiKey);
        editor.apply();
    }

    public String getApiKey() {
        return sharedPreferences.getString(KEY_API_KEY, null);
    }
}