package com.example.meetingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.app.ActionBar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends ActionBarActivity implements DownloadResultReceiver.Receiver {
    private static final String TAG = "MainActivity";
    private JSONObject res;
    public static final String APP_PREFERENCES = "com.example.meetingapp_preferences";
    public static final String APP_PREFERENCES_NAME = "userName"; // имя пользователя
    public static final String APP_PREFERENCES_PASSWORD = "passwordKey"; // пароль
    public static final String APP_RECEIVER="receiver";
    final int TASK1_CODE = 1;
    private SharedPreferences preferences;
    String username;
    String password;
    ListView mListView;
    DownloadResultReceiver mReceiver;
    String jsonFileName = "messages.json";
    JSONArray array=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setIcon(R.drawable.ic_launcher);
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = format.format(date);
        getOverflowMenu();
    }

    @Override
    public void onResume(){
        super.onResume();
        boolean isOnline = isOnline();

        preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if(preferences!=null){
            if(preferences.contains(APP_PREFERENCES_NAME) && preferences.contains(APP_PREFERENCES_PASSWORD)){
                username = preferences.getString(APP_PREFERENCES_NAME, "");
                password = preferences.getString(APP_PREFERENCES_PASSWORD, "");
                if(!isOnline)
                    Toast.makeText(MainActivity.this, R.string.workInternet, Toast.LENGTH_SHORT).show();
                else{
                    mReceiver = new DownloadResultReceiver(new Handler());
                    mReceiver.setReceiver(this);
                    Intent i = new Intent(this, RestClientService.class);
                    i.putExtra(APP_PREFERENCES_NAME, username);
                    i.putExtra(APP_PREFERENCES_PASSWORD, password);
                    i.putExtra(APP_RECEIVER, mReceiver);
                    this.startService(i);
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.missAccount, Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
//            case DownloadService.STATUS_RUNNING:
//                setProgressBarIndeterminateVisibility(true);
//                break;
            case RestClientService.STATUS_FINISHED:
                /* Hide progress & extract result from bundle */
                try {
                    setProgressBarIndeterminateVisibility(false);
                    String result = resultData.getString("result");
                    array = new JSONArray(result);
                    fillListView();
                } catch (JSONException e){
                    Log.e(TAG, "onReceiveResult "+e.getMessage());
                }
                /* Update ListView with result */
                /*arrayAdapter = new ArrayAdapter(MyActivity.this, android.R.layout.simple_list_item_2, results);
                listView.setAdapter(arrayAdapter);*/
                break;
            case RestClientService.STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
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
            case R.id.action_settings:{
                Intent intent;
                Activity currentActivity = this;
                intent = new Intent(currentActivity, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                currentActivity.startActivity(intent);
            } break;
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
    private void fillListView(){
        if(array!=null) {
            try {
                mListView = (ListView) findViewById(R.id.meetingList);
                mListView.setAdapter(null);
                ArrayList<TransferItem> transferList = new ArrayList<TransferItem>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    transferList.add(new TransferItem(item.getString(getString(R.string.jsonMeetingName)),
                            item.getString(getString(R.string.jsonBeginDate)),
                            item.getString(getString(R.string.jsonEndDate))));
                }
                mListView.setAdapter(new TransferAdapter(this, R.layout.list_item, transferList));
            } catch (JSONException e){
                Log.e(TAG, "fillListView "+e.getMessage());
            }
        }
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

    class TransferItem{
        private String meetingName;
        private String beginDate;
        private String endDate;
        public TransferItem(String meetingName, String beginDate, String endDate) {
            this.meetingName = meetingName;
            this.beginDate = beginDate;
            this.endDate = endDate;
        }

        public String getBeginDate() {
            return beginDate;
        }

        public void setBeginDate(String beginDate) {
            this.beginDate = beginDate;
        }

        public String getMeetingName() {
            return meetingName;
        }

        public void setMeetingName(String meetingName) {
            this.meetingName = meetingName;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }
    public class TransferAdapter extends ArrayAdapter<TransferItem> {
        private ArrayList<TransferItem> items;
        private TransferViewHolder transferHolder;


        private class TransferViewHolder {
            TextView beginDate;
            TextView meetingName;
            TextView endDate;
            RelativeLayout listItem;
            LinearLayout mainView;
        }

        public TransferAdapter(Context context, int tvResId, ArrayList<TransferItem> items) {
            super(context, tvResId, items);
            this.items = items;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
                //v = vi.inflate(R.layout.feed_view, null);
                v = vi.inflate(R.layout.list_item, null);
                transferHolder = new TransferViewHolder();
                transferHolder.mainView = (LinearLayout) v.findViewById(R.id.mainview);
                transferHolder.listItem = (RelativeLayout) v.findViewById(R.id.listitem);
                transferHolder.meetingName = (TextView) v.findViewById(R.id.meetingName);
                transferHolder.beginDate = (TextView) v.findViewById(R.id.beginDate);
                transferHolder.endDate = (TextView) v.findViewById(R.id.endDate);
                v.setTag(transferHolder);
            } else transferHolder = (TransferViewHolder) v.getTag();

            TransferItem transfer = items.get(pos);

            if (transfer != null) {
                transferHolder.meetingName.setText(transfer.getMeetingName());
                transferHolder.beginDate.setText(transfer.getBeginDate());
                transferHolder.endDate.setText(transfer.getEndDate());
            }
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) transferHolder.mainView.getLayoutParams();
            params.rightMargin = 0;
            params.leftMargin = 0;
            transferHolder.mainView.setLayoutParams(params);

            v.setOnTouchListener(new SwipeDetector(transferHolder, pos));
            return v;
        }

        public class SwipeDetector implements View.OnTouchListener {
            private static final int MIN_DISTANCE = 300;
            private static final int MIN_LOCK_DICTANCE = 30;
            private boolean motionInterceptDisallowed = false;
            private float downX, upX;
            private TransferViewHolder holder;
            private int position;

            public SwipeDetector(TransferViewHolder holder, int position) {
                this.holder = holder;
                this.position = position;
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        Log.d(TAG, "onTouch ACTION_DOWN");
                        downX = event.getX();
                        Log.d(TAG, "onTouch ACTION_DOWN position " + position);
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        Log.d(TAG, "onTouch ACTION_MOVE");
                        upX = event.getX();
                        float deltaX = downX - upX;
                        if (Math.abs(deltaX) > MIN_LOCK_DICTANCE && mListView != null && !motionInterceptDisallowed) {
                            mListView.requestDisallowInterceptTouchEvent(true);
                            motionInterceptDisallowed = true;
                        }
                        if (deltaX > 0) {
                            holder.listItem.setVisibility(View.GONE);
                        } else {
                            holder.listItem.setVisibility(View.VISIBLE);
                        }
                        swipe(-(int) deltaX);
                        return true;
                    }
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch ACTION_UP");
                        upX = event.getX();
                        float deltaX = upX - downX;
                        if (deltaX > MIN_DISTANCE) {
                            // left or right
                            swipeRemove();
                        } else {
                            swipe(0);
                        }

                        if (mListView != null) {
                            mListView.requestDisallowInterceptTouchEvent(false);
                            motionInterceptDisallowed = false;
                        }

                        holder.listItem.setVisibility(View.VISIBLE);
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        Log.d(TAG, "onTouch ACTION_CANCEL");
                        swipe(0);
                        holder.listItem.setVisibility(View.VISIBLE);
                        return false;
                }
                return true;

            }

            private void swipe(int distance) {
                View animationView = holder.mainView;
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
                params.rightMargin = -distance;
                params.leftMargin = distance;
                animationView.setLayoutParams(params);
            }

            private void swipeRemove() {
                remove(getItem(position));
                //deleteJsonObject(position);
                notifyDataSetChanged();
            }
        }
    }
}
