package org.socialcoding.privacyguardian.Inteface;

/**
 * Created by disxc on 2017-02-22.
 */

public interface MainActivityInterfaces {
    interface OnAnalyzeInteractionListener {
        void onAnalyzePressed();
        void onSamplePayloadPressed(int index);
        void onClearDBPressed();
        String[] onListRequired();
    }

    interface OnFirstpageInteractionListener {
        void onFirstpageInteraction();
    }

    interface OnSettingsInteractionListener {
        void onSettingsInteraction();
    }
}
