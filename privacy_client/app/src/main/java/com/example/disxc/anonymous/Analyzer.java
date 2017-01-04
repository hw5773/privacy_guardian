package com.example.disxc.anonymous;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by disxc on 2016-10-13.
 */

public class Analyzer {
    String samplePayload0 = "{\"Logitute\": 12, \"Latitude\" : 34, \"message\" : \"test_post\"}";
    String samplePayload1 = "{\"abc\": 12, \"bc\" : 34, \"message\" : \"test_post\"}";
    CacheMaker cache;
    Context ctx;
    public Analyzer(CacheMaker cm, Context context){
        cache = cm;
        ctx = context;
    }

    public void analyze(String appname, String payload){
        JSONObject payloadObject;
        JSONArray hooker;
        String ret = "";
        try {
            payloadObject = new JSONObject(payload);
            if(cache.getByAppId(appname) == null){
                Log.d("analyze", "Cannot find by appid:" + appname);
                return;
            }
            hooker = cache.getByAppId(appname).getJSONArray("HookTarget");
            Log.d("analyze", "target: " + payloadObject.toString());
            for(int i = 0; i < hooker.length(); i++){
                JSONObject jo = hooker.getJSONObject(i);
                Log.d("analyze", "analyze:" + jo.getString("Keyword"));
                try {
                    String value = payloadObject.getString(jo.getString("Keyword"));
                    String type = jo.getString("Type");
                    ret += type + ": " + value + "\n";
                    log(appname, "123", "127.0.0.1", type, value);
                }
                catch(JSONException e){
                    Log.d("analyze", "object parse failed:" + jo.toString());
                    continue;
                }
            }
            if(ret.compareTo("") == 0)
                Toast.makeText(ctx, "해당정보가 없습니다.", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(ctx, ret, Toast.LENGTH_LONG).show();
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return;
    }

    public void sample(int i){
        if(i == 0){
            analyze("testapp", samplePayload0);
        }
        else{
            analyze("testapp", samplePayload1);
        }
    }

    public void log(String packageName, String timeStamp, String ip, String type, String value){
        //TODO: implement db transactions.
    }
}
