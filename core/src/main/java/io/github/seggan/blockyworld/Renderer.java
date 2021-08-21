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

package io.github.seggan.blockyworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Rectangle;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.block.Block;
import io.github.seggan.blockyworld.world.block.Material;
import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.EnumMap;
import java.util.Map;

@Getter
public final class Renderer {

    @Getter(AccessLevel.NONE)
    private final Map<Material, Texture> cache = new EnumMap<>(Material.class);

    private final MainScreen screen;

    public Renderer(@NotNull MainScreen screen) {
        this.screen = screen;
    }

    public void render(@NonNull Block block, int offset) {
        Rectangle rectangle = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Material material = block.material();
        if (material == Material.AIR) return;
        if (!cache.containsKey(material)) {
            Pixmap orig = new Pixmap(Gdx.files.internal("blocks/" + material.defaultTexture() + ".png"));
            Pixmap newPix = new Pixmap(
                MagicNumbers.WORLD_SCREEN_RATIO,
                MagicNumbers.WORLD_SCREEN_RATIO,
                orig.getFormat()
            );
            newPix.drawPixmap(orig, 0, 0, orig.getWidth(), orig.getHeight(), 0, 0, newPix.getWidth(), newPix.getHeight());

            cache.put(material, new Texture(newPix));

            orig.dispose();
            newPix.dispose();
        }

        Position pos = screen.worldToScreen(block.position());
        int x = pos.x() + offset;
        int y = pos.y();
        Frustum frustum = screen.viewport().getCamera().frustum;
        if (frustum.pointInFrustum(x, y, 0) ||
            frustum.pointInFrustum(
                x + MagicNumbers.WORLD_SCREEN_RATIO,
                y + MagicNumbers.WORLD_SCREEN_RATIO,
                0
            ) || frustum.pointInFrustum(x + MagicNumbers.WORLD_SCREEN_RATIO, y, 0) ||
            frustum.pointInFrustum(x, y + MagicNumbers.WORLD_SCREEN_RATIO, 0)
        ) {
            screen.batch().draw(cache.get(material), x, y);
        }
    }

    public void render(@NonNull Chunk chunk) {
        int offset = chunk.position() * MagicNumbers.CHUNK_WIDTH * MagicNumbers.WORLD_SCREEN_RATIO;
        for (Block b : chunk.blocks()) {
            render(b, offset);
        }
    }

    void dispose() {
        clearCache();
    }

    public void clearCache() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();
    }
}
