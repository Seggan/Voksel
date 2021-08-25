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

package io.github.seggan.blockyworld.entity;

import com.badlogic.gdx.math.Vector2;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.github.seggan.blockyworld.util.Location;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.SerialUtil;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.util.UUID;

@Getter
public abstract class Entity {

    private final Location position;
    private final Vector2 direction;
    private final UUID uuid;

    protected Entity(@NonNull Location position, @NonNull Vector2 direction, @NonNull UUID uuid) {
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
        SerialUtil.packVector(packer, direction);
        SerialUtil.packUUID(packer, uuid);
    }

    protected static Triple<Location, Vector2, UUID> beginUnpack(@NonNull MessageUnpacker unpacker) throws IOException {
        return new ImmutableTriple<>(Location.unpack(unpacker), SerialUtil.unpackVector(unpacker), SerialUtil.unpackUUID(unpacker));
    }

}
