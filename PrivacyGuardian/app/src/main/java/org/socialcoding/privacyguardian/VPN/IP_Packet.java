package org.socialcoding.privacyguardian.VPN;

/**
 * Created by 신승수 on 2016-10-25.
 */
public class IP_Packet {
    private byte[] packet;
    private int ihl;
    IP_Packet(byte[] input_packet){
        packet = input_packet;
    }
    int get_Ihl(){
        return ((int)(packet[0] & 0x0f)) * 4;
    }
}
