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

/**
 * Created by disxc on 2016-10-13.
 */

public class Analyzer {
    private CacheMaker cache;
    private Context ctx;
    private DatabaseHelper mDatabase;
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
                String keyword = jo.getString("Keyword");
                String type = jo.getString("Type");
                String s = "";

                Log.d("Analyze", "target : " + keyword);
                try {
                    String value = payloadObject.getString(keyword);
                    s += type + ": " + value + "\n";
                    log(appName, "127.0.0.1", type, value);
                    mListener.onLogGenerated(s);
                    ret += s;
                }
                catch(JSONException e){
                    Log.d("Analyze", "No such keyword:" + keyword);
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
        final String samplePayload1 = ctx.getResources().getString(R.string.sample_payload1);
        final String samplePayload2 = ctx.getResources().getString(R.string.sample_payload2);
        if(i == 0){
            analyze(sampleAppName, samplePayload2);
        }
        else{
            analyze(sampleAppName, samplePayload1);
        }
    }

    public void log(String packageName, String ip, String type, String value){
        Calendar c = Calendar.getInstance();

        SQLiteDatabase db = mDatabase.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(LogEntry.COLUMN_DATETIME, c.getTime().getTime());
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
