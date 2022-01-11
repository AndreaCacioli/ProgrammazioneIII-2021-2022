package com.progiii.mailclientserver.client.model;

/*
* This enum represents all possible states an email could be.
* This state can vary between clients and is not proper of the text:
* Example: if client A sends an email to client B,
* the same email will be "SENT" for A
* but at the same time "RECEIVED" for B
* */
public enum EmailState
{
    RECEIVED,
    SENT,
    DRAFTED,
    TRASHED,

}
