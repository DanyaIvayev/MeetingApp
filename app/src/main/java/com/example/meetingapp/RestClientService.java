package com.example.meetingapp;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Дамир on 14.11.2015.
 */
public class RestClientService extends IntentService {
    private static final String TAG = "RestClientService";
    public static final String APP_PREFERENCES_NAME = "username"; // имя пользователя
    public static final String APP_PREFERENCES_PASSWORD = "password"; // пароль
    public static final String APP_RECEIVER="receiver";
    public static final String APP_CODE_TASK="codeTask";    // код задачи
    public static final String APP_MEETING_NAME="name";     // название встречи
    public static final String APP_BEGIN_DATE="begindate";  //дата начала
    public static final String APP_END_DATE="enddate";      //дата конца
    public static final String APP_ID="id";
    final int TASK1_RECEIVE_MEETINGS = 1;
    final int TASK2_DELETE_MEETING=2;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;
    public RestClientService() {
        super("RestClientService");
    }
    String url;
    String username;
    String password;
    protected void onHandleIntent(Intent i) {
        username = i.getStringExtra(APP_PREFERENCES_NAME);
        password = i.getStringExtra(APP_PREFERENCES_PASSWORD);
        int code = i.getIntExtra(APP_CODE_TASK, -1);
        final ResultReceiver receiver = i.getParcelableExtra(APP_RECEIVER);
        switch(code){
            case TASK1_RECEIVE_MEETINGS:{
                getMeetingsRequest(receiver);
            } break;
            case TASK2_DELETE_MEETING:{
                deleteMeetingRequest(receiver, i);
            } break;

        }
    }
    private void getMeetingsRequest(final ResultReceiver receiver){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        url = getString(R.string.urlGetMeeting);
        url += "?"+APP_PREFERENCES_NAME+"=" + username + "&"+APP_PREFERENCES_PASSWORD+"=" + password;

        JsonArrayRequest request =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Bundle bundle = new Bundle();
                                JSONArray array = response;
                                if(response.toString().equals("[{\"response\":\"true\"}]")){
                                    bundle.putString(Intent.EXTRA_TEXT, "Логин и пароль указаны неверно");
                                    bundle.putInt(APP_CODE_TASK, TASK1_RECEIVE_MEETINGS);
                                    receiver.send(STATUS_ERROR, bundle);
                                } else {
                                    bundle.putString("result", response.toString());
                                    bundle.putInt(APP_CODE_TASK, TASK1_RECEIVE_MEETINGS);
                                    receiver.send(STATUS_FINISHED, bundle);
                                }
                                Log.d(TAG, "onResponse "+response.toString());

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "onErrorResponse Request failed: " + error.toString());
                            }
                        });
        request.setRetryPolicy(
                new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        queue.add(request);
    }

    private void deleteMeetingRequest(final ResultReceiver receiver, Intent i){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        url = getString(R.string.urlDeleteMeeting);
        final int id = i.getIntExtra(APP_ID, -1);
        try{
        JsonArrayRequest request =
                    new JsonArrayRequest(Request.Method.DELETE, url, null,
                            new Response.Listener<JSONArray>(){
                                @Override
                                public void onResponse(JSONArray response) {
                                    Bundle bundle = new Bundle();
                                    if(response.toString().equals("[{\"response\":\"false\"}]")){
                                        bundle.putString(Intent.EXTRA_TEXT, "Логин и пароль указаны неверно, либо ошибка на сервере");
                                        bundle.putInt(APP_CODE_TASK, TASK2_DELETE_MEETING);
                                        receiver.send(STATUS_ERROR, bundle);
                                    } else {
                                        bundle.putInt(APP_CODE_TASK, TASK2_DELETE_MEETING);
                                        receiver.send(STATUS_FINISHED, bundle);
                                    }
                                    Log.d(TAG, "onResponse "+response.toString());
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "onErrorResponse Request failed: " + error.toString());
                                }
                            })
                    {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/json");
                            headers.put("Accept-Charset", "UTF-8");
                            headers.put(APP_ID, String.valueOf(id));
                            headers.put(APP_PREFERENCES_NAME, username);
                            headers.put(APP_PREFERENCES_PASSWORD, password);
                            return headers;
                        }
                    };
            request.setRetryPolicy(
                    new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
