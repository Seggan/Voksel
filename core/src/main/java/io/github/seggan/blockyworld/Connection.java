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

package io.github.seggan.blockyworld;

import io.github.seggan.blockyworld.server.packets.ChunkPacket;
import io.github.seggan.blockyworld.server.packets.OKPacket;
import io.github.seggan.blockyworld.server.packets.Packet;
import io.github.seggan.blockyworld.server.packets.PacketType;
import io.github.seggan.blockyworld.server.packets.PlayerPacket;
import io.github.seggan.blockyworld.server.packets.UserMovePacket;
import io.github.seggan.blockyworld.server.packets.WorldPacket;
import io.github.seggan.blockyworld.util.Vector;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import io.github.seggan.blockyworld.world.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessagePack;

import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class Connection {

    private static final int HEADER_SIZE = Short.BYTES + Integer.BYTES;

    private final Socket socket;
    private final InputStream in;

    private final BlockingQueue<Packet> queue = new LinkedBlockingQueue<>();

    public Connection(@NonNull Socket socket) {
        this.socket = socket;
        try {
            OutputStream out = this.socket.getOutputStream();
            in = this.socket.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Thread thread = new Thread(() -> {
            while (!socket.isInputShutdown()) {
                try {
                    byte[] header;
                    try {
                        header = in.readNBytes(HEADER_SIZE);
                    } catch (SocketException e) {
                        break;
                    }
                    ByteBuffer buffer = ByteBuffer.wrap(header);
                    short code = buffer.getShort();
                    int length = buffer.getInt();
                    byte[] body = in.readNBytes(length);
                    Packet pack = PacketType.getByCode(code)
                        .deserializer()
                        .deserialize(MessagePack.newDefaultUnpacker(body), true)
                        .address(socket.getInetAddress());
                    queue.add(pack);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Sends the packet
     *
     * @param toSend the packet to send
     * @return a packet sent pack
     */
    @Nullable
    public <T extends Packet> T sendPacket(@NonNull Packet toSend, @Nullable Class<T> returnType) {
        try {
            socket.getOutputStream().write(toSend.serialize());

            while (true) {
                Packet pack = queue.take();
                if (toSend.type().allowOk() && pack instanceof OKPacket) {
                    return null;
                }
                if (pack.getClass().equals(returnType)) {
                    return returnType.cast(pack);
                }
                queue.add(pack);
            }
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    @Nullable
    public World requestWorld() {
        WorldPacket packet = sendPacket(new WorldPacket(), WorldPacket.class);
        if (packet == null) return null;
        return packet.world();
    }

    @Nullable
    public Chunk requestChunk(int pos, @NonNull World world) {
        ChunkPacket packet = sendPacket(new ChunkPacket(pos, world), ChunkPacket.class);
        if (packet == null) return null;
        return packet.chunk();
    }

    public void sendPlayerMove(@NonNull Player player, @NonNull Vector vector) {
        sendPacket(new UserMovePacket(vector, player.uuid()), null);
    }

    public void connectPlayer(@NonNull Player player) {
        sendPacket(new PlayerPacket(player), null);
    }

    @Nullable
    public Packet nextReceived() {
        return queue.poll();
    }

}
