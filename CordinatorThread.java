import java.net.*;
import java.io.*;

public class CordinatorThread extends Thread {

    public Process process;
    public String message;

    public CordinatorThread (Process process, String message) {

        this.process = process;
        this.message = message;

    }

    @Override
    public void run () {
        
        try {
            
            System.out.println("THREAD CORDINATOR");
            process.m = message.getBytes();
            process.messageOut = new DatagramPacket(process.m, process.m.length, process.group, 6789);
            process.multicastSocket.send(process.messageOut);	
            process.buffer = new byte[1000];
            process.cordinatorMessagesSent++;

            Thread.sleep(5000);

        } catch (Exception e) {

            System.out.println("Exception: " + e.getMessage());

        }

    }

}

