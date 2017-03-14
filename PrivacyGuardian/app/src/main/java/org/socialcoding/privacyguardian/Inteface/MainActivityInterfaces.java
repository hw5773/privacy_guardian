package org.socialcoding.privacyguardian.Inteface;

import org.socialcoding.privacyguardian.ResultItem;

/**
 * Created by disxc on 2017-02-22.
 */

public interface MainActivityInterfaces {
    interface OnAnalyzeInteractionListener {
        void onAnalyzePressed();
        void onSamplePayloadPressed(int index);
        void onClearDBPressed();
        ResultItem[] onListRequired();

        void onMapsPressed();

    }

    interface OnFirstpageInteractionListener {
        void onFirstpageInteraction();
    }

    interface OnSettingsInteractionListener {
        void onSettingsInteraction();
    }

    interface OnGoogleMapsInteractionListener{
        void onBackPressed();
    }
}
