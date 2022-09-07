import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Process {

    public static final int NUMBER_OF_PROCESSES = 4;
    private int id;
    private int port;
    private boolean isActive;
    private boolean isCordinator;
    private byte [] m;
    private byte[] buffer;
    private int[] processesId;
    private int cordinatorMessagesSent;
    private MulticastSocket multicastSocket;
    private InetAddress group;
    private DatagramPacket messageOut;
    private DatagramPacket messageIn;
    private String lastMessageReceived;

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
        this.lastMessageReceived = "";
        this.cordinatorMessagesSent = 0;

    }

    public static void main (String args[]) throws InterruptedException {
        // 239.192.0.1 IP group
        Scanner scanner = new Scanner(System.in);
        
        Process process;
        String group_ip;
        String message;
        int id;
        int option;
        int processesActive = 0;
        boolean electionStarted = false;

        System.out.println("Insert your ID (Must be a number between 1 and 4):");
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
            
            while (process.isActive) {	
                
                process.receiveMessage(process);
                char typeMessage = process.lastMessageReceived.charAt(0);
                
                if (typeMessage == 'H') {
                    
                    process.processesId[processesActive] = Character.getNumericValue(process.lastMessageReceived.charAt(37));
                    processesActive++;
                    
                }
                
                if (typeMessage == 'A'){

                    process.getIdNumbers(process.processesId, process.lastMessageReceived);
                    electionStarted = true;

                }

                if (typeMessage == 'S'){

                    int idToRemove = Character.getNumericValue(process.lastMessageReceived.charAt(48));
                    process.removeId(process.processesId, idToRemove);
                    electionStarted = true;

                }

                if (processesActive == NUMBER_OF_PROCESSES) {
                    
                    message = "A";

                    for (int i = 0; i < process.processesId.length; i++){

                        message += process.processesId[i];
            
                    }
                    
                    process.sendMessage(message, process);
                    processesActive = 0;

                }

                if (electionStarted) {

                    System.out.println("\n\n\nELECTION HAS STARTED\n\n\n");

                    if (process.id == process.getHighestId(process.processesId)){

                        System.out.println("Your ID is the highest. Would you like to be the cordinator?\n1 - Yes\n2 - No");
                        option = scanner.nextInt();
                        scanner.nextLine();

                        if(option == 1) {

                            process.isCordinator = true;
                            process.sendMessage("I am the Cordinator. My id is: " + process.id, process);
                            option = 0;
    
                        }

                    }

                    else {

                        System.out.println("Your ID is not the highest. You can start an election though. Would you like to?\n1 - Yes\n2 - No");
                        option = scanner.nextInt();
                        scanner.nextLine();

                        if(option == 1) {

                            // Begin Unicast algorithm
                            option = 0;
    
                        }

                    }

                    electionStarted = false;

                }

                if (process.isCordinator) {    

                    process.sendMessage("Olá\n", process);
                    process.cordinatorMessagesSent++;
                    electionStarted = false;

                }         

                if (process.cordinatorMessagesSent == 9) {

                    process.sendMessage("Sorry I failed being the cordinator. My id was: " + process.id, process);
                    process.cordinatorMessagesSent = 0;
                    process.isCordinator = false;
                    process.isActive = false;
                    scanner.close();
                    process.multicastSocket.leaveGroup(process.group);

                }

                process.buffer = new byte[1000];
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

        } catch (Exception e) {

            System.out.println("Socket: " + e.getMessage());

        }  finally {

            if(process.multicastSocket != null) process.multicastSocket.close();
        
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
            String messageReceived = new String(process.messageIn.getData());
            process.lastMessageReceived = messageReceived;
            System.out.println("\nReceived: " + messageReceived);

        } catch (Exception e) {

            System.out.println("Exception: " + e.getMessage());

        }

    }

    private void removeId(int[] array, int id) {

        for (int i = 0; i < array.length; i++){

            if(id == array[i])
                array[i] = -1;

        }

    }

}