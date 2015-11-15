package com.example.meetingapp;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * Created by Дамир on 14.11.2015.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new FragmentSettings()).commit();
        }
    }
}
