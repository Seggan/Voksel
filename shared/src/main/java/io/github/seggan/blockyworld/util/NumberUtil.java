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

import io.github.seggan.blockyworld.world.block.Block;

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

    public static boolean intersectSegments(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (d == 0) return false;

        double yd = y1 - y3;
        double xd = x1 - x3;
        double ua = ((x4 - x3) * yd - (y4 - y3) * xd) / d;
        if (ua < 0 || ua > 1) return false;

        double ub = ((x2 - x1) * yd - (y2 - y1) * xd) / d;
        return !(ub < 0) && !(ub > 1);
    }

    public static boolean vectorIntersectBlock(@NonNull Vector pos, @NonNull Vector vector, @NonNull Block block) {
        double bX = block.position().x();
        double bY = block.position().y();
        double endX = bX + 1;
        double endY = bY + 1;
        double x = pos.x();
        double y = pos.y();

        double vectorX = vector.x();
        double vectorY = vector.y();

        if (intersectSegments(x, y, vectorX, vectorY, bX, bY, endX, bY)) return true;
        if (intersectSegments(x, y, vectorX, vectorY, bX, bY, bX, endY)) return true;
        if (intersectSegments(x, y, vectorX, vectorY, endX, bY, endX, endY)) return true;
        if (intersectSegments(x, y, vectorX, vectorY, bX, endY, endX, endY)) return true;

        return bX <= x && endX >= x && bY <= y && endY >= y;
    }

}
