package org.socialcoding.privacyguardian.VPN;
import java.nio.channels.SocketChannel;
import java.util.Vector;

/**
 * Created by 신승수 on 2017-01-25.
 */

public class SocketDB {
    private Vector<Long> ackVector    = new Vector<Long>();               //acks which should be sended.
    private Vector<Long> seqVector    = new Vector<Long>();               //seqs which should be sended.
    private Vector<String> ipVector     = new Vector<String>();            //source ip+port Strings       combine ip and port cuz there might be same ips and ports
    private Vector<SocketChannel> socketVector = new Vector<SocketChannel>();      //socketchannel  between server.
    SocketDB(){
    }
    void addSocket(long ack, long seq, String sourceIp, int sPort, SocketChannel socketChannel ){
        ackVector.add(ack);
        seqVector.add(seq);
        ipVector.add(sourceIp+":"+sPort);
        socketVector.add(socketChannel);
        return;
    }
    public void updateVectors(String sourceIp, int sPort){                     //update ack and seq numbers.
        int index =ipVector.indexOf(sourceIp+":"+sPort);
        long ack = ackVector.get(index);
        long seq = seqVector.get(index);

        ackVector.add(index, ack+1);
        seqVector.add(index, seq+1);

        return;
    }
    public boolean find(String sourceIp, int sPort){
        int index = ipVector.indexOf(sourceIp+":"+sPort);
        if(-1==index) return false;                     //no such that socket.
        else          return true;                      //there is.
    }
}
