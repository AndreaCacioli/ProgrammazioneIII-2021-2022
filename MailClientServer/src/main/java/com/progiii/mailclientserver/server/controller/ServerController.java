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
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerController {



    private Server server;

    private ServerSocket serverSocket;

    private ExecutorService executorService;

    @FXML
    private TextArea logTextArea;

    @FXML
    private Button startServerButton;

    @FXML
    private Button stopServerButton;

    @FXML
    public void initialize() {
        if (this.server != null)
            throw new IllegalStateException("Server: Server can only be initialized once");
        this.server = new Server();
        logTextArea.textProperty().bind(server.logProperty());
        executorService = Executors.newFixedThreadPool(9);
        startServerButton.setDisable(true);
        serverLife();
    }

    /**
     * We use this method to start Server
     */
    @FXML
    private void onStartServerClicked() {
        server.setRunning(true);
        startServerButton.setDisable(true);
        stopServerButton.setDisable(false);
        serverLife();
    }

    /**
     * We use this method to stop Server
     */
    @FXML
    private void onStopServerClicked() {
        server.setRunning(false);
        stopServerButton.setDisable(true);
        startServerButton.setDisable(false);
        stopServer();
    }

    /**
     * Function used to clean Server's log
     */
    @FXML
    private void clearServerLog() {
        server.logProperty().setValue("");
    }

    /**
     * serverLife is a method that
     * handle server's life,
     * we are able to stop or start a server.
     */
    private void serverLife() {
        server.logProperty().setValue(server.logProperty().getValue() + " START SERVER... " + '\n');
        System.out.println();
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
                    server.logProperty().setValue(server.logProperty().getValue() + " RICHIESTA ACCETTATA " + '\n');
                    ServerTask st = new ServerTask(incomingRequestSocket);
                    executorService.execute(st);
                    server.logProperty().setValue(server.logProperty().getValue() + " ESEGUITA " + '\n');
                } catch (SocketException socketException) {
                    System.out.println("Server: Socket Closing");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Server: Thread finish his life");
        }).start();
    }

    public void stopServer() {
        server.setRunning(false);
        executorService.shutdown();
        try {
            serverSocket.close();
        } catch (IOException ioException) {
            System.out.println("Server: Server Closing, aborting wait");
        }
        server.logProperty().setValue(server.logProperty().getValue() + " SHUTTING DOWN " + '\n');
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
                if (incomingRequest.getOperation() == Operation.SEND_EMAIL) {
                    sendEmail(incomingRequest, inStream);
                } else if (incomingRequest.getOperation() == Operation.NEW_DRAFT) {
                    draftsEmail(incomingRequest, inStream);
                } else if (incomingRequest.getOperation() == Operation.DELETE_EMAIL) {
                    deleteEmail(incomingRequest, inStream);
                } else if (incomingRequest.getOperation() == Operation.GET_ALL_EMAILS) {
                    sendAllEmails(incomingRequest);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        void sendEmail(Action incomingRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email sentEmail = new Email(incomingRequest.getSender().strip(), incomingRequest.getReceiver().strip(), serializableEmail.getSubject(), serializableEmail.getBody(), EmailState.SENT, serializableEmail.getDate());
                Email inboxEmail = sentEmail.clone();
                inboxEmail.setState(EmailState.RECEIVED);
                Client sender = null;
                Client receiver = null;
                ArrayList<Client> clients = server.getClients();
                int i = 0;
                while ((sender == null || receiver == null) && i < clients.size()) {
                    if (clients.get(i).getAddress().equals(sentEmail.getSender())) sender = clients.get(i);
                    if (clients.get(i).getAddress().equals(sentEmail.getReceiver())) receiver = clients.get(i);
                    i++;
                }
                try {
                    if (sender != null)
                        sender.sentProperty().add(sentEmail);
                    if (receiver != null)
                        receiver.inboxProperty().add(inboxEmail);
                } catch (Exception ex) {
                    System.out.println("Server: Sender or Receiver not founded");
                }
                server.add(incomingRequest);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        void draftsEmail(Action incomingRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email deletedEmail = new Email(incomingRequest.getSender().strip(), incomingRequest.getReceiver().strip(), serializableEmail.getSubject(), serializableEmail.getBody(), EmailState.TRASHED, serializableEmail.getDate());
                addActionToServer(incomingRequest, deletedEmail);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        void deleteEmail(Action incomingRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email draftedEmail = new Email(incomingRequest.getSender().strip(), incomingRequest.getReceiver().strip(), serializableEmail.getSubject(), serializableEmail.getBody(), EmailState.DRAFTED, serializableEmail.getDate());
                addActionToServer(incomingRequest, draftedEmail);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void addActionToServer(Action incomingRequest, Email deletedEmail) {
            Client sender = null;
            ArrayList<Client> clients = server.getClients();
            int i = 0;
            while ((sender == null) && i < clients.size()) {
                if (clients.get(i).getAddress().equals(deletedEmail.getSender())) sender = clients.get(i);
                i++;
            }
            try {
                if (sender != null)
                    sender.sentProperty().add(deletedEmail);
            } catch (Exception ex) {
                System.out.println("Server: Sender not founded");
            }
            server.add(incomingRequest);
        }


        void sendAllEmails(Action incomingRequest) {
            ObjectOutputStream objectOutputStream = null;
            try {
                Client requestClient = null;
                for (Client client : server.getClients()) {
                    if (incomingRequest.getSender().equals(client.getAddress())) {
                        requestClient = client;
                        break;
                    }
                }
                if (requestClient == null) {
                    System.out.println("Server: Client doesn't exist");
                    return;
                }
                objectOutputStream = new ObjectOutputStream(incoming.getOutputStream());
                SimpleListProperty<Email> allEmails = new SimpleListProperty<>(FXCollections.observableArrayList());
                allEmails.addAll(requestClient.inboxProperty());
                allEmails.addAll(requestClient.trashProperty());
                allEmails.addAll(requestClient.sentProperty());
                allEmails.addAll(requestClient.draftsProperty());
                for (Email email : allEmails) {
                    SerializableEmail serializableEmail = new SerializableEmail(email);
                    objectOutputStream.writeObject(serializableEmail);
                    objectOutputStream.flush();
                }
                server.add(incomingRequest);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    incoming.close();
                    if (objectOutputStream != null) {
                        objectOutputStream.close();
                    }
                } catch (IOException ex) {
                    System.out.println("Server: Error in closing ServerSocket/Output Stream");
                }
            }
        }
    }
}