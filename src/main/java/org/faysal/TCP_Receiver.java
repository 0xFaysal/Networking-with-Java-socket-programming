package org.faysal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCP_Receiver {
    static int userCount = 0;
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Server is running on port 1234");
        System.out.println("IP :"+InetAddress.getLocalHost().getHostAddress());
        while (!serverSocket.isClosed()){
            userCount++;
            new TCP_ReceiverThread(serverSocket.accept(),userCount).start();
        }
    }
}

class TCP_ReceiverThread extends Thread {
    private final Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    int userCount;


    public TCP_ReceiverThread(Socket socket,int userCount) {
        this.socket = socket;
        this.userCount = userCount;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();

    }}

    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                String message = (String) objectInputStream.readObject();
                System.out.println("Received from user-"+userCount+" " + message);
                objectOutputStream.writeObject("User-"+ userCount + " :"+ message);
                objectOutputStream.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                objectInputStream.close();
                objectOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

