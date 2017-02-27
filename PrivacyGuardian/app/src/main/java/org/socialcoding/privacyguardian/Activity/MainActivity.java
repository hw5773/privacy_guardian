package org.socialcoding.privacyguardian.Activity;

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

import org.socialcoding.privacyguardian.Analyzer;
import org.socialcoding.privacyguardian.CacheMaker;
import org.socialcoding.privacyguardian.DatabaseHelper;
import org.socialcoding.privacyguardian.Fragment.AnalyzeFragment;
import org.socialcoding.privacyguardian.Fragment.FirstpageFragment;
import org.socialcoding.privacyguardian.Fragment.SettingsFragment;
import org.socialcoding.privacyguardian.Inteface.MainActivityInterfaces.*;
import org.socialcoding.privacyguardian.Inteface.OnCacheMakerInteractionListener;
import org.socialcoding.privacyguardian.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements OnFirstpageInteractionListener, OnAnalyzeInteractionListener,
        OnSettingsInteractionListener, OnCacheMakerInteractionListener {

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

    public static String APPS_LIST = "AppsList";
    static final int START_ANALYZE_REQUEST_CODE = 1;

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
        if (requestCode == START_ANALYZE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Calendar date = Calendar.getInstance();
                String app, type;
                date.setTimeInMillis(data.getLongExtra("date", 0L));
                app = data.getStringExtra("app");
                type = data.getStringExtra("type");
                Log.d("onActivityResult", date.toString() + "/" + app + "/" + type);
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


    public String[] getQueryList() {
        SQLiteDatabase db = mDatabase.getReadableDatabase();
        //TODO: 내 언어에 맞는 시간대 출력하는 방법 찾기

        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("mmm dd HH:mm");

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseHelper.LogEntry._ID,
                DatabaseHelper.LogEntry.COLUMN_DATETIME,
                DatabaseHelper.LogEntry.COLUMN_PACKAGE_NAME,
                DatabaseHelper.LogEntry.COLUMN_DATA_TYPE,
                DatabaseHelper.LogEntry.COLUMN_DATA_VALUE
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
        String[] strings = new String[c.getCount()];
        int i = 0;
        if (c.moveToFirst()) {
            do {
                Date date = null;
                String str = "";
                str += c.getString(0);/*
                //TODO: 제대로 된 LOCALE 시간 알아내기
                try{
                    date = inputFormat.parse(c.getString(1));
                    str += outputFormat.format(date);
                }
                catch(Exception e){
                    e.printStackTrace();
                }*/
                str += ", " + c.getString(2);
                str += ", " + c.getString(3);
                str += ", " + c.getString(4);
                strings[i++] = str;

            } while (c.moveToNext());
        }
        if (i == 0)
            return null;
        return strings;
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
    public String[] onListRequired() {
        if (mDatabase != null) {
            return getQueryList();
        }
        return null;
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
                    ((AnalyzeFragment) mSectionsPagerAdapter.getCurrentFragment()).refreshList();
                } else {
                    Log.d("onLogGenerated", "failed...");
                }
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
