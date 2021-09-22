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

import io.github.seggan.blockyworld.server.packets.Packet;
import io.github.seggan.blockyworld.server.packets.PacketType;
import org.msgpack.core.MessagePack;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class ClientRecvThread extends ClientThread {

    private static final int HEADER_SIZE = Short.BYTES + Integer.BYTES; // 6
    private final IServer server;

    public ClientRecvThread(Socket client, IServer server) {
        super(client, "Receiving thread for " + client.getInetAddress().getHostAddress());
        this.server = server;
    }

    @Override
    @SneakyThrows(IOException.class)
    public void run() {
        InputStream in = client.getInputStream();
        while (!client.isClosed() && !stop) {
            byte[] header;
            try {
                header = in.readNBytes(HEADER_SIZE);
            } catch (SocketException e) {
                break;
            }
            if (header.length == 0) break;
            ByteBuffer buffer = ByteBuffer.wrap(header);
            short code = buffer.getShort();
            if (code == 4) {
                continue;
            }
            int length = buffer.getInt();
            byte[] body = in.readNBytes(length);
            Packet packet = PacketType.getByCode(code)
                .deserializer()
                .deserialize(MessagePack.newDefaultUnpacker(body), false)
                .address(client.getInetAddress());
            server.mainThread().addRequest(packet);
        }
        server.stopThread(client.getInetAddress());
    }
}
