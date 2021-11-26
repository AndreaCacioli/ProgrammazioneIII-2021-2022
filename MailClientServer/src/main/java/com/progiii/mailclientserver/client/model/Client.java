package com.progiii.mailclientserver.client.model;

import com.progiii.mailclientserver.client.utils.GravatarRequests;
import com.progiii.mailclientserver.client.utils.MD5Util;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Client
{
    public  String id = "GruppoProgIII@javafxslayss.it";

    private SimpleListProperty<Email> inbox;
    private SimpleListProperty<Email> drafts;
    private SimpleListProperty<Email> sent;
    private SimpleListProperty<Email> trash;
    private SimpleObjectProperty<Image> image;

    public SimpleObjectProperty<Image> imageProperty() {return image;}

    public SimpleListProperty<Email> inboxProperty() {return inbox;}

    public SimpleListProperty<Email> draftsProperty() {return drafts;}

    public SimpleListProperty<Email> sentProperty() {return sent;}

    public SimpleListProperty<Email> trashProperty() {return trash;}

    public Email selectedEmail;
    public Email newEmail;

    public Client()
    {
        image = new SimpleObjectProperty<Image>(GravatarRequests.getProfilePicture(id));
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
