package com.example.meetingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.app.ActionBar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.support.v4.widget.SwipeRefreshLayout;

public class MainActivity extends ActionBarActivity implements DownloadResultReceiver.Receiver, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "MainActivity";
    private JSONObject res;
    public static final String APP_PREFERENCES = "com.example.meetingapp_preferences";
    public static final String APP_PREFERENCES_NAME = "username"; // имя пользователя
    public static final String APP_PREFERENCES_PASSWORD = "password"; // пароль
    public static final String APP_RECEIVER = "receiver";     // ресивер
    public static final String APP_CODE_TASK = "codeTask";    // код задачи
    //    public static final String APP_MEETING_NAME="name";     // название встречи
//    public static final String APP_BEGIN_DATE="begindate";  //дата начала
//    public static final String APP_END_DATE="enddate";      //дата конца
    public static final String APP_ID = "id";
    final int TASK1_RECEIVE_MEETINGS = 1;
    final int TASK2_DELETE_MEETING = 2;
    final int TASK3_FULL_DESCRIPTION = 3;
    private SharedPreferences preferences;
    String username;
    String password;
    int swipeID;
    ListView mListView;
    DownloadResultReceiver mReceiver;
    String jsonFileName = "messages.json";
    JSONArray array = null;
    ArrayList<TransferItem> transferList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setIcon(R.drawable.ic_launcher);
        mListView = (ListView) findViewById(R.id.meetingList);
        registerForContextMenu(mListView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        mContext = getApplicationContext();
        getOverflowMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isOnline = isOnline();
        preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (preferences != null) {
            if (preferences.contains(APP_PREFERENCES_NAME) && preferences.contains(APP_PREFERENCES_PASSWORD)) {
                username = preferences.getString(APP_PREFERENCES_NAME, "");
                password = preferences.getString(APP_PREFERENCES_PASSWORD, "");
                if (!isOnline)
                    Toast.makeText(MainActivity.this, R.string.workInternet, Toast.LENGTH_SHORT).show();
                else {
                    startSendService(TASK1_RECEIVE_MEETINGS);
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.missAccount, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        int taskCode = resultData.getInt(APP_CODE_TASK);
        switch (taskCode) {
            case TASK1_RECEIVE_MEETINGS: {
                switch (resultCode) {
                    case RestClientService.STATUS_FINISHED:
                /* Hide progress & extract result from bundle */
                        try {
                            setProgressBarIndeterminateVisibility(false);
                            String result = resultData.getString("result");
                            array = new JSONArray(result);
                            fillListView();
                        } catch (JSONException e) {
                            Log.e(TAG, "onReceiveResult " + e.getMessage());
                        }
                        break;
                    case RestClientService.STATUS_ERROR:
                        String error = resultData.getString(Intent.EXTRA_TEXT);
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        break;
                }
            }
            break;
            case TASK2_DELETE_MEETING: {
                switch (resultCode) {
                    case RestClientService.STATUS_FINISHED: {
                        Toast.makeText(this, R.string.delete_message, Toast.LENGTH_SHORT).show();
                    }
                    break;
                    case RestClientService.STATUS_ERROR: {
                        String error = resultData.getString(Intent.EXTRA_TEXT);
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
            break;
            case TASK3_FULL_DESCRIPTION: {
                switch (resultCode) {
                    case RestClientService.STATUS_FINISHED: {
                        String result = resultData.getString("result");
                        result = result.substring(result.indexOf(":") + 2, result.lastIndexOf("\""));
                        showAboutDialog(true, result);
                    }
                    break;
                    case RestClientService.STATUS_ERROR: {
                        String error = resultData.getString(Intent.EXTRA_TEXT);
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        break;
                    }
                }
            }
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
            case R.id.action_settings: {
                Intent intent;
                Activity currentActivity = this;
                intent = new Intent(currentActivity, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                currentActivity.startActivity(intent);
            }
            break;
            case R.id.action_about: {
                showAboutDialog(false, null);
            }
            break;
            case R.id.action_exit: {
                showAlert();
            }
            break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void fillListView() {
        if (array != null) {
            try {
                mListView = (ListView) findViewById(R.id.meetingList);
                mListView.setAdapter(null);
                transferList = new ArrayList<TransferItem>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    transferList.add(new TransferItem(
                            item.getInt(getString(R.string.jsonId)),
                            item.getString(getString(R.string.jsonMeetingName)),
                            item.getString(getString(R.string.jsonBeginDate)),
                            item.getString(getString(R.string.jsonEndDate))));
                }
                mListView.setAdapter(new TransferAdapter(this, R.layout.list_item, transferList));

            } catch (JSONException e) {
                Log.e(TAG, "fillListView " + e.getMessage());
            }
        }
    }

    @Override
    public void onRefresh() {
        fetchMeetings();
    }

    /**
     * Fetching movies json by making http call
     */
    private void fetchMeetings() {

        // showing refresh animation before making http call
        swipeRefreshLayout.setRefreshing(true);
        if (!isOnline())
            Toast.makeText(MainActivity.this, R.string.workInternet, Toast.LENGTH_SHORT).show();
        else {
            startSendService(TASK1_RECEIVE_MEETINGS);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showAlert() {

        new AlertDialog.Builder(this).setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.quit)
                .setMessage(R.string.really_quit)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showAboutDialog(boolean isDecription, String message) {
        Drawable icon = ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_dialog_info).mutate();
        icon.setColorFilter(new ColorMatrixColorFilter(new float[]{
                0.5f, 0, 0, 0, 0,
                0, 0.5f, 0, 0, 0,
                0, 0, 0, 0.5f, 0,
                0, 0, 0, 1, 0,
        }));
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false).setIcon(icon)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                });
        if (isDecription) {
            dialog.setTitle(R.string.description)
                    .setMessage(message);
        } else {
            dialog.setTitle(R.string.about)
                    .setMessage(R.string.aboutText);
        }
        dialog.show();
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

    private void startSendService(int code) {
        mReceiver = new DownloadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent i = new Intent(this, RestClientService.class);
        switch (code) {
            case TASK2_DELETE_MEETING:
            case TASK3_FULL_DESCRIPTION: {
                i.putExtra(APP_ID, swipeID);
            }
        }
        i.putExtra(APP_PREFERENCES_NAME, username);
        i.putExtra(APP_PREFERENCES_PASSWORD, password);
        i.putExtra(APP_CODE_TASK, code);
        i.putExtra(APP_RECEIVER, mReceiver);
        this.startService(i);
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void showParticipantDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View participantDialogView = factory.inflate(
                R.layout.dialog_participant, null);
        final AlertDialog.Builder partDialog = new AlertDialog.Builder(this);
        partDialog.setView(participantDialogView)
                .setMessage(getString(R.string.participantText))
                .setTitle(R.string.participantTitle)
                .setIcon(R.drawable.ic_participant)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        partDialog.show();
    }

    class TransferItem {
        private int id;
        private String meetingName;
        private String beginDate;
        private String endDate;

        public TransferItem(int id, String meetingName, String beginDate, String endDate) {
            this.id = id;
            this.meetingName = meetingName;
            this.beginDate = beginDate;
            this.endDate = endDate;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
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
        SwipeDetector swipeDetector;

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
        public View getView(final int pos, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.list_item, null);
                transferHolder = new TransferViewHolder();
                transferHolder.mainView = (LinearLayout) view.findViewById(R.id.mainview);
                transferHolder.listItem = (RelativeLayout) view.findViewById(R.id.listitem);
                transferHolder.meetingName = (TextView) view.findViewById(R.id.meetingName);
                transferHolder.beginDate = (TextView) view.findViewById(R.id.beginDate);
                transferHolder.endDate = (TextView) view.findViewById(R.id.endDate);
                view.setTag(transferHolder);
            } else transferHolder = (TransferViewHolder) view.getTag();

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

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!swipeDetector.isNone()) {
                        Log.d(TAG, "onLongClick swipe ? isNone=" + swipeDetector.isNone());
                        return true;
                    } else {
                        Log.d(TAG, "onLongClick notSwipe?  isNone=" + swipeDetector.isNone());
                        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                        popupMenu.inflate(R.menu.menu_context);
                        popupMenu
                                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.add: {
                                                showParticipantDialog();
                                                return true;
                                            }
                                            case R.id.info: {
                                                //int position = swipeDetector.getPosition();
                                                TransferItem ti = getItem(pos);
                                                swipeID = ti.getId();
                                                startSendService(TASK3_FULL_DESCRIPTION);
                                                return true;
                                            }
                                        }
                                        return true;
                                    }
                                });

                        popupMenu.show();
                        return true;
                    }
                }
            });
            swipeDetector = new SwipeDetector(transferHolder, pos);
            view.setOnTouchListener(swipeDetector);
            return view;
        }

        public class SwipeDetector implements View.OnTouchListener {
            private static final int MIN_DISTANCE = 130;
            private static final int MIN_LOCK_DICTANCE = 30;
            private float downX, upX;
            private TransferViewHolder holder;
            private int position;
            private boolean isNone = true;

            public SwipeDetector(TransferViewHolder holder, int position) {
                this.holder = holder;
                this.position = position;
            }

            public boolean isNone() {
                return isNone;
            }

            public int getPosition() {
                return position;
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        Log.d(TAG, "onTouch ACTION_DOWN");
                        downX = event.getX();
                        isNone = true;
                        Log.d(TAG, "onTouch ACTION_DOWN position " + position);
                        return false;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        Log.d(TAG, "onTouch ACTION_MOVE");
                        upX = event.getX();
                        float deltaX = downX - upX;
                        if (Math.abs(deltaX) > MIN_LOCK_DICTANCE && mListView != null) {// && !motionInterceptDisallowed) {
                            if (deltaX > 0) {
                                holder.listItem.setVisibility(View.GONE);
                                isNone = false;
                            } else {
                                holder.listItem.setVisibility(View.VISIBLE);
                                isNone = false;
                            }
                            swipe(-(int) deltaX);
                            return true;
                        }
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        Log.d(TAG, "onTouch ACTION_UP");
                        upX = event.getX();
                        float deltaX = upX - downX;
                        if (deltaX > MIN_DISTANCE) {
                            swipeRemove();
                        } else {
                            swipe(0);
                        }

                        holder.listItem.setVisibility(View.VISIBLE);
                        return true;
                    }

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
                TransferItem ti = getItem(position);
                swipeID = ti.getId();
                remove(ti);
                startSendService(TASK2_DELETE_MEETING);
                notifyDataSetChanged();
            }
        }
    }
}
