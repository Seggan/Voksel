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
import io.github.seggan.blockyworld.util.Location;
import org.apache.commons.lang3.tuple.Triple;
import org.msgpack.core.MessageUnpacker;

import lombok.NonNull;

import java.io.IOException;
import java.util.UUID;

public final class Player extends Entity {

    public Player() {
        this(new Location(null,0, 0), Vector2.Zero, UUID.randomUUID());
    }

    private Player(@NonNull Location location, @NonNull Vector2 direction, @NonNull UUID uuid) {
        super(location, direction, uuid);
    }

    public static Player unpack(@NonNull MessageUnpacker unpacker) throws IOException {
        Triple<Location, Vector2, UUID> dat = beginUnpack(unpacker);
        return new Player(dat.getLeft(), dat.getMiddle(), dat.getRight());
    }
}
