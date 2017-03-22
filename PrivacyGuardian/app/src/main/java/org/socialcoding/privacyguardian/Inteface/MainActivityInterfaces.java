package org.socialcoding.privacyguardian.Inteface;

import android.support.v4.app.DialogFragment;

import org.socialcoding.privacyguardian.Structs.ResultItem;

import java.util.ArrayList;

/**
 * Created by disxc on 2017-02-22.
 */

public interface MainActivityInterfaces {
    interface OnAnalyzeInteractionListener {
        void onAnalyzePressed();
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
  
    interface DatabaseDialogListener {
        void onDialogPositiveClick(String packageName, Long time, String ip, String type, String value);
    }
}
