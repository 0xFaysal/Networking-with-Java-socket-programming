package org.faysal;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class VoiceCallClient_v1 {
    private static final String SERVER_ADDRESS = "10.15.35.53";
    private static final int SERVER_PORT = 5000;
    private boolean stopCapture = false;
    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;
    private SourceDataLine sourceDataLine;
    private SocketChannel socketChannel;

    public static void main(String[] args) {
        VoiceCallClient_v1 client = new VoiceCallClient_v1();
        client.captureAudio();
    }

    private void captureAudio() {
        try {
            socketChannel = SocketChannel.open(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT));
            socketChannel.configureBlocking(false);

            audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            System.out.println("Available mixers:");
            for (int i = 0; i < mixerInfo.length; i++) {
                System.out.println(i + ": " + mixerInfo[i].getName());
            }

            Scanner scanner = new Scanner(System.in);
            System.out.print("Select a mixer by number: ");
            int mixerIndex = scanner.nextInt();

            Mixer mixer = AudioSystem.getMixer(mixerInfo[mixerIndex]);
            if (mixer.isLineSupported(dataLineInfo)) {
                targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
            } else {
                throw new LineUnavailableException("Selected mixer does not support the specified audio format.");
            }

            targetDataLine.open(audioFormat);
            targetDataLine.start();

            new CaptureThread().start();
            new PlayThread().start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    private class CaptureThread extends Thread {
        private byte[] tempBuffer = new byte[1024]; // Increased buffer size

        @Override
        public void run() {
            try {
                while (!stopCapture) {
                    int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                    ByteBuffer buffer = ByteBuffer.wrap(tempBuffer, 0, cnt);
                    while (buffer.hasRemaining()) {
                        socketChannel.write(buffer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class PlayThread extends Thread {
        private byte[] tempBuffer = new byte[1024]; // Increased buffer size

        @Override
        public void run() {
            try {
                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                sourceDataLine.open(audioFormat);
                sourceDataLine.start();

                ByteBuffer buffer = ByteBuffer.allocateDirect(1024); // Use direct buffer
                while (true) {
                    int bytesRead = socketChannel.read(buffer);
                    if (bytesRead == -1) {
                        break; // End of stream reached
                    }
                    buffer.flip();
                    while (buffer.remaining() >= 2) {
                        buffer.get(tempBuffer, 0, 2);
                        sourceDataLine.write(tempBuffer, 0, 2);
                    }
                    buffer.compact();
                }
                sourceDataLine.drain();
                sourceDataLine.close();
            } catch (IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }
}