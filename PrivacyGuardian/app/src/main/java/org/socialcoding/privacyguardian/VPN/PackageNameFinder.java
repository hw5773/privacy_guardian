package org.socialcoding.privacyguardian.VPN;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by 신승수 on 2016-09-22.
 */
public class PackageNameFinder {
   private Context mContext;
    PackageNameFinder(){

    }
    public static String getPackage(int SourcePort, String SourceIP, String payLoad, boolean isTCP, Context mContext) throws IOException {
        String cmd = "cat /proc/net/";              //cat /proc/uid/status

        if(isTCP){
            cmd += "tcp6";
        }
        else{
            cmd += "udp";
        }
        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line =  null;
        Log.d("VpnServiceTest", SourceIP);
        String port = Integer.toHexString(SourcePort).toUpperCase();
        Log.d("VpnServiceTest",port);
        int Uid=0;
        while((line = br.readLine())!=null){
            Log.d("VpnServiceTest",line);
            String[] parser = line.trim().split(" ");
            Log.d("parser", "parsed : " + parser[7]);
            if(parser[2].contains(port)) {
                if(parser[7].equals(""))
                    Uid=0;
                else
                    Uid = Integer.parseInt(parser[7]);
                Log.d("VpnServiceTest","uid is "+ Uid);
                break;
            }
            continue;
        }
        String Packagename = "";
        PackageManager pm = mContext.getPackageManager();
        String[] names = pm.getPackagesForUid(Uid);
        if(names == null){
            Log.d("VpnServiceTest", "UID NOT FOUND : " + Uid);
            return "";
        }
        Log.d("VpnServiceTest","name = " + names[0]);           //get package name
        return names[0];
        //test codes.
        /*ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
       // List<ActivityManager.RunningAppProcessInfo> tasks =am.getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> tasks =am.getRunningServices(1000);*/
        /*Log.d("VpnServiceTest","size of task = "+ String.valueOf(tasks.size()) );
        for(ActivityManager.RunningServiceInfo app : tasks){
            if(true){
                Log.d("VpnServiceTest",app.process +" : " +app.uid);
                //Packagename = app.processName;
            }
        }*/

    }
}
