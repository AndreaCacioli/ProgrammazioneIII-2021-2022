package com.progiii.mailclientserver.server.model;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.utils.Action;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;

public class Server
{
    ArrayList<Action> actions;
    ArrayList<Client> clients;
    SimpleStringProperty log;
    boolean running = true;

    public boolean isRunning()
    {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public SimpleStringProperty logProperty() {return  log;}

    public Server()
    {
        clients = new ArrayList<Client>();
        actions = new ArrayList<Action>();
        log = new SimpleStringProperty();
    }

    public void add(Action incomingRequest)
    {
        actions.add(incomingRequest);
        log.setValue(log.getValue() + incomingRequest.toString() + '\n');
    }
}
