package com.progiii.mailclientserver.utils;

public enum ServerResponse {
    ACTION_COMPLETED,
    CLIENT_NOT_FOUND,
    RECEIVER_NOT_FOUND, //This is a no-operation response
    UNKNOWN_ERROR
}
