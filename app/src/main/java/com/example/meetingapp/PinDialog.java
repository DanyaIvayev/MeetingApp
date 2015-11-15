package com.example.meetingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Дамир on 14.11.2015.
 */
public class PinDialog extends DialogPreference {
    private EditText username;
    private EditText password;
    private Button okButton;
    private Button cancelButton;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_NAME = "userName"; // имя пользователя
    public static final String APP_PREFERENCES_PASSWORD = "passwordKey"; // пароль

    public PinDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setDialogLayoutResource(R.layout.dialog_text);
        getPreferenceManager().setSharedPreferencesName(APP_PREFERENCES);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setTitle(R.string.loginTitle);
        builder.setMessage(R.string.loginText);
        builder.setIcon(R.drawable.password_dialog);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
                preferences.edit().putString(APP_PREFERENCES_PASSWORD, password.getText().toString()).commit();
                preferences.edit().putString(APP_PREFERENCES_NAME, username.getText().toString()).commit();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onBindDialogView(@NonNull View view){
        username = (EditText)view.findViewById(R.id.userText);
        password = (EditText) view.findViewById(R.id.passwordText);
        super.onBindDialogView(view);

    }

    public String getUsername(){
        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        return preferences.getString("userName", "DEFAULT");

    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if(positiveResult) {
            persistBoolean(!getPersistedBoolean(true));

        }
        String user = username.getText().toString();
        if(!user.equals(""))
            setSummary(user);
        Log.d("PIN_DIALOG", "# onDialogClosed: " + positiveResult);
    }
}
