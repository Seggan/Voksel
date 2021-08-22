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

import com.badlogic.gdx.math.Vector2;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.UUID;

@UtilityClass
public final class SerialUtil {

    public static void packUUID(@NonNull MessageBufferPacker packer, @NonNull UUID uuid) throws IOException {
        packer.packLong(uuid.getMostSignificantBits());
        packer.packLong(uuid.getLeastSignificantBits());
    }

    public static UUID unpackUUID(@NonNull MessageUnpacker unpacker) throws IOException {
        return new UUID(unpacker.unpackLong(), unpacker.unpackLong());
    }

    public static void packVector(@NonNull MessageBufferPacker packer, @NonNull Vector2 vector) throws IOException {
        packer.packFloat(vector.x);
        packer.packFloat(vector.y);
    }

    public static Vector2 unpackVector(@NonNull MessageUnpacker unpacker) throws IOException {
        return new Vector2(unpacker.unpackFloat(), unpacker.unpackFloat());
    }

    public static void packPoint(@NonNull MessageBufferPacker packer, @NonNull Point2D point) throws IOException {
        packer.packDouble(point.getX());
        packer.packDouble(point.getY());
    }

    public static Point2D unpackPoint(@NonNull MessageUnpacker unpacker) throws IOException {
        return new Point2D.Double(unpacker.unpackDouble(), unpacker.unpackDouble());
    }
}
