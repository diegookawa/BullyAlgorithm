import java.net.*;
import java.io.*;

public class ReceiveMulticastMessage extends Thread {

    public Process process;

    public ReceiveMulticastMessage (Process process) {

        this.process = process;

    }

    @Override
    public void run () {

        try {

            System.out.println("THREAD RECEIVEMULTICAST");
            // process.multicastSocket.setSoTimeout(5000);
            process.messageIn = new DatagramPacket(process.buffer, process.buffer.length);
            process.multicastSocket.receive(process.messageIn);
            String messageReceived = new String(process.messageIn.getData());
            process.lastMessageReceived[1] = messageReceived;
            System.out.println("\nReceived: " + messageReceived);

            if (messageReceived.charAt(0) == 'H') {

                process.lastMessageReceived[0] = "NEW_PROCESS_ARRIVED";

            }

            else if (messageReceived.charAt(0) == 'A') {

                process.lastMessageReceived[0] = "ALL_PROCESSES_ARRIVED";

            }

            else if (messageReceived.charAt(0) == 'S') {

                process.lastMessageReceived[0] = "CORDINATOR_FAILED";

            }

            else if (messageReceived.charAt(0) == 'I') {

                process.lastMessageReceived[0] = "NEW_CORDINATOR";

            }

            else if (messageReceived.charAt(0) == 'E') {

                process.lastMessageReceived[0] = "ID_REPEATED";

            }

            else if (messageReceived.charAt(0) == 'O') {

                process.lastMessageReceived[0] = "OLA_MESSAGE";

            }
            
            else {

                process.lastMessageReceived[0] = "ERROR";

            }

            Thread.sleep(3000);

        } catch (Exception e) {

            
            if (process.lastMessageReceived[0] == "OLA_MESSAGE") {
                
                process.lastMessageReceived[0] = "CORDINATOR_FAILED";
                System.out.println("TIMEOUT: CORDINATOR FAILED");

            }

        }

    }

}

