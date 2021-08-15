package ru.gb.java2.chat.server.chat.auth;

import java.sql.*;

public class AuthService {

    private Connection connection;
    private PreparedStatement findByLogin;

    public AuthService() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:auth_base.db");
            System.out.println("DataBase connection up.");
            findByLogin = connection.prepareStatement("SELECT * FROM AUTHENTICATION WHERE LOGIN = ? AND PASSWORD = ?");
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
}
