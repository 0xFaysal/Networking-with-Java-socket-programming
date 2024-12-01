package org.faysal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TCP_Sender {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 1234);
        System.out.println("Connected to server on port 1234");
        new TCP_SenderThread(socket).start();

    }
}

class TCP_SenderThread extends Thread {
    private final Socket socket;
    int i = 0;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public TCP_SenderThread(Socket socket) throws IOException {
        this.socket = socket;
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                objectOutputStream.writeObject("Hello from client" + i++);
                objectOutputStream.flush();
                String message = (String) objectInputStream.readObject();
                System.out.println("Received: " + message);
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
