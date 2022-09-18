import java.net.*;
import java.util.Scanner;

public class Process {

    public static final int NUMBER_OF_PROCESSES = 4;
    public static final int FAIL_MESSAGE_NUMBER = 8;
    public int id;
    public int cordinatorMessagesSent;
    public int currentCordinator;
    public int higherProcesses;
    public int processesAproved;
    public boolean isActive;
    public boolean isCordinator;
    public byte[] m;
    public byte[] buffer;
    public byte[] mUnicast;
    public byte[] bufferUnicast;
    public int[] processesId;
    public String[] lastMessageReceived;
    public MulticastSocket multicastSocket;
    public InetAddress group;
    public DatagramPacket messageOut;
    public DatagramPacket messageIn;
    public DatagramSocket socket;
    public DatagramSocket socketReceive;

    public Process (int id) {

        this.id = id;
        this.cordinatorMessagesSent = 0;
        this.currentCordinator = 0;
        this.higherProcesses = 0;
        this.processesAproved = 0;
        this.multicastSocket = null;
        this.group = null;
        this.messageOut = null;
        this.messageIn = null;
        this.buffer = null;
        this.m = null;
        this.lastMessageReceived = null;
        this.socket = null;
        this.socketReceive = null;
        this.bufferUnicast = null;
        this.mUnicast = null;
        this.isActive = true;
        this.isCordinator = false;
        
    }

    public static void main (String args[]) throws InterruptedException {
        // 239.192.0.1 IP group
        Scanner scanner; 
        String group_ip;
        String message;
        Process process;
        boolean electionStarted = false;
        boolean allProcessesArrived = false;
        int processesActive = 0;
        int option;
        int id;

        scanner = new Scanner(System.in);
        System.out.println("Insert your ID/PORT:");
        id = scanner.nextInt();
        process = new Process(id);
        scanner.nextLine();
        process.processesId = new int[NUMBER_OF_PROCESSES];
        process.lastMessageReceived = new String[2];
        process.initializeArray(process.processesId);

        try {

            process.socketReceive = new DatagramSocket(process.id);

        } catch (Exception e) {}

        try {

            System.out.println("Insert multicast group:");
            group_ip = scanner.nextLine();
            process.joinMulticastGroup(process, group_ip);
            process.sendMessage("Hello! I joined the group. My ID is: " + process.id, process);
            
            while (process.isActive) {	
                
                if (electionStarted)
                    process.receiveUnicastMessage(process);
                    
                process.receiveMessage(process, electionStarted);

                String typeMessage = process.lastMessageReceived[0];

                if (typeMessage == "NEW_PROCESS_ARRIVED") {
                    
                    String portNumber = "";
                    portNumber += process.lastMessageReceived[1].charAt(37) + "" + process.lastMessageReceived[1].charAt(38) + "" + process.lastMessageReceived[1].charAt(39) + "" + process.lastMessageReceived[1].charAt(40);
                    process.processesId[processesActive] = Integer.parseInt(portNumber);
                    processesActive++;
                    
                }

                if (typeMessage == "NEW_CORDINATOR") {

                    option = 0;
                    String portNumber = "";
                    portNumber += process.lastMessageReceived[1].charAt(31) + "" + process.lastMessageReceived[1].charAt(32) + "" + process.lastMessageReceived[1].charAt(33) + "" + process.lastMessageReceived[1].charAt(34);
                    process.currentCordinator = Integer.parseInt(portNumber);
                    electionStarted = false;

                }
                
                if (typeMessage == "ALL_PROCESSES_ARRIVED") {

                    allProcessesArrived = true;
                    process.getIdNumbers(process.processesId, process.lastMessageReceived[1]);
                    if (process.verifyIdRepeated(process.processesId))
                        process.sendMessage("ERROR: SAME ID VALUE", process);

                    else
                        electionStarted = true;

                }

                if (typeMessage == "CORDINATOR_FAILED") {

                    process.removeId(process.processesId, process.currentCordinator);
                    process.currentCordinator = 0;
                    electionStarted = true;

                }

                if (typeMessage == "ELECTION_MESSAGE") {
                    
                    String portNumber = "";
                    portNumber += process.lastMessageReceived[1].charAt(35) + "" + process.lastMessageReceived[1].charAt(36) + "" + process.lastMessageReceived[1].charAt(37) + "" + process.lastMessageReceived[1].charAt(38);
                    option = scanner.nextInt();
                    scanner.nextLine();
                    process.sendUnicastMessage(String.valueOf(option), process, Integer.parseInt(portNumber));

                }

                if (typeMessage == "ID_REPEATED") {

                    process.multicastSocket.leaveGroup(process.group);
                    break;

                }

                if (processesActive == NUMBER_OF_PROCESSES) {
                    
                    message = "A";

                    for (int i = 0; i < process.processesId.length; i++){

                        message += process.processesId[i];
            
                    }
                    
                    process.sendMessage(message, process);
                    processesActive = 0;

                }

                if (electionStarted && allProcessesArrived) {

                    System.out.println("\n\n\nELECTION HAS STARTED\n\n\n");
                    
                    process.higherProcesses = 0;
                    process.processesAproved = 0;
                    process.currentCordinator = 0;
                    if (process.id == process.getHighestId(process.processesId)) {

                        System.out.println("Your ID is the highest. Would you like to be the cordinator?\n1 - Yes\n2 - No");
                        option = scanner.nextInt();
                        scanner.nextLine();

                        if (option == 1) {

                            process.isCordinator = true;
                            process.currentCordinator = process.id;
                            process.sendMessage("I am the Cordinator. My id is: " + process.id, process);
                            option = 0;
    
                        }

                    }

                    else {

                        System.out.println("Your ID is not the highest. You can start an election though. Would you like to?\n1 - Yes\n2 - No");
                        option = scanner.nextInt();
                        scanner.nextLine();

                        if(option == 1) {

                            for (int i = 0; i < NUMBER_OF_PROCESSES; i++) {

                                if (process.processesId[i] > process.id) {
                                    
                                    process.higherProcesses++;

                                }

                            }

                            boolean processAllowed = true;

                            for (int i = 0; i < NUMBER_OF_PROCESSES; i++) {

                                if (process.processesId[i] > process.id) {
                                    
                                    if (process.currentCordinator == 0) {
                                        
                                        process.sendUnicastMessage("Can I be the Cortinator? My id is: " + process.id + " \nWait for timeout or type (1) if no", process, process.processesId[i]);

                                    }

                                }

                                
                            }

                            for (int i = 0; i < NUMBER_OF_PROCESSES; i++) {

                                if (process.processesId[i] > process.id) {
                                    
                                    if (process.currentCordinator == 0) {
                                        
                                        process.receiveUnicastMessage(process);
                                        if (process.lastMessageReceived[0] == "ELECTION_NOT_ALLOWED")
                                            processAllowed = false;
                                    }

                                }

                                
                            }

                            if (processAllowed){

                                process.isCordinator = true;
                                process.currentCordinator = process.id;
                                process.sendMessage("I am the Cordinator. My id is: " + process.id, process);
                                process.higherProcesses = 0;
                                process.processesAproved = 0;
                                electionStarted = false;

                            }

                            option = 0;
    
                        }

                    }

                }

                if (process.isCordinator) {    

                    process.sendMessage("Olá\n", process);
                    process.cordinatorMessagesSent++;
                    electionStarted = false;
                    
                }         

                if (process.cordinatorMessagesSent == FAIL_MESSAGE_NUMBER) {

                    process.cordinatorMessagesSent = 0;
                    process.isCordinator = false;
                    process.isActive = false;
                    scanner.close();
                    process.multicastSocket.leaveGroup(process.group);

                }

                process.buffer = new byte[1000];
                System.out.flush();
                Thread.sleep(1000);
                
            }
            
        } catch (Exception e) {

            System.out.println("Exception: " + e.getMessage());

        }  finally {

            if (process.multicastSocket != null)
                process.multicastSocket.close();
        
        }
        
    }

