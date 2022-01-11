package com.progiii.mailclientserver.utils;

/*
* This enum represents a list of possible outcomes after a request is made to the server
*
* It is used to explain to the client what went wrong in case it did
* */
public enum ServerResponse {
    ACTION_COMPLETED,
    CLIENT_NOT_FOUND,
    RECEIVER_NOT_FOUND,
    UNKNOWN_ERROR,
    EMAIL_NOT_FOUND,
}
