package org.faysal;

import org.faysal.model.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    static int userCount = 0;
    private static final List<ClientHandler> clientHandlers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Server is running on port 1234");
        System.out.println("IP: " + InetAddress.getLocalHost().getHostAddress());
        while (!serverSocket.isClosed()) {
            userCount++;
            Socket clientSocket = serverSocket.accept();
            System.out.println("User-" + userCount + " connected");
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ClientHandler clientHandler = new ClientHandler(clientSocket, outputStream);
            clientHandlers.add(clientHandler);
            new ServerThread(clientSocket, outputStream, clientHandler).start();
        }
    }

    public static void broadcastMessage(Message message, String senderUsername) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (!clientHandler.getUsername().equals(senderUsername)) {
                try {
                    clientHandler.getOutputStream().writeObject(message);
                    clientHandler.getOutputStream().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void sendMessageToUser(Message message, String receiverUsername) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getUsername().equals(receiverUsername)) {
                try {
                    clientHandler.getOutputStream().writeObject(message);
                    clientHandler.getOutputStream().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public static List<String> getActiveUsers() {
        List<String> activeUsers = new ArrayList<>();
        for (ClientHandler clientHandler : clientHandlers) {
            activeUsers.add(clientHandler.getUsername());
        }
        return activeUsers;
    }
}

class ServerThread extends Thread {
    private final Socket socket;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private final ClientHandler clientHandler;

    public ServerThread(Socket socket, ObjectOutputStream objectOutputStream, ClientHandler clientHandler) throws IOException {
        this.socket = socket;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        this.clientHandler = clientHandler;
    }

    @Override
    public void run() {
        try {
            // Register the client with the provided username
            Message initialMessage = (Message) objectInputStream.readObject();
            clientHandler.setUsername(initialMessage.getUsername());

            while (socket.isConnected()) {
                Message message = (Message) objectInputStream.readObject();
                System.out.println();
                System.out.println(STR."Received from user-\{message.getUsername()} :\{message}");
                if ("all".equalsIgnoreCase(message.getReceiver())) {
                    Server.broadcastMessage(message, message.getUsername());
                }
                else if ("list".equalsIgnoreCase(message.getMessage())) {
                    // Send the list of active users to the requesting client
                    List<String> activeUsers = Server.getActiveUsers();
                    Message userListMessage = new Message("server", String.join(", ", activeUsers), message.getUsername());
                    Server.sendMessageToUser(userListMessage, message.getUsername());
                }
                else {
                    Server.sendMessageToUser(message, message.getReceiver());
                }
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

class ClientHandler {
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private String username;

    public ClientHandler(Socket socket, ObjectOutputStream outputStream) {
        this.socket = socket;
        this.outputStream = outputStream;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}