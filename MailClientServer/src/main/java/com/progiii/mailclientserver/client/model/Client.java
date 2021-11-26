package com.progiii.mailclientserver.client.model;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class Client {
    public final String id = "GruppoProgIII@javafxslays.it";

    //TODO make them private
    private SimpleListProperty<Email> inbox;
    private SimpleListProperty<Email> drafts;
    private SimpleListProperty<Email> sent;
    private SimpleListProperty<Email> trash;

    public SimpleListProperty<Email> inboxProperty() {
        return inbox;
    }

    public SimpleListProperty<Email> draftsProperty() {
        return drafts;
    }

    public SimpleListProperty<Email> sentProperty() {
        return sent;
    }

    public SimpleListProperty<Email> trashProperty() {
        return trash;
    }

    public Email selectedEmail;
    public Email newEmail;

    public Client() {
        inbox = new SimpleListProperty<Email>(FXCollections.observableArrayList());
        drafts = new SimpleListProperty<Email>(FXCollections.observableArrayList());
        sent = new SimpleListProperty<Email>(FXCollections.observableArrayList());
        trash = new SimpleListProperty<Email>(FXCollections.observableArrayList());

        for (int i = 0; i < 10; i++) {
            inbox.add(Email.getRandomEmail(EmailState.RECEIVED));
            drafts.add(Email.getRandomEmail(EmailState.DRAFTED));
            sent.add(Email.getRandomEmail(EmailState.SENT));
            trash.add(Email.getRandomEmail(EmailState.TRASHED));
        }
    }
}
