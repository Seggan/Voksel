package io.github.seggan.blockyworld.util;

import lombok.NonNull;

public record Position(int x, int y) {

    public static Position decompress(long compressed) {
        return new Position((int) (compressed >> 32), (int) compressed);
    }

    public static Position decompressShort(short compressed) {
        return new Position((byte) (compressed >> 8), (byte) compressed);
    }

    public long compress() {
        return (long) x << 32 | y & 0xFFFFFFFFL;
    }

    public short compressShort() {
        return (short) ((x << 8) | (y & 0xFF));
    }

    public int distanceSquared(@NonNull Position other) {
        return (other.y - y) * (other.y - y) + (other.x - x) * (other.x - x);
    }

    public double distanceTo(@NonNull Position other) {
        return Math.sqrt(distanceSquared(other));
    }
}
