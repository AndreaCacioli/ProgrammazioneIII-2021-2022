package com.progiii.mailclientserver.client.model;

import com.progiii.mailclientserver.utils.SerializableEmail;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Email implements Comparable<Email> {

    //We Identify an email by its sender and ID values together

    private final StringProperty sender;
    private final StringProperty receiver;
    private final StringProperty subject;
    private final StringProperty body;
    private long ID;
    private boolean read = true;


    private EmailState state;
    private final LocalDateTime dateTime;

    public boolean isRead() {
        return read;
    }

    public long getID() {
        return ID;
    }

    public void setID(long id) {
        ID = id;
    }

    public String getSender() {
        return sender.get();
    }

    public void setSender(String sender) {
        this.sender.set(sender);
    }

    public void setReceiver(String receiver) {
        this.receiver.set(receiver);
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public EmailState getState() {
        return state;
    }

    public void setState(EmailState state) {
        this.state = state;
    }

    public void setRead(boolean value) {
        this.read = value;
    }

    public void setSubject(String subject) {
        this.subject.setValue(subject);
    }

    public void setBody(String body) {
        this.body.set(body);
    }

    public Email(String sender, String receiver, String subject, String body, EmailState state, long ID) {
        this.sender = new SimpleStringProperty(sender);
        this.receiver = new SimpleStringProperty(receiver);
        this.subject = new SimpleStringProperty(subject);
        this.body = new SimpleStringProperty(body);
        this.state = state;
        dateTime = LocalDateTime.now();
        this.ID = ID;
    }

    public Email(String sender, String receiver, String subject, String body, EmailState state, LocalDateTime dateTime, long ID) {
        this.sender = new SimpleStringProperty(sender);
        this.receiver = new SimpleStringProperty(receiver);
        this.subject = new SimpleStringProperty(subject);
        this.body = new SimpleStringProperty(body);
        this.state = state;
        this.dateTime = dateTime;
        this.ID = ID;
    }

    public Email(SerializableEmail email) {
        this.sender = new SimpleStringProperty(email.getSender());
        this.receiver = new SimpleStringProperty(email.getReceiver());
        this.subject = new SimpleStringProperty(email.getSubject());
        this.body = new SimpleStringProperty(email.getBody());
        this.state = email.getState();
        this.dateTime = email.getDateTime();
        ID = email.getID();
        read = email.isRead();
    }

    public Email(long ID) {
        this.sender = new SimpleStringProperty("");
        this.receiver = new SimpleStringProperty("");
        this.subject = new SimpleStringProperty("");
        this.body = new SimpleStringProperty("");
        this.state = EmailState.DRAFTED;
        dateTime = LocalDateTime.now();
        this.ID = ID;
    }

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validateEmailAddress(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    @Override
    public String toString() {
        DateTimeFormatter ft = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return "From: " + getSender() + "\n" +
                "To: " + getReceiver() + "\n" +
                "Subject: " + getSubject() + '\n' +
                ft.format(this.dateTime) + "Stato = " + state + "\nID = " + getID();
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
                state,
                -1
                //Check the -1: Unstable
        );

    }

    public static String getRandomAddress() {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr + "@unito.it";

    }

    public Email clone() {
        return new Email(this.getSender(), this.getReceiver(), this.getSubject(), this.getBody(), this.getState(), this.getDateTime(), this.getID());
    }

    @Override
    public int compareTo(Email o) {
        //Strings have priority
        if (sender.getValue().compareTo(o.getSender()) != 0) return sender.getValue().compareTo(o.getSender());
        //if they are equal, then we move to the IDs
        return Long.compare(getID(), o.getID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return ID == email.ID && (sender.getValue().compareTo(email.getSender()) == 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }
}
