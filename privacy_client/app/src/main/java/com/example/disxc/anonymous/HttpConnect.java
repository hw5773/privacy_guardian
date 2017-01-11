package com.example.disxc.anonymous;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by disxc on 2016-09-23.
 */

public class HttpConnect extends AsyncTask<String, Object, String> {
    //Context ctx;
    public HttpConnect() {
        //ctx = context;
    }

    @Override
    public String doInBackground(String... params) {
        String ret = ":(";
        try {
            URL url = new URL(params[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET"); // get방식 통신
            //urlConnection.setDoOutput(true);       // 쓰기모드 지정 + 다른 port를 사용할 때 사용하면 Exception
            urlConnection.setDoInput(true);        // 읽기모드 지정
            urlConnection.setUseCaches(false);     // 캐싱데이터를 받을지 안받을지
            urlConnection.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정
            try {
                Log.d("httpconn", "fetching");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                StringBuilder builder = new StringBuilder();   //문자열을 담기 위한 객체
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));  //문자열 셋 세팅
                Log.d("httpconn", "building");
                String line = "";
                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }
                //Log.d("httpconn", url.getProtocol() + builder.toString());
                //print(builder.toString());
                ret = builder.toString();

            } catch(IOException e) {
                Log.d("httpconn", "something gone wrong in connection\n" + e.toString());

            } finally {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException | ProtocolException exception) {
            exception.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }

        return ret;
    }
}