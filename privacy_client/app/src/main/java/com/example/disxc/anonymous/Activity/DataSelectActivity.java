package com.example.disxc.anonymous.Activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
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
import android.widget.Toast;

import com.example.disxc.anonymous.Fragment.DataSelectAppFragment;
import com.example.disxc.anonymous.Fragment.DataSelectDateFragment;
import com.example.disxc.anonymous.Fragment.DataSelectTypeFragment;
import com.example.disxc.anonymous.Fragment.dummy.DataSelectAppContent;
import com.example.disxc.anonymous.R;

import java.util.Date;

public class DataSelectActivity extends AppCompatActivity
    implements DataSelectAppFragment.OnAppSelectionChangedListener,
    DataSelectDateFragment.OnDateSelectionChangedListener,
    DataSelectTypeFragment.OnTypeSelectionChangedListener {
    private static final int APP_SELECT_COLUMN = 3;
    public static String[] appsList;

    private String selectedApp = "";
    private Date selectedDate = null;
    private String selectedType = "";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataselect);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.datatab);
        tabLayout.setupWithViewPager(mViewPager);

        Intent intent = getIntent();
        appsList = intent.getStringArrayExtra(MainActivity.APPS_LIST);

    }

    public void setFilter(View view){
        //TODO: send work argument to main activity and return to main activity
        if(selectedApp.compareTo("") == 0){
            Toast.makeText(getApplicationContext(), "앱을 선택해 주세요", Toast.LENGTH_SHORT);
            return;
        }
        if(selectedDate == null){
            Toast.makeText(getApplicationContext(), "날짜를 선택해 주세요", Toast.LENGTH_SHORT);
            return;
        }
        if(selectedType.compareTo("") == 0){
            Toast.makeText(getApplicationContext(), "타입을 선택해 주세요", Toast.LENGTH_SHORT);
            return;
        }
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dataselect, menu);
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

    @Override
    public void onAppSelectionChanged(DataSelectAppContent.AppsItem item) {
        Log.d("onAppSelectionChanged", "App selected");
        selectedApp = item.content;
    }

    @Override
    public void onDateSelectionChanged(Date start, Date end) {
        Log.d("onDateSelectionChanged", "Date selected");
        selectedDate = start;
    }

    @Override
    public void onTypeSelectionChanged(String type) {
        Log.d("onTypeSelectionChanged", "Type selected");
        selectedType = type;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 1:
                    return DataSelectAppFragment.newInstance(APP_SELECT_COLUMN);
                case 2:
                    return DataSelectTypeFragment.newInstance("", "");
                default:
                    return DataSelectDateFragment.newInstance("", "");
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
                    return "날짜";
                case 1:
                    return "앱";
                case 2:
                    return "종류";
            }
            return null;
        }
    }
}
