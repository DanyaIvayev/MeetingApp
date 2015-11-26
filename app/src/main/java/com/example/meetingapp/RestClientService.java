package com.example.meetingapp;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Дамир on 14.11.2015.
 */
public class RestClientService extends IntentService {
    private static final String TAG = "RestClientService";
    public static final String APP_PREFERENCES_NAME = "username"; // имя пользователя
    public static final String APP_PREFERENCES_PASSWORD = "password"; // пароль
    public static final String APP_RECEIVER = "receiver";
    public static final String APP_CODE_TASK = "codeTask";    // код задачи
    public static final String APP_ID = "id";
    public static final String APP_LASTNAME = "lastName";
    public static final String APP_FIRSTNAME = "firstName";
    public static final String APP_PATONYMIC = "patronymic";
    public static final String APP_POST = "post";
    public static final String APP_DESCRIPTION = "description";
    public static final String APP_MEETING_NAME = "name";
    public static final String APP_BEGIN_DATE = "begindate";
    public static final String APP_END_DATE = "enddate";
    public static final String APP_PRIORITY = "priority";
    String jsonFileName = "messages.json";
    final int TASK1_RECEIVE_MEETINGS = 1;
    final int TASK2_DELETE_MEETING = 2;
    final int TASK3_FULL_DESCRIPTION = 3;
    final int TASK4_PUT_PARTICIPANT = 4;
    final int TASK5_MEETING_ON_DES = 5;
    final int TASK6_SET_MEETING = 6;
    final int TASK7_BACKGROUND_RECEIVE = 7;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;
    private NotificationManager mManager;
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
        Log.d("BroadReceive", "onReceive receiver in service "+receiver);
        switch (code) {
            case TASK1_RECEIVE_MEETINGS: {
                url = getString(R.string.urlGetMeeting);
                url += "?" + APP_PREFERENCES_NAME + "=" + username + "&" + APP_PREFERENCES_PASSWORD + "=" + password;
                getMeetingsRequest(receiver, url, code);
            }
            break;
            case TASK2_DELETE_MEETING: {
                deleteMeetingRequest(receiver, i);
            }
            break;
            case TASK3_FULL_DESCRIPTION: {
                final int id = i.getIntExtra(APP_ID, -1);
                url = getString(R.string.urlGetDescription);
                url += "?" + APP_PREFERENCES_NAME + "=" + username + "&" + APP_PREFERENCES_PASSWORD + "=" + password +
                        "&" + APP_ID + "=" + id;
                getMeetingsRequest(receiver, url, code);
            }
            break;
            case TASK4_PUT_PARTICIPANT: {

                putParticipant(receiver, i);
            }
            break;
            case TASK5_MEETING_ON_DES: {
                try {
                    String description = i.getStringExtra(APP_DESCRIPTION);
                    description = URLEncoder.encode(description, "UTF-8");
                    url = getString(R.string.urlGetMeetOnDes);
                    url += "?" + APP_PREFERENCES_NAME + "=" + username + "&" + APP_PREFERENCES_PASSWORD + "=" + password
                            + "&" + APP_DESCRIPTION + "=" + description;
                    getMeetingsRequest(receiver, url, code);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "onHandleIntent " + e.getMessage());
                }
            }
            case TASK6_SET_MEETING: {
                postMeeting(receiver, i);
            }
            case TASK7_BACKGROUND_RECEIVE:{
                url = getString(R.string.urlGetMeeting);
                url += "?" + APP_PREFERENCES_NAME + "=" + username + "&" + APP_PREFERENCES_PASSWORD + "=" + password;
                getMeetingsBackground(receiver, url);
            }

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

