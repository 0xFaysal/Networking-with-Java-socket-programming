package org.faysal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP_Sender {
    private static final String RECEIVER_IP = "10.15.38.139"; // Replace with receiver's IP address
    private static final int PORT = 4446;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress receiverAddress = InetAddress.getByName(RECEIVER_IP);
            String message = "Hello, UDP Receiver!";
            while (true) {
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), receiverAddress, PORT);
                socket.send(packet);
                System.out.println("Message sent to receiver.");
                Thread.sleep(1000); // Wait for 1 second before sending the next message
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}