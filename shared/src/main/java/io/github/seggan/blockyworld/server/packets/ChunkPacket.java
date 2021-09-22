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

import io.github.seggan.blockyworld.util.SerialUtil;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import org.jetbrains.annotations.NotNull;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;

@Getter
public final class ChunkPacket extends Packet {

    private final Chunk chunk;
    private final int position;
    private final World world;

    public ChunkPacket(@NonNull Chunk chunk) {
        super(PacketType.REQUEST_CHUNK, true);
        this.chunk = chunk;
        this.position = 0;
        this.world = null;
    }

    public ChunkPacket(int position, @NonNull World world) {
        super(PacketType.REQUEST_CHUNK, false);
        this.position = position;
        this.world = world;
        this.chunk = null;
    }

    @Override
    protected void pack(@NonNull MessageBufferPacker packer) throws IOException {
        if (server()) {
            chunk.pack(packer);
        } else {
            packer.packInt(position);
            SerialUtil.packUUID(packer, world.uuid());
        }
    }

    public static final class Deserializer implements PacketDeserializer {

        @Override
        public @NotNull Packet deserialize(@NonNull MessageUnpacker unpacker, boolean fromServer) throws IOException {
            if (fromServer) {
                return new ChunkPacket(Chunk.unpack(unpacker));
            } else {
                return new ChunkPacket(
                    unpacker.unpackInt(),
                    World.byUUID(SerialUtil.unpackUUID(unpacker))
                );
            }
        }
    }
}
