package org.socialcoding.privacyguardian.Inteface;

import org.socialcoding.privacyguardian.AppInfoCache;
import org.socialcoding.privacyguardian.Fragment.AppsItem.DataSelectAppContent;

import java.util.Calendar;

/**
 * Created by disxc on 2017-03-14.
 */

public interface DataSelectActivityInterFaces {
    interface OnAppSelectionInteractionListener {
        void onAppSelectionChanged(DataSelectAppContent.AppsItem item);
        AppInfoCache onAppCacheDemanded();
    }

    interface OnDateSelectionChangedListener {
        void onStartDateSelectionChanged(Calendar start);
        void onEndDateSelectionChanged(Calendar end);
    }

    interface OnTypeSelectionChangedListener {
        void onTypeSelectionChanged(String type);
    }
}
