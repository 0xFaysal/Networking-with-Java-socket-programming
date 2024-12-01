package org.faysal;

import javax.sound.sampled.*;
import java.net.*;
import java.util.Scanner;

public class VoiceCallClientUDP_v1 {
    private static final String SERVER_ADDRESS = "10.15.35.53";
    private static final int SERVER_PORT = 5000;
    private static final int BUFFER_SIZE = 512; // Reduced buffer size
    private DatagramSocket socket;
    private TargetDataLine targetDataLine;
    private SourceDataLine speakers;

    public static void main(String[] args) {
        new VoiceCallClientUDP_v1().start();
    }

    public void start() {
        try {
            socket = new DatagramSocket();
            AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
            DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);

            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            System.out.println("Available mixers:");
            for (int i = 0; i < mixerInfo.length; i++) {
                System.out.println(i + ": " + mixerInfo[i].getName());
            }

            Scanner scanner = new Scanner(System.in);
            System.out.print("Select a mixer by number: ");
            int mixerIndex = scanner.nextInt();

            Mixer mixer = AudioSystem.getMixer(mixerInfo[mixerIndex]);
            if (mixer.isLineSupported(targetInfo)) {
                targetDataLine = (TargetDataLine) mixer.getLine(targetInfo);
            } else {
                throw new LineUnavailableException("Selected mixer does not support the specified audio format.");
            }

            if (AudioSystem.isLineSupported(sourceInfo)) {
                speakers = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            } else {
                throw new LineUnavailableException("Speakers do not support the specified audio format.");
            }

            targetDataLine.open(format);
            targetDataLine.start();
            speakers.open(format);
            speakers.start();

            new Thread(this::sendAudio).start();
            new Thread(this::receiveAudio).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendAudio() {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            while (true) {
                int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
                socket.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveAudio() {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                speakers.write(packet.getData(), 0, packet.getLength());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}