package org.faysal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastApp {
    private static final String MULTICAST_GROUP_IP = "230.0.0.0";
    private static final int PORT = 4446;

    public static void main(String[] args) {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP_IP);
            String message = "Hello, Multicast Clients!";
            while (true) {
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), group, PORT);
                socket.send(packet);
                System.out.println("Message sent to multicast group.");
                Thread.sleep(1000); // Wait for 1 second before sending the next message
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}