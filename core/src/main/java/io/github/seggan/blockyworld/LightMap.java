package io.github.seggan.blockyworld;

import io.github.seggan.blockyworld.util.MagicValues;
import io.github.seggan.blockyworld.world.chunk.Chunk;

import lombok.NonNull;

public class LightMap {

    private final int[][] map = new int[MagicValues.CHUNK_WIDTH][MagicValues.CHUNK_HEIGHT];
    private final Chunk chunk;

    public LightMap(@NonNull Chunk chunk) {
        this.chunk = chunk;
    }
}
