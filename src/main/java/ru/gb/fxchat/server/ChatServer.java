package ru.gb.fxchat.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {


     private final List<ClientHandler> clients;

    public ChatServer() {
        this.clients = new ArrayList<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
        AuthService authService = new InMemoryAuthService())  {
        while (true) {
            System.out.println("Ожидаю подключения...");
            final Socket socket = serverSocket.accept();
            new ClientHandler(socket, this, authService);
            System.out.println("Клиент подключился!");
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
                client.sendMessage(message);
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (nick.equals(client.getNick())) {
                return true;
            }
        }
        return false;
    }

    public void unsubsribe(ClientHandler client) {
        clients.remove(client);
    }

    // метод для приватных сообщений
    public void privateMessage(String message) {
        if (message.startsWith("/w")) {
            final String[] split = message.split("\\p{Blank}+");
            final String name = split[1];
            int startNum = 8;
            int endNum = message.length();
            message = message.substring(startNum, endNum);
            for (ClientHandler client : clients) {
                if (name.equals(client.getNick())) {
                    client.sendPrivateMessage(name, message);
                }

            }
        }
    }

    }