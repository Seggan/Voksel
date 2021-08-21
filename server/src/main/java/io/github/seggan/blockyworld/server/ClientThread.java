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

import lombok.Getter;
import lombok.NonNull;

import java.net.Socket;

abstract class ClientThread extends Thread {

    @Getter
    protected final Socket client;
    protected boolean stop;

    protected ClientThread(@NonNull Socket client, @NonNull String name) {
        super(name);
        this.client = client;
        setDaemon(true);
    }

    @Override
    public void run() {
        super.run();
    }

    public void stopThread() {
        stop = true;
    }
}
