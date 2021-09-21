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

package io.github.seggan.blockyworld.world.entity;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.SerialUtil;
import io.github.seggan.blockyworld.util.Vector;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;
import java.util.UUID;

@Getter
public abstract class Entity {

    private final Vector position;
    private final Vector direction;
    private final UUID uuid;

    @Setter
    private boolean gravity = true;

    protected Entity(@NonNull Vector position, @NonNull Vector direction, @NonNull UUID uuid) {
        this.position = position;
        this.direction = direction;
        this.uuid = uuid;
    }

    public void applyGravity() {
        direction.add(MagicNumbers.GRAVITY);
    }

    @OverridingMethodsMustInvokeSuper
    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        position.pack(packer);
        direction.pack(packer);
        SerialUtil.packUUID(packer, uuid);
    }

    public void position(@NonNull Vector location) {
        position(location.x(), location.y());
    }

    public void position(double x, double y) {
        position.set(x, y);
    }

    protected static Triple<Vector, Vector, UUID> beginUnpack(@NonNull MessageUnpacker unpacker) throws IOException {
        return new ImmutableTriple<>(Vector.unpack(unpacker), Vector.unpack(unpacker), SerialUtil.unpackUUID(unpacker));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Entity other)) return false;
        return this.uuid.equals(other.uuid);
    }
}
