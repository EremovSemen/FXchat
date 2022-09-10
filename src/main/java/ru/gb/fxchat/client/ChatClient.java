package ru.gb.fxchat.client;

import javafx.application.Platform;
import ru.gb.fxchat.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.gb.fxchat.Command.*;

public class ChatClient {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String NAME;

    private final ChatController controller;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
              if(waitAuth()) {
                  readMessages();
              }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }).start();

    }

    private boolean waitAuth() throws IOException {
        while (true) {
            final String message = in.readUTF();
            final Command command = getCommand(message);
            final String[] params = command.parse(message);
            if (command == AUTHOK) { // /authok nick1
                final String nick = params[0];
                NAME = nick;
//                Path file = Path.of("root", "files", (NAME + ".txt"));
//                createDeleteFile(file);
                controller.setAuth(true);
                controller.addMessage("Успешная авторизация под ником " + nick);
               return true;
            }
            if (command == ERROR) {
                Platform.runLater(() -> controller.showError(params[0]));
                continue;
            }
            if (command == STOP) {
                Platform.runLater(() -> controller.showError("Истекло время на авторизацию, перезапустите приложение"));
                try {
                    Thread.sleep(5000); // Без sleep пользователь не увидит сообщение об ошибке. Хочется более изящного решения, но лень его искать
                    sendMessage(END);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }
                Path file = Path.of("root", "files", "1.txt");
                createDeleteFile(file);
                writeDataToFile(file, message);

        }
    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    private void readMessages() throws IOException {
        while (true) {
            final String message = in.readUTF();
            final Command command = getCommand(message);

            if (END == command) {
                controller.setAuth(false);
                break;
            }
            final String[] params = command.parse(message);
            Path file = Path.of("root", "files", (NAME + ".txt"));
            createDeleteFile(file);
            writeDataToFile(file,params[0]);
            if (ERROR == command) {
                String messageError = params[0];
                Platform.runLater(() -> controller.showError(messageError));
                continue;
            }
            if (MESSAGE == command) {
                Platform.runLater(() -> controller.addMessage(params[0]));
            }
            if (CLIENTS == command) {
                Platform.runLater(() -> controller.updateClientsList(params));

            }
        }
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }

    private static void createDeleteFile(Path file) {
        Path parent = file.getParent();
        if (!Files.exists(parent)) {
            System.out.println("Родительской папки не существует. Создадим");
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (Files.exists(file)) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                Files.createFile(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static void writeDataToFile(Path file, String data) {
        try {
            Files.writeString(file, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}