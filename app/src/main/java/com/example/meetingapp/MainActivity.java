package com.example.meetingapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";
    private View userDialog;
    private JSONObject res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setIcon(R.drawable.ic_launcher);
        getOverflowMenu();
        boolean isOnline = isOnline();
        if (isOnline)
            showLoginDialog();
        else
            Toast.makeText(MainActivity.this, R.string.workInternet, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about: {
                showAboutDialog();
            }
            break;
            case R.id.action_exit: {
                showAlert();
            }
            break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showAlert() {

        new AlertDialog.Builder(this).setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.quit)
                .setMessage(R.string.really_quit)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Stop the activity
                        finish();
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showAboutDialog() {
        Drawable icon = ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_dialog_info).mutate();
        icon.setColorFilter(new ColorMatrixColorFilter(new float[]{
                0.5f, 0, 0, 0, 0,
                0, 0.5f, 0, 0, 0,
                0, 0, 0, 0.5f, 0,
                0, 0, 0, 1, 0,
        }));
        new AlertDialog.Builder(this)
                .setCancelable(false)

                .setIcon(icon)
                .setTitle(R.string.about)
                .setMessage(R.string.aboutText)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                }).show();
    }

    private void getOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoginDialog() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View userDialog = inflater.inflate(R.layout.dialog_text, null);
        new AlertDialog.Builder(this)
                .setCancelable(false)

                .setIcon(R.drawable.password_dialog)
                .setTitle(R.string.loginTitle)
                .setMessage(R.string.loginText)
                .setView(userDialog)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        EditText user = (EditText)userDialog.findViewById(R.id.userText);
                        EditText password = (EditText) userDialog.findViewById(R.id.passwordText);
                        checkLoginRequest(user.getText().toString(), password.getText().toString());
                        dialog.cancel();
                        Log.d(TAG, "onCreate response = " + res);
                    }

                }).show();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void checkLoginRequest(String username, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.43.246:8080/rest/rest/hello/user?userName=" + username + "&password=" + password;
        final ProgressDialog dlg = ProgressDialog.show(
                this,
                "Retrieving REST data",
                "Please Wait...", true);

        JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                dlg.dismiss();
                                res = response;
                                printRes();

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "onErrorResponse Request failed: " + error.toString());
                                dlg.dismiss();
                            }

                        });
        request.setRetryPolicy(
                new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        queue.add(request);
}
   private void printRes(){
       Log.d(TAG, "printRes response = "+ res.toString());
   }

    public class MeetingDataLoader extends AsyncTask<Void, Void, JSONObject> {
        Context context;

        public MeetingDataLoader(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = "http://192.168.43.246:8080/rest/rest/meeting/getMeeting";
            JsonObjectRequest request =
                    new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONArray array = new JSONArray(response.toString());

                                    } catch (JSONException je){
                                        Log.e(TAG, "onResponse "+ je.getMessage());
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
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

        }

    }
}
