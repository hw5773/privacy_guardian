package org.socialcoding.privacyguardian;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Vector;

import org.socialcoding.privacyguardian.Structs.SensitiveInfoTypes;

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

    public void analyze(String packageName, String payload){
        JSONObject payloadObject;
        JSONArray hookedTarget;
        String ret = "";
        HttpResponse httpResponse = new HttpResponse(payload);
        String responseBody = httpResponse.responseBody;

        String latFound = null;
        String lngFound = null;
        String latlngHost = null;

        if(responseBody.length() == 0){
            Log.d("AnalyzeFragment", "Response is null...");
            return;
        }
        if(httpResponse.contentType.compareTo("application/json") != 0){
            Log.d("AnalyzeFragment", "dropping non-json payload");
            return;
        }

        try {
            payloadObject = new JSONArray(responseBody).getJSONObject(0);
            JSONObject target = cache.getByAppId(packageName);
            if(target == null){
                Toast.makeText(ctx, "분석할 수 없는 앱: " + packageName, Toast.LENGTH_SHORT);
                Log.d("AnalyzeFragment", "Couldn't find app:" + packageName);
                return;
            }

            //Cache is only implmented for json
            if(target.getString("Format").compareTo("json") != 0){
                throw new RuntimeException("not implemented Exception");
            }

            hookedTarget = target.getJSONArray("HookTarget");
            Log.d("AnalyzeFragment", "target: " + payloadObject.toString());
            Vector<Vector<String>> logvector = new Vector<>();
            for(int i = 0; i < hookedTarget.length(); i++){
                JSONObject jo = hookedTarget.getJSONObject(i);
                String keyword = jo.getString("Keyword");
                String type = jo.getString("Type");
                String s = "";
                Vector<String> v = new Vector<>();
                Log.d("AnalyzeFragment", "target : " + keyword);
                try {
                    String value = payloadObject.getString(keyword);
                    s += type + ": " + value + "\n";
                    //TODO: Implment Host IP
                    v.add("127.0.0.1");
                    v.add(type);
                    v.add(value);
                    logvector.add(v);
                    if(type.compareTo(SensitiveInfoTypes.TYPE_LOCATION_LAT) == 0){
                        latFound = value;
                        latlngHost = "127.0.0.1";
                    }
                    if(type.compareTo(SensitiveInfoTypes.TYPE_LOCATION_LNG) == 0){
                        lngFound = value;
                    }
                    //log(appName, "127.0.0.1", type, value);
                    mListener.onLogGenerated();
                    ret += s;
                }
                catch(JSONException e){
                    Log.d("AnalyzeFragment", "No such keyword:" + keyword);
                    continue;
                }
            }

            for(int i = 0; i < logvector.size(); i++){
                Vector<String> c = logvector.elementAt(i);
                String host = c.elementAt(0);
                String type = c.elementAt(1);
                String value = c.elementAt(2);
                if(latFound != null && lngFound != null && type.contains("Location.")){
                    continue;
                }
                log(packageName, host, type, value);
            }

            //when lat lng both found
            if(latFound != null && lngFound !=null){
                log(packageName, latlngHost, SensitiveInfoTypes.TYPE_LOCATION_LATLNG, latFound +";" + lngFound);
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

        //saves time in milliseconds
        values.put(DatabaseHelper.LogEntry.COLUMN_DATETIME, c.getTime().getTime());
        values.put(DatabaseHelper.LogEntry.COLUMN_PACKAGE_NAME, packageName);
        values.put(DatabaseHelper.LogEntry.COLUMN_HOST_ADDRESS, ip);
        values.put(DatabaseHelper.LogEntry.COLUMN_DATA_TYPE, type);
        values.put(DatabaseHelper.LogEntry.COLUMN_DATA_VALUE, value);

        long newRowId = db.insert(DatabaseHelper.LogEntry.TABLE_NAME, null, values);
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
        void onLogGenerated();
    }

    public void setOnLogGenerated(onLogGeneratedListener listener){
        mListener = listener;
    }
}
