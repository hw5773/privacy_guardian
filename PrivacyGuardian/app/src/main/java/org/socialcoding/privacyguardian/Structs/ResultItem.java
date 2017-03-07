package org.socialcoding.privacyguardian.Structs;

import android.graphics.drawable.Drawable;

import java.util.Calendar;

/**
 * Created by disxc on 2017-02-22.
 */
public class ResultItem {
    public Calendar time;
    public Drawable appIcon;
    public String packageName;
    public String appName;
    public String dataType;
    public String dataValue;
    public String hostAddress;

    public ResultItem(){
        time = Calendar.getInstance();
        appIcon = null;
        packageName = appName = dataType = dataValue = hostAddress = "?";
    }
}
