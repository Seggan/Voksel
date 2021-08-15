package io.github.seggan.blockyworld.world;

import com.google.errorprone.annotations.ForOverride;
import io.github.seggan.blockyworld.world.block.Block;
import io.github.seggan.blockyworld.world.block.BlockData;
import io.github.seggan.blockyworld.world.block.Material;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuppressWarnings("ClassCanBeRecord")
public final class ChunkGenerator {

    private final @NonNull Chunk chunk;

    public ChunkGenerator(@NonNull Chunk chunk) {
        this.chunk = chunk;
    }

    protected void setRange(int y, @NonNull Block[][] blocks, @NonNull Material mat, @Nullable BlockData data) {
        for (int x = 0; x < blocks.length; x++) {
            blocks[x][y] = new Block(mat, x, y, chunk, data);
        }
    }

    void generateChunk(@NonNull Block[][] blocks) {
        for (int y = 0; y < 5; y++) {
            setRange(y, blocks, Material.STONE, null);
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
