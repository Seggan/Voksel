package io.github.seggan.blockyworld.util;

import lombok.NonNull;

public record Position(int x, int y) {

    public static Position decompress(long compressed) {
        return new Position((int) (compressed >> 32), (int) compressed);
    }

    public static Position decompressShort(int compressed) {
        return new Position((short) (compressed >> 16), (short) compressed);
    }

    public long compress() {
        return (long) x << 32 | y & 0xFFFFFFFFL;
    }

    public int compressShort() {
        return (x << 16) | (y & 0xFFFF);
    }

    public int distanceSquared(@NonNull Position other) {
        return (other.y - y) * (other.y - y) + (other.x - x) * (other.x - x);
    }

    public double distanceTo(@NonNull Position other) {
        return Math.sqrt(distanceSquared(other));
    }
}
