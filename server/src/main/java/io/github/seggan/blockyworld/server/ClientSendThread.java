package io.github.seggan.blockyworld.server;

import java.net.Socket;

public class ClientSendThread extends Thread {

    private final Socket client;

    public ClientSendThread(Socket client) {
        super();
        setDaemon(true);
        this.client = client;
    }

    @Override
    public void run() {

    }
}
