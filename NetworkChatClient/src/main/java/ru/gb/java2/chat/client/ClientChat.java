package ru.gb.java2.chat.client;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.gb.java2.chat.client.controllers.AuthController;
import ru.gb.java2.chat.client.controllers.ChatController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;


public class ClientChat extends Application {
    public static ClientChat INSTANCE;

    private static final String CHAT_WINDOW_FXML = "chat.fxml";
    private static final String AUTH_DIALOG_FXML = "authDialog.fxml";
    private static final String FILE_NAME = "chatHistory.txt";

    private Stage primaryStage;
    private Stage authStage;
    private FXMLLoader chatWindowLoader;
    private FXMLLoader authLoader;
    private int historyLinesCount;


    @Override
    public void init() throws Exception {
        INSTANCE = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        initViews();
        getChatStage().show();
        loadHistory();
        getAuthStage().show();
        getAuthController().initMessageHandler();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Stage getAuthStage() {
        return authStage;
    }

    private AuthController getAuthController() {
        return authLoader.getController();
    }

    public ChatController getChatController() {
        return chatWindowLoader.getController();
    }

    private void initViews() throws IOException {
        initChatWindow();
        initAuthDialog();
    }

    private void initChatWindow() throws IOException {
        chatWindowLoader = new FXMLLoader();
        chatWindowLoader.setLocation(ClientChat.class.getResource(CHAT_WINDOW_FXML));

        primaryStage.setOnCloseRequest(event -> {
            saveHistory();
            Platform.exit();
        });

        Parent root = chatWindowLoader.load();
        this.primaryStage.setScene(new Scene(root));

        setStageForSecondScreen(primaryStage);

    }



    private void initAuthDialog() throws java.io.IOException {
        authLoader = new FXMLLoader();
        authLoader.setLocation(ClientChat.class.getResource(AUTH_DIALOG_FXML));
        Parent authDialogPanel = authLoader.load();

        authStage = new Stage();
        authStage.initOwner(primaryStage);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.setScene(new Scene(authDialogPanel));
    }


    private void setStageForSecondScreen(Stage primaryStage) {
        Screen secondScreen = getSecondScreen();
        Rectangle2D bounds = secondScreen.getBounds();
        primaryStage.setX(bounds.getMinX() + (bounds.getWidth() - 300) / 2);
        primaryStage.setY(bounds.getMinY() + (bounds.getHeight() - 200) / 2);
    }

    private Screen getSecondScreen() {
        for (Screen screen : Screen.getScreens()) {
            if (!screen.equals(Screen.getPrimary())) {
                return screen;
            }
        }
        return Screen.getPrimary();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Stage getChatStage() {
        return primaryStage;
    }

    public void switchToMainChatWindow(String username) {
        getPrimaryStage().setTitle(username);
        getChatController().initMessageHandler();
        getAuthController().close();
        getAuthStage().close();
    }
    public void setNewUserName(String newUserName){
        getPrimaryStage().setTitle(newUserName);
    }

    private void saveHistory() {
        String[] history = ClientChat.INSTANCE.getChatController().getChatHistory().getText().split("\\n");
        if (history.length == historyLinesCount){
            return;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = historyLinesCount; i < history.length; i++) {
                    stringBuilder.append(history[i]).append("\n");
            }
            try (PrintWriter printWriter = new PrintWriter(new FileWriter(FILE_NAME,StandardCharsets.UTF_8, true))){
                printWriter.println(stringBuilder.toString());
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public String getTitle(){
        return getPrimaryStage().getTitle();
    }

    private void loadHistory() throws IOException {
        List<String> previousMessages = Files.readAllLines(Paths.get(FILE_NAME));
        historyLinesCount = previousMessages.size() >=100 ? 100 : previousMessages.size();
        int startIndex = previousMessages.size() >= 100 ? previousMessages.size()-100 : 0;
        for (int i = startIndex; i < previousMessages.size(); i++) {
            ClientChat.INSTANCE.getChatController().getChatHistory().appendText(previousMessages.get(i) + "\n");
        }
    }
}