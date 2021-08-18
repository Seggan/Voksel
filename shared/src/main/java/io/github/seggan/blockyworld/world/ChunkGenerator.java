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
        noise.SetFractalOctaves(8);
        noise.SetFrequency(0.01F);
        noise2.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
        noise2.SetFractalOctaves(8);
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
            int height = (int) (40 + 20 * (n + n2));
            for (int y = 0; y < height; y++) {
                chunk.setBlock(Material.STONE, x, y, null);
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
