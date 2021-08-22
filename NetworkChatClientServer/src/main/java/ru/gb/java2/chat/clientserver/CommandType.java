package ru.gb.java2.chat.clientserver;

public enum CommandType {
    AUTH,
    AUTH_OK,
    CLIENT_MESSAGE,
    END,
    ERROR,
    PRIVATE_MESSAGE,
    PUBLIC_MESSAGE,
    RENAME,
    RENAME_OK,
    UPDATE_USERS_LIST,
}
