package io.github.seggan.blockyworld.server;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Launches the server application.
 */
public class Server {

    public static final Map<InetAddress, ClientRecvThread> RECEIVING_THREADS = new HashMap<>();
    public static final Map<InetAddress, ClientSendThread> SENDING_THREADS = new HashMap<>();

    @Getter
    private static MainThread mainThread;

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(16255);
        mainThread = new MainThread(server.getInetAddress());
        mainThread.start();
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

    @SneakyThrows(IOException.class)
    public static void send(@NonNull Request request, @Nullable InetAddress sendTo) {
        byte[] bytes = request.serialize();
        if (sendTo == null) {
            for (ClientSendThread thread : SENDING_THREADS.values()) {
                thread.send(bytes);
            }
        } else {
            SENDING_THREADS.get(sendTo).send(bytes);
        }
    }
}