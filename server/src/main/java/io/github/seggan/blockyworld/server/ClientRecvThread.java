package io.github.seggan.blockyworld.server;

import java.net.Socket;

public class ClientRecvThread extends Thread {

    private final Socket client;

    public ClientRecvThread(Socket client) {
        super();
        setDaemon(true);
        this.client = client;
    }

    @Override
    public void run() {

    }
}
