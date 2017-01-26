package com.example.disxc.anonymous.Fragment.dummy;

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

    /**
     * An array of sample (dummy) items.
     */
    public static final List<AppsItem> ITEMS = new ArrayList<AppsItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, AppsItem> ITEM_MAP = new HashMap<String, AppsItem>();

    private static final int COUNT = 25;
    private static boolean initiated = false;

    /*static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }*/

    public static void init(String[] appsList){
        if(initiated)
            return;
        for(int i = 0; i < appsList.length; i++){
            addItem(createDummyItem(i, appsList[i]));
        }
        initiated = true;
    }

    private static void addItem(AppsItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static AppsItem createDummyItem(int position, String name) {
        return new AppsItem(String.valueOf(position), "", name, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class AppsItem {
        public final String id;
        public final String image;
        public final String content;
        public final String details;

        public AppsItem(String id, String image, String content, String details) {
            this.id = id;
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
