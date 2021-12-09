package com.progiii.mailclientserver.utils;

import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SerializableEmail implements Serializable
{
    private final String sender;
    private final String receiver;
    private final String subject;
    private final String body;


    private EmailState state;
    private LocalDateTime dateTime;

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public EmailState getState() {
        return state;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public LocalDateTime getDate() {
        return dateTime;
    }

    public SerializableEmail(String sender, String receiver, String subject, String body, EmailState state, LocalDateTime dateTime) {
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.body = body;
        this.state = state;
        this.dateTime = dateTime;
    }

    public SerializableEmail(Email email)
    {
        this.sender = email.getSender();
        this.receiver = email.getReceiver();
        this.subject = email.getSubject();
        this.body = email.getBody();
        this.state = email.getState();
        this.dateTime = email.getDateTime();
    }

}