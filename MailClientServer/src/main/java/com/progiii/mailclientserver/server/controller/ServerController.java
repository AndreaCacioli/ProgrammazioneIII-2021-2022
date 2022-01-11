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
    private Server server; //Model

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
     * This method is called by click button event
     * to allow the user to start the Server by:
     * Setting the running state to true,
     * Disable the start button because it has already been clicked
     * set to Visible the stop button and finally call the method
     * startServer
     */
    @FXML
    private void onStartServerClicked() {
        server.setRunning(true);
        startServerButton.setDisable(true);
        stopServerButton.setDisable(false);
        startServer();
    }

    /**
     * This method is called by click button event
     * to allow to the user to stop the Server by:
     * Setting the running state to false,
     * Disable the stop button because is already clicked
     * set to Visible the start button and finally call the method
     * stopServer
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
     * that creates a serverSocket,
     * loads all Clients' info from the JSON,
     * creates a pool of Threads used to catch and execute the Client request connection,
     * creates a scheduled pool of Thread to save Clients' info every 30sec, started with a delay of 30sec.
     * Finally, to allow the user to use the view we create a Single Thread that handles the Connection phase
     */
    private void startServer() {
        try {
            serverSocket = new ServerSocket(6969);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        server.updateLog(" START SERVER... " + '\n');
        server.readFromJSONClientsFile();
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
        }).start();
    }

    /**
     * stopServer is the method that shuts down
     * every pool created from the startServer and
     * save all Clients' info in the JSON file
     */
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

    /**
     * findClientByAddress is a support method,
     * it simply searches the Client obj in the ArrayList
     * @param address string of Client's email
     * @return Client obj
     */
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

    /**
     * ServerTask is the Task that will be executed,
     * this task contains an action sent from Client by using
     * ObjectOutputStream, Server according to the request sent
     * will do an operation and send a response to the Client
     */
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
                    actionRequest.setSuccessful(true);
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
         * a response to Client by using
         * the socket objectOutputStream
         *
         * @param response that will be sent to Client
         */
        private void sendResponse(ServerResponse response) {
            try {
                objectOutputStream.writeObject(response);
                objectOutputStream.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * addEmailToReceiversInbox is used to send an Email,
         * first we read the SerializableEmail which is the Email to be sent to the receiver/s,
         * we search in our Clients list if it exists by using findClientByAddress,
         * then we add to the local Client(receiver) inbox property the inboxEmail and
         * add to the local Client(sender) sent property the sentEmail
         * finally we send the response:
         *      if all is done correctly ACTION_COMPLETED
         *
         * @param actionRequest the Action sent from Client
         * @param inStream used to read the SerializableEmail sent from Client
         * @return ServerResponse
         */
        ServerResponse addEmailToReceiversInbox(Action actionRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email sentEmail = new Email(serializableEmail);
                sentEmail.setReceiver(sentEmail.getReceiver().replaceAll(" ", ""));

                //We get the clients to operate on
                Client sender = findClientByAddress(actionRequest.getSender());

                if(sentEmail.getState() == EmailState.DRAFTED)
                {
                    //remove it from the drafts
                    sender.draftsProperty().removeIf(email -> email.getID() == serializableEmail.getID());
                }


                sentEmail.setState(EmailState.SENT);
                String[] receiversTmp = actionRequest.getReceiver().split(",");
                ArrayList<Client> receivers = new ArrayList<>();

                //check if there are null receivers
                for (int i = 0; i < receiversTmp.length; i++) {
                    Client client = findClientByAddress(receiversTmp[i].strip());
                    if (receiversTmp[i] == null || client == null)
                    {
                        actionRequest.setSuccessful(false);
                        return ServerResponse.RECEIVER_NOT_FOUND;
                    }
                    receivers.add(client);
                    receiversTmp[i] = receiversTmp[i].strip();
                }

                for (int j = 0; j < receiversTmp.length; j++) {
                    Client receiver = receivers.get(j);

                    Email inboxEmail = sentEmail.clone();
                    inboxEmail.setState(EmailState.RECEIVED);
                    inboxEmail.setRead(false);
                    // Privacy
                    // inboxEmail.setReceiver(receiversTmp[j]);

                    inboxEmail.setID(receiver.getLargestID() + 1);
                    receiver.inboxProperty().add(inboxEmail);
                }
                sentEmail.setID(sender.getLargestID() + 1);
                sender.sentProperty().add(sentEmail);
                actionRequest.setSuccessful(true);
                return ServerResponse.ACTION_COMPLETED;
            } catch (Exception ex) {
                ex.printStackTrace();
                actionRequest.setSuccessful(false);
                return ServerResponse.UNKNOWN_ERROR;
            }
        }

        /**
         * addEmailToSendersDrafts is used to draft an Email,
         * first we read the SerializableEmail which is the Email to be draft,
         * we search in our Clients list if it exists by using findClientByAddress,
         * then we search if the Email is already drafted,
         * if already exists we just delete the old draft and add to local the new one(same ID),
         * else add to local draft the Email
         *
         * @param actionRequest the Action sent from Client
         * @param inStream used to read the SerializableEmail sent from Client
         * @return ServerResponse
         */
        ServerResponse addEmailToSendersDrafts(Action actionRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email emailToBeDrafted = new Email(serializableEmail);
                emailToBeDrafted.setState(EmailState.DRAFTED);

                //We get the client who asked for a deletion
                Client sender = findClientByAddress(actionRequest.getSender());
                if (sender == null)
                {
                    actionRequest.setSuccessful(false);
                    return ServerResponse.CLIENT_NOT_FOUND;
                }

                //We check if the client had already drafted that email
                if (sender.contains(sender.draftsProperty(), emailToBeDrafted)) {
                    //In this case it could be just a modification of the email, so we update it to the new one
                    Email emailToBeModified = sender.findEmailById(sender.draftsProperty(), emailToBeDrafted.getID());
                    sender.draftsProperty().remove(emailToBeModified);
                    sender.draftsProperty().add(emailToBeDrafted);
                    actionRequest.setSuccessful(true);
                    return ServerResponse.ACTION_COMPLETED;
                }

                //Otherwise, we add it to the list
                emailToBeDrafted.setID(sender.getLargestID() + 1);
                sender.draftsProperty().add(emailToBeDrafted);
                actionRequest.setSuccessful(true);
                return ServerResponse.ACTION_COMPLETED;
            } catch (Exception ex) {
                ex.printStackTrace();
                actionRequest.setSuccessful(false);
                return ServerResponse.UNKNOWN_ERROR;
            }
        }

        /**
         * deleteEmail is used to delete an Email,
         * first we read the SerializableEmail which is the Email to be deleted,
         * we search in our Clients list if exists by using findClientByAddress,
         * we have to know where the Email is, we use a Client method whereIs
         * that return the SimpleListProperty that contain the Email
         * if is in the Trash simply we remove it
         * otherwise we remove from the List, send it to the Trash list and set
         * the state of Email to TRASHED
         *
         * @param actionRequest the Action sent from Client
         * @param inStream used to read the SerializableEmail sent from Client
         * @return ServerResponse
         */
        ServerResponse deleteEmail(Action actionRequest, ObjectInputStream inStream) {
            try {
                SerializableEmail serializableEmail = (SerializableEmail) inStream.readObject();
                Email emailToBeDeleted = new Email(serializableEmail);

                //We get the client who asked for a deletion
                Client sender = findClientByAddress(actionRequest.getSender());
                if (sender == null)
                {
                    actionRequest.setSuccessful(false);
                    return ServerResponse.CLIENT_NOT_FOUND;
                }

                //We find where the email is for that client
                SimpleListProperty<Email> list = sender.whereIs(emailToBeDeleted);
                if (list == null) {
                    actionRequest.setSuccessful(false);
                    return ServerResponse.EMAIL_NOT_FOUND;
                }

                //If it is already in the trash we just delete it permanently, otherwise we move it to trash
                if (list == sender.trashProperty()) {
                    list.remove(emailToBeDeleted);
                    actionRequest.setSuccessful(true);
                    return ServerResponse.ACTION_COMPLETED;
                } else {
                    list.remove(emailToBeDeleted);
                    sender.trashProperty().add(emailToBeDeleted);
                    emailToBeDeleted.setState(EmailState.TRASHED);
                    actionRequest.setSuccessful(true);
                    return ServerResponse.ACTION_COMPLETED;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                actionRequest.setSuccessful(false);
                return ServerResponse.UNKNOWN_ERROR;
            }
        }

        /**
         * sendAllEmails is the method that allows a Client
         * to download each email contained in the different sections
         * if the Client is new, we save it on JSON file but because he is new,
         * he doesn't have Emails, so we send CLIENT_NOT_FOUND as response.
         * Otherwise, we send to the Client every single Email (found locally)
         * by using objectOutputStream
         *
         * @param actionRequest the Action sent from Client
         */
        void sendAllEmails(Action actionRequest) {
            try {
                Client requestClient = findClientByAddress(actionRequest.getSender());

                //If the client has never been seen before, since he just logged on we add him to possible new clients
                if (requestClient == null) //only one time this could happen
                {
                    server.addClient(new Client(actionRequest.getSender(), false));
                    server.saveClientsToJSON();
                    sendResponse(ServerResponse.CLIENT_NOT_FOUND);
                    actionRequest.setSuccessful(false);
                    return;
                }

                //Get all emails of given client
                SimpleListProperty<Email> allEmails = new SimpleListProperty<>(FXCollections.observableArrayList());
                allEmails.addAll(requestClient.inboxProperty());
                allEmails.addAll(requestClient.trashProperty());
                allEmails.addAll(requestClient.sentProperty());
                allEmails.addAll(requestClient.draftsProperty());

                sendResponse(ServerResponse.ACTION_COMPLETED);
                actionRequest.setSuccessful(true);

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

    /**
     * setEmailAsRead is the method that handle the read state of an Email,
     * first we read the SerializableEmail from objectInputStream,
     * we search if Client exists,
     * if is true, we search only in inboxSection because an unread Email
     * can only be there, if there isn't we send EMAIL_NOT_FOUND as response
     * otherwise we set the Email state to true and saving to the JSON file the info,
     * and finally send ACTION_COMPLETED as response.
     *
     * @param actionRequest the Action sent from Client
     * @param objectInputStream used to read the SerializableEmail sent from Client
     * @return ServerResponse
     */
    private ServerResponse setEmailAsRead(Action actionRequest, ObjectInputStream objectInputStream) {
        try {
            SerializableEmail serializableEmail = (SerializableEmail) objectInputStream.readObject();
            long id = serializableEmail.getID();

            Client sender = findClientByAddress(actionRequest.getSender());
            if (sender == null)
            {
                actionRequest.setSuccessful(false);
                return ServerResponse.CLIENT_NOT_FOUND;
            }

            //We only check inbox as an unread email can only be there
            Email email = sender.findEmailById(sender.inboxProperty(), id);
            if (email == null) {
                actionRequest.setSuccessful(false);
                return ServerResponse.EMAIL_NOT_FOUND;
            }

            email.setRead(true);
            server.saveClientsToJSON();
            actionRequest.setSuccessful(true);
            return ServerResponse.ACTION_COMPLETED;
        } catch (Exception ex) {
            ex.printStackTrace();
            actionRequest.setSuccessful(false);
            return ServerResponse.UNKNOWN_ERROR;
        }
    }

    /**
     * We use this runnable class
     * to save Clients' info by using
     * its method run() that call our
     * function saveClientsToJSON
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

