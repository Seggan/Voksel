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

package io.github.seggan.voksel;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import io.github.seggan.voksel.util.MagicValues;
import io.github.seggan.voksel.util.Position;
import io.github.seggan.voksel.world.block.Block;
import io.github.seggan.voksel.world.block.Material;
import io.github.seggan.voksel.world.chunk.Chunk;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import lombok.NonNull;

import java.util.EnumMap;
import java.util.Map;

public final class Renderer {

    private final Map<Material, Texture> cache = new EnumMap<>(Material.class);
    private final Object2IntMap<Position> lighting = new Object2IntOpenHashMap<>();

    private final MainScreen screen;
    private final SpriteBatch batch;

    public Renderer(@NotNull MainScreen screen) {
        this.screen = screen;
        this.batch = screen.batch();
    }

    public void render(@NonNull Block block, int offset) {
        Material material = block.material();
        if (material == Material.AIR) return;

        if (!cache.containsKey(material)) {
            cache.put(material, TextureUtils.load("blocks/" + material.defaultTexture() + ".png"));
        }

        Position pos = screen.worldToScreen(block.position());
        int x = pos.x() + offset;
        int y = pos.y();
        Frustum frustum = screen.viewport().getCamera().frustum;
        if (frustum.pointInFrustum(x, y, 0) ||
            frustum.pointInFrustum(
                x + MagicValues.WORLD_SCREEN_RATIO,
                y + MagicValues.WORLD_SCREEN_RATIO,
                0
            ) || frustum.pointInFrustum(x + MagicValues.WORLD_SCREEN_RATIO, y, 0) ||
            frustum.pointInFrustum(x, y + MagicValues.WORLD_SCREEN_RATIO, 0)
        ) {
            batch.draw(cache.get(material), x, y);
        }
    }

    public void render(@NonNull Chunk chunk) {
        recalculateLighting(chunk);
        int offset = chunk.position() * MagicValues.CHUNK_WIDTH * MagicValues.WORLD_SCREEN_RATIO;
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

    private void recalculateLighting(Chunk chunk) {
        /*
        lighting.clear();

        for (int x = 0; x < MagicValues.CHUNK_WIDTH; x++) {
            for (int y = 0; y < MagicValues.CHUNK_HEIGHT; y++) {
                Block b = chunk.getBlock(x, y);
                Position position = b.position();
                if (b.material() == Material.AIR) {
                    putLighting(position, 3);
                    for (BlockSide side : BlockSide.values()) {
                        Block b1 = b.relativeToThis(side);
                        for (BlockSide side1 : BlockSide.values()) {
                            Block b2 = b.relativeToThis(side1);
                            for (BlockSide side2 : BlockSide.values()) {
                                Block b3 = b.relativeToThis(side2);
                                putLighting(b3.position(), 1);
                            }
                            putLighting(b2.position(), 2);
                        }
                        putLighting(b1.position(), 3);
                    }
                }
            }
        }
         */
    }

    private void putLighting(Position position, int level) {
        lighting.computeInt(position, (k, v) -> {
            if (v == null) return level;
            else return Math.min(level + v, 3);
        });
    }
}
