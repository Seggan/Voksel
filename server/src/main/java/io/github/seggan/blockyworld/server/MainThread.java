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

import io.github.seggan.blockyworld.server.packets.BlockUpdatePacket;
import io.github.seggan.blockyworld.server.packets.ChunkPacket;
import io.github.seggan.blockyworld.server.packets.EntityMovePacket;
import io.github.seggan.blockyworld.server.packets.OKPacket;
import io.github.seggan.blockyworld.server.packets.Packet;
import io.github.seggan.blockyworld.server.packets.PlayerPacket;
import io.github.seggan.blockyworld.server.packets.UserMovePacket;
import io.github.seggan.blockyworld.server.packets.WorldPacket;
import io.github.seggan.blockyworld.world.ServerWorld;
import io.github.seggan.blockyworld.world.block.Block;
import io.github.seggan.blockyworld.world.entity.Entity;
import io.github.seggan.blockyworld.world.entity.Player;

import lombok.Getter;
import lombok.NonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
public class MainThread extends Thread {

    private final BlockingQueue<Packet> pending = new LinkedBlockingQueue<>();

    private final ServerWorld world = new ServerWorld("world");

    private final InetAddress thisAddress = InetAddress.getLocalHost();
    private final OKPacket okPacket;
    private final IServer server;

    public MainThread(IServer server) throws UnknownHostException {
        super("Main Server Thread");
        this.okPacket = new OKPacket();
        this.server = server;
        setDaemon(true);
    }

    @Override
    public void run() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(new Ticker(this), 0, 50, TimeUnit.MILLISECONDS);
        while (!server.isTerminated()) {
            Packet packet;
            try {
                packet = pending.take();
            } catch (InterruptedException e) {
                break;
            }

            InetAddress sourceAddress = packet.address();
            if (packet instanceof ChunkPacket chunkPacket) {
                ChunkPacket back = new ChunkPacket(world.chunkAt(chunkPacket.position()));
                server.send(back, sourceAddress);
            } else if (packet instanceof WorldPacket) {
                WorldPacket back = new WorldPacket(world);
                server.send(back, sourceAddress);
            } else if (packet instanceof PlayerPacket playerPacket) {
                world.addPlayer(playerPacket.player());
                server.send(okPacket, sourceAddress);
            } else if (packet instanceof EntityMovePacket entityMovePacket) {
                Entity e = world.entity(entityMovePacket.uuid());
                e.direction().add(entityMovePacket.vector());
                server.send(okPacket, sourceAddress);
                server.send(new EntityMovePacket(e.uuid(), e.direction()), null);
            } else if (packet instanceof UserMovePacket userMovePacket) {
                Player p = world.player(userMovePacket.uuid());
                p.moving().add(userMovePacket.vector());
                server.send(okPacket, sourceAddress);
            } else if (packet instanceof BlockUpdatePacket blockUpdatePacket) {
                Block b = blockUpdatePacket.block();
                world.chunkAt(b.chunk().position()).setBlock(b);
                server.send(okPacket, sourceAddress);
                server.send(blockUpdatePacket, null);
            }
        }
        executor.shutdownNow();
    }

    public void addRequest(@NonNull Packet packet) {
        pending.add(packet);
    }
}
