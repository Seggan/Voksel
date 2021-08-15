package io.github.seggan.blockyworld.server;

import io.github.seggan.blockyworld.world.World;

import lombok.NonNull;

import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MainThread extends Thread {

    private final Queue<Request> pending = new ConcurrentLinkedDeque<>();

    private final World world = new World("world");

    private final InetAddress thisAddress;

    public MainThread(InetAddress thisAddress) {
        super();
        this.thisAddress = thisAddress;
        setDaemon(true);
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            Request request;
            if ((request = pending.poll()) != null) {
                if (request instanceof ChunkRequest chunkRequest) {
                    ChunkRequest back = new ChunkRequest(world.getChunk(chunkRequest.position()), thisAddress);
                    Server.send(back, chunkRequest.address());
                } else if (request instanceof WorldRequest worldRequest) {
                    WorldRequest back = new WorldRequest(world, thisAddress);
                    Server.send(back, worldRequest.address());
                }
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public void addRequest(@NonNull Request request) {
        pending.add(request);
    }
}
