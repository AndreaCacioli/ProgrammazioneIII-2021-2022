package com.progiii.mailclientserver.server.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;
import com.progiii.mailclientserver.server.model.Server;
import com.progiii.mailclientserver.utils.Action;
import com.progiii.mailclientserver.utils.Operation;
import com.progiii.mailclientserver.utils.SerializableEmail;
import com.progiii.mailclientserver.utils.ServerResponse;
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerController {



    private Server server;

    private ServerSocket serverSocket;

    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;

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
        startServer();
    }

    /**
     * We use this method to start Server
     */
    @FXML
    private void onStartServerClicked() {
        server.setRunning(true);
        startServerButton.setDisable(true);
        stopServerButton.setDisable(false);
        startServer();
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

    private void startServer() {
        try {
            serverSocket = new ServerSocket(6969);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        server.logProperty().setValue(server.logProperty().getValue() + " START SERVER... " + '\n');
        server.readFromJSonClientsFile();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new SaveAllTask(), 30, 30, TimeUnit.SECONDS);

        new Thread(() -> {
            while (server.isRunning()) {
                try {
                    Socket incomingRequestSocket = serverSocket.accept();
                    ServerTask st = new ServerTask(incomingRequestSocket);
                    executorService.execute(st);
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
        scheduledExecutorService.shutdown();
        new Thread(new SaveAllTask()).start();
        try {
            serverSocket.close();
        } catch (IOException ioException) {
            System.out.println("Server: Server Closing, aborting wait");
        }
        server.logProperty().setValue(server.logProperty().getValue() + " SHUTTING DOWN " + '\n');
    }

    class ServerTask implements Runnable {
        Socket socketS;
        ObjectOutputStream objectOutputStream;
        ObjectInputStream objectInputStream;

        public ServerTask(Socket socketS) {
            this.socketS = socketS;
            try
            {
                objectOutputStream = new ObjectOutputStream(socketS.getOutputStream());
                objectInputStream = new ObjectInputStream(socketS.getInputStream());
            }catch (Exception ex) {ex.printStackTrace();}
        }

        @Override
        public void run() {
            try {
                synchronized (logTextArea)
                {
                    server.logProperty().setValue(server.logProperty().getValue() + " Incoming Request handled by thread " + Thread.currentThread().getName() + '\n');
                }
                Action actionRequest = (Action) objectInputStream.readObject();
                ServerResponse response;
                if (actionRequest.getOperation() == Operation.SEND_EMAIL) {
                    response = sendEmail(actionRequest, objectInputStream);
                    sendResponse(response);
                } else if (actionRequest.getOperation() == Operation.NEW_DRAFT) {
                    response = draftsEmail(actionRequest, objectInputStream);
                    sendResponse(response);
                } else if (actionRequest.getOperation() == Operation.DELETE_EMAIL) {
                    response = deleteEmail(actionRequest, objectInputStream);
                    sendResponse(response);
                } else if (actionRequest.getOperation() == Operation.GET_ALL_EMAILS) {
                    response = sendAllEmails(actionRequest);
                    sendResponse(response);
                }
                server.logProperty().setValue(server.logProperty().getValue() + " Request Handled by " + Thread.currentThread().getName() + '\n');
            } catch (Exception ex) {
                server.logProperty().setValue(server.logProperty().getValue() + " Error Processing Request " + '\n');
                ex.printStackTrace();
            }
            finally {
                try {
                    objectOutputStream.close();
                    objectInputStream.close();
                }catch (Exception ex) {ex.printStackTrace();}
            }
        }



        private Client findClientByAddress(String address) {
            Client sender = null;
            ArrayList<Client> clients = server.getClients();
            int i = 0;
            while ((sender == null) && i < clients.size()) {
                if (clients.get(i).getAddress().equals(address)) sender = clients.get(i);
                i++;
            }
            return sender;
        }

        private void sendResponse(ServerResponse response) {
                try {
                    objectOutputStream.writeObject(response);
                    objectOutputStream.flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }

        ServerResponse sendEmail(Action actionRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email sentEmail = new Email(actionRequest.getSender().strip(), actionRequest.getReceiver().strip(), serializableEmail.getSubject(), serializableEmail.getBody(), EmailState.SENT, serializableEmail.getDate());
                Email inboxEmail = sentEmail.clone();
                inboxEmail.setState(EmailState.RECEIVED);
                Client sender = findClientByAddress(actionRequest.getSender());
                Client receiver = findClientByAddress(actionRequest.getReceiver());
                if (receiver != null) {
                    server.add(actionRequest);
                    sender.sentProperty().add(sentEmail);
                    receiver.inboxProperty().add(inboxEmail);
                    return ServerResponse.ACTION_COMPLETED;
                } else {
                    return ServerResponse.RECEIVER_NOT_FOUND;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return ServerResponse.UNKNOWN_ERROR;
            }
        }

        ServerResponse draftsEmail(Action actionRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email draftedEmail = new Email(actionRequest.getSender().strip(), actionRequest.getReceiver().strip(), serializableEmail.getSubject(), serializableEmail.getBody(), EmailState.DRAFTED, serializableEmail.getDate());
                Client sender = findClientByAddress(actionRequest.getSender());
                sender.trashProperty().add(draftedEmail);
                return ServerResponse.ACTION_COMPLETED;
            } catch (Exception ex) {
                ex.printStackTrace();
                return ServerResponse.UNKNOWN_ERROR;
            }
        }

        ServerResponse deleteEmail(Action actionRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email deletedEmail = new Email(actionRequest.getSender().strip(), actionRequest.getReceiver().strip(), serializableEmail.getSubject(), serializableEmail.getBody(), EmailState.TRASHED, serializableEmail.getDate());
                Client sender = findClientByAddress(actionRequest.getSender());
                sender.trashProperty().add(deletedEmail);
                return ServerResponse.ACTION_COMPLETED;
            } catch (Exception ex) {
                ex.printStackTrace();
                return ServerResponse.UNKNOWN_ERROR;
            }
        }

        ServerResponse sendAllEmails(Action actionRequest) {
                try {
                    Client requestClient = findClientByAddress(actionRequest.getSender());

                    if(requestClient == null) //only one time this could happen
                    {
                        server.addClient(new Client(actionRequest.getSender()));
                        return ServerResponse.CLIENT_NOT_FOUND;
                    }

                    //get all emails of given client
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
                    server.add(actionRequest);

                    return ServerResponse.ACTION_COMPLETED;

                } catch (Exception ex) {
                    ex.printStackTrace();
                    return ServerResponse.UNKNOWN_ERROR;
                }
        }
    }

    class SaveAllTask implements Runnable {
        public SaveAllTask() {
        }

        @Override
        public void run() {
            server.createClientsJSon();
        }
    }

}