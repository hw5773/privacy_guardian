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

    public static String getPackage(boolean isTCP, int sourcePort, Context mContext) throws IOException {
        String cmd = "cat /proc/net/";              //cat /proc/uid/status

        if(isTCP){
            cmd += "tcp6";
        } else {
            System.out.println("UDP is not supported yet");
            return "";
        }

        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line =  null;

        System.out.println("Find the Package name about " + sourcePort);
        String port = Integer.toHexString(sourcePort).toUpperCase();
        int uid = 0, i = 0;
        int localAddrIdx = 0, uidIdx = 0;

        line = br.readLine();
        String[] keywords = line.trim().split("\\s+");

        for (i=0; i<keywords.length; i++) {

            if (keywords[i].contains("local_address")) {
                localAddrIdx = i;
            }

            if (keywords[i].contains("uid")) {
                uidIdx = i - 2;
            }
        }

        while((line = br.readLine()) != null){
            String[] values = line.trim().split(" ");
            if(values[localAddrIdx].contains(port)) {

                if(values[uidIdx].equals(""))
                    uid = 0;
                else
                    uid = Integer.parseInt(values[uidIdx]);

                break;
            }
            continue;
        }
        String Packagename = "";
        PackageManager pm = mContext.getPackageManager();
        String[] names = pm.getPackagesForUid(uid);

        if(names == null){
            System.out.println("UID NOT FOUND : " + uid);
            return "";
        }
        //System.out.println("name = " + names[0]);
        process.destroy();
        return names[0];
    }
}
