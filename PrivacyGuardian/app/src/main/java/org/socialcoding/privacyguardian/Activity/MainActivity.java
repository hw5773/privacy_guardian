package org.socialcoding.privacyguardian.Activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import org.socialcoding.privacyguardian.Fragment.AnalyzeFragment;
import org.socialcoding.privacyguardian.Fragment.FirstpageFragment;
import org.socialcoding.privacyguardian.Fragment.SettingsFragment;
import org.socialcoding.privacyguardian.R;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements FirstpageFragment.onFirstpageInteractionListener, AnalyzeFragment.OnAnalyzePressedListener,
    SettingsFragment.OnSettingsInteractionListener{

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
    public void startAnalyze(List<String> appsList){
        Log.d("startAnalyze", appsList.toArray().toString());
        Intent intent = new Intent(this, DataSelectActivity.class);
        intent.putExtra(APPS_LIST, (appsList.toArray(new String[0])));
        startActivityForResult(intent, START_ANALYZE_REQUEST_CODE);
    }

    public void startVPN(){
        Intent intent = new Intent(this, VPNTestActivity.class);
        startActivity(intent);
    }

    //분석 필터 액티비티 결과 수신
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == START_ANALYZE_REQUEST_CODE){
            if(resultCode == RESULT_OK){
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
    public void onFirstpageInteraction(){
        startVPN();
    }

    public void onAnalyzePressed(List<String> appsList){
        startAnalyze(appsList);
    }

    public void onSettingsInteraction(){

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
            //return PlaceholderFragment.newInstance(position + 1);
            switch(position){
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
