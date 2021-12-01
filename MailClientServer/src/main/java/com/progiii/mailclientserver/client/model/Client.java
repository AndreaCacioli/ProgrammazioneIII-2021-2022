package com.progiii.mailclientserver.client.model;

import com.progiii.mailclientserver.utils.GravatarRequests;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Client {
    private SimpleStringProperty address;
    private SimpleListProperty<Email> inbox;
    private SimpleListProperty<Email> drafts;
    private SimpleListProperty<Email> sent;
    private SimpleListProperty<Email> trash;
    private SimpleObjectProperty<Image> image;

    public SimpleObjectProperty<Image> imageProperty() {
        return image;
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

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

    public Email selectedEmail;
    public Email newEmail;

    public Client() {
        address = new SimpleStringProperty();
        address.setValue(Email.getRandomAddress());
        image = new SimpleObjectProperty<Image>(GravatarRequests.getProfilePicture(address.getValue()));
        inbox = new SimpleListProperty<Email>(FXCollections.observableArrayList());
        drafts = new SimpleListProperty<Email>(FXCollections.observableArrayList());
        sent = new SimpleListProperty<Email>(FXCollections.observableArrayList());
        trash = new SimpleListProperty<Email>(FXCollections.observableArrayList());
        /*for(int i=0;i<10;i++){
            inbox.add(Email.getRandomEmail(EmailState.RECEIVED));
            drafts.add(Email.getRandomEmail(EmailState.DRAFTED));
            sent.add(Email.getRandomEmail(EmailState.SENT));
            trash.add(Email.getRandomEmail(EmailState.TRASHED));
        }*/

        String[] names = {"inbox", "sent", "drafts", "trashed"};
        //for (int i = 0; i < names.length; i++)
        readFromJSon("./MailClientServer/src/main/resources/com/progiii/mailclientserver/client/data/" + names[2] + ".json", drafts);

    }

    private void readFromJSon(String JSonFile, SimpleListProperty<Email> state) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(JSonFile)) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONArray emailList = (JSONArray) obj;
            System.out.println(emailList);
            for(int i = 0; i < emailList.size()-1;i++) {
                state.add(parseEmailObject((JSONObject) emailList.get(i)));
                System.out.println("email:"+emailList.get(i));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static Email parseEmailObject(JSONObject email) {
        JSONObject emailObject = (JSONObject) email.get("email");
        String sender = (String) emailObject.get("sender");
        String receiver = (String) emailObject.get("receiver");
        String subject = (String) emailObject.get("subject");
        String body = (String) emailObject.get("body");
        return new Email(sender, receiver, subject, body, EmailState.DRAFTED);
    }
}
