package com.progiii.mailclientserver.utils;

/**
 * An enumeration containing all the possible operations that a client could request to the server
 * */
public enum Operation
{
    SEND_EMAIL,
    NEW_DRAFT,
    DELETE_EMAIL,
    GET_ALL_EMAILS,
    PING,
    READ_EMAIL
}
