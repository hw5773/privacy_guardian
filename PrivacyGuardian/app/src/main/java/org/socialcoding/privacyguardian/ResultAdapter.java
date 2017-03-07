package org.socialcoding.privacyguardian;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.socialcoding.privacyguardian.Structs.ResultItem;
import org.socialcoding.privacyguardian.Structs.SensitiveInfoTypes;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by disxc on 2017-02-28.
 */


public class ResultAdapter extends ArrayAdapter<ResultItem> {

    private ArrayList<ResultItem> items;
    private HashMap<String, String> types;

    public ResultAdapter(Context context, int resource, ArrayList<ResultItem> objects) {
        super(context, resource, objects);
        items = objects;
        Resources r = context.getResources();
        types = new HashMap<>();
        types.put(SensitiveInfoTypes.TYPE_LOCATION_LAT, r.getString(R.string.latitude));
        types.put(SensitiveInfoTypes.TYPE_LOCATION_LNG, r.getString(R.string.longitude));
        types.put(SensitiveInfoTypes.TYPE_LOCATION_LATLNG, r.getString(R.string.latlng));
        types.put("unknown", r.getString(R.string.unknown_type));
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.result_list_item, null);
        }
        ResultItem resultItem = items.get(position);
        if (resultItem != null) {
            ImageView imageView = (ImageView) v.findViewById(R.id.app_image);
            TextView tvAppName = (TextView) v.findViewById(R.id.appname);
            TextView tvType = (TextView) v.findViewById(R.id.type);
            TextView tvDatetime = (TextView) v.findViewById(R.id.datetime);
            TextView tvValue = (TextView) v.findViewById(R.id.value);
            if(imageView != null){
                imageView.setImageDrawable(resultItem.appIcon);
            }
            if (tvAppName != null){
                tvAppName.setText(resultItem.appName);
            }
            if(tvType != null){
                String typestr = types.get(resultItem.dataType);
                if(typestr == null){
                    typestr = types.get("unknown");
                }
                tvType.setText(typestr);
            }
            if(tvDatetime != null){
                tvDatetime.setText(DateFormat.getInstance().format(resultItem.time.getTime()));
            }
            if(tvValue != null){
                tvValue.setText(resultItem.dataValue);
            }
        }

        return v;
    }
}

