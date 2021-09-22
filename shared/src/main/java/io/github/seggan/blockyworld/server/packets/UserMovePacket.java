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
import org.msgpack.core.MessageBufferPacker;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.util.UUID;

@Getter
public final class UserMovePacket extends Packet {

    private final Vector vector;
    private final UUID uuid;

    /**
     */
    public UserMovePacket(@NonNull Vector vector, @NonNull UUID uuid) {
        super(PacketType.USER_MOVE, false);
        this.vector = vector;
        this.uuid = uuid;
    }

    @Override
    protected void pack(@NonNull MessageBufferPacker packer) throws IOException {
        vector.pack(packer);
        SerialUtil.packUUID(packer, uuid);
    }
}
