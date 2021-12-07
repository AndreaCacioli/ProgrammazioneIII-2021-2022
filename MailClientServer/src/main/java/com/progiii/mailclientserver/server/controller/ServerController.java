package com.progiii.mailclientserver.server.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;
import com.progiii.mailclientserver.server.model.Server;
import com.progiii.mailclientserver.utils.Action;
import com.progiii.mailclientserver.utils.Operation;
import com.progiii.mailclientserver.utils.SerializableEmail;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerController {

    private Server server;

    @FXML
    private TextArea logTextArea;

    private ServerSocket serverSocket;
    private ExecutorService executorService;

    @FXML
    public void initialize() {
        if (this.server != null)
            throw new IllegalStateException("Server can only be initialized once");
        this.server = new Server();
        //TODO: load clients from json
        //addClientFromJSon("./src/main/resources/com/progiii/mailclientserver/server/data/inbox.json");
        logTextArea.textProperty().bind(server.logProperty());
        executorService = Executors.newFixedThreadPool(9);
        serverLife();
    }

    @FXML
    private void onStartServerClicked() {
        server.setRunning(true);
        serverLife();
    }

    @FXML
    private void onStopServerClicked() throws IOException {
        server.setRunning(false);
        stopServer();
    }

    @FXML
    private void clearServerLog() {
        server.logProperty().setValue("");
    }

    private void addClientFromJSon(String JSonFile) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(JSonFile)) {
            Object obj = jsonParser.parse(reader);
            JSONArray emailList = (JSONArray) obj;
            for (int i = 0; i < emailList.size() - 1; i++) {
                System.out.println(parseClientObject((JSONObject) emailList.get(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String parseClientObject(JSONObject email) {
        JSONObject emailObject = (JSONObject) email.get("email");
        String sender = (String) emailObject.get("sender");
        return sender;
    }

    private void serverLife() {
        System.out.println("START SERVER...");
        try {
            serverSocket = new ServerSocket(6969);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        new Thread(() -> {
            while (server.isRunning()) {
                try {
                    Socket incomingRequestSocket = serverSocket.accept();
                    System.out.println("ACCETTATO...");
                    ServerTask st = new ServerTask(incomingRequestSocket);
                    executorService.execute(st);
                    System.out.println("ESEGUITO...");
                } catch (SocketException socketException) {
                    System.out.println("Socket Closing");
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Thread closing");
        }).start();
    }

    public void stopServer() {
        server.setRunning(false);
        executorService.shutdown();
        try {
            serverSocket.close();
        } catch (IOException ioException) {
            System.out.println("Server Closing, aborting wait");
        }
        System.out.println("Server shutting down...");
    }

    class ServerTask implements Runnable {
        Socket incoming;

        public ServerTask(Socket incoming) {
            this.incoming = incoming;
        }

        @Override
        public void run() {
            try {
                ObjectInputStream inStream = new ObjectInputStream(incoming.getInputStream());
                Action incomingRequest = (Action) inStream.readObject();
                if(incomingRequest.getOperation() == Operation.SEND_EMAIL)
                {
                    sendEmail(incomingRequest, inStream);
                }
                else if (incomingRequest.getOperation() == Operation.GET_ALL_EMAILS)
                {
                    sendAllEmails(incomingRequest);
                }

            }catch (Exception ex) {ex.printStackTrace();}
        }

        void sendEmail(Action incomingRequest, ObjectInputStream inStream)
        {
           try{
               SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
               Email sentEmail = new Email(incomingRequest.getSender().strip(), incomingRequest.getReceiver().strip(), serializableEmail.getSubject(), serializableEmail.getBody(), EmailState.SENT, serializableEmail.getDate());
               Email inboxEmail = sentEmail.clone();
               inboxEmail.setState(EmailState.RECEIVED);
               Client sender = null;
               Client receiver = null;
               ArrayList<Client> clients = server.getClients();
               int i = 0;
               while((sender == null || receiver == null) && i < clients.size())
               {
                   if(clients.get(i).getAddress().equals(sentEmail.getSender())) sender = clients.get(i);
                   if(clients.get(i).getAddress().equals(sentEmail.getReceiver())) receiver = clients.get(i);
                   i++;
               }
               try{
                   sender.sentProperty().add(sentEmail);
                   receiver.inboxProperty().add(inboxEmail);
               }catch (Exception ex) {System.out.println("sender or receiver not found");}
               server.add(incomingRequest);
           } catch (Exception ex) {ex.printStackTrace();}
        }

        void sendAllEmails (Action incomingRequest)
        {
            try
            {
                Client requestClient = null;
                for(Client client : server.getClients())
                {
                    if(incomingRequest.getSender().equals(client.getAddress()))
                    {
                        requestClient = client;
                        break;
                    }
                }
                if(requestClient == null)
                {
                    System.out.println("client doesn't exist");
                    return;
                }
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(incoming.getOutputStream());
                SimpleListProperty<Email> allEmails = new SimpleListProperty<>(FXCollections.observableArrayList());
                allEmails.addAll(requestClient.inboxProperty());
                allEmails.addAll(requestClient.trashProperty());
                allEmails.addAll(requestClient.sentProperty());
                allEmails.addAll(requestClient.draftsProperty());
                for(Email email : allEmails)
                {
                    SerializableEmail serializableEmail = new SerializableEmail(email);
                    objectOutputStream.writeObject(serializableEmail);
                    objectOutputStream.flush();
                }
                incoming.close();
                objectOutputStream.close();
                server.add(incomingRequest);
            }catch (Exception ex) {ex.printStackTrace();}
        }



    }
}