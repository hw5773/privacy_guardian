package org.socialcoding.privacyguardian.Inteface;

import org.socialcoding.privacyguardian.ResultItem;

import java.util.ArrayList;

/**
 * Created by disxc on 2017-02-22.
 */

public interface MainActivityInterfaces {
    interface OnAnalyzeInteractionListener {
        void onAnalyzePressed();
        void onSamplePayloadPressed(int index);
        void onClearDBPressed();
        ResultItem[] onListRequired();
        void onMapsPressed(ArrayList<ResultItem> arrayList);

    }

    interface OnFirstpageInteractionListener {
        void onFirstpageInteraction();
    }

    interface OnSettingsInteractionListener {
        void onSettingsInteraction();
    }

    interface OnGoogleMapsInteractionListener{
        void onbackButtonPressed();
    }
}
