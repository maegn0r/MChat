package ru.gb.java2.chat.clientserver.commands;

import java.io.Serializable;

public class RenameCommandData implements Serializable {

    private final String nicknameToRename;

    public RenameCommandData(String loginToRename) { this.nicknameToRename = loginToRename; }

    public String getNicknameToRename() {
        return nicknameToRename;
    }

}
