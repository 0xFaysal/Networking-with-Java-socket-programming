package org.faysal.model;

import java.io.Serializable;

public class Message implements Serializable {
    String username;
    String message;
    String receiver;

    public Message(String username) {
        this.username = username;
    }

    public Message(String message, String receiver) {
        this.message = message;
        this.receiver = receiver;
    }

    public Message(String username, String messageText, String receiver) {
        this.username = username;
        this.message = messageText;
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Message [message=" + message + ", receiver=" + receiver + ", username=" + username
                + "]";
    }
}