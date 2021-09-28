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

package io.github.seggan.blockyworld.lwjgl3;

import io.github.seggan.blockyworld.server.ClientRecvThread;
import io.github.seggan.blockyworld.server.IServer;
import io.github.seggan.blockyworld.server.MainThread;
import io.github.seggan.blockyworld.server.packets.OKPacket;
import io.github.seggan.blockyworld.server.packets.Packet;
import io.github.seggan.blockyworld.util.MagicValues;
import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ClientServerImpl implements IServer {

    private final ClientRecvThread recvThread;
    private final MainThread mainThread;

    private volatile boolean terminated;
    private final ServerSocket server;
    private final Socket conn;

    public ClientServerImpl(BlockingQueue<Object> queue) throws IOException, InterruptedException {
        server = new ServerSocket(MagicValues.PORT);
        queue.add(new Object());
        try {
            conn = server.accept();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    conn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }));

            // this is in integrated server; no need for protocol check
            conn.getInputStream().readNBytes(2);
            conn.getOutputStream().write(new OKPacket().serialize());

            recvThread = new ClientRecvThread(conn, ClientServerImpl.this);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            throw null;
        }
        mainThread = new MainThread(this);
        recvThread.start();
        mainThread.start();
    }

    @Override
    public void send(@NonNull Packet packet, @Nullable InetAddress sendTo) {
        try {
            byte[] ser = packet.serialize();
            synchronized (conn) {
                conn.getOutputStream().write(ser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void stopThread(@NonNull InetAddress address) {
        terminated = true;
        recvThread.interrupt();
        mainThread.interrupt();
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public MainThread mainThread() {
        return mainThread;
    }
}
