package com.example.meetingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by Дамир on 14.11.2015.
 */
public class FragmentSettings extends PreferenceFragment {
    final static private String login= "login";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_headers);
        PinDialog dialog = (PinDialog) findPreference(login);
    }
}
