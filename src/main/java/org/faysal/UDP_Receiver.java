package org.faysal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;

public class UDP_Receiver {
    static Random random = new Random();
    private static final int PORT = random.nextInt(65535);

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Message received: " + received);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}