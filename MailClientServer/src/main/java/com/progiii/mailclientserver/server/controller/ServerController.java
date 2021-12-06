package com.progiii.mailclientserver.server.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;
import com.progiii.mailclientserver.server.model.Server;
import com.progiii.mailclientserver.utils.Action;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
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
                String subject = (String) inStream.readObject();
                String body = (String) inStream.readObject();
                //LocalDateTime date = (LocalDateTime) inStream.readObject();
                Email email = new Email(incomingRequest.getSender(), incomingRequest.getReceiver(), subject, body, EmailState.SENT);
                for (Client client : server.getClients()) {
                    System.out.println(client);
                    if (client.getAddress() == email.getReceiver())
                        client.inboxProperty().add(email);
                }
                server.add(incomingRequest);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public void stopServer() throws IOException {
        server.setRunning(false);
        executorService.shutdown();
        serverSocket.close();
        System.out.println("Server shutting down...");
    }
}