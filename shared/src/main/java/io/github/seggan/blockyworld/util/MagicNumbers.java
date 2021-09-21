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

import lombok.experimental.UtilityClass;

@UtilityClass
public final class MagicNumbers {

    public static final int WORLD_SCREEN_RATIO = 16;

    public static final int CHUNK_HEIGHT = 256;
    public static final int CHUNK_WIDTH = 16;

    public static final Vector GRAVITY = new Vector(0, -3);

    public static final int PORT = 16255;
}
