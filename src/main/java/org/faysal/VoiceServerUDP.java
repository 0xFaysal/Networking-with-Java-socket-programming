package org.faysal;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceServerUDP {
    private static final int SERVER_PORT = 5000;
    private static final int BUFFER_SIZE = 256; // Increased buffer size
    private Selector selector;
    private Set<SocketAddress> clientAddresses = new HashSet<>();
    private DatagramChannel serverChannel;
    private ExecutorService threadPool = Executors.newFixedThreadPool(10); // Thread pool to handle multiple clients

    public static void main(String[] args) {
        new VoiceServerUDP().start();
    }

    public void start() {
        try {
            selector = Selector.open();
            serverChannel = DatagramChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(SERVER_PORT));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_READ);

            System.out.println("Server started on port " + SERVER_PORT);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRead(SelectionKey key) {
        DatagramChannel channel = (DatagramChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        try {
            SocketAddress clientAddress = channel.receive(buffer);
            if (clientAddress != null) {
                System.out.println("Received packet from " + clientAddress + " with length " + buffer.position());
                clientAddresses.add(clientAddress);
                buffer.flip();
                threadPool.submit(() -> broadcast(buffer, clientAddress));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcast(ByteBuffer buffer, SocketAddress sourceAddress) {
        for (SocketAddress clientAddress : clientAddresses) {
            if (!clientAddress.equals(sourceAddress)) {
                try {
                    buffer.rewind();
                    serverChannel.send(buffer, clientAddress);
                    System.out.println("Sent packet to " + clientAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}