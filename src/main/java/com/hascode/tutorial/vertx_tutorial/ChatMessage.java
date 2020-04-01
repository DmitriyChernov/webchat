package com.hascode.tutorial.vertx_tutorial;

import org.joda.time.DateTime;

public class ChatMessage {
    private final DateTime time;
    private final String message;
    private final long senderId;

    public  ChatMessage(DateTime time, String message, long senderId) {
        this.time = time;
        this.message = message;
        this.senderId = senderId;
    }
}
