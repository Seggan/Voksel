package io.github.seggan.blockyworld.world;

import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.world.block.Block;
import io.github.seggan.blockyworld.world.block.BlockData;
import io.github.seggan.blockyworld.world.block.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessageBufferPacker;

import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;

import java.io.IOException;

@Getter(onMethod_ = @Synchronized)
public final class Chunk {

    private final Block[][] blocks = new Block[MagicNumbers.CHUNK_WIDTH][MagicNumbers.CHUNK_HEIGHT];
    private final int position;
    private final World world;

    public Chunk(int position, World world) {
        this.position = position;
        this.world = world;
    }

    public synchronized void setBlock(@NonNull Material material, int x, int y, @Nullable BlockData data) {
        if (x >= 0 && x < MagicNumbers.CHUNK_WIDTH && y >= 0 && y < MagicNumbers.CHUNK_HEIGHT) {
            blocks[x][y] = new Block(material, x, y, this, data);
        }

        throw new IndexOutOfBoundsException(String.format(
            "Chunk index out of bounds: %d, %d",
            x,
            y
        ));
    }

    @NotNull
    public synchronized Block getBlock(int x, int y) {
        if (x >= 0 && x < MagicNumbers.CHUNK_WIDTH && y >= 0 && y < MagicNumbers.CHUNK_HEIGHT) {
            Block b = blocks[x][y];
            return b == null ? new Block(Material.AIR, x, y, this, null) : b;
        }

        throw new IndexOutOfBoundsException(String.format(
            "Chunk index out of bounds: %d, %d",
            x,
            y
        ));
    }

    public synchronized void setBlock(@NonNull Material material, @NonNull Position position, @Nullable BlockData data) {
        setBlock(material, position.x(), position.y(), data);
    }

    @NotNull
    public synchronized Block getBlock(@NonNull Position position) {
        return getBlock(position.x(), position.y());
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        packer.packInt(position);
        packer.packString(world.uuid().toString());
        for (Block[] arr : blocks) {
            for (Block b : arr) {
                if (b == null) {
                    packer.packNil();
                } else {
                    b.pack(packer);
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        else if (!(obj instanceof Chunk chunk)) return false;
        else return chunk.position == this.position;
    }

    @Override
    public String toString() {
        return "Chunk(position=" + this.position() + ", world=" + this.world() + ")";
    }
}
