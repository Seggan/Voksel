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

package io.github.seggan.blockyworld.world;

import com.google.errorprone.annotations.ForOverride;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.world.block.Block;
import io.github.seggan.blockyworld.world.block.BlockData;
import io.github.seggan.blockyworld.world.block.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class ChunkGenerator {

    private final FastNoiseLite noise = new FastNoiseLite();
    private final FastNoiseLite noise2 = new FastNoiseLite();

    private final @NotNull Chunk chunk;

    public ChunkGenerator(@NonNull Chunk chunk) {
        this.chunk = chunk;
        noise.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
        noise.SetFractalOctaves(16);
        noise.SetFrequency(0.01F);
        noise2.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
        noise2.SetFractalOctaves(32);
        noise2.SetFrequency(0.01F);
    }

    protected void setRange(int y, @NonNull Block[][] blocks, @NonNull Material mat, @Nullable BlockData data) {
        for (int x = 0; x < blocks.length; x++) {
            blocks[x][y] = new Block(mat, x, y, chunk, data);
        }
    }

    protected void generateChunk(@NonNull Chunk chunk) {
        for (int x = 0; x < MagicNumbers.CHUNK_WIDTH; x++) {
            float n = noise.GetNoise(x + chunk().position() * MagicNumbers.CHUNK_WIDTH, 0);
            float n2 = noise2.GetNoise(x + chunk.position() * MagicNumbers.CHUNK_WIDTH, 0);
            int height = Math.min((int) (50 + 50 + 20 * (n + n2)), 255);
            for (int y = 0; y < height; y++) {
                chunk.block(Material.STONE, x, y, null);
            }
        }
    }

    void populate(@NonNull Block[][] blocks) {
        for (Populator populator : getPopulators()) {
            populator.populate(blocks);
        }
    }

    @ForOverride
    protected List<Populator> getPopulators() {
        return new ArrayList<>();
    }

    protected abstract class Populator {

        protected abstract void populate(@NonNull Block[][] blocks);
    }
}
