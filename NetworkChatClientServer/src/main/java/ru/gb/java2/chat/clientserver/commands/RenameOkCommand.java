package ru.gb.java2.chat.clientserver.commands;

import java.io.Serializable;


    public class RenameOkCommand implements Serializable {

        private final String newUsername;

        public RenameOkCommand(String username) {
            this.newUsername = username;
        }

        public String getUsername() {
            return newUsername;
        }
    }

