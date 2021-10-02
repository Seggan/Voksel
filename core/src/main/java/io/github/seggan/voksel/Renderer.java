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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Frustum;
import io.github.seggan.voksel.screens.MainScreen;
import io.github.seggan.voksel.util.MagicValues;
import io.github.seggan.voksel.util.Position;
import io.github.seggan.voksel.util.TextureUtils;
import io.github.seggan.voksel.world.block.Block;
import io.github.seggan.voksel.world.block.Material;
import io.github.seggan.voksel.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import lombok.NonNull;

public final class Renderer {

    private static final TextureAtlas atlas = new TextureAtlas();

    private final MainScreen screen;
    private final SpriteBatch batch;

    public Renderer(@NotNull MainScreen screen) {
        this.screen = screen;
        this.batch = screen.batch();
    }

    public static void init() {
        int x = 0;
        for (Material material : Material.values()) {
            Texture texture = TextureUtils.load("blocks/" + material.defaultTexture() + ".png");
            atlas.addRegion(material.name(), texture, x, x / 10, MagicValues.WORLD_SCREEN_RATIO, MagicValues.WORLD_SCREEN_RATIO);
            x++;
        }
    }

    public void render(@NonNull Block block, int offset) {
        Material material = block.material();
        if (material == Material.AIR) return;

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
            batch.draw(atlas.findRegion(material.name()), x, y);
        }
    }

    public void render(@NonNull Chunk chunk) {
        int offset = chunk.position() * MagicValues.CHUNK_WIDTH * MagicValues.WORLD_SCREEN_RATIO;
        for (Block b : chunk.blocks()) {
            render(b, offset);
        }
    }

    public static void dispose() {
        atlas.dispose();
    }
}
