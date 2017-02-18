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
public class Analyzer {
    int SourcePort;
    String SourceIP;
    String payLoad;
   private Context mContext;
    Analyzer(int sP, String sIP, String data, Context mc){
        SourcePort = sP;
        SourceIP = sIP;
        payLoad = data;
        mContext = mc;
    }
    String getPackage() throws IOException {
        String cmd = "cat /proc/net/tcp6";              //cat /proc/uid/status
        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line =  null;
        Log.d("VpnServiceTest",SourceIP);
        String port = Integer.toHexString(SourcePort).toUpperCase();
        Log.d("VpnServiceTest",port);
        int Uid=0;
        while((line = br.readLine())!=null){
            Log.d("VpnServiceTest",line);
            String[] parser = line.trim().split(" ");
            if(parser[2].contains(port)==true) {
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
