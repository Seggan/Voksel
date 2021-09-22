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

package io.github.seggan.blockyworld.server.packets;

import io.github.seggan.blockyworld.world.World;
import org.jetbrains.annotations.NotNull;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;

@Getter
public final class WorldPacket extends Packet {

    private final World world;

    /**
     * @param world a world
     */
    public WorldPacket(@NonNull World world) {
        super(PacketType.REQUEST_WORLD, true);
        this.world = world;
    }

    public WorldPacket() {
        super(PacketType.REQUEST_WORLD, false);
        this.world = null;
    }

    @Override
    protected void pack(@NonNull MessageBufferPacker packer) throws IOException {
        if (server()) {
            world.pack(packer);
        }
    }

    public static final class Deserializer implements PacketDeserializer {

        @Override
        public @NotNull Packet deserialize(@NonNull MessageUnpacker unpacker, boolean fromServer) throws IOException {
            if (fromServer) {
                return new WorldPacket(World.unpack(unpacker));
            } else {
                return new WorldPacket();
            }
        }
    }
}
