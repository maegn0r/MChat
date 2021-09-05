package server.chat;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gb.java2.chat.clientserver.Command;
import ru.gb.java2.chat.clientserver.CommandType;
import ru.gb.java2.chat.clientserver.commands.AuthCommandData;
import ru.gb.java2.chat.clientserver.commands.PrivateMessageCommandData;
import ru.gb.java2.chat.clientserver.commands.PublicMessageCommandData;
import ru.gb.java2.chat.clientserver.commands.RenameCommandData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final int CONNECTION_REFUSE_TIMEOUT = 120_00;
    private final MyServer server;
    private final Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String username;

    public ClientHandler(MyServer server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public void handle(ExecutorService executorService) throws IOException {
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        executorService.execute(() -> {
            try {
                authentication();
                readMessages();
            } catch (IOException e) {
                logger.error("Failed to process message from client");
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    logger.error("Failed to close connection");

                }
            }
        });
    }

    private void authentication() throws IOException {

        Timer timer = createCloseConnectionByTimeoutTimer();

        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            if (command.getType() == CommandType.AUTH) {
                AuthCommandData data = (AuthCommandData) command.getData();
                String login    = data.getLogin();
                String password = data.getPassword();

                String username = server.getAuthService().getUsernameByLoginAndPassword(login, password);
                if (username == null) {
                    sendCommand(Command.errorCommand("Некорректные логин и пароль!"));
                } else if (server.isUsernameBusy(username)) {
                    sendCommand(Command.errorCommand("Такой юзер уже существует!"));
                } else {
                    this.username = username;
                    timer.cancel();
                    sendCommand(Command.authOkCommand(username));
                    server.subscribe(this);
                    return;
                }
            }
        }
    }

    private Timer createCloseConnectionByTimeoutTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (username == null) {
                    try {
                        closeConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, CONNECTION_REFUSE_TIMEOUT);

        return timer;
    }


    private Command readCommand() throws IOException {
        Command command = null;
        try {
            command = (Command) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            logger.error("Failed to read Command class");
            e.printStackTrace();
        }
        return command;
    }

    private void closeConnection() throws IOException {
        server.unsubscribe(this);
        clientSocket.close();
    }

    private void readMessages() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            switch (command.getType()) {
                case END:
                    return;
                case PRIVATE_MESSAGE: {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    String recipient = data.getReceiver();
                    String privateMessage = data.getMessage();
                    server.sendPrivateMessage(this, recipient, privateMessage);
                    break;
                }

                case RENAME: {
                    RenameCommandData data = (RenameCommandData) command.getData();
                    String newName = data.getNicknameToRename();
                    boolean result = server.getAuthService().tryRename(this, newName);
                    if (!result) {sendCommand(Command.errorCommand("Не удалось поменять ник! Неверные данные!"));
                    } else {
                        this.username = newName;
                        sendCommand(Command.renameOkCommand(newName));
                        server.notifyAboutNickChange();
                    }

                    break;
                }
                case PUBLIC_MESSAGE: {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    processMessage(data.getMessage());
                }
            }
        }
    }

    private void processMessage(String message) throws IOException {
        server.broadcastMessage(message, this);
    }

    public void sendCommand(Command command) throws IOException {
        outputStream.writeObject(command);
    }

    public String getUsername() {
        return username;
    }


}
