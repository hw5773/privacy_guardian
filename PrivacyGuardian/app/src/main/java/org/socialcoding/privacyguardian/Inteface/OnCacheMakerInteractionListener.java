package org.socialcoding.privacyguardian.Inteface;

import org.socialcoding.privacyguardian.CacheMaker;

/**
 * Created by disxc on 2017-02-22.
 */

public interface OnCacheMakerInteractionListener {
    void onCacheMakerCreated(CacheMaker cacheMaker, String patchResultMessage);
}
