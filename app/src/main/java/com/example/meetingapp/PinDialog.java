package com.example.meetingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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
    private SharedPreferences mSharedPreferences;
    private final String DEFAULT_VALUE = "Вход не произведен";
    private int selectedValue;

    String mValue;

    public PinDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setPersistent(false);
        setDialogLayoutResource(R.layout.dialog_text);
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
                selectedValue = which;
                mValue= username.getText().toString();
                preferences.edit().putString(APP_PREFERENCES_PASSWORD, password.getText().toString()).commit();
                preferences.edit().putString(APP_PREFERENCES_NAME, username.getText().toString()).commit();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedValue=which;
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
        if(selectedValue==DialogInterface.BUTTON_POSITIVE)
        {
            //persistBoolean(!getPersistedBoolean(true));
            String user = username.getText().toString();
            if(!user.equals("")) {
                setSummary(user);
                persistString(mValue);
            }
        }


//        String user = username.getText().toString();
//        if(!user.equals("")) {
//            setSummary(user);
//            persistString(mValue);
//        }
     //   Log.d("PIN_DIALOG", "# onDialogClosed: " + positiveResult);
    }
    protected void onSaveUsername(String user) {
        persistString(user != null ? user : "");
    }
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // Log.d(TAG, "boolean: " + restorePersistedValue + " object: " + defaultValue);
        if (restorePersistedValue) {
            mValue = getPersistedString(DEFAULT_VALUE);
        }
        else {
            mValue = (String) defaultValue;
            persistString(mValue);
        }
    }



}
