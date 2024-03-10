package io.codeforall.bootcamp.javatars;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private final int PORT = 9000;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private Map<String, ClientHandler> usernameMap = new HashMap<>();

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    /**
     * Starts the server and waits for client connections.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message the message to be broadcast
     */
    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * Broadcasts the list of online users to all connected clients.
     */
    public synchronized void broadcastUserList() {
        StringBuilder userList = new StringBuilder("Users Online: \n");
        clients.forEach(client -> userList.append(client.getUsername()).append(", "));
        broadcast(userList.toString());
    }

    /**
     * Adds a user to the user map.
     *
     * @param username       the username of the client
     * @param clientHandler  the associated client handler
     */
    public synchronized void addUserToMap(String username, ClientHandler clientHandler) {
        usernameMap.put(username, clientHandler);
    }

    /**
     * Removes a user from the user map.
     *
     * @param username the username of the client to be removed
     */
    public synchronized void removeUserFromMap(String username) {
        usernameMap.remove(username);
    }

    /**
     * Checks if a username is available.
     *
     * @param username the username to be checked
     * @return true if the username is available, false otherwise
     */
    public synchronized boolean isUsernameAvailable(String username) {
        return !usernameMap.containsKey(username);
    }

    /**
     * Sends a private message from a sender to a receiver.
     *
     * @param sender   the sender of the message
     * @param receiver the receiver of the message
     * @param message  the message content
     */
    public synchronized void sendPrivateMessage(String sender, String receiver, String message) {
        Optional.ofNullable(usernameMap.get(receiver)).ifPresent(handler -> handler.sendMessage("Private message from " + sender + ": " + message));
    }

    /**
     * Blocks a user.
     *
     * @param blocker  the username of the user blocking
     * @param blocked  the username of the user to be blocked
     */
    public synchronized void blockUser(String blocker, String blocked) {
        Optional.ofNullable(usernameMap.get(blocked)).ifPresent(handler -> handler.blockUser(blocker));
    }

    /**
     * Unblocks a user.
     *
     * @param unblocker    the username of the user unblocking
     * @param unblocked    the username of the user to be unblocked
     */
    public synchronized void unblockUser(String unblocker, String unblocked) {
        Optional.ofNullable(usernameMap.get(unblocked)).ifPresent(handler -> handler.unblockUser(unblocker));
    }

    /**
     * Returns the list of connected clients.
     *
     * @return the list of client handlers
     */
    public List<ClientHandler> getClients() {
        return clients;
    }
}
