import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Process extends Thread {

    public static final int NUMBER_OF_PROCESSES = 4;
    public int id;
    public int port;
    public boolean isActive;
    public boolean isCordinator;
    int[] processesId;
    byte [] m;
    byte[] buffer;
    MulticastSocket multicastSocket;
    InetAddress group;
    DatagramPacket messageOut;
    DatagramPacket messageIn;
    String lastMessageReceived;
    String lastMessageReceivedType;

    public Process (int id) {

        this.id = id;
        this.isActive = true;
        this.isCordinator = false;
        this.port = 0;
        this.multicastSocket = null;
        this.group = null;
        this.messageOut = null;
        this.messageIn = null;
        this.buffer = null;
        this.m = null;
        this.lastMessageReceivedType = "";
        this.lastMessageReceived = "";
        
    }

    public static void main (String args[]) throws InterruptedException {
        // 239.192.0.1 IP group
        Scanner scanner = new Scanner(System.in);
        
        Process process;
        String group_ip;
        String message;
        
        int id;
        int option;
        int contId = 0;
        boolean electionStarted = false;

        System.out.println("Insert your ID:");
        id = scanner.nextInt();
        process = new Process(id);
        scanner.nextLine();
        process.processesId = new int[NUMBER_OF_PROCESSES];
        process.initializeArray(process.processesId);

        try {

            System.out.println("Insert multicast group:");
            group_ip = scanner.nextLine();
            
            process.joinMulticastGroup(process, group_ip);
            process.sendMessage("Hello! I joined the group. My ID is: " + process.id, process);
            
            while (true) {	
                
                process.receiveMessage(process);
                
                if (process.lastMessageReceivedType == "NEW_PROCESS_ARRIVED") {
                    
                    process.processesId[contId] = Character.getNumericValue(process.lastMessageReceived.charAt(37));
                    contId++;
                    
                }
                
                if (process.lastMessageReceivedType == "ALL_PROCESSES_ARRIVED"){

                    process.getIdNumbers(process.processesId, process.lastMessageReceived);
                    electionStarted = true;

                }

                if (contId == 4) {
                    
                    message = "A";

                    for (int i = 0; i < process.processesId.length; i++){

                        message += process.processesId[i];
            
                    }
                    
                    process.sendMessage(message, process);

                }

                if (electionStarted) {

                    System.out.println("\n\n\nELECTION HAS STARTED\n\n\n");

                    if (process.id == process.getHighestId(process.processesId)){

                        System.out.println("Your ID is the highest. Would you like to be the cordinator?\n1 - Yes\n2 - No");
                        option = scanner.nextInt();

                        if(option == 1) {

                            process.isCordinator = true;
                            message = "I am the Cordinator. My id is: " + process.id;
                            process.sendMessage(message, process);
    
                        }

                    }

                    electionStarted = false;

                }

                if (process.isCordinator) {    

                    message = "\nOlá";
                    process.m = message.getBytes();
                    process.messageOut = new DatagramPacket(process.m, process.m.length, process.group, 6789);
                    process.multicastSocket.send(process.messageOut);	
                    process.buffer = new byte[1000];
                    electionStarted = false;

                }         

                System.out.flush();
                Thread.sleep(1000);
                
                // System.out.println("\nWould you like to leave the group?\n1 - Yes\n2 - No");        
                // option = scanner.nextInt();  
                // scanner.nextLine();

                // if (option == 1) {

                //     process.multicastSocket.leaveGroup(process.group);
                //     break;

                // }
                
            }
            
            // scanner.close();

        } catch (SocketException e) {

            System.out.println("Socket: " + e.getMessage());

        } catch (IOException e) {

            System.out.println("IO: " + e.getMessage());

        } finally {

            if (process.multicastSocket != null) process.multicastSocket.close();
        
        }

    }

    private void initializeArray(int[] array) {

        for (int i = 0; i < array.length; i++){

            array[i] = 0;

        }

    }

    private void printArray(int[] array) {

        for (int i = 0; i < array.length; i++){

            System.out.print(array[i] + " ");

        }

    }

    private void getIdNumbers(int[] array, String message){

        for (int i = 0; i < array.length; i++){

            array[i] = Character.getNumericValue(message.charAt(i + 1));

        }

    }

    private int getHighestId(int[] array) {

        int highest = array[0];

        for (int i = 1; i < array.length; i++){

            if(array[i] > highest)
                highest = array[i];

        }

        return highest;

    }

    private void joinMulticastGroup(Process process, String group_ip) {

        try {

            process.group = InetAddress.getByName(group_ip);
            process.multicastSocket = new MulticastSocket(6789);
            process.multicastSocket.joinGroup(process.group);

        } catch (Exception e) {

            System.out.println("Exception: " + e.getMessage());

        }

    }

    private void sendMessage(String message, Process process) {

        try {

            process.m = message.getBytes();
            process.messageOut = new DatagramPacket(m, m.length, process.group, 6789);
            process.multicastSocket.send(process.messageOut);	
            process.buffer = new byte[1000];

        } catch (Exception e) {

            System.out.println("Exception: " + e.getMessage());

        }

    }

    private void receiveMessage(Process process) {

        try {

            process.messageIn = new DatagramPacket(process.buffer, process.buffer.length);
            process.multicastSocket.receive(process.messageIn);
            process.lastMessageReceived = new String(process.messageIn.getData());
            System.out.println("\nReceived: " + lastMessageReceived);

            if (process.lastMessageReceived.charAt(0) == 'H')
                process.lastMessageReceivedType = "NEW_PROCESS_ARRIVED";

            else if (process.lastMessageReceived.charAt(0) == 'A')
                process.lastMessageReceivedType = "ALL_PROCESSES_ARRIVED";


        } catch (Exception e) {

            System.out.println("Exception: " + e.getMessage());

        }

    }

}