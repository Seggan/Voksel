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

import com.google.common.primitives.Bytes;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Launches the server application.
 */
public class Server {

    public static final Map<InetAddress, ClientRecvThread> RECEIVING_THREADS = new HashMap<>();
    public static final Map<InetAddress, ClientSendThread> SENDING_THREADS = new HashMap<>();

    @Getter
    private static MainThread mainThread;

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(16255);
        mainThread = new MainThread(server.getInetAddress());
        mainThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (ClientThread thread : RECEIVING_THREADS.values()) {
                try {
                    thread.client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (ClientThread thread : SENDING_THREADS.values()) {
                try {
                    thread.client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));

        while (!server.isClosed()) {
            Socket socket = server.accept();
            short ver = ByteBuffer.wrap(socket.getInputStream().readNBytes(2)).getShort();
            if (ver != Packet.PROTOCOL_VERSION) {
                // The invalid protocol packet: 00 03 packet,
                // 2 bytes protocol version
                socket.getOutputStream().write(Bytes.concat(new byte[]{0, 3},
                    ByteBuffer.allocate(Short.BYTES).putShort(Packet.PROTOCOL_VERSION).array()));
                socket.close();
                continue;
            }

            socket.getOutputStream().write(new OKPacket(server.getInetAddress()).serialize());

            InetAddress address = socket.getInetAddress();
            ClientRecvThread thread = new ClientRecvThread(socket);
            RECEIVING_THREADS.put(address, thread);
            thread.start();
            ClientSendThread thread1 = new ClientSendThread(socket);
            SENDING_THREADS.put(address, thread1);
            thread1.start();
        }
    }

    @SneakyThrows(IOException.class)
    public static void send(@NonNull Packet packet, @Nullable InetAddress sendTo) {
        byte[] bytes = packet.serialize();
        if (sendTo == null) {
            for (ClientSendThread thread : SENDING_THREADS.values()) {
                thread.send(bytes);
            }
        } else {
            ClientSendThread thread = SENDING_THREADS.get(sendTo);
            if (thread != null) {
                thread.send(bytes);
            }
        }
    }
}