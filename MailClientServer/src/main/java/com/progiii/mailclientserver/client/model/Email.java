package com.progiii.mailclientserver.client.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Email
{
    String sender;
    String receiver;
    String subject;
    String body;

    LocalDateTime dateTime;

    boolean sent;
    boolean read;
    boolean draft;

    public Email(String sender, String receiver, String subject, String body, boolean sent, boolean read, boolean draft)
    {
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.body = body;
        this.sent = sent;
        this.read = read;
        this.draft = draft;
        dateTime = LocalDateTime.now();
    }

    public Email()
    {
        this.sender = "";
        this.receiver = "";
        this.subject = "";
        this.body = "";
        this.sent = false;
        this.read = false;
        this.draft = false;
        dateTime = LocalDateTime.now();
    }

    @Override
    public String toString()
    {
        DateTimeFormatter ft = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return   "From: " + sender + ":\n" +
                 subject + '\t' +
                (this.sent ? 's' : "ns") + ' ' +
                (this.read ? 'r' : "nr") + '\n' +
                ft.format(this.dateTime);
    }

    public static Email getRandomEmail()
    {
        Random r = new Random();
        String[] possibleUsers = {"Gianni_gamer123@libero.it", "treMorten@gmail.com", "JordanLover95@gmail.com", "George@hotmail.com"};

        String[] possibleSubjects = {"Your Marketing Sucks: Why You Need to Think Local", "Info: Your Product", "What your Cat says about YOU"};

        String[] possibleBodies = {"Dear TimeLord7725,\n We would absolutely love to have you as part of our international Marvel-Fans meeting on Sat 9-15 at 11p.m."};

        return new Email(
                possibleUsers[r.nextInt(possibleUsers.length)],
                possibleUsers[r.nextInt(possibleUsers.length)],
                possibleSubjects[r.nextInt(possibleSubjects.length)],
                possibleBodies[r.nextInt(possibleBodies.length)],
                r.nextBoolean(),
                r.nextBoolean(),
                r.nextBoolean());
    }
}
