package com.example.disxc.anonymous;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import com.example.disxc.anonymous.DatabaseHelper.LogEntry;

import layout.Analyze;

/**
 * Created by disxc on 2016-10-13.
 */

public class Analyzer {
    CacheMaker cache;
    Context ctx;
    DatabaseHelper mDatabase;
    private onLogGeneratedListener mListener;

    public Analyzer(CacheMaker cm, Context context){
        cache = cm;
        ctx = context;
        mDatabase = new DatabaseHelper(context);
    }

    public void analyze(String appName, String payload){
        JSONObject payloadObject;
        JSONArray hookedTarget;
        String ret = "";
        HttpResponse httpResponse = new HttpResponse(payload);
        String responseBody = httpResponse.responseBody;
        if(responseBody.length() == 0){
            Log.d("Analyze", "Response is null...");
            return;
        }
        if(httpResponse.contentType.compareTo("application/json") != 0){
            Log.d("Analyze", "dropping non-json payload");
            return;
        }

        try {
            payloadObject = new JSONArray(responseBody).getJSONObject(0);
            JSONObject target = cache.getByAppId(appName);
            if(target == null){
                Toast.makeText(ctx, "분석할 수 없는 앱: " + appName, Toast.LENGTH_SHORT);
                Log.d("Analyze", "Couldn't find app:" + appName);
                return;
            }

            //Cache is only implmented for json
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
                    log(appName, "127.0.0.1", type, value);
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
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return;
    }

    public void runSamplePayload(int i){
        final String sampleAppName = "org.locationprivacy.locationprivacy";
        final String samplePayload0 = "POST /location HTTP/1.1\\r\\nContent-Type: application/json\\r\\nUser-Agent: Dalvik/2.1.0 (Linux; U; Android 5.1.1; Android SDK built for x86_64 Build/LMY48X)\\r\\nHost: 147.46.215.152:7979\\r\\nConnection: Keep-Alive\\r\\nAccept-Encoding: gzip\\r\\nContent-Length: 62\\r\\n\\r\\n[{\"message\":\"client\",\"latitude\":\"37.42\",\"longitude\":\"122.08\"}]\n";
        final String samplePayload1 = "POST /location HTTP/1.1\\r\\nContent-Type: application/json\\r\\nUser-Agent: Dalvik/2.1.0 (Linux; U; Android 5.1.1; Android SDK built for x86_64 Build/LMY48X)\\r\\nHost: 147.46.215.152:7979\\r\\nConnection: Keep-Alive\\r\\nAccept-Encoding: gzip\\r\\nContent-Length: 30\\r\\n\\r\\n[{\"test1\":\"38\",\"test2\":\"120\"}]";
        if(i == 0){
            analyze(sampleAppName, samplePayload0);
        }
        else{
            analyze(sampleAppName, samplePayload1);
        }
    }

    public void log(String packageName, String ip, String type, String value){
        Calendar c = Calendar.getInstance();

        SQLiteDatabase db = mDatabase.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(LogEntry.COLUMN_DATETIME, c.getTime().toString());
        values.put(LogEntry.COLUMN_PACKAGE_NAME, packageName);
        values.put(LogEntry.COLUMN_HOST_ADDRESS, ip);
        values.put(LogEntry.COLUMN_DATA_TYPE, type);
        values.put(LogEntry.COLUMN_DATA_VALUE, value);

        long newRowId = db.insert(LogEntry.TABLE_NAME, null, values);
    }

    public class HttpResponse{
        public String responseBody = "";
        public String contentType = "";
        public HttpResponse(String str){
            try{
                responseBody = str.split("\\\\r\\\\n\\\\r\\\\n", 2)[1].trim();
                contentType = str.split("Content-Type: ", 2)[1].split("\\\\r\\\\n")[0].trim();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public interface onLogGeneratedListener{
        void onLogGenerated(String log);
    }

    public void setOnLogGenerated(onLogGeneratedListener listener){
        mListener = listener;
    }
}
