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

    private static final int SIZE_OF_THREAD_POOL = 6;
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
        startServerButton.setDisable(true);
        logTextArea.textProperty().bind(server.logProperty());
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

    /**
     * startServer is a method
     * that create socket, create a pool of thread
     * used to catch and execute the client request connection, create
     * a scheduled pool of Thread to save client's info every 30sec.
     * A single Thread handle the request of client.
     */
    private void startServer() {
        try {
            serverSocket = new ServerSocket(6969);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        server.updateLog(" START SERVER... " + '\n');
        server.readFromJSonClientsFile();
        executorService = Executors.newFixedThreadPool(SIZE_OF_THREAD_POOL);
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new SaveAllTask(), 30, 30, TimeUnit.SECONDS);

        new Thread(() -> {
            while (server.isRunning()) {
                try {
                    Socket incomingRequestSocket = serverSocket.accept();
                    ServerTask st = new ServerTask(incomingRequestSocket);
                    executorService.execute(st);
                } catch (SocketException socketException) {
                    System.out.println("Socket Closing");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Server Thread finish his life");
        }).start();
    }

    public void stopServer() {
        server.setRunning(false);
        executorService.shutdown();
        scheduledExecutorService.shutdown();
        server.saveClientsToJSON();
        try {
            serverSocket.close();
        } catch (IOException ioException) {
            System.out.println("Server Closing, aborting wait");
        }
        server.updateLog(" SHUTTING DOWN " + '\n');
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

    class ServerTask implements Runnable {
        Socket socketS;
        ObjectOutputStream objectOutputStream;
        ObjectInputStream objectInputStream;
        boolean everythingInitialized = false;

        public ServerTask(Socket socketS) {
            this.socketS = socketS;
            try {
                objectOutputStream = new ObjectOutputStream(socketS.getOutputStream());
                objectInputStream = new ObjectInputStream(socketS.getInputStream());
                everythingInitialized = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (!everythingInitialized) return;
            try {
                synchronized (logTextArea) {
                    server.updateLog(" Incoming Request " + Thread.currentThread().getName() + " will take care of\n");
                }
                Action actionRequest = (Action) objectInputStream.readObject();
                ServerResponse response;
                if (actionRequest.getOperation() == Operation.PING) {
                    sendResponse(ServerResponse.ACTION_COMPLETED);
                    server.add(actionRequest);
                }
                if (actionRequest.getOperation() == Operation.SEND_EMAIL) {
                    response = addEmailToReceiversInbox(actionRequest, objectInputStream);
                    sendResponse(response);
                    server.add(actionRequest);
                    server.saveClientsToJSON();
                } else if (actionRequest.getOperation() == Operation.NEW_DRAFT) {
                    response = addEmailToSendersDrafts(actionRequest, objectInputStream);
                    sendResponse(response);
                    server.add(actionRequest);
                    server.saveClientsToJSON();
                } else if (actionRequest.getOperation() == Operation.DELETE_EMAIL) {
                    response = deleteEmail(actionRequest, objectInputStream);
                    sendResponse(response);
                    server.add(actionRequest);
                    server.saveClientsToJSON();
                } else if (actionRequest.getOperation() == Operation.READ_EMAIL) {
                    response = setEmailAsRead(actionRequest, objectInputStream);
                    sendResponse(response);
                    server.add(actionRequest);
                    server.saveClientsToJSON();
                } else if (actionRequest.getOperation() == Operation.GET_ALL_EMAILS) {
                    //The only void method because it sends the response before sending all the emails
                    sendAllEmails(actionRequest);
                    server.add(actionRequest);
                }
                synchronized (logTextArea) {
                    server.updateLog(" Request Handled by " + Thread.currentThread().getName() + '\n');
                }
            } catch (Exception ex) {
                synchronized (logTextArea) {
                    server.updateLog(" Error Processing Request " + '\n');
                }
                ex.printStackTrace();
            } finally {
                try {
                    objectOutputStream.close();
                    objectInputStream.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        /**
         * Method used to send
         * a response to Client using
         * socket outputStream
         *
         * @param response will be sent to Client
         */
        private void sendResponse(ServerResponse response) {
            try {
                objectOutputStream.writeObject(response);
                objectOutputStream.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        ServerResponse addEmailToReceiversInbox(Action actionRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email sentEmail = new Email(serializableEmail);
                sentEmail.setState(EmailState.SENT);
                Email inboxEmail = sentEmail.clone();
                inboxEmail.setState(EmailState.RECEIVED);
                inboxEmail.setRead(false);


                //We get the clients to operate on
                Client sender = findClientByAddress(actionRequest.getSender());
                Client receiver = findClientByAddress(actionRequest.getReceiver());

                //If receiver is found
                if (receiver != null) {
                    sentEmail.setID(receiver.getLargestID() + 1);
                    sender.sentProperty().add(sentEmail);
                    inboxEmail.setID(receiver.getLargestID() + 1);
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

        ServerResponse addEmailToSendersDrafts(Action actionRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email emailToBeDrafted = new Email(serializableEmail);
                emailToBeDrafted.setState(EmailState.DRAFTED);

                //We get the client who asked for a deletion
                Client sender = findClientByAddress(actionRequest.getSender());
                if (sender == null) return ServerResponse.CLIENT_NOT_FOUND;

                //We check if the client had already drafted that email
                if (sender.contains(sender.draftsProperty(), emailToBeDrafted)) {
                    //In this case it could be just a modification of the email, so we update it to the new one
                    Email emailToBeModified = sender.findEmailById(sender.draftsProperty(), emailToBeDrafted.getID());
                    sender.draftsProperty().remove(emailToBeModified);
                    sender.draftsProperty().add(emailToBeDrafted);
                    return ServerResponse.ACTION_COMPLETED;
                }

                //Otherwise, we add it to the list
                emailToBeDrafted.setID(sender.getLargestID() + 1);
                sender.draftsProperty().add(emailToBeDrafted);
                return ServerResponse.ACTION_COMPLETED;
            } catch (Exception ex) {
                ex.printStackTrace();
                return ServerResponse.UNKNOWN_ERROR;
            }
        }

        ServerResponse deleteEmail(Action actionRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email emailToBeDeleted = new Email(serializableEmail);
                emailToBeDeleted.setState(EmailState.TRASHED);

                //We get the client who asked for a deletion
                Client sender = findClientByAddress(actionRequest.getSender());
                if (sender == null) return ServerResponse.CLIENT_NOT_FOUND;

                //We find where the email is for that client
                SimpleListProperty<Email> list = sender.whereIs(emailToBeDeleted);
                if (list == null) return ServerResponse.EMAIL_NOT_FOUND;

                //If it is already in the trash we just delete it permanently, otherwise we move it to trash
                if (list == sender.trashProperty()) {
                    list.remove(emailToBeDeleted);
                    return ServerResponse.ACTION_COMPLETED;
                } else {
                    list.remove(emailToBeDeleted);
                    sender.trashProperty().add(emailToBeDeleted);
                    return ServerResponse.ACTION_COMPLETED;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                return ServerResponse.UNKNOWN_ERROR;
            }
        }

        void sendAllEmails(Action actionRequest) {
            try {
                Client requestClient = findClientByAddress(actionRequest.getSender());

                //If the client has never been seen before, since he just logged on we add him to possible new clients
                if (requestClient == null) //only one time this could happen
                {
                    server.addClient(new Client(actionRequest.getSender(), false));
                    System.out.println("New Client!");
                    server.saveClientsToJSON();
                    sendResponse(ServerResponse.CLIENT_NOT_FOUND);
                    return;
                }

                //Get all emails of given client
                SimpleListProperty<Email> allEmails = new SimpleListProperty<>(FXCollections.observableArrayList());
                allEmails.addAll(requestClient.inboxProperty());
                allEmails.addAll(requestClient.trashProperty());
                allEmails.addAll(requestClient.sentProperty());
                allEmails.addAll(requestClient.draftsProperty());

                sendResponse(ServerResponse.ACTION_COMPLETED);

                //Send all of them through the socket
                for (Email email : allEmails) {
                    SerializableEmail serializableEmail = new SerializableEmail(email);
                    objectOutputStream.writeObject(serializableEmail);
                    objectOutputStream.flush();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private ServerResponse setEmailAsRead(Action actionRequest, ObjectInputStream objectInputStream) {
        try {
            SerializableEmail serializableEmail = (SerializableEmail) objectInputStream.readObject();
            long id = serializableEmail.getID();

            Client sender = findClientByAddress(actionRequest.getSender());
            if (sender == null) return ServerResponse.CLIENT_NOT_FOUND;

            //We only check inbox as an unread email can only be there
            Email email = sender.findEmailById(sender.inboxProperty(), id);
            if (email == null) return ServerResponse.EMAIL_NOT_FOUND;

            email.setRead(true);
            server.saveClientsToJSON();
            return ServerResponse.ACTION_COMPLETED;
        } catch (Exception ex) {
            ex.printStackTrace();
            return ServerResponse.UNKNOWN_ERROR;
        }
    }

    /**
     * We use this runnable class
     * to save client's info
     */
    class SaveAllTask implements Runnable {
        public SaveAllTask() {
        }

        @Override
        public void run() {
            server.saveClientsToJSON();
        }
    }
}

