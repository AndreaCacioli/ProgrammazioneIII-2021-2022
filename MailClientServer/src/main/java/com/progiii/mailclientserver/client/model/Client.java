package com.progiii.mailclientserver.client.model;

import com.progiii.mailclientserver.utils.GravatarRequests;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Client {
    private SimpleStringProperty address;
    private SimpleListProperty<Email> inbox;
    private SimpleListProperty<Email> drafts;
    private SimpleListProperty<Email> sent;
    private SimpleListProperty<Email> trash;
    private SimpleObjectProperty<Image> image;
    private SimpleStringProperty status;

    public SimpleStringProperty addressProperty() { return address; }

    public SimpleListProperty<Email> inboxProperty() {
        return inbox;
    }

    public SimpleListProperty<Email> draftsProperty() {
        return drafts;
    }

    public SimpleListProperty<Email> sentProperty() {
        return sent;
    }

    public SimpleListProperty<Email> trashProperty() {
        return trash;
    }

    public SimpleObjectProperty<Image> imageProperty() {
        return image;
    }

    public SimpleStringProperty statusProperty() { return status; }

    public String getAddress() {
        return address.get();
    }

    public Email selectedEmail;
    public Email newEmail;

    /* NON CI SERVE PIU'
        public Client() {
            address = new SimpleStringProperty();
            image = new SimpleObjectProperty<>(GravatarRequests.getProfilePicture(address.getValue()));
            inbox = new SimpleListProperty<>(FXCollections.observableArrayList());
            drafts = new SimpleListProperty<>(FXCollections.observableArrayList());
            sent = new SimpleListProperty<>(FXCollections.observableArrayList());
            trash = new SimpleListProperty<>(FXCollections.observableArrayList());

            String[] names = {"inbox", "sent", "drafts", "trashed"};
            SimpleListProperty[] simpleListProperties = {inbox, sent, drafts, trash};
            EmailState[] emailStates = {EmailState.RECEIVED, EmailState.SENT, EmailState.DRAFTED, EmailState.TRASHED};
            for (int i = 0; i < names.length; i++)
                readFromJSon("./src/main/resources/com/progiii/mailclientserver/client/data/" + names[i] + ".json", simpleListProperties[i], emailStates[i]);

        }
    */
    public Client(String address, boolean withImage) {
        this.address = new SimpleStringProperty(address);
        this.inbox = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.sent = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.trash = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.drafts = new SimpleListProperty<>(FXCollections.observableArrayList());
        if(withImage) image = new SimpleObjectProperty<Image>(GravatarRequests.getProfilePicture(address));
        this.status = new SimpleStringProperty("");

    }

    private void readFromJSon(String JSonFile, SimpleListProperty<Email> state, EmailState emailState) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(JSonFile)) {
            Object obj = jsonParser.parse(reader);
            JSONArray emailList = (JSONArray) obj;
            for (int i = 0; i < emailList.size() - 1; i++) {
                state.add(parseEmailObject((JSONObject) emailList.get(i), emailState));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Email parseEmailObject(JSONObject email, EmailState emailState) {
        JSONObject emailObject = (JSONObject) email.get("email");
        String sender = (String) emailObject.get("sender");
        String receiver = (String) emailObject.get("receiver");
        String subject = (String) emailObject.get("subject");
        String body = (String) emailObject.get("body");
        return new Email(sender, receiver, subject, body, emailState);
    }

    public void saveAll() {
        int i = 0;
        String[] names = {"inbox", "sent", "drafts", "trashed"};
        SimpleListProperty<Email>[] lists = new SimpleListProperty[]{this.inboxProperty(), this.sentProperty(), this.draftsProperty(), this.trashProperty()};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        for (SimpleListProperty<Email> list : lists) {
            JSONArray array = new JSONArray();
            for (Email email : list) {
                JSONObject emailDetails = new JSONObject();
                emailDetails.put("sender", email.getSender());
                emailDetails.put("receiver", email.getReceiver());
                emailDetails.put("subject", email.getSubject());
                emailDetails.put("body", email.getBody());
                emailDetails.put("dateTime", email.getDateTime().format(formatter));
                JSONObject emailList = new JSONObject();
                emailList.put("email", emailDetails);
                array.add(emailList);
            }
            try {
                Files.createDirectory(Path.of("./src/main/resources/com/progiii/mailclientserver/client/data/" + getAddress().split("@")[0]));
                FileWriter fileWriter = new FileWriter("./src/main/resources/com/progiii/mailclientserver/client/data/" + getAddress().split("@")[0] + '/' + names[i] + ".json");
                BufferedWriter out = new BufferedWriter(fileWriter);
                try {
                    fileWriter.flush();
                    out.write(array.toJSONString());
                    out.flush();
                    fileWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    out.close();
                    fileWriter.close();
                }
            } catch (FileAlreadyExistsException fileEx) {
                System.out.println("Update JSon File");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            i++;
        }
    }
}
