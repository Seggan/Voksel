package io.github.seggan.blockyworld.server;

import lombok.NonNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RequestProcessor extends Thread {

    private final Queue<Request> pending = new ConcurrentLinkedDeque<>();

    public RequestProcessor() {
        super();
        setDaemon(true);
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            // TODO implement
        }
    }

    public void addRequest(@NonNull Request request) {
        pending.add(request);
    }
}
