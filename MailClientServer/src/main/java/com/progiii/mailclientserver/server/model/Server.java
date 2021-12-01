package com.progiii.mailclientserver.server.model;

import com.progiii.mailclientserver.client.model.Client;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;

public class Server
{
    ArrayList<Client> clients;
    ArrayList<Action> actions;
    StringProperty log;

    public Server()
    {
        clients = new ArrayList<Client>();
        actions = new ArrayList<Action>();
        log = new SimpleStringProperty();
    }
}