    private void initializeArray(int[] array) {

        for (int i = 0; i < array.length; i++){

            array[i] = 0;

        }

    }

    private void getIdNumbers(int[] array, String message) {

        int pos = 1;

        for (int i = 0; i < array.length; i++){

            String portNumber = "";
            portNumber += message.charAt(pos) + "" + message.charAt(pos + 1) + "" + message.charAt(pos + 2) + "" + message.charAt(pos + 3);
            array[i] = Integer.parseInt(portNumber);
            pos += 4;

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

    private void sendUnicastMessage(String message, Process process, int port) {

        try {

            process.socket = new DatagramSocket();
            process.mUnicast = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(process.mUnicast,  message.length(), aHost, port);
            process.socket.send(request);
            process.bufferUnicast = new byte[1000];

        } catch (Exception e) {}
        
    }

    private void receiveMessage(Process process, boolean electionStarted) {

        try {

            if(!electionStarted)
                process.multicastSocket.setSoTimeout(5000);
                
            else
                process.multicastSocket.setSoTimeout(15000);

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

            else {

                process.lastMessageReceived[0] = "ERROR";

            }

        } catch (Exception e) {

            System.out.println("TIMEOUT CORDINATOR FAILED");
            process.lastMessageReceived[0] = "CORDINATOR_FAILED";

        }

    }

    private void receiveUnicastMessage(Process process) {

        try {

            process.socketReceive.setSoTimeout(3000);
            process.bufferUnicast = new byte[1000];
            DatagramPacket request = new DatagramPacket(process.bufferUnicast, process.bufferUnicast.length);
            process.socketReceive.receive(request);
            String messageReceived = new String(request.getData());
            process.lastMessageReceived[1] = messageReceived;
            System.out.println("\nReceived: " + messageReceived);

            if (messageReceived.charAt(0) == 'C') {

                process.lastMessageReceived[0] = "ELECTION_MESSAGE";

            }

            if (messageReceived.charAt(0) == '1') {

                System.out.print("ENTROU");
                process.lastMessageReceived[0] = "ELECTION_NOT_ALLOWED";

            }
            

        } catch (Exception e) {}

    }

    private void removeId(int[] array, int id) {

        for (int i = 0; i < array.length; i++) {

            if(id == array[i])
                array[i] = -1;

        }

    }

    private boolean verifyIdRepeated(int[] array) {

        int repeated, idNumber;

        for (int i = 0; i < NUMBER_OF_PROCESSES; i++) {

            repeated = 0;
            idNumber = array[i];

            for (int j = 0; j < NUMBER_OF_PROCESSES; j++) {

                if (array[j] == idNumber)
                    repeated++;

            }

            if (repeated >= 2)
                return true;

        }

        return false;

    }

}