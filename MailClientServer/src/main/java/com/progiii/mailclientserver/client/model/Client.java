package com.progiii.mailclientserver.client.model;

import com.progiii.mailclientserver.utils.GravatarRequests;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;

public class Client
{
    private SimpleStringProperty address;
    private SimpleListProperty<Email> inbox;
    private SimpleListProperty<Email> drafts;
    private SimpleListProperty<Email> sent;
    private SimpleListProperty<Email> trash;
    private SimpleObjectProperty<Image> image;

    public SimpleObjectProperty<Image> imageProperty() {return image;}

    public SimpleStringProperty addressProperty() {return address;}

    public SimpleListProperty<Email> inboxProperty() {return inbox;}

    public SimpleListProperty<Email> draftsProperty() {return drafts;}

    public SimpleListProperty<Email> sentProperty() {return sent;}

    public SimpleListProperty<Email> trashProperty() {return trash;}

    public Email selectedEmail;
    public Email newEmail;

    public Client()
    {
        address = new SimpleStringProperty();
        address.setValue(Email.getRandomAddress());
        image = new SimpleObjectProperty<Image>(GravatarRequests.getProfilePicture(address.getValue()));
        inbox = new SimpleListProperty<Email>(FXCollections.observableArrayList());
        drafts = new SimpleListProperty<Email>(FXCollections.observableArrayList());
        sent = new SimpleListProperty<Email>(FXCollections.observableArrayList());
        trash = new SimpleListProperty<Email>(FXCollections.observableArrayList());

        for (int i = 0; i < 10; i++)
        {
            inbox.add(Email.getRandomEmail(EmailState.RECEIVED));
            drafts.add(Email.getRandomEmail(EmailState.DRAFTED));
            sent.add(Email.getRandomEmail(EmailState.SENT));
            trash.add(Email.getRandomEmail(EmailState.TRASHED));
        }
    }
}
