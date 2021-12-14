package com.progiii.mailclientserver.client.model;

import com.progiii.mailclientserver.utils.GravatarRequests;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;


public class Client {
    private final SimpleStringProperty address;
    private SimpleListProperty<Email> inbox;
    private SimpleListProperty<Email> sent;
    private SimpleListProperty<Email> drafts;
    private SimpleListProperty<Email> trash;
    private SimpleObjectProperty<Image> image;
    private final SimpleStringProperty status;

    public Email selectedEmail;
    public Email newEmail;

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

    public SimpleObjectProperty<Image> imageProperty() {
        return image;
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public String getAddress() {
        return address.get();
    }

    public Client(String address, boolean withImage) {
        this.address = new SimpleStringProperty(address);
        this.inbox = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.sent = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.trash = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.drafts = new SimpleListProperty<>(FXCollections.observableArrayList());
        if (withImage) image = new SimpleObjectProperty<>(GravatarRequests.getProfilePicture(address));
        this.status = new SimpleStringProperty("");

    }

    public boolean contains(SimpleListProperty<Email> emailList, Email email) {
        for (Email e : emailList) {
            if (e.equals(email))
                return true;
        }
        return false;
    }

    public Email findEmailById(SimpleListProperty<Email> emailList, long id) {
        for (Email e : emailList) {
            if (e.getID() == id)
                return e;
        }
        return null;
    }

    public SimpleListProperty<Email> whereIs(Email email) {
        SimpleListProperty<Email> ret = null;
        if (inboxProperty().contains(email)) ret = inboxProperty();
        if (draftsProperty().contains(email)) ret = draftsProperty();
        if (sentProperty().contains(email)) ret = sentProperty();
        if (trashProperty().contains(email)) ret = trashProperty();
        return ret;
    }

    public void emptySelf() {
        //TODO check if it works
        Platform.runLater(() -> {
            inbox.setValue(FXCollections.observableArrayList());
            sent.setValue(FXCollections.observableArrayList());
            drafts.setValue(FXCollections.observableArrayList());
            trash.setValue(FXCollections.observableArrayList());
        });
    }

    public long getLargestID()
    {
        long max = 0;
        SimpleListProperty<Email> allEmails = new SimpleListProperty<>(FXCollections.observableArrayList());
        allEmails.addAll(inboxProperty());
        allEmails.addAll(sentProperty());
        allEmails.addAll(draftsProperty());
        allEmails.addAll(trashProperty());
        for (Email email : allEmails) {
            if (email.getID() > max) max = email.getID();
        }
        return max;
    }
}
