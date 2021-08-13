package io.github.seggan.blockyworld.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class NumberUtil {

    public static Position chunkToWorld(int chunkPos, @NonNull Position position) {
        return new Position(chunkPos * MagicNumbers.CHUNK_WIDTH + position.x(), position.y());
    }

    public static Position worldToInChunk(@NonNull Position position) {
        return new Position(position.x() % MagicNumbers.CHUNK_WIDTH, position.y());
    }

    public static int worldToChunk(int pos) {
        return pos / MagicNumbers.CHUNK_WIDTH;
    }
}
