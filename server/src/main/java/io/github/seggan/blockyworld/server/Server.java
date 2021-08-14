package io.github.seggan.blockyworld.server;

import org.apache.commons.lang3.Validate;

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
    private static final RequestProcessor requestProcessor = new RequestProcessor();

    public static void main(String[] args) throws IOException {
        requestProcessor.start();
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

    @SneakyThrows(IOException.class)
    public void send(@NonNull Request request) {
        RequestType type = request.type();
        Validate.isTrue(type.toClient(), "Request type %s is not to client!", type.name());
        byte[] bytes = request.serialize();
        for (ClientSendThread thread : SENDING_THREADS.values()) {
            thread.send(bytes);
        }
    }
}