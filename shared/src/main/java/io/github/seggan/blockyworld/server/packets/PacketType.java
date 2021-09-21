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
import io.github.seggan.blockyworld.util.Vector;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import io.github.seggan.blockyworld.world.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.msgpack.core.MessageUnpacker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.net.InetAddress;

@Getter
@AllArgsConstructor
public enum PacketType {
    REQUEST_CHUNK(0x01, false) {
        @NotNull
        @Override
        public Packet unpack(@NonNull MessageUnpacker unpacker, boolean server, @NonNull InetAddress address) throws IOException {
            if (server) {
                return new ChunkPacket(Chunk.unpack(unpacker), address);
            } else {
                return new ChunkPacket(
                    unpacker.unpackInt(),
                    World.byUUID(SerialUtil.unpackUUID(unpacker)),
                    address
                );
            }
        }
    },
    REQUEST_WORLD(0x02, false) {
        @NotNull
        @Override
        public Packet unpack(@NonNull MessageUnpacker unpacker, boolean server, @NonNull InetAddress address) throws IOException {
            if (server) {
                return new WorldPacket(World.unpack(unpacker), address);
            } else {
                return new WorldPacket(address);
            }
        }
    },
    /*
     *  0x03 is reserved for the invalid protocol packet
     */
    OK(0x04, true) {
        @Override
        public @NotNull Packet unpack(@NonNull MessageUnpacker unpacker, boolean server, @NonNull InetAddress address) throws IOException {
            return new OKPacket(address);
        }
    },
    PLAYER_CONNECT(0x05, true) {
        @NotNull
        @Override
        public Packet unpack(@NonNull MessageUnpacker unpacker, boolean server, @NonNull InetAddress address) throws IOException {
            return new PlayerPacket(Player.unpack(unpacker), address);
        }
    },
    ENTITY_MOVE(0x06, false) {
        @NotNull
        @Override
        public Packet unpack(@NonNull MessageUnpacker unpacker, boolean server, @NonNull InetAddress address) throws IOException {
            return new EntityMovePacket(SerialUtil.unpackUUID(unpacker), Vector.unpack(unpacker), address);
        }
    },
    USER_MOVE(0x07, true) {
        @NotNull
        @Override
        public Packet unpack(@NonNull MessageUnpacker unpacker, boolean server, @NonNull InetAddress address) throws IOException {
            return new UserMovePacket(Vector.unpack(unpacker), SerialUtil.unpackUUID(unpacker), address);
        }
    };

    private final short code;
    private final boolean allowOk;

    PacketType(int code, boolean allowOk) {
        this((short) code, allowOk);
    }

    /**
     * Unpacks the request
     * @param unpacker the {@link MessageUnpacker} with the data in it
     * @param server if the creator is the server
     * @param address the address of the creator
     * @return the unpacked request
     */
    @NotNull
    public abstract Packet unpack(@NonNull MessageUnpacker unpacker, boolean server, @NonNull InetAddress address) throws IOException;

    @NotNull
    public static PacketType getByCode(short code) {
        for (PacketType type : values()) {
            if (code == type.code) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
