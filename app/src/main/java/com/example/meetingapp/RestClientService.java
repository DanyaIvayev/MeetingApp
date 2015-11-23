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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    public static final String APP_ID="id";
    public static final String APP_LASTNAME="lastName";
    public static final String APP_FIRSTNAME="firstName";
    public static final String APP_PATONYMIC="patronymic";
    public static final String APP_POST="post";
    final int TASK1_RECEIVE_MEETINGS = 1;
    final int TASK2_DELETE_MEETING=2;
    final int TASK3_FULL_DESCRIPTION=3;
    final int TASK4_PUT_PARTICIPANT = 4;
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
                url = getString(R.string.urlGetMeeting);
                url += "?"+APP_PREFERENCES_NAME+"=" + username + "&"+APP_PREFERENCES_PASSWORD+"=" + password;
                getMeetingsRequest(receiver, url, code);
            } break;
            case TASK2_DELETE_MEETING:{
                deleteMeetingRequest(receiver, i);
            } break;
            case TASK3_FULL_DESCRIPTION:{
                final int id = i.getIntExtra(APP_ID, -1);
                url = getString(R.string.urlGetDescription);
                url += "?"+APP_PREFERENCES_NAME+"=" + username + "&"+APP_PREFERENCES_PASSWORD+"=" + password+
                "&"+APP_ID+"="+id;
                getMeetingsRequest(receiver, url, code);
            } break;
            case TASK4_PUT_PARTICIPANT:{

                putParticipant(receiver, i);
            } break;

        }
    }

    private void putParticipant(final ResultReceiver receiver, Intent i) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        int id = i.getIntExtra(APP_ID, -1);
        String firstname = i.getStringExtra(APP_FIRSTNAME);
        String lastname = i.getStringExtra(APP_LASTNAME);
        String patronymic = i.getStringExtra(APP_PATONYMIC);
        String post = i.getStringExtra(APP_POST);
        url = getString(R.string.urlAddParticipant);

        try{
            firstname = URLEncoder.encode(firstname,"UTF-8");
            lastname = URLEncoder.encode(lastname, "UTF-8");
            patronymic=URLEncoder.encode(patronymic,"UTF-8");
            post = URLEncoder.encode(post, "UTF-8");
        url += "/"+ username + "/"+ password+"/"+id+"/"+firstname
                +"/"+lastname+"/"+patronymic+"/"+post;

            JsonArrayRequest request =
                    new JsonArrayRequest(Request.Method.PUT, url, null,
                            new Response.Listener<JSONArray>(){
                                @Override
                                public void onResponse(JSONArray response) {
                                    Bundle bundle = new Bundle();
                                    if(response.toString().equals("[{\"response\":\"false\"}]")){
                                        bundle.putString(Intent.EXTRA_TEXT, "Логин и пароль указаны неверно, либо ошибка на сервере");
                                        bundle.putInt(APP_CODE_TASK, TASK4_PUT_PARTICIPANT);
                                        receiver.send(STATUS_ERROR, bundle);
                                    } else {
                                        bundle.putInt(APP_CODE_TASK, TASK4_PUT_PARTICIPANT);
                                        receiver.send(STATUS_FINISHED, bundle);
                                    }
                                    Log.d(TAG, "onResponse "+response.toString());
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "onErrorResponse Request failed: " + error.toString());
                                    Bundle bundle = new Bundle();
                                    bundle.putString(Intent.EXTRA_TEXT, "Сервер не доступен: Превышено время ожидания");
                                    bundle.putInt(APP_CODE_TASK, TASK4_PUT_PARTICIPANT);
                                    receiver.send(STATUS_ERROR, bundle);
                                }
                            }){


                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
//                            params.put(APP_PREFERENCES_NAME, username);
//                            params.put(APP_PREFERENCES_PASSWORD, password);
//                            params.put(APP_ID, String.valueOf(id));
//                            params.put(APP_LASTNAME, lastname);
//                            params.put(APP_FIRSTNAME, firstname);
//                            params.put(APP_PATONYMIC, patronymic);
//                            params.put(APP_POST, post);
                            return params;
                        }
                        @Override
                        public Map<String, String> getHeaders()  {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
//                            params.put(APP_PREFERENCES_NAME, username);
//                            params.put(APP_PREFERENCES_PASSWORD, password);
//                            params.put(APP_ID, String.valueOf(id));
//                            params.put(APP_LASTNAME, lastname);
//                            params.put(APP_FIRSTNAME, firstname);
//                            params.put(APP_PATONYMIC, patronymic);
//                            params.put(APP_POST, post);
                            return params;
                        }

                    };
//                    {
//                        protected Map<String, String> getParams() throws AuthFailureError {
//                            Map<String, String> params = new HashMap<String, String>();
//                            params.put(APP_PREFERENCES_NAME, username);
//                            params.put(APP_PREFERENCES_PASSWORD, password);
//                            params.put(APP_ID, String.valueOf(id));
//                            params.put(APP_LASTNAME, lastname);
//                            params.put(APP_FIRSTNAME, firstname);
//                            params.put(APP_PATONYMIC, patronymic);
//                            params.put(APP_POST, post);
//
//                            return params;
//                        }
//
//                        @Override
//                        public Map<String, String> getHeaders() throws AuthFailureError {
//                            HashMap<String, String> headers = new HashMap<String, String>();
//                            headers.put("Content-Type", "application/json");
//                            headers.put("Accept", "application/json");
//                            return headers;
//                        }
//                        @Override
//                        public byte[] getBody() {
//
//                            try {
//                                Log.i("json", object.toString());
//                                return object.toString().getBytes("UTF-8");
//                            } catch (UnsupportedEncodingException e) {
//                                e.printStackTrace();
//                            }
//                            return null;
//                        }
//            };
            request.setRetryPolicy(
                    new DefaultRetryPolicy(3 * 1000,  DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMeetingsRequest(final ResultReceiver receiver, String url, final int code){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest request =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Bundle bundle = new Bundle();
                                JSONArray array = response;
                                if(response.toString().equals("[{\"response\":\"false\"}]")){
                                    bundle.putString(Intent.EXTRA_TEXT, "Логин и пароль указаны неверно");
                                    bundle.putInt(APP_CODE_TASK, code);
                                    receiver.send(STATUS_ERROR, bundle);
                                } else {
                                    bundle.putString("result", response.toString());
                                    bundle.putInt(APP_CODE_TASK, code);
                                    receiver.send(STATUS_FINISHED, bundle);
                                }
                                Log.d(TAG, "onResponse "+response.toString());

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Bundle bundle = new Bundle();
                                Log.e(TAG, "onErrorResponse Request failed: " + error.toString());
                                bundle.putString(Intent.EXTRA_TEXT, "Сервер не доступен: Превышено время ожидания");
                                bundle.putInt(APP_CODE_TASK, code);
                                receiver.send(STATUS_ERROR, bundle);
                            }
                        }
                );
        request.setRetryPolicy(
                new DefaultRetryPolicy(3 * 1000,  DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
                                    Bundle bundle = new Bundle();
                                    bundle.putString(Intent.EXTRA_TEXT, "Сервер не доступен: Превышено время ожидания");
                                    bundle.putInt(APP_CODE_TASK, TASK2_DELETE_MEETING);
                                    receiver.send(STATUS_ERROR, bundle);
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
                    new DefaultRetryPolicy(3 * 1000,  DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
