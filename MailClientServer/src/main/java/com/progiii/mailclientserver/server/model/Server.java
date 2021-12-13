package com.progiii.mailclientserver.server.model;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;
import com.progiii.mailclientserver.utils.Action;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class Server {
    ArrayList<Action> actions;
    ArrayList<Client> clients;
    SimpleStringProperty log;
    private final String JSONClientsFile = "./src/main/resources/com/progiii/mailclientserver/server/data/clients.json";
    private boolean running = true;
    private final String[] names = {"inbox", "sent", "drafts", "trashed"};

    public ArrayList<Action> getActions() {
        return actions;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public SimpleStringProperty logProperty() {
        return log;
    }

    /**
     * Server's builder
     */
    public Server() {
        clients = new ArrayList<>();
        actions = new ArrayList<>();
        log = new SimpleStringProperty("");
        readFromJSonClientsFile();
    }

    public static EmailState stringToEmailState(String s) {
        if (s.compareTo("inbox") == 0) {
            return EmailState.RECEIVED;
        }
        if (s.compareTo("sent") == 0) {
            return EmailState.SENT;
        }
        if (s.compareTo("drafts") == 0) {
            return EmailState.DRAFTED;
        }
        if (s.compareTo("trashed") == 0) {
            return EmailState.TRASHED;
        } else return null;
    }


    public synchronized void add(Action incomingRequest) {
        actions.add(incomingRequest);
        log.setValue(log.getValue() + incomingRequest.toString() + '\n');
    }

    /**
     * Function which we used to add a Client
     * in our clientsList
     *
     * @param c
     */
    public void addClient(Client c) {
        if (c != null)
            clients.add(c);
    }

    /**
     * Server's state Handler
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * we use readFromJSonClientsFile to
     * read the client's JSon
     */
    public void readFromJSonClientsFile() {
        clients = new ArrayList<>();
        System.out.println("Loading Client's info...");
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(JSONClientsFile)) {
            Object obj = jsonParser.parse(reader);
            JSONArray clientsList = (JSONArray) obj;
            for (int i = 0; i < clientsList.size(); i++) {
                JSONObject jsonClient = (JSONObject) clientsList.get(i);
                String clientString = jsonClient.toString();
                String[] junk = clientString.split("\"");
                Client client = new Client(junk[1], false);
                clients.add(client);
                parseClientObject((JSONObject) clientsList.get(i), client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * pareClientObject is a function used by
     * readFromJSonClientsFile to take every information
     * of one single client
     */
    private void parseClientObject(JSONObject clientJson, Client clientObject) {
        JSONArray sectionList = (JSONArray) clientJson.get(clientObject.getAddress());
        for (int i = 0; i < sectionList.size(); i++) {
            JSONObject sectionObj = (JSONObject) sectionList.get(i);
            JSONArray emailList = (JSONArray) sectionObj.get(names[i]);
            for (int j = 0; j < emailList.size(); j++) {
                JSONObject emailObj = (JSONObject) emailList.get(j);
                String sender = (String) emailObj.get("sender");
                String receiver = (String) emailObj.get("receiver");
                String subject = (String) emailObj.get("subject");
                String body = (String) emailObj.get("body");
                String dateTime = (String) emailObj.get("dateTime");
                long ID = (long) emailObj.get("ID");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                EmailState emailState = stringToEmailState(names[i]);
                Email email = new Email(sender, receiver, subject, body, emailState, LocalDateTime.parse(dateTime, formatter), ID);
                switch (emailState) {
                    case RECEIVED -> clientObject.inboxProperty().add(email);
                    case SENT -> clientObject.sentProperty().add(email);
                    case DRAFTED -> clientObject.draftsProperty().add(email);
                    case TRASHED -> clientObject.trashProperty().add(email);
                }
            }
        }
    }

    public synchronized void saveClientsToJSON() {
        System.out.println("Server Saving");
        JSONArray array = new JSONArray();

        for (Client client : clients) {

            JSONObject clients = new JSONObject();
            JSONArray arrayOfsection = new JSONArray();
            JSONObject section = null;
            JSONArray arrayOfEmail = null;
            JSONObject emailDetails = null;

            SimpleListProperty<Email>[] lists = new SimpleListProperty[]{client.inboxProperty(), client.sentProperty(), client.draftsProperty(), client.trashProperty()};
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            int i = 0;

            for (SimpleListProperty<Email> list : lists) {

                arrayOfEmail = new JSONArray();

                for (Email email : list) {
                    emailDetails = new JSONObject();
                    emailDetails.put("sender", email.getSender());
                    emailDetails.put("receiver", email.getReceiver());
                    emailDetails.put("subject", email.getSubject());
                    emailDetails.put("body", email.getBody());
                    emailDetails.put("dateTime", email.getDateTime().format(formatter));
                    emailDetails.put("ID", email.getID());
                    arrayOfEmail.add(emailDetails);
                }
                section = new JSONObject();
                section.put(names[i], arrayOfEmail);
                arrayOfsection.add(section);
                clients.put(client.getAddress(), arrayOfsection);
                i++;
            }

            array.add(clients);

            try {
                File file = new File("./src/main/resources/com/progiii/mailclientserver/server/data/clients.json");
                PrintWriter out = new PrintWriter(file);
                try {
                    out.write(array.toJSONString());
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
