package ru.gb.fxchat.server;

import java.io.Closeable;
import java.sql.SQLException;

public interface AuthService extends Closeable {
    String getNickByLoginAndPassword(String login, String password) throws SQLException;
}
