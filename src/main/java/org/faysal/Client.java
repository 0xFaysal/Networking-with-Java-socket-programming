package org.faysal;

import org.faysal.model.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 1234);
        System.out.println("Connected to server on port 1234");
        new ClientSenderThread(socket).start();
        new ClientReceiverThread(socket).start();
    }
}

class ClientSenderThread extends Thread {
    private final Socket socket;
    private final ObjectOutputStream objectOutputStream;

    public ClientSenderThread(Socket socket) throws IOException {
        this.socket = socket;
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            // Send username to server
            objectOutputStream.writeObject(new Message(username));
            objectOutputStream.flush();

            while (socket.isConnected()) {

                String receiver = scanner.nextLine();
                System.out.print("Enter your message: ");
                String messageText = scanner.nextLine();

                Message message = new Message(username, messageText, receiver);
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ClientReceiverThread extends Thread {
    private final Socket socket;
    private final ObjectInputStream objectInputStream;

    public ClientReceiverThread(Socket socket) throws IOException {
        this.socket = socket;
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                Message receivedMessage = (Message) objectInputStream.readObject();

                System.out.println("\nReceived from "+ receivedMessage.getUsername()+": " + receivedMessage.getMessage());

            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}