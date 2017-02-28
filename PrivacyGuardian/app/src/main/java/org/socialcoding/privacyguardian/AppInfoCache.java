package org.socialcoding.privacyguardian;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by disxc on 2017-02-28.
 */
public class AppInfoCache {
    Context ctx;
    PackageManager pm;
    HashMap<String, String> nameCache;
    HashMap<String, Drawable> iconCache;
    public AppInfoCache(Context context){
        ctx = context;
        pm = context.getPackageManager();
        nameCache = new HashMap<>();
        iconCache = new HashMap<>();
    }

    public String getAppName(String packageName){
        String appName = nameCache.get(packageName);
        if(appName == null){
            ApplicationInfo ai;
            try{
                ai = pm.getApplicationInfo(packageName, 0);
                appName = (String) pm.getApplicationLabel(ai);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d("AppInfoCache", "No such package: " + packageName);
                appName = packageName;
            }
            nameCache.put(packageName, appName);
        }
        return appName;
    }

    public Drawable getAppIcon(String packageName){
        Drawable icon = iconCache.get(packageName);
        if(icon == null){
            ApplicationInfo ai;
            try{
                icon = pm.getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d("AppInfoCache", "No icon for such package: " + packageName);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    icon = ctx.getDrawable(android.R.drawable.sym_def_app_icon);
                else
                    icon = ctx.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
            }
            iconCache.put(packageName, icon);
        }
        return icon;
    }

}
