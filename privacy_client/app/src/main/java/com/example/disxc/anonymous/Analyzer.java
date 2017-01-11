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
    DatabaseHelper mDatabase;

    public Analyzer(CacheMaker cm, Context context){
        cache = cm;
        ctx = context;
    }

    public void analyze(String appname, String payload){
        JSONObject payloadObject;
        JSONArray hookedTarget;
        String ret = "";
        try {
            payloadObject = new JSONObject(payload);
            JSONObject target = cache.getByAppId(appname);
            if(target == null){
                Toast.makeText(ctx, "Couldn't find app: " + appname, Toast.LENGTH_SHORT);
                Log.d("Analyze", "Couldn't find app:" + appname);
                return;
            }
            if(target.getString("Format").compareTo("json") == 0){
                throw new RuntimeException("not implemented Exception");
            }
            hookedTarget = target.getJSONArray("HookTarget");
            Log.d("Analyze", "target: " + payloadObject.toString());
            for(int i = 0; i < hookedTarget.length(); i++){
                JSONObject jo = hookedTarget.getJSONObject(i);
                Log.d("Analyze", "Analyze:" + jo.getString("Keyword"));
                try {
                    String value = payloadObject.getString(jo.getString("Keyword"));
                    String type = jo.getString("Type");
                    ret += type + ": " + value + "\n";
                    log(appname, "123", "127.0.0.1", type, value);
                }
                catch(JSONException e){
                    Log.d("Analyze", "object parse failed:" + jo.toString());
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
        mDatabase.log(packageName, timeStamp, ip, type, value);
    }
}
