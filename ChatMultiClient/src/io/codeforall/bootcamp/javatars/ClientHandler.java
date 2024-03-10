package io.codeforall.bootcamp.javatars;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private Server server;
    private Set<String> blockedUsers = new HashSet<>();

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Enter your username: ");
            username = in.readLine();

            while (!server.isUsernameAvailable(username)) {
                out.println("Username is already taken. Enter a different username: ");
                username = in.readLine();
            }

            server.addUserToMap(username, this);
            server.broadcast(username + " has joined the chat.");

            String input;
            while ((input = in.readLine()) != null) {
                handleInput(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.removeUserFromMap(username);
                server.getClients().remove(this);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the input received from the client.
     *
     * @param input the input received from the client
     */
    private void handleInput(String input) {
        String[] parts = input.split(" ", 3);
        String command = parts[0];
        String argument = parts.length > 1 ? parts[1] : "";
        String message = parts.length > 2 ? parts[2] : "";

        handleCommand(command, argument, message);
    }

    /**
     * Handles the command received from the client.
     *
     * @param command  the command received from the client
     * @param argument the argument associated with the command
     * @param message  the message associated with the command
     */
    private void handleCommand(String command, String argument, String message) {
        Runnable[] actions = {
                () -> blockUser(argument),
                () -> unblockUser(argument),
                () -> server.broadcastUserList(),
                () -> handlePrivateMessage(argument, message),
                () -> broadcastMessage(argument)
        };

        String[] commandList = {"/block", "/unblock", "/online", "/private"};

        for (int i = 0; i < commandList.length; i++) {
            if (command.equals(commandList[i])) {
                actions[i].run();
                return;
            }
        }
    }

    /**
     * Sends a message to the client.
     *
     * @param message the message to be sent
     */
    public void sendMessage(String message) {
        out.println(message);
    }

    /**
     * Retrieves the username of the client.
     *
     * @return the username of the client
     */
    public String getUsername() {
        return username;
    }

    /**
     * Blocks a user.
     *
     * @param username the username of the user to be blocked
     */
    public void blockUser(String username) {
        blockedUsers.add(username);
    }

    /**
     * Unblocks a user.
     *
     * @param username the username of the user to be unblocked
     */
    public void unblockUser(String username) {
        blockedUsers.remove(username);
    }

    /**
     * Handles a private message from the client.
     *
     * @param receiver the recipient of the private message
     * @param message  the content of the private message
     */
    public void handlePrivateMessage(String receiver, String message) {
        server.sendPrivateMessage(username, receiver, message);
    }

    /**
     * Broadcasts a message to all clients except the blocked users.
     *
     * @param message the message to be broadcasted
     */
    public void broadcastMessage(String message) {
        if (!blockedUsers.contains(username)) {
            server.broadcast(username + ": " + message);
        }
    }
}
