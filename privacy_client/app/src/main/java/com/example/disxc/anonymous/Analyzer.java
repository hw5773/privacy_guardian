package com.example.disxc.anonymous;

import android.app.Activity;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import layout.Analyze;

/**
 * Created by disxc on 2016-10-13.
 */

public class Analyzer {
    String samplePayload0 = "POST /location HTTP/1.1\\r\\nContent-Type: application/json\\r\\nUser-Agent: Dalvik/2.1.0 (Linux; U; Android 5.1.1; Android SDK built for x86_64 Build/LMY48X)\\r\\nHost: 147.46.215.152:7979\\r\\nConnection: Keep-Alive\\r\\nAccept-Encoding: gzip\\r\\nContent-Length: 62\\r\\n\\r\\n[{\"message\":\"client\",\"latitude\":\"37.42\",\"longitude\":\"122.08\"}]\n";
    String samplePayload1 = "POST /location HTTP/1.1\\r\\nContent-Type: application/json\\r\\nUser-Agent: Dalvik/2.1.0 (Linux; U; Android 5.1.1; Android SDK built for x86_64 Build/LMY48X)\\r\\nHost: 147.46.215.152:7979\\r\\nConnection: Keep-Alive\\r\\nAccept-Encoding: gzip\\r\\nContent-Length: 30\\r\\n\\r\\n[{\"test1\":\"38\",\"test2\":\"120\"}]";
    CacheMaker cache;
    Context ctx;
    DatabaseHelper mDatabase;
    private onLogGeneratedListener mListener;

    public Analyzer(CacheMaker cm, Context context){
        cache = cm;
        ctx = context;
    }

    public void analyze(String appName, String payload){
        JSONObject payloadObject;
        JSONArray hookedTarget;
        String ret = "";
        String parsedResponse = responseParser(payload);
        if(parsedResponse.length() == 0){
            Log.d("Analyze", "Response is null...");
            return;
        }
        try {
            payloadObject = new JSONArray(parsedResponse).getJSONObject(0);
            JSONObject target = cache.getByAppId(appName);
            if(target == null){
                Toast.makeText(ctx, "Couldn't find app: " + appName, Toast.LENGTH_SHORT);
                Log.d("Analyze", "Couldn't find app:" + appName);
                return;
            }
            if(target.getString("Format").compareTo("json") != 0){
                throw new RuntimeException("not implemented Exception");
            }
            hookedTarget = target.getJSONArray("HookTarget");
            Log.d("Analyze", "target: " + payloadObject.toString());
            for(int i = 0; i < hookedTarget.length(); i++){
                JSONObject jo = hookedTarget.getJSONObject(i);
                Log.d("Analyze", jo.getString("Keyword"));
                String s = "";
                try {
                    String value = payloadObject.getString(jo.getString("Keyword"));
                    String type = jo.getString("Type");
                    s += type + ": " + value + "\n";
                    log(appName, "123", "127.0.0.1", type, value);
                    mListener.onLogGenerated(s);
                    ret += s;
                }
                catch(JSONException e){
                    Log.d("Analyze", "object parse failed:" + jo.toString());
                    continue;
                }
            }
            if(ret.compareTo("") == 0)
                Toast.makeText(ctx, "해당정보가 없습니다.", Toast.LENGTH_LONG).show();
            /*else
                Toast.makeText(ctx, ret, Toast.LENGTH_LONG).show();*/
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return;
    }

    public void sample(int i){
        if(i == 0){
            analyze("org.locationprivacy.locationprivacy", samplePayload0);
        }
        else{
            analyze("org.locationprivacy.locationprivacy", samplePayload1);
        }
    }

    public void log(String packageName, String timeStamp, String ip, String type, String value){
        //TODO: implement db transactions.
        //mDatabase.log(packageName, timeStamp, ip, type, value);

    }

    String responseParser(String httpResponse){
        String[] parts = httpResponse.split("nContent-Length: [0-9]+\\\\r\\\\n\\\\r\\\\n", 2);
        if(parts.length < 2){
            return "";
        }
        else{
            return parts[1].trim();
        }
    }

    public interface onLogGeneratedListener{
        void onLogGenerated(String log);
    }

    public void setOnLogGenerated(onLogGeneratedListener listener){
        mListener = listener;
    }
}
