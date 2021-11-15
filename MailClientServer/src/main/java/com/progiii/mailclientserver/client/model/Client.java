package com.progiii.mailclientserver.client.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Client
{
    public final String id = "GruppoProgIII@javafxslays.it";
    public ObservableList<Email> inbox;
    public ObservableList<Email> drafts;
    public ObservableList<Email> sent;
    //TODO maybe add trash

    public Client()
    {
        inbox = FXCollections.observableArrayList();
        drafts = FXCollections.observableArrayList();
        sent = FXCollections.observableArrayList();

        for (int i = 0; i < 10; i++)
        {
            inbox.add(Email.getRandomEmail());
            drafts.add(Email.getRandomEmail());
            sent.add(Email.getRandomEmail());
        }



    }

}
