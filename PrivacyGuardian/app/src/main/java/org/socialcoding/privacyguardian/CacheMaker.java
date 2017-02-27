package org.socialcoding.privacyguardian;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.socialcoding.privacyguardian.Inteface.OnCacheMakerInteractionListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by disxc on 2016-09-27.
 */

public class CacheMaker extends AsyncTask<Void, Void, Void>{
    //create DB update and table creation method.
    private final String DATE_URL = "http://147.46.215.152:2507/lastupdate";
    private final String FETCH_URL = "http://147.46.215.152:2507/sensitiveinfo";

    private Context ctx;
    private String lastUpdate;
    private JSONArray jsonArray;
    private ArrayList<String> appsList;
    private String patchResultMessage;

    private OnCacheMakerInteractionListener mListener;

    public CacheMaker(Context context) {
        ctx = context;
        if(context instanceof OnCacheMakerInteractionListener){
            mListener = (OnCacheMakerInteractionListener) context;
        }
        else{
            throw new RuntimeException("Not implemented onCacheMakerIteractionListener");
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        String dateString = createHTTPConnection(DATE_URL);
        //if invalid dateString as connection failed..
        Log.d("CacheMaker", "datestring is :" + dateString);
        if( dateString.length() < 3){
            Log.d("CacheMaker", "Invalid date string... update from file");
            lastUpdate = openFromFile("ver");
            if(fetchFromFile()){
                patchResultMessage = "기존 DB를 사용합니다.";
            }
            else {
                patchResultMessage = "업데이트에 실패하였습니다.";
            }
            return null;
        }
        //if dateString is out-of-date
        else if (!checkUpdate(dateString)) {
            Log.d("creation", "updating to new version");
            fetchFromServer(dateString, createHTTPConnection(FETCH_URL));
        }
        // if dateString is up-to-date
        else {
            //it have valid version file
            Log.d("creation", "up to date");
            lastUpdate = dateString;
            fetchFromFile();
            patchResultMessage = "최신 DB입니다.";
        }

        //create new list of apps.
        appsList = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++){
            try {
                appsList.add(jsonArray.getJSONObject(i).getString("AppId"));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void params) {
        mListener.onCacheMakerCreated(this, patchResultMessage);
    }

    private String createHTTPConnection(String urlString){
        HttpConnect h = new HttpConnect();
        String ret = "";
        try {
            ret = h.execute(urlString).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return ret;
    }

    boolean fetchFromFile(){
        try {
            String fileString = openFromFile("json");
            if(fileString.compareTo("") == 0){
                Log.d("fetchFromFile", "file not found");
                return false;
            }
            jsonArray = new JSONArray(fileString);
            saveCacheData();
            return true;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void fetchFromServer(String dateString, String patchData){
        try {
            JSONObject jo = new JSONObject(patchData);
            jsonArray = new JSONArray(jo.getString("List"));
            lastUpdate = dateString;
            saveCacheData();
            Toast.makeText(ctx, "최신 DB로 업데이트 했습니다.", Toast.LENGTH_SHORT).show();
            Log.d("fetchFromServer", "Successful");
        }
        catch(Exception e) {
            Log.d("CacheMaker", "creating JSONArray failed");
            e.printStackTrace();
        }
    }

    public String openFromFile(String filename){
        try {
            InputStream inputstream = ctx.openFileInput(filename);
            if(inputstream != null){
                InputStreamReader in = new InputStreamReader(inputstream);
                BufferedReader br = new BufferedReader(in);
                String fileString;
                StringBuilder stringBuilder = new StringBuilder();

                while((fileString = br.readLine()) != null){
                    stringBuilder.append(fileString);
                }
                inputstream.close();
                return stringBuilder.toString();
            }
            else{
                Log.d("fetchFromFile", "File not found");
            }
        }
        catch (FileNotFoundException e){
            Log.d("readFromFile", "File Not Found:" + filename);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //newly is server-given last update. return false if outdated, return true if updated.
    public boolean checkUpdate(String newly){
        String ihave;
        ihave = openFromFile("ver");
        newly = newly.replaceAll("(\\r|\\n)", "");
        Log.d("checkUpdate", "newly :" + newly + " ihave :" + ihave);
        return ihave.compareTo(newly) == 0;
    }

    public JSONObject getByAppId(String key) throws JSONException {
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jo = jsonArray.getJSONObject(i);
            if(jo.getString("AppId").compareTo(key) == 0){
                return jo;
            }
        }
        Log.d("getByAppId", "cannot find APPID");
        return null;
    }

    // prints update date and cache data
    public void printCacheData(){
        Log.d("printCacheData", "cache :" + jsonArray.toString());
        Log.d("printCacheData", "last update : " + lastUpdate);
    }

    // write jsonArray and lastUpdate to file ("json", "ver")
    private void saveCacheData(){
        OutputStream outputStream;
        try {
            outputStream = ctx.openFileOutput("json", ctx.MODE_PRIVATE);
            outputStream.write(jsonArray.toString().getBytes());
            Log.d("saveCacheData", "writed:" + jsonArray.toString());
            outputStream.close();
            outputStream = ctx.openFileOutput("ver", ctx.MODE_PRIVATE);
            outputStream.write(lastUpdate.getBytes());
            outputStream.close();
            Log.d("saveCacheData", "writed:" + lastUpdate);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public List<String> getAppsList(){
        return appsList;
    }

}