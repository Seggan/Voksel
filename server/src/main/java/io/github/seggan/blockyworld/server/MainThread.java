/*
 * A light 2D Minecraft clone
 * Copyright (C) 2021 Seggan (segganew@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
