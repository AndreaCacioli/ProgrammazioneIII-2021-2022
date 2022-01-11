package com.progiii.mailclientserver.utils;

import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
* A class to make an email serializable and therefore ready to be sent via Socket
* This representation of an email should only be used for content sharing, not as the real model,
* because it has no property and therefore this doesn't allow you to bind it to anything.
*
* */
public class SerializableEmail implements Serializable, Comparable<SerializableEmail> {
    private final String sender;
    private final String receiver;
    private final String subject;
    private final String body;

    private final EmailState state;
    private final LocalDateTime dateTime;
    private final long ID;
    private final boolean read;

    public boolean isRead() {
        return read;
    }

    public long getID() {
        return ID;
    }

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

    @SuppressWarnings("All")
    public LocalDateTime getDate() {
        return dateTime;
    }

    public SerializableEmail(Email email) {
        this.sender = email.getSender().strip();
        this.receiver = email.getReceiver().strip();
        this.subject = email.getSubject();
        this.body = email.getBody();
        this.state = email.getState();
        this.dateTime = email.getDateTime();
        ID = email.getID();
        this.read = email.isRead();
    }

    @Override
    public int compareTo(SerializableEmail o) {
        return Long.compare(getID(), o.getID());
    }
}
