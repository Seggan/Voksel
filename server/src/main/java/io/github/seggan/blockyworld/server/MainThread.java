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

import io.github.seggan.blockyworld.world.ServerWorld;

import lombok.NonNull;

import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MainThread extends Thread {

    private final BlockingQueue<Packet> pending = new LinkedBlockingQueue<>();

    private final ServerWorld world = new ServerWorld("world");

    private final InetAddress thisAddress;
    private final OKPacket okPacket;

    public MainThread(InetAddress thisAddress) {
        super("Main Server Thread");
        this.thisAddress = thisAddress;
        this.okPacket = new OKPacket(thisAddress);
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            Packet packet;
            try {
                packet = pending.take();
            } catch (InterruptedException e) {
                return;
            }
            if (packet instanceof ChunkPacket chunkPacket) {
                ChunkPacket back = new ChunkPacket(world.chunk(chunkPacket.position()), thisAddress);
                Server.send(back, chunkPacket.address());
            } else if (packet instanceof WorldPacket worldPacket) {
                WorldPacket back = new WorldPacket(world, thisAddress);
                Server.send(back, worldPacket.address());
            } else if (packet instanceof PlayerPacket playerPacket) {
                world.addPlayer(playerPacket.player());
                Server.send(okPacket, playerPacket.address());
            }
        }
    }

    public void addRequest(@NonNull Packet packet) {
        pending.add(packet);
    }
}
