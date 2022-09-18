import java.net.*;
import java.io.*;

public class SendMulticastMessage extends Thread {

    public Process process;
    public String message;

    public SendMulticastMessage (Process process, String message) {

        this.process = process;
        this.message = message;

    }

    @Override
    public void run () {
        
        try {
            
            process.m = message.getBytes();
            process.messageOut = new DatagramPacket(process.m, process.m.length, process.group, 6789);
            process.multicastSocket.send(process.messageOut);	
            process.buffer = new byte[1000];

        } catch (Exception e) {

            System.out.println("Exception: " + e.getMessage());

        }

    }

}

