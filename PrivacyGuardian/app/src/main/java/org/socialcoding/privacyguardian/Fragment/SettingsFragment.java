package org.socialcoding.privacyguardian.Fragment;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Button;

import org.socialcoding.privacyguardian.R;

/**
 * Created by 신승수 on 2017-07-06.
 */

public class SettingsFragment extends PreferenceFragment {
    private static final String TAG= "VPNService";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
