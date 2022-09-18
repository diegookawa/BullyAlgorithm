import java.net.*;
import java.io.*;

public class ReceiveUnicastMessage extends Thread {

    public Process process;

    public ReceiveUnicastMessage (Process process) {

        this.process = process;

    }

    @Override
    public void run () {
        
        try {
            
            System.out.println("THREAD UNICAST");
            // process.socketReceive.setSoTimeout(3000);
            process.bufferUnicast = new byte[1000];
            DatagramPacket request = new DatagramPacket(process.bufferUnicast, process.bufferUnicast.length);
            process.socketReceive.receive(request);
            String messageReceived = new String(request.getData());
            process.lastMessageReceived[1] = messageReceived;
            System.out.println("\nReceived: " + messageReceived);

            if (messageReceived.charAt(0) == 'C') {

                process.lastMessageReceived[0] = "ELECTION_MESSAGE";

            }

            Thread.sleep(3000);

        } catch (Exception e) {
            
            System.out.println("TIMEOUT UNICAST");
            // process.isCordinator = true;
            // process.currentCordinator = process.id;
            // process.sendMessage("I am the Cordinator. My id is: " + process.id, process);
            // process.lastMessageReceived[0] = "NEW_CORDINATOR";

        }

    }

}

