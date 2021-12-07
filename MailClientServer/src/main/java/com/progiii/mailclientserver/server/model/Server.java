package com.progiii.mailclientserver.server.model;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;
import com.progiii.mailclientserver.utils.Action;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;

public class Server {
    ArrayList<Action> actions;
    ArrayList<Client> clients;
    SimpleStringProperty log;
    boolean running = true;

    public ArrayList<Action> getActions() {
        return actions;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public void addClient(Client c) {
        if (c != null)
            clients.add(c);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public SimpleStringProperty logProperty() {
        return log;
    }

    public Server() {
        clients = new ArrayList<Client>();


        //TODO prendere i dati dei client da file
        clients.add(new Client("gianniGamer@libero.it"));
        clients.add(new Client("treMorten@gmail.com"));

        for(int i = 0; i  < 10 ; i++)
        {
            clients.get(0).inboxProperty().add(Email.getRandomEmail(EmailState.RECEIVED));
        }

        actions = new ArrayList<Action>();
        log = new SimpleStringProperty();
    }

    public void add(Action incomingRequest) {
        actions.add(incomingRequest);
        log.setValue(log.getValue() + incomingRequest.toString() + '\n');
    }
}
