package org.socialcoding.privacyguardian.Activity;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;


import org.socialcoding.privacyguardian.Analyzer;
import org.socialcoding.privacyguardian.AppInfoCache;
import org.socialcoding.privacyguardian.CacheMaker;
import org.socialcoding.privacyguardian.DatabaseHelper;
import org.socialcoding.privacyguardian.Fragment.AnalyzeFragment;
import org.socialcoding.privacyguardian.Fragment.FeedbackFragment;
import org.socialcoding.privacyguardian.Fragment.GoogleMapsFragment;
import org.socialcoding.privacyguardian.Fragment.SettingsFragment;
import org.socialcoding.privacyguardian.Inteface.MainActivityInterfaces.*;
import org.socialcoding.privacyguardian.Inteface.OnCacheMakerInteractionListener;
import org.socialcoding.privacyguardian.R;

import org.socialcoding.privacyguardian.Structs.ResultItem;
import org.socialcoding.privacyguardian.VPN.Vpn;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static org.socialcoding.privacyguardian.Structs.SensitiveInfoTypes.TYPE_LOCATION_LATLNG;

public class MainActivity extends AppCompatActivity
        implements OnFirstpageInteractionListener, OnAnalyzeInteractionListener,
        OnSettingsInteractionListener, OnCacheMakerInteractionListener, DatabaseDialogListener, OnGoogleMapsInteractionListener {


    private static final int NOTIFICATION_ON_DETECTED = 123;
    private static final String GET_TO_ANALYZE_FRAGMENT = "FRAGGOGO";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    CacheMaker cm = null;
    Analyzer analyzer = null;
    DatabaseHelper mDatabase;
    AppInfoCache mAppInfoCache;
    SwitchCompat mVpnSwitch;
    private boolean mIsRunning = false;

    public static final String APPS_LIST = "AppsList";
    static final int START_ANALYZE_REQUEST_CODE = 1;
    static final int VPN_ACTIVITY_REQUEST_CODE = 2;

    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

    //Notification Alarming
    private Boolean notificationEnabled = true;

    /***** Messenger part start *****/

    Messenger mService = null;
    boolean mIsBound;

    private HashMap<String, Integer> detectionHashMap = new HashMap<>();

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Vpn.SENDPAYLOAD:
                    Log.d("incoming", "i got packet");
                    if (analyzer != null) {
                        Bundle bundle = msg.getData();
                        analyzer.analyze(bundle.getString("packageName"), bundle.getString("payload"));
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            Log.d("service", "connected");
            try {
                Message msg = Message.obtain(null, Vpn.REGISTER);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            Log.d("service", "disconnected");
        }
    };

    void doBindService() {
        bindService(new Intent(this, Vpn.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.d("service", "binding");
    }

    void doUnbindService() {
        if(mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        Log.d("service", "unbinding");
    }

    /***** messenger part end *****/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setting
        //    getFragmentManager().beginTransaction().replace(android.R.id.content,new SettingsFragment()).commit();
        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //initiate update
        try {
            Snackbar.make(findViewById(R.id.container), "업데이트를 시작합니다.", Snackbar.LENGTH_SHORT).show();
            new CacheMaker(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("init.update", "something Wrong...");
        }

        mVpnSwitch = (SwitchCompat) findViewById(R.id.vpn_toggle);

        //if vpn is running, set switch to true
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if (Vpn.class.getName().equals(service.service.getClassName())){
                Log.d("onCreate", "service found");
                mIsRunning = true;
                doBindService();
                mVpnSwitch.setChecked(true);
            }
        }
        Log.d("onCreate", "service running:" + mIsRunning);

        //set toggle button event
        mVpnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("check", "changed to state:" + isChecked);
                if (isChecked) {
                    if (mIsRunning){
                        return;
                    }
                    if (analyzer == null) {
                        Toast.makeText(getApplicationContext(), "업데이트를 수행해 주세요", Toast.LENGTH_LONG).show();
                        buttonView.setChecked(false);
                        return;
                    }
                    //Start VPN connection establishing
                    Intent intent = VpnService.prepare(getApplicationContext());
                    if (intent != null) {
                        startActivityForResult(intent, VPN_ACTIVITY_REQUEST_CODE);
                    } else {
                        onActivityResult(VPN_ACTIVITY_REQUEST_CODE, RESULT_OK, null);
                    }

                } else {
                    //end VPN Connection
                    if (mIsBound) {
                        try {
                            Message msg = Message.obtain(null, Vpn.ENDVPN);
                            mService.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        mIsRunning = false;
                        doUnbindService();
                        stopService(new Intent(getApplicationContext(), Vpn.class));
                    }
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mDatabase = new DatabaseHelper(this);
        mAppInfoCache = new AppInfoCache(this);

        //goto analyzer if notification called
        Boolean menu = getIntent().getBooleanExtra(GET_TO_ANALYZE_FRAGMENT, false);
        if(menu){
            mViewPager.setCurrentItem(1);
        }


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mainpage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d("VPNService","settings");
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //분석 필터 액티비티 시작
    public void startAnalyze(List<String> appsList) {
        Log.d("startAnalyze", appsList.toArray().toString());
        Intent intent = new Intent(this, DataSelectActivity.class);
        intent.putExtra(APPS_LIST, (appsList.toArray(new String[0])));
        startActivityForResult(intent, START_ANALYZE_REQUEST_CODE);
    }

    //분석 필터 액티비티 결과 수신
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //FILTER ACTIVITY
        if (requestCode == START_ANALYZE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Calendar date = Calendar.getInstance();
                String app, type;
                date.setTimeInMillis(data.getLongExtra("date_start", 0L));
                app = data.getStringExtra("app");
                type = data.getStringExtra("type");
                Log.d("onActivityResult", date.toString() + "/" + app + "/" + type);
            }
        }

        //VPN Service
        if (requestCode == VPN_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, Vpn.class);
                startService(intent);
                doBindService();
                Log.d("onActivityResult", "service running start");
            }
        }
    }

    //firstpage와 interaction 하는 리스너?
    public void onFirstpageInteraction() {

    }

    public void onSettingsInteraction() {

    }


    //returns array of resultItem that matches with query
    public ResultItem[] getQueryList() {
        SQLiteDatabase db = mDatabase.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.LogEntry._ID,
                DatabaseHelper.LogEntry.COLUMN_DATETIME,
                DatabaseHelper.LogEntry.COLUMN_PACKAGE_NAME,
                DatabaseHelper.LogEntry.COLUMN_DATA_TYPE,
                DatabaseHelper.LogEntry.COLUMN_DATA_VALUE,
                DatabaseHelper.LogEntry.COLUMN_HOST_ADDRESS
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = DatabaseHelper.LogEntry.COLUMN_PACKAGE_NAME + " = ?";
        String[] selectionArgs = {"*"};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseHelper.LogEntry.COLUMN_DATETIME + " DESC";

        Cursor c = db.query(
                DatabaseHelper.LogEntry.TABLE_NAME,                      // The table to query
                projection,                               // The columns to return
                null,                                     // The not columns for the WHERE clause
                null,                                     // The not values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        Log.d("getQueryList", "query got " + c.getCount());
        ResultItem[] resultItems = new ResultItem[c.getCount()];
        int i = 0;
        if (c.moveToFirst()) {
            do {
                ResultItem ri = new ResultItem();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(c.getLong(1));
                ri.time = cal;
                ri.packageName = c.getString(2);
                ri.appName = mAppInfoCache.getAppName(ri.packageName);
                ri.dataType = c.getString(3);
                ri.dataValue = c.getString(4);
                ri.hostAddress = c.getString(5);
                ri.appIcon = mAppInfoCache.getAppIcon(ri.packageName);
                resultItems[i++] = ri;
            } while (c.moveToNext());
        }
        if (i == 0)
            return null;
        return resultItems;
    }

    /***** analyzer fragment interaction *****/

    @Override
    public void onAnalyzePressed() {
        if (cm != null && analyzer != null) {
            startAnalyze(cm.getAppsList());
        }
    }

    @Override
    public void onClearDBPressed() {
        if (mDatabase != null)
            mDatabase.clearDB();
    }

    @Override
    public ResultItem[] onListRequired() {
        if (mDatabase != null) {
            return getQueryList();
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onbackButtonPressed(){
        getSupportFragmentManager().popBackStack();
        Log.d("back", "i'm back");
    }

    @Override
    public void onMapsPressed(ArrayList<ResultItem> arrayList) {
        Fragment fragment = new GoogleMapsFragment();
        //loading arguments
        Bundle bundle = new Bundle();
        ArrayList<String> stringArrayList = new ArrayList<>();

        for(int i = 0; i < arrayList.size(); i++){
            ResultItem item = arrayList.get(i);
            if(item.dataType.equals(TYPE_LOCATION_LATLNG)){
                stringArrayList.add(item.dataValue);
            }
        }
        bundle.putStringArrayList(GoogleMapsFragment.ARG_LAT_LANG, stringArrayList);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_analyze, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    /***** CacheMaker interaction *****/

    @Override
    public void onCacheMakerCreated(CacheMaker cm, String pm) {
        this.cm = cm;
        Snackbar.make(findViewById(R.id.container), pm, Snackbar.LENGTH_SHORT).show();
        analyzer = new Analyzer(cm, getApplicationContext());
        analyzer.setOnLogGenerated(new Analyzer.OnAnalyzerInteractionListener() {
            @Override
            public void onLogGenerated(String packageName) {
                if (notificationEnabled) {
                    createNotification(packageName);
                }
                refreshResultList();
            }
        });
    }

    /***** Dialog interaction *****/

    @Override
    public void onDialogPositiveClick(String packageName, Long time, String ip, String type, String value) {
        if(analyzer != null){
            analyzer.log(packageName, time, ip, type, value);
        }
        refreshResultList();
    }


    private void refreshResultList(){
        if (mSectionsPagerAdapter.getCurrentFragment() instanceof AnalyzeFragment) {
            ((AnalyzeFragment) mSectionsPagerAdapter.getCurrentFragment()).refreshList();
        } else {
            Log.d("refreshResultList", "not in fragment_analyze");
        }
        Log.d("refreshResultList", "refreshed");
    }

    //creates app notifications for a package
    private void createNotification(String packageName) {
        Integer pNum = detectionHashMap.get(packageName);
        if(pNum == null){
            detectionHashMap.put(packageName, 1);
            pNum = 1;
        }
        else{
            pNum += 1;
            detectionHashMap.put(packageName, pNum);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.location_icon)
                        .setContentTitle(pNum + "개의 위치정보 전송을 감지")
                        .setContentText(mAppInfoCache.getAppName(packageName))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(GET_TO_ANALYZE_FRAGMENT, true);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        int mId = NOTIFICATION_ON_DETECTED;
        mNotificationManager.notify(mId, mBuilder.build());

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private Fragment mCurrentFragment;

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = (Fragment) object;
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case 0:
                    return FeedbackFragment.newInstance("1", "2");
                case 1:
                    return AnalyzeFragment.newInstance();
                default:
                    return FeedbackFragment.newInstance("1","2");
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "첫 페이지";
                case 1:
                    return "분석";
                case 2:
                    return "피드백";
            }
            return null;
        }
    }
}
