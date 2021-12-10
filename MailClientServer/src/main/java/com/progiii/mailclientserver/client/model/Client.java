package com.progiii.mailclientserver.client.model;

import com.progiii.mailclientserver.utils.GravatarRequests;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;


public class Client {
    private final SimpleStringProperty address;
    private final SimpleListProperty<Email> inbox;
    private final SimpleListProperty<Email> sent;
    private final SimpleListProperty<Email> drafts;
    private final SimpleListProperty<Email> trash;
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
}
