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
import io.github.seggan.blockyworld.world.block.Block;
import io.github.seggan.blockyworld.world.entity.Player;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.NonNull;

@Getter
public enum PacketType {
    REQUEST_CHUNK(0x01, false, new ChunkPacket.Deserializer()),

    REQUEST_WORLD(0x02, false, new WorldPacket.Deserializer()),

    /*
     *  0x03 is reserved for the invalid protocol packet
     */

    OK(0x04, true, (unpacker, fromServer) -> new OKPacket()),

    PLAYER_CONNECT(0x05, true, (unpacker, fromServer) -> new PlayerPacket(Player.unpack(unpacker))),

    ENTITY_MOVE(0x06, false, (unpacker, fromServer) -> new EntityMovePacket(SerialUtil.unpackUUID(unpacker), Vector.unpack(unpacker))),

    USER_MOVE(0x07, true, (unpacker, fromServer) -> new UserMovePacket(Vector.unpack(unpacker), SerialUtil.unpackUUID(unpacker))),

    BLOCK_BREAK(0x08, false, (unpacker, fromServer) -> new BlockUpdatePacket(Block.unpack(unpacker)));

    private final short code;
    private final boolean allowOk;
    private final PacketDeserializer deserializer;

    PacketType(int code, boolean allowOk, @NonNull PacketDeserializer deserializer) {
        this.code = (short) code;
        this.allowOk = allowOk;
        this.deserializer = deserializer;
    }

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
