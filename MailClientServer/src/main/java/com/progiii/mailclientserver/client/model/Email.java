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


    private EmailState state;
    private LocalDateTime dateTime;

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

    public StringProperty bodyProperty() {return body;}

    public LocalDateTime getDateTime() {return dateTime;}

    public EmailState getState() {return state;}

    public void setState(EmailState state) {this.state = state;}


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
        String[] possibleUsers = {
                "Gianni_gamer123@libero.it",
                "treMorten@gmail.com",
                "JordanLover95@gmail.com",
                "George@hotmail.com",
                "stressedWorker55@disrespectfulcompany.en",
                "MomEllie@care.us",
                "fatherLewis@irc.it",
                "healtierTogether@gymgymgym.roar",
                "jokerjokerjoker@isp.int"
        };

        String[] possibleSubjects = {
                "Your Marketing Sucks: Why You Need to Think Local",
                "Info: Your Product",
                "What your Cat says about YOU",
                "READ MY EMAIL!!! aehm... please...",
                "Quit your lazy ass life and start getting your acts together: join our gym",
                "How long is too long for an email subject???",
                "Why your favorite Marvel character is worse than the worst DC character",
                "If you could choose a comic book which of these would you choose and why the Marvel one?"
        };

        String[] possibleBodies = {
                "Dear TimeLord7725,\n We would absolutely love to have you as part of our international Marvel-Fans meeting on Sat 9-15 at 11p.m.",
                "In all honesty I don't even know you right? But after all isn't it how things go nowadays?! Has internet really put us any closer or has it created a digital barrier separating us all?! IT IS TIME TO WAKE UP!!!\n",
                "I don't think you really got the point of emails bro!\nYou don't just go around and tell people that internet is the worst.",
                "Hi Mary, I'm really looking forward to meeting you in person for the first time!\nThis whole catfish... thing really had me fed up!\nSee you on Wednesday.\nxoxo\n",
                "Working at a company can really push you to the limit sometimes! Please consider taking a break... PLEASE"
        };

        return new Email(
                possibleUsers[r.nextInt(possibleUsers.length)],
                possibleUsers[r.nextInt(possibleUsers.length)],
                possibleSubjects[r.nextInt(possibleSubjects.length)],
                possibleBodies[r.nextInt(possibleBodies.length)],
                state
        );

    }

    public static String getRandomAddress()
    {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr + "@unito.it";

    }
}
