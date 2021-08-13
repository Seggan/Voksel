package io.github.seggan.blockyworld.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Launches the server application.
 */
public class ServerLauncher {

    public static final Map<InetAddress, ClientRecvThread> RECEIVING_THREADS = new HashMap<>();
    public static final Map<InetAddress, ClientSendThread> SENDING_THREADS = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(16255);
        while (!server.isClosed()) {
            Socket socket = server.accept();
            InetAddress address = socket.getInetAddress();
            ClientRecvThread thread = new ClientRecvThread(socket);
            RECEIVING_THREADS.put(address, thread);
            thread.start();
            ClientSendThread thread1 = new ClientSendThread(socket);
            SENDING_THREADS.put(address, thread1);
            thread1.start();
        }
    }
}