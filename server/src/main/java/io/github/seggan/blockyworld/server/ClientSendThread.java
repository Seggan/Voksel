package io.github.seggan.blockyworld.server;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ClientSendThread extends Thread {

    private final Socket client;

    private final Queue<byte[]> sendQueue = new ConcurrentLinkedDeque<>();

    public ClientSendThread(Socket client) {
        super();
        setDaemon(true);
        this.client = client;
    }

    @Override
    @SneakyThrows(IOException.class)
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            byte[] bytes;
            if ((bytes = sendQueue.poll()) != null) {
                client.getOutputStream().write(bytes);
            }
        }
    }

    public void send(byte[] bytes) {
        sendQueue.add(bytes);
    }
}
