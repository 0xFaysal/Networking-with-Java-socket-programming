package org.faysal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceCallServer_v1 {
    private static final int SERVER_PORT = 500;
    private Selector selector;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        VoiceCallServer_v1 server = new VoiceCallServer_v1();
        server.startServer();
    }

    private void startServer() {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(SERVER_PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server started on port " + SERVER_PORT);

            while (true) {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        handleAccept(serverSocketChannel);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept(ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client connected: " + socketChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024); // Use direct buffer
        try {
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                socketChannel.close();
                System.out.println("Client disconnected");
            } else {
                buffer.flip();
                executorService.submit(() -> {
                    try {
                        broadcast(buffer, socketChannel);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            try {
                socketChannel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("Client disconnected due to error: " + e.getMessage());
        }
    }

    private void broadcast(ByteBuffer buffer, SocketChannel sourceChannel) throws IOException {
        for (SelectionKey key : selector.keys()) {
            Channel targetChannel = key.channel();
            if (targetChannel instanceof SocketChannel && targetChannel != sourceChannel) {
                SocketChannel destChannel = (SocketChannel) targetChannel;
                try {
                    buffer.rewind();
                    destChannel.write(buffer);
                } catch (IOException e) {
                    destChannel.close();
                    System.out.println("Client disconnected due to error: " + e.getMessage());
                }
            }
        }
    }
}