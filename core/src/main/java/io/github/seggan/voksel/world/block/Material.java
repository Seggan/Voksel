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

package io.github.seggan.voksel.world.block;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum Material {
    AIR("air"),
    STONE("stone"),
    UNKNOWN("unknown"),
    LIGHT("light", true);

    private static final Map<String, Material> valueMap;

    static {
        valueMap = new HashMap<>();
        for (Material material : values()) {
            valueMap.put(material.name(), material);
        }
    }

    private final String defaultTexture;
    private final boolean transparent;

    Material(@NonNull String defaultTexture, boolean transparent) {
        this.defaultTexture = defaultTexture;
        this.transparent = transparent;
    }

    Material(@NonNull String defaultTexture) {
        this(defaultTexture, false);
    }

    @NotNull
    public static Material valueOfOrDefault(@NonNull String name) {
        return valueMap.getOrDefault(name, UNKNOWN);
    }
}
