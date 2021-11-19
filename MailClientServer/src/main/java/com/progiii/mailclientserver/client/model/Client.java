package com.progiii.mailclientserver.client.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Client
{
    public final String id = "GruppoProgIII@javafxslays.it";
    public ObservableList<Email> inbox;
    public ObservableList<Email> drafts;
    public ObservableList<Email> sent;
    public ObservableList<Email> trash;

    public Email selectedEmail;

    public Client()
    {
        inbox = FXCollections.observableArrayList();
        drafts = FXCollections.observableArrayList();
        sent = FXCollections.observableArrayList();
        trash = FXCollections.observableArrayList();

        for (int i = 0; i < 10; i++)
        {
            inbox.add(Email.getRandomEmail(EmailState.RECEIVED));
            drafts.add(Email.getRandomEmail(EmailState.DRAFTED));
            sent.add(Email.getRandomEmail(EmailState.SENT));
            trash.add(Email.getRandomEmail(EmailState.TRASHED));
        }



    }

}
