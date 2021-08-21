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

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ClientSendThread extends ClientThread {

    private final Queue<byte[]> sendQueue = new ConcurrentLinkedDeque<>();

    public ClientSendThread(Socket client) {
        super(client, "Sending thread for " + client.getInetAddress().getHostAddress());
    }

    @Override
    @SneakyThrows(IOException.class)
    public void run() {
        while (!client.isClosed() && !stop) {
            byte[] bytes;
            if ((bytes = sendQueue.poll()) != null) {
                client.getOutputStream().write(bytes);
            }
        }
    }

    public void send(byte[] bytes) {
        sendQueue.add(bytes);
    }
}
