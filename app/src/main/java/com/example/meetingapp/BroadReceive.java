package com.example.meetingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Created by Дамир on 26.11.2015.
 */
public class BroadReceive extends BroadcastReceiver {
    public static final String APP_CODE_TASK = "codeTask";    // код задачи
    public static final String APP_PREFERENCES_NAME = "username"; // имя пользователя
    public static final String APP_PREFERENCES_PASSWORD = "password"; // пароль
    public static final String APP_PREFERENCES = "com.example.meetingapp_preferences";
    public static String STARTFOREGROUND_ACTION = "com.example.meetingapp.action.startforeground";
    public static final String APP_RECEIVER = "receiver";     // ресивер
    private SharedPreferences preferences;
    final int TASK7_BACKGROUND_RECEIVE = 7;
    String username;
    String password;
    public void onReceive(Context context, Intent intent){
        Intent i = new Intent(context, RestClientService.class);
        preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (preferences != null) {
            if (preferences.contains(APP_PREFERENCES_NAME) && preferences.contains(APP_PREFERENCES_PASSWORD)) {
                username = preferences.getString(APP_PREFERENCES_NAME, "");
                password = preferences.getString(APP_PREFERENCES_PASSWORD, "");
                i.putExtra(APP_CODE_TASK, TASK7_BACKGROUND_RECEIVE);
                i.putExtra(APP_PREFERENCES_NAME, username);
                i.putExtra(APP_PREFERENCES_PASSWORD, password);
                final ResultReceiver receiver = intent.getParcelableExtra(APP_RECEIVER);
                Log.d("BroadReceive", "onReceive receiver"+receiver);
                i.putExtra(APP_RECEIVER, receiver);
                //i.setAction(STARTFOREGROUND_ACTION);
                context.startService(i);
            }
        }

    }
}
