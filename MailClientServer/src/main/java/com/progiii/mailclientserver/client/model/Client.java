package com.progiii.mailclientserver.client.model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Client {
    public final String id = "GruppoProgIII@javafxslays.it";

    //TODO make them private
    public SimpleListProperty<Email> inbox;
    public SimpleListProperty<Email> drafts;
    public SimpleListProperty<Email> sent;
    public SimpleListProperty<Email> trash;

    public Email selectedEmail;

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
