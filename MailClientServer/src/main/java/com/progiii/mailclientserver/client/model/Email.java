package com.progiii.mailclientserver.client.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Email
{
    String sender;
    String receiver;
    String subject;


    private final StringProperty body;

    public String getBody()
    {
        return body.get();
    }

    public StringProperty bodyProperty()
    {
        return body;
    }

    public EmailState state;

    LocalDateTime dateTime;


    public Email(String sender, String receiver, String subject, String body, EmailState state)
    {
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.body = new SimpleStringProperty(body);
        this.state = state;
        dateTime = LocalDateTime.now();
    }

    public Email()
    {
        this.sender = "";
        this.receiver = "";
        this.subject = "";
        this.body = new SimpleStringProperty(" ");
        this.state = EmailState.DRAFTED;
        dateTime = LocalDateTime.now();
    }

    @Override
    public String toString()
    {
        DateTimeFormatter ft = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return   "From: " + sender + ":\n" +
                 subject + '\t' +
                ft.format(this.dateTime);
    }

    public static Email getRandomEmail(EmailState state)
    {
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
