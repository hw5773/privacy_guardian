package org.socialcoding.privacyguardian.Fragment.AppsItem;

import android.graphics.drawable.Drawable;

import org.socialcoding.privacyguardian.AppInfoCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DataSelectAppContent {

    public static final List<AppsItem> ITEMS = new ArrayList<AppsItem>();

    public static final Map<String, AppsItem> ITEM_MAP = new HashMap<String, AppsItem>();

    private static final int COUNT = 25;
    private static boolean initiated = false;
    private static AppInfoCache infoCache;

    public static void init(String[] appsList, AppInfoCache appInfoCache){
        if(initiated)
            return;
        infoCache = appInfoCache;
        for(int i = 0; i < appsList.length; i++){
            addItem(createDummyItem(i, appsList[i]));
        }
        initiated = true;
    }

    private static void addItem(AppsItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.appName, item);
    }

    private static AppsItem createDummyItem(int position, String name) {
        return new AppsItem(String.valueOf(position), infoCache.getAppIcon(name), infoCache.getAppName(name), makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    //app item
    public static class AppsItem {
        public final String appName;
        public final Drawable image;
        public final String content;
        public final String details;

        public AppsItem(String appName, Drawable image, String content, String details) {
            this.appName = appName;
            this.image = image;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
