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

package io.github.seggan.blockyworld.util;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.NonNull;

import java.io.IOException;

public record Position(int x, int y) {

    public static Position unpack(MessageUnpacker unpacker) throws IOException {
        return new Position(unpacker.unpackInt(), unpacker.unpackInt());
    }

    public void pack(MessageBufferPacker packer) throws IOException {
        packer.packInt(x);
        packer.packInt(y);
    }

    public long compress() {
        return (long) x << 32 | y & 0xFFFFFFFFL;
    }

    public int distanceSquared(@NonNull Position other) {
        return (other.y - y) * (other.y - y) + (other.x - x) * (other.x - x);
    }

    public double distanceTo(@NonNull Position other) {
        return Math.sqrt(distanceSquared(other));
    }
}
