package com.progiii.mailclientserver.client.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Email {
    private final StringProperty sender;
    private final StringProperty receiver;
    private final StringProperty subject;
    private final StringProperty body;
    public EmailState state;
    LocalDateTime dateTime;

    public String getSender() {
        return sender.get();
    }

    public StringProperty senderProperty() {
        return sender;
    }

    public String getReceiver() {
        return receiver.get();
    }

    public StringProperty receiverProperty() {
        return receiver;
    }

    public String getSubject() {
        return subject.get();
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public String getBody() {
        return body.get();
    }

    public StringProperty bodyProperty() {
        return body;
    }


    public Email(String sender, String receiver, String subject, String body, EmailState state) {
        this.sender = new SimpleStringProperty(sender);
        this.receiver = new SimpleStringProperty(receiver);
        this.subject = new SimpleStringProperty(subject);
        this.body = new SimpleStringProperty(body);
        this.state = state;
        dateTime = LocalDateTime.now();
    }

    public Email() {
        this.sender = new SimpleStringProperty(" ");
        this.receiver = new SimpleStringProperty(" ");
        this.subject = new SimpleStringProperty(" ");
        this.body = new SimpleStringProperty("");
        this.state = EmailState.DRAFTED;
        dateTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        DateTimeFormatter ft = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return "From: " + getSender() + ":\n" +
                getSubject() + '\n' +
                ft.format(this.dateTime);
    }

    public static Email getRandomEmail(EmailState state) {
        Random r = new Random();
        String[] possibleUsers = {"Gianni_gamer123@libero.it", "treMorten@gmail.com", "JordanLover95@gmail.com", "George@hotmail.com"};

        String[] possibleSubjects = {"Your Marketing Sucks: Why You Need to Think Local", "Info: Your Product", "What your Cat says about YOU"};

        String[] possibleBodies = {
                "Dear TimeLord7725,\n We would absolutely love to have you as part of our international Marvel-Fans meeting on Sat 9-15 at 11p.m.",
                "1234234141234\n",
        };

        return new Email(
                possibleUsers[r.nextInt(possibleUsers.length)],
                possibleUsers[r.nextInt(possibleUsers.length)],
                possibleSubjects[r.nextInt(possibleSubjects.length)],
                possibleBodies[r.nextInt(possibleBodies.length)],
                state
        );

    }
}
