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

    /**
     * Constructor for Client
     *
     * @param address   his Email Address
     * @param withImage boolean var that allows to choose if Client uses Gravatar Image
     */
    public Client(String address, boolean withImage) {
        this.address = new SimpleStringProperty(address);
        this.inbox = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.sent = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.trash = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.drafts = new SimpleListProperty<>(FXCollections.observableArrayList());
        if (withImage) image = new SimpleObjectProperty<>(GravatarRequests.getProfilePicture(address));
        this.status = new SimpleStringProperty("");

    }

    /**
     * @param emailList section where we check if Email exists
     * @param email     Email to be searched in the list
     * @return true if List contains the Email
     */
    public boolean contains(SimpleListProperty<Email> emailList, Email email) {
        for (Email e : emailList) {
            if (e.equals(email))
                return true;
        }
        return false;
    }

    /**
     * @param emailList section where we check Email
     * @param id        Email id used to search in the List the Email
     * @return Email with the id passed by param
     */
    public Email findEmailById(SimpleListProperty<Email> emailList, long id) {
        for (Email e : emailList) {
            if (e.getID() == id)
                return e;
        }
        return null;
    }

    /**
     * @param email the email to be searched.
     * @return the SimpleListProperty where is the Email passed by param
     */
    public SimpleListProperty<Email> whereIs(Email email) {
        SimpleListProperty<Email> ret = null;
        if (inboxProperty().contains(email)) ret = inboxProperty();
        if (draftsProperty().contains(email)) ret = draftsProperty();
        if (sentProperty().contains(email)) ret = sentProperty();
        if (trashProperty().contains(email)) ret = trashProperty();
        return ret;
    }

    /**
     * Method that checks every Email of Client
     * and return his max ID
     *
     * @return max ID
     */
    public long getLargestID() {
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

    /**
     * Method used to search in the section if
     * there is another email with same ID of Email passed
     * by param
     * @param list  section where we check Email
     * @param email Email to be search in the section
     * @return true if already exists
     */
    @SuppressWarnings("All")
    public boolean hasSameIDInCollection(SimpleListProperty<Email> list, Email email) {
        for (Email emailIterated : list) {
            if (emailIterated.getID() == email.getID()) return true;
        }
        return false;
    }
}
