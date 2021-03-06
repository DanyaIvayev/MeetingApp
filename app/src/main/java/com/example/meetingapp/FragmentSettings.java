package com.example.meetingapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Дамир on 14.11.2015.
 */
public class FragmentSettings extends PreferenceFragment {
    final static private String login= "login";
    public static final String APP_PREFERENCES = "com.example.meetingapp_preferences";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_headers);
        PinDialog dialog = (PinDialog) findPreference(login);
        String defaultUser = dialog.getSharedPreferences().getString(dialog.getKey(), "Вход не произведен");
        dialog.setSummary(defaultUser);

    }
}