        try {
            firstname = URLEncoder.encode(firstname, "UTF-8");
            lastname = URLEncoder.encode(lastname, "UTF-8");
            patronymic = URLEncoder.encode(patronymic, "UTF-8");
            post = URLEncoder.encode(post, "UTF-8");
            url += "/" + username + "/" + password + "/" + id + "/" + firstname
                    + "/" + lastname + "/" + patronymic + "/" + post;

            JsonArrayRequest request =
                    new JsonArrayRequest(Request.Method.PUT, url, null,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    Bundle bundle = new Bundle();
                                    if (response.toString().equals("[{\"response\":\"false\"}]")) {
                                        bundle.putString(Intent.EXTRA_TEXT, "Логин и пароль указаны неверно, либо ошибка на сервере");
                                        bundle.putInt(APP_CODE_TASK, TASK4_PUT_PARTICIPANT);
                                        receiver.send(STATUS_ERROR, bundle);
                                    } else {
                                        bundle.putInt(APP_CODE_TASK, TASK4_PUT_PARTICIPANT);
                                        receiver.send(STATUS_FINISHED, bundle);
                                    }
                                    Log.d(TAG, "onResponse " + response.toString());
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
                            }) {


                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                            return params;
                        }

                    };

            request.setRetryPolicy(
                    new DefaultRetryPolicy(3 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMeetingsRequest(final ResultReceiver receiver, String url, final int code) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest request =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Bundle bundle = new Bundle();
                                JSONArray array = response;
                                if (response.toString().equals("[{\"response\":\"false\"}]")) {
                                    bundle.putString(Intent.EXTRA_TEXT, "Логин и пароль указаны неверно");
                                    bundle.putInt(APP_CODE_TASK, code);
                                    receiver.send(STATUS_ERROR, bundle);
                                } else {
                                    bundle.putString("result", response.toString());
                                    bundle.putInt(APP_CODE_TASK, code);
                                    receiver.send(STATUS_FINISHED, bundle);
                                }
                                Log.d(TAG, "onResponse " + response.toString());

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
                new DefaultRetryPolicy(3 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    private void deleteMeetingRequest(final ResultReceiver receiver, Intent i) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        url = getString(R.string.urlDeleteMeeting);
        final int id = i.getIntExtra(APP_ID, -1);
        try {
            JsonArrayRequest request =
                    new JsonArrayRequest(Request.Method.DELETE, url, null,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    Bundle bundle = new Bundle();
                                    if (response.toString().equals("[{\"response\":\"false\"}]")) {
                                        bundle.putString(Intent.EXTRA_TEXT, "Логин и пароль указаны неверно, либо ошибка на сервере");
                                        bundle.putInt(APP_CODE_TASK, TASK2_DELETE_MEETING);
                                        receiver.send(STATUS_ERROR, bundle);
                                    } else {
                                        bundle.putInt(APP_CODE_TASK, TASK2_DELETE_MEETING);
                                        receiver.send(STATUS_FINISHED, bundle);
                                    }
                                    Log.d(TAG, "onResponse " + response.toString());
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
                            }) {
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
                    new DefaultRetryPolicy(3 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postMeeting(final ResultReceiver receiver, Intent i) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String meetingName = i.getStringExtra(APP_MEETING_NAME);
        String description = i.getStringExtra(APP_DESCRIPTION);
        String beginDate = i.getStringExtra(APP_BEGIN_DATE);
        String endDate = i.getStringExtra(APP_END_DATE);
        String priority = i.getStringExtra(APP_PRIORITY);
        url = getString(R.string.urlSetMeeting);

        try {
            meetingName = URLEncoder.encode(meetingName, "UTF-8");
            description = URLEncoder.encode(description, "UTF-8");
            beginDate = URLEncoder.encode(beginDate, "UTF-8");
            endDate = URLEncoder.encode(endDate, "UTF-8");
            priority = URLEncoder.encode(priority, "UTF-8");
            url += "/" + username + "/" + password + "/" + meetingName
                    + "/" + description + "/" + beginDate + "/" + endDate + "/" + priority;

            JsonArrayRequest request =
                    new JsonArrayRequest(Request.Method.POST, url, null,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    Bundle bundle = new Bundle();
                                    if (response.toString().equals("[{\"response\":\"false\"}]")) {
                                        bundle.putString(Intent.EXTRA_TEXT, "Логин и пароль указаны неверно, либо ошибка на сервере");
                                        bundle.putInt(APP_CODE_TASK, TASK6_SET_MEETING);
                                        receiver.send(STATUS_ERROR, bundle);
                                    } else {
                                        bundle.putInt(APP_CODE_TASK, TASK6_SET_MEETING);
                                        receiver.send(STATUS_FINISHED, bundle);
                                    }
                                    Log.d(TAG, "onResponse " + response.toString());
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "onErrorResponse Request failed: " + error.toString());
                                    Bundle bundle = new Bundle();
                                    bundle.putString(Intent.EXTRA_TEXT, "Сервер не доступен: Превышено время ожидания");
                                    bundle.putInt(APP_CODE_TASK, TASK6_SET_MEETING);
                                    receiver.send(STATUS_ERROR, bundle);
                                }
                            }) {


                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                            return params;
                        }

                    };

            request.setRetryPolicy(
                    new DefaultRetryPolicy(3 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMeetingsBackground(final ResultReceiver receiver, String url) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest request =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Bundle bundle = new Bundle();
                                JSONArray array = response;
                                if (!response.toString().equals("[{\"response\":\"false\"}]")) {
                                    if(!response.toString().equals("[]")){
                                        JSONArray read = readJsonObject();
                                        if(read!=null){
                                            if(!array.equals(read)){

                                                if(!checkApp()) {
                                                    writeJsonObject(array);
                                                    showNotification();
                                                } else {
                                                        bundle.putString("result", response.toString());
                                                        bundle.putInt(APP_CODE_TASK, TASK7_BACKGROUND_RECEIVE);
                                                        receiver.send(STATUS_FINISHED, bundle);
                                                    Log.d(TAG, "onResponse " + response.toString());
                                                }
                                            }
                                        }
                                    } else {
                                        if(checkApp()) {
                                            bundle.putString(Intent.EXTRA_TEXT, "Логин и пароль указаны неверно");
                                            bundle.putInt(APP_CODE_TASK, TASK7_BACKGROUND_RECEIVE);
                                            receiver.send(STATUS_ERROR, bundle);
                                        }
                                    }
                                }
                                Log.d(TAG, "onResponse " + response.toString());

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (checkApp()) {
                                    Bundle bundle = new Bundle();
                                    Log.e(TAG, "onErrorResponse Request failed: " + error.toString());
                                    bundle.putString(Intent.EXTRA_TEXT, "Сервер не доступен: Превышено время ожидания");
                                    bundle.putInt(APP_CODE_TASK, TASK7_BACKGROUND_RECEIVE);
                                    receiver.send(STATUS_ERROR, bundle);
                                }
                            }
                        }
                );
        request.setRetryPolicy(
                new DefaultRetryPolicy(3 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public boolean checkApp() {
        ActivityManager am = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);

        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        if (componentInfo.getPackageName().equalsIgnoreCase("com.example.meetingapp")) {
            return true;
        } else {
            return false;
        }
    }

    private void writeJsonObject(JSONArray array) {
        FileOutputStream outputStream = null;
        if (array != null) {
            try {
                outputStream = openFileOutput(jsonFileName, Context.MODE_PRIVATE);
                outputStream.write(array.toString().getBytes());

            } catch (IOException e) {
                Log.e(TAG, "writeJsonObject " + e.getMessage());
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

    }

    private JSONArray readJsonObject() {
        JSONArray array = null;
        String path = this.getFilesDir().getAbsolutePath() + "/" + jsonFileName;
        File file = new File(path);
        FileInputStream stream =null;
        if (file.exists()) {
            try {
                stream = new FileInputStream(file);
                String jsonStr = null;

                FileChannel channel = stream.getChannel();
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                jsonStr = Charset.defaultCharset().decode(buffer).toString();
                Log.d(TAG, "readJsonObject " + jsonStr);

                if (jsonStr != null) {
                    array = new JSONArray(jsonStr);
                }
            } catch ( JSONException | IOException fnfe) {
                Log.e(TAG, "readJsonObject " + fnfe.getMessage());
            } finally {
                try {
                    if(stream!=null)
                        stream.close();
                } catch (IOException ie) {
                    Log.e(TAG, "readJsonObject " + ie.getMessage());
                }
                return array;
            }

        } else {
            File jFile = new File(getFilesDir(), jsonFileName);
            return new JSONArray();
        }
    }
    private void showNotification(){
        mManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);

        Notification notification = new Notification(R.drawable.ic_launcher, "New meetings was received", System.currentTimeMillis());
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent1.putExtra("isStartedFromNotification", true);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(getApplicationContext(), "AlarmManagerDemo", "New meetings was received", pendingNotificationIntent);
        mManager.notify(0, notification);
    }
}
