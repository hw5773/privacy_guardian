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
            cmd += "udp6";
        }
        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line =  null;
        Log.d("VpnServiceTest", SourceIP);
        String port = Integer.toHexString(SourcePort).toUpperCase();
        Log.d("VpnServiceTest",port);
        int uid = 0, i = 0;
        int localAddrIdx = 0, uidIdx = 0;

        line = br.readLine();
        Log.d("VpnServiceTest", line);
        System.out.println("line: " + line);
        String[] keywords = line.trim().split("\\s+");

        for (i=0; i<keywords.length; i++) {

            if (keywords[i].contains("local_address")) {
                localAddrIdx = i;
                System.out.println("localAddrIdx: " + localAddrIdx);
            }

            if (keywords[i].contains("uid")) {
                uidIdx = i;
                System.out.println("uidIdx: " + uidIdx);
            }
        }

        while((line = br.readLine()) != null){
            System.out.println("Line: " + line);
            String[] values = line.trim().split(" ");
            if(values[localAddrIdx].contains(port)) {
                if(values[uidIdx].equals(""))
                    uid = 0;
                else
                    uid = Integer.parseInt(values[uidIdx]);
                Log.d("VpnServiceTest","uid is " + uid);
                break;
            }
            continue;
        }
        String Packagename = "";
        PackageManager pm = mContext.getPackageManager();
        String[] names = pm.getPackagesForUid(uid);
        if(names == null){
            Log.d("VpnServiceTest", "UID NOT FOUND : " + uid);
            return "";
        }
        Log.d("VpnServiceTest","name = " + names[0]);           //get package name
        process.destroy();
        return names[0];
    }
}
