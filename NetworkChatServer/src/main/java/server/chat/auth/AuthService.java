package server.chat.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.chat.ClientHandler;

import java.sql.*;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private Connection connection;
    private PreparedStatement findByLogin;
    private PreparedStatement rename;

    public AuthService() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:auth_base.db");
            logger.info("DataBase connection up.");
            findByLogin = connection.prepareStatement("SELECT * FROM AUTHENTICATION WHERE LOGIN = ? AND PASSWORD = ?");
            rename = connection.prepareStatement("UPDATE AUTHENTICATION SET nickname = ? WHERE nickname = ?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public synchronized String getUsernameByLoginAndPassword(String login, String password) {
        try {
            findByLogin.setString(1, login);
            findByLogin.setString(2, password);
            ResultSet resultSet = findByLogin.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nickname");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void stop() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean tryRename(ClientHandler clientHandler, String newName) {
        int result = 0;
        try {
            rename.setString(1,newName);
            rename.setString(2, clientHandler.getUsername());
            result = rename.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result == 1;
    }
}
