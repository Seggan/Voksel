package io.github.seggan.blockyworld.server;

import lombok.Getter;
import lombok.NonNull;

import java.net.Socket;

abstract class ClientThread extends Thread {

    @Getter
    protected final Socket client;

    protected ClientThread(@NonNull Socket client) {
        this.client = client;
        setDaemon(true);
    }

    @Override
    public void run() {
        super.run();
    }
}
