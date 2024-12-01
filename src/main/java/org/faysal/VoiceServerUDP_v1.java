package org.faysal;

import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class VoiceServerUDP_v1 {
    private static final int SERVER_PORT = 5000;
    private static final int BUFFER_SIZE = 512; // Reduced buffer size
    private DatagramSocket socket;
    private Set<InetSocketAddress> clientAddresses = new HashSet<>();

    public static void main(String[] args) {
        new VoiceServerUDP_v1().start();
    }

    public void start() {
        try {
            socket = new DatagramSocket(SERVER_PORT);
            System.out.println("Server started on port " + SERVER_PORT);
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                InetSocketAddress clientAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
                clientAddresses.add(clientAddress);

                broadcast(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcast(DatagramPacket packet) {
        for (InetSocketAddress clientAddress : clientAddresses) {
            if (!clientAddress.equals(new InetSocketAddress(packet.getAddress(), packet.getPort()))) {
                try {

                    DatagramPacket sendPacket = new DatagramPacket(packet.getData(), packet.getLength(), clientAddress.getAddress(), clientAddress.getPort());
                    socket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}