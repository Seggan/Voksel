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

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class NumberUtil {

    public static final double EPSILON = 1e-6;

    public static Position chunkToWorld(int chunkPos, @NonNull Position position) {
        return new Position(chunkPos * MagicNumbers.CHUNK_WIDTH + position.x(), position.y());
    }

    public static int worldToInChunk(int x) {
        return x % MagicNumbers.CHUNK_WIDTH;
    }

    public static int worldToChunk(int pos) {
        return pos / MagicNumbers.CHUNK_WIDTH;
    }

    public static boolean isDoubleZero(double d) {
        return d >= -EPSILON && d <= EPSILON;
    }
}
