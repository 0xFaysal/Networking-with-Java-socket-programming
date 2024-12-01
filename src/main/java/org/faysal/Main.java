package org.faysal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TimeZone;

public class Main {
    public static void main(String[] args) throws UnknownHostException {
        long last = System.currentTimeMillis();
        System.out.println(InetAddress.getLocalHost().getHostAddress());
        System.out.println(InetAddress.getLocalHost().getHostName());
        long now = System.currentTimeMillis();
        System.out.println("Time taken: " + (now - last) + "ms");
    }
}