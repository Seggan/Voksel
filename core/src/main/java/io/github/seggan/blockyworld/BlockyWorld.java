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

import com.badlogic.gdx.Game;
import io.github.seggan.blockyworld.server.Packet;
import io.github.seggan.blockyworld.server.PacketType;

import lombok.Getter;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class BlockyWorld extends Game {

    @Getter
    private static Connection connection;

    @Override
    public void create() {
        Socket soc;
        try {
            soc = new Socket("localhost", 16255);
            soc.getOutputStream().write(ByteBuffer.allocate(2).putShort(Packet.PROTOCOL_VERSION).array());
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    soc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            short code = ByteBuffer.wrap(soc.getInputStream().readNBytes(2)).getShort();
            if (code == 3) {
                short version = ByteBuffer.wrap(soc.getInputStream().readNBytes(Short.BYTES)).getShort();
                System.err.println("Incompatible protocol version; server " + version + " client " + Packet.PROTOCOL_VERSION);
                System.exit(0);
                throw null;
            } else if (code != PacketType.OK.code()) {
                System.err.println("Invalid packet: " + code);
                System.exit(1);
                throw null;
            }
            soc.getInputStream().skipNBytes(Integer.BYTES);
        } catch (IOException e) {
            System.err.println("Could not connect to server:");
            e.printStackTrace();
            dispose();
            System.exit(1);
            throw null;
        }

        connection = new Connection(soc);

        setScreen(new MainScreen());
    }
}