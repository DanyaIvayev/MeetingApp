package com.example.meetingapp;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Дамир on 14.11.2015.
 */
public class RestClientService extends IntentService{
    private static final String TAG = "RestClientService";
    public RestClientService() {
        super("RestClientService");
    }

    protected void onHandleIntent(Intent i){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.43.246:8080/rest/rest/meeting/getMeeting";
        JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray array = new JSONArray(response.toString());
                                } catch (JSONException je){
                                    Log.e(TAG, "onResponse " + je.getMessage());
                                }
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
}
