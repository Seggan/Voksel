package io.github.seggan.blockyworld.server;

import io.github.seggan.blockyworld.world.World;

import lombok.NonNull;

import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MainThread extends Thread {

    private final Queue<Packet> pending = new ConcurrentLinkedDeque<>();

    private final World world = new World("world");

    private final InetAddress thisAddress;

    public MainThread(InetAddress thisAddress) {
        super("Main Server Thread");
        this.thisAddress = thisAddress;
        setDaemon(true);
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            Packet packet;
            if ((packet = pending.poll()) != null) {
                if (packet instanceof ChunkPacket chunkRequest) {
                    ChunkPacket back = new ChunkPacket(world.getChunk(chunkRequest.position()), thisAddress);
                    Server.send(back, chunkRequest.address());
                } else if (packet instanceof WorldPacket worldRequest) {
                    WorldPacket back = new WorldPacket(world, thisAddress);
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

    public void addRequest(@NonNull Packet packet) {
        pending.add(packet);
    }
}
