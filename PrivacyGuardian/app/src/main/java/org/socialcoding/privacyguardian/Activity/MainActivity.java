package org.socialcoding.privacyguardian.Activity;

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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;


import org.socialcoding.privacyguardian.Analyzer;
import org.socialcoding.privacyguardian.AppInfoCache;
import org.socialcoding.privacyguardian.CacheMaker;
import org.socialcoding.privacyguardian.DatabaseHelper;
import org.socialcoding.privacyguardian.Fragment.AnalyzeFragment;
import org.socialcoding.privacyguardian.Fragment.FirstpageFragment;
import org.socialcoding.privacyguardian.Fragment.GoogleMapsFragment;
import org.socialcoding.privacyguardian.Fragment.SettingsFragment;
import org.socialcoding.privacyguardian.Inteface.MainActivityInterfaces.*;
import org.socialcoding.privacyguardian.Inteface.OnCacheMakerInteractionListener;
import org.socialcoding.privacyguardian.R;
import org.socialcoding.privacyguardian.ResultItem;
import org.socialcoding.privacyguardian.VPN.Vpn;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements OnFirstpageInteractionListener, OnAnalyzeInteractionListener,
        OnSettingsInteractionListener, OnCacheMakerInteractionListener, OnGoogleMapsInteractionListener{

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    CacheMaker cm = null;
    Analyzer analyzer = null;
    DatabaseHelper mDatabase;
    AppInfoCache mAppInfoCache;

    public static String APPS_LIST = "AppsList";
    static final int START_ANALYZE_REQUEST_CODE = 1;
    static final int VPN_ACTIVITY_REQUEST_CODE = 2;
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    //VPN MESSEAGING SECTIONS

    Messenger mService = null;
    boolean mIsBound;

    class IncomingHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case Vpn.SENDPAYLOAD:
                    Log.d("incoming", "i got packet");
                    if(analyzer != null){
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
            try{
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

    void doBindService(){
        bindService(new Intent(this, Vpn.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.d("service", "binding");
    }

    void doUnbindService(){
        mIsBound = false;
        Log.d("service", "unbinding");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //set toggle button event
        SwitchCompat vpnSwitch = (SwitchCompat) findViewById(R.id.vpn_toggle);
        vpnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(analyzer == null){
                        Toast.makeText(getApplicationContext(), "업데이트를 수행해 주세요", Toast.LENGTH_LONG).show();
                        buttonView.setChecked(false);
                        return;
                    }
                    //Start VPN connection establishing
                    Log.d("onChecked", "checked");
                    Intent intent = VpnService.prepare(getApplicationContext());
                    if (intent != null) {
                        startActivityForResult(intent, VPN_ACTIVITY_REQUEST_CODE);
                    } else {
                        onActivityResult(VPN_ACTIVITY_REQUEST_CODE, RESULT_OK, null);
                    }

                }
                else{
                    //end VPN Connection
                    Log.d("onChecked", "unchecked");
                    if(mIsBound){
                        try{
                            Message msg = Message.obtain(null, Vpn.ENDVPN);
                            mService.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        doUnbindService();
                    }
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mDatabase = new DatabaseHelper(this);
        mAppInfoCache = new AppInfoCache(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
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

    public void startVPN() {
        Intent intent = new Intent(this, VPNTestActivity.class);
        startActivity(intent);
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
        if (requestCode == VPN_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, Vpn.class);
                startService(intent);
                doBindService();
            }
        }
    }

    //firstpage와 interaction 하는 리스너?
    public void onFirstpageInteraction() {
        startVPN();
    }

    public void onSettingsInteraction() {

    }

    //button when update button pressed
    public void onUpdateButtonClicked(View v) {
        try {
            Snackbar.make(findViewById(R.id.fab), "업데이트를 시작합니다.", Snackbar.LENGTH_SHORT).show();
            new CacheMaker(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("button", "something Wrong...");
        }
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

    @Override
    public void onAnalyzePressed() {
        if (cm != null && analyzer != null)
            startAnalyze(cm.getAppsList());
    }

    @Override
    public void onSamplePayloadPressed(int index) {
        if (analyzer != null)
            analyzer.runSamplePayload(index);
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
        Fragment fragment= new AnalyzeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_analyze, fragment);
        fragmentTransaction.commit();
        Log.d("back", "i'm back");
    }


    @Override

    public void onMapsPressed() {
        Fragment fragment = new GoogleMapsFragment();
       FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.add(R.id.fragment_analyze,fragment);
        fragmentTransaction.replace(R.id.fragment_analyze, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onCacheMakerCreated(CacheMaker cm, String pm) {
        this.cm = cm;
        Snackbar.make(findViewById(R.id.fab), pm, Snackbar.LENGTH_SHORT).show();
        analyzer = new Analyzer(cm, getApplicationContext());
        analyzer.setOnLogGenerated(new Analyzer.onLogGeneratedListener(){
            @Override
            public void onLogGenerated() {
                if (mSectionsPagerAdapter.getCurrentFragment() instanceof AnalyzeFragment) {
                    Log.d("onLogGenerated", "find success...");
                } else {
                    Log.d("onLogGenerated", "failed...");
                }
                ((AnalyzeFragment) mSectionsPagerAdapter.getCurrentFragment()).refreshList();
            }
        });
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
                    return FirstpageFragment.newInstance("1", "2");
                case 1:
                    return AnalyzeFragment.newInstance();
                default:
                    return SettingsFragment.newInstance();
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
                    return "설정";
            }
            return null;
        }
    }
}
