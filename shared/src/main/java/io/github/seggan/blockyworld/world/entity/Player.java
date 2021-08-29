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

import io.github.seggan.blockyworld.util.Vector;
import org.apache.commons.lang3.tuple.Triple;
import org.msgpack.core.MessageUnpacker;

import lombok.NonNull;

import java.io.IOException;
import java.util.UUID;

public final class Player extends Entity {

    public Player() {
        this(new Vector(0, 0), new Vector(0, 0), UUID.randomUUID());
    }

    private Player(@NonNull Vector Vector, @NonNull Vector direction, @NonNull UUID uuid) {
        super(Vector, direction, uuid);
    }

    public static Player unpack(@NonNull MessageUnpacker unpacker) throws IOException {
        Triple<Vector, Vector, UUID> dat = beginUnpack(unpacker);
        return new Player(dat.getLeft(), dat.getMiddle(), dat.getRight());
    }
}
