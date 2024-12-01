package org.faysal;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class VoiceCallClientSwing {
    boolean stopCapture = false;
    ByteArrayOutputStream byteArrayOutputStream;
    AudioFormat audioFormat;
    TargetDataLine targetDataLine;
    AudioInputStream audioInputStream;
    BufferedOutputStream out = null;
    BufferedInputStream in = null;
    Socket sock = null;
    SourceDataLine sourceDataLine;

    public static void main(String[] args) {
        VoiceCallClientSwing tx = new VoiceCallClientSwing();
        tx.captureAudio();
    }

    private void captureAudio() {
        try {
            sock = new Socket("10.15.2.223", 500);
            out = new BufferedOutputStream(sock.getOutputStream());
            in = new BufferedInputStream(sock.getInputStream());

            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            System.out.println("Available mixers:");
            for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
                System.out.println(mixerInfo[cnt].getName());
            }
            audioFormat = getAudioFormat();

            DataLine.Info dataLineInfo = new DataLine.Info(
                    TargetDataLine.class, audioFormat);

//            Mixer mixer = AudioSystem.getMixer(mixerInfo[2]);
//            targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);

            Mixer mixer = null;
            for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
                mixer = AudioSystem.getMixer(mixerInfo[cnt]);
                if (mixer.isLineSupported(dataLineInfo)) {
                    System.out.println(mixerInfo[cnt].getName());
                    targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
                    break;
                }
            }
            if (targetDataLine == null) {
                throw new LineUnavailableException("No suitable mixer found for the specified audio format.");
            }

            targetDataLine.open(audioFormat);
            targetDataLine.start();

            Thread captureThread = new CaptureThread();
            captureThread.start();

            DataLine.Info dataLineInfo1 = new DataLine.Info(
                    SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine) AudioSystem
                    .getLine(dataLineInfo1);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            Thread playThread = new PlayThread();
            playThread.start();

        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    class CaptureThread extends Thread {

        byte tempBuffer[] = new byte[10000];

        @Override
        public void run() {
            byteArrayOutputStream = new ByteArrayOutputStream();
            stopCapture = false;
            try {
                while (!stopCapture) {

                    int cnt = targetDataLine.read(tempBuffer, 0,
                            tempBuffer.length);

                    out.write(tempBuffer);

                    if (cnt > 0) {

                        byteArrayOutputStream.write(tempBuffer, 0, cnt);

                    }
                }
                byteArrayOutputStream.close();
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;

        int sampleSizeInBits = 8;

        int channels = 1;

        boolean signed = true;

        boolean bigEndian = false;

        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
                bigEndian);
    }

    class PlayThread extends Thread {

        byte tempBuffer[] = new byte[10000];

        @Override
        public void run() {
            try {
                while (true) {
                    int bytesRead = in.read(tempBuffer);
                    if (bytesRead == -1) {
                        break; // End of stream reached
                    }
                    sourceDataLine.write(tempBuffer, 0, bytesRead);
                }
                sourceDataLine.drain();
                sourceDataLine.close();
            } catch (SocketException e) {
                System.out.println("Connection reset by peer: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (sock != null) {
                        sock.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}