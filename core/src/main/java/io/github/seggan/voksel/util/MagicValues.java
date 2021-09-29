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

package io.github.seggan.voksel.util;

import com.badlogic.gdx.math.Vector2;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class MagicValues {

    public static final int WORLD_SCREEN_RATIO = 32;

    public static final int CHUNK_HEIGHT = 256;
    public static final int CHUNK_WIDTH = 16;

    public static final int SEA_LEVEL = 30;

    public static final Vector2 GRAVITY = new Vector2(0, -8);

    public static final int PORT = 16255;
}
