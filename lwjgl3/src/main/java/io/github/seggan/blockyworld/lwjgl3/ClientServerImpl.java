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

import com.google.common.primitives.Bytes;
import io.github.seggan.blockyworld.server.ClientRecvThread;
import io.github.seggan.blockyworld.server.ClientSendThread;
import io.github.seggan.blockyworld.server.IServer;
import io.github.seggan.blockyworld.server.MainThread;
import io.github.seggan.blockyworld.server.packets.OKPacket;
import io.github.seggan.blockyworld.server.packets.Packet;
import io.github.seggan.blockyworld.util.MagicNumbers;
import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

public class ClientServerImpl implements IServer {

    private volatile ClientRecvThread recvThread = null;
    private volatile ClientSendThread sendThread = null;
    private volatile MainThread mainThread = null;

    private volatile boolean terminated;

    public ClientServerImpl(BlockingQueue<Object> queue) throws IOException, InterruptedException {
        ServerSocket server = new ServerSocket(MagicNumbers.PORT);
        Thread thread = new Thread(() -> {
            try {
                Socket socket = server.accept();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }));
                short ver = ByteBuffer.wrap(socket.getInputStream().readNBytes(2)).getShort();
                if (ver != Packet.PROTOCOL_VERSION) {
                    // The invalid protocol packet: 00 03 packet,
                    // 2 bytes protocol version
                    socket.getOutputStream().write(Bytes.concat(new byte[]{0, 3},
                        ByteBuffer.allocate(Short.BYTES).putShort(Packet.PROTOCOL_VERSION).array()));
                    socket.close();
                    System.exit(1);
                }

                socket.getOutputStream().write(new OKPacket(server.getInetAddress()).serialize());

                recvThread = new ClientRecvThread(socket, ClientServerImpl.this);
                sendThread = new ClientSendThread(socket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        thread.start();
        queue.add(new Object());
        thread.join();
        mainThread = new MainThread(this);
        mainThread.start();
        sendThread.start();
        recvThread.start();
    }

    @Override
    public void send(@NonNull Packet packet, @Nullable InetAddress sendTo) {
        try {
            sendThread.send(packet.serialize());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void stopThread(@NonNull InetAddress address) {
        terminated = true;
        sendThread.stopThread();
        recvThread.stopThread();
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
