package io.github.seggan.blockyworld.world;

import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.util.SerialUtil;
import io.github.seggan.blockyworld.world.block.Block;
import io.github.seggan.blockyworld.world.block.BlockData;
import io.github.seggan.blockyworld.world.block.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class Chunk {

    private final Block[][] blocks;
    @Getter
    private final int position;
    @Getter
    private final World world;

    public Chunk(int position, World world) {
        this.blocks = new Block[MagicNumbers.CHUNK_WIDTH][MagicNumbers.CHUNK_HEIGHT];
        this.position = position;
        this.world = world;
        ChunkGenerator generator = new ChunkGenerator(this);
        generator.generateChunk(this.blocks);
    }

    public void setBlock(@NonNull Material material, int x, int y, @Nullable BlockData data) {
        setBlock(material, new Position(x, y), data);
    }

    public void setBlock(@NonNull Material material, @NonNull Position position, @Nullable BlockData data) {
        setBlock(new Block(material, position, this, data));
    }

    public void setBlock(@NonNull Block block) {
        Position position = block.position();
        synchronized (blocks) {
            blocks[position.x()][position.y()] = block;
        }
    }

    /**
     * Returns a set of the blocks in the chunk
     *
     * @return a set of blocks in the chunk. Air blocks will <b>not</b> be included unless
     * explicitly set
     */
    public Set<Block> blocks() {
        Set<Block> blockSet = new HashSet<>();
        synchronized (blocks) {
            for (Block[] arr : blocks) {
                for (Block b : arr) {
                    if (b == null) continue;
                    blockSet.add(b);
                }
            }
        }

        return blockSet;
    }

    @NotNull
    public Block getBlock(int x, int y) {
        if (x >= 0 && x < MagicNumbers.CHUNK_WIDTH && y >= 0 && y < MagicNumbers.CHUNK_HEIGHT) {
            synchronized (blocks) {
                Block b = blocks[x][y];
                return b == null ? new Block(Material.AIR, x, y, this, null) : b;
            }
        }

        throw new IndexOutOfBoundsException(String.format(
            "Chunk index out of bounds: %d, %d",
            x,
            y
        ));
    }

    @NotNull
    public Block getBlock(@NonNull Position position) {
        return getBlock(position.x(), position.y());
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        packer.packInt(position);
        SerialUtil.packUUID(packer, world.uuid());
        for (Block[] arr : blocks) {
            for (Block b : arr) {
                if (b == null || b.material() == Material.AIR) {
                    packer.packNil();
                } else {
                    // Reason I'm doing this instead of Block#pack is because I don't need
                    // to pack duplicate UUIDs/chunk positions, reducing size
                    packer.packShort(b.position().compressShort());
                    packer.packString(b.material().name());
                    BlockData data = b.blockData();
                    if (data == null) {
                        packer.packNil();
                    } else {
                        data.pack(packer);
                    }
                }
            }
        }
    }

    public static Chunk unpack(@NonNull MessageUnpacker unpacker) throws IOException {
        int cPos = unpacker.unpackInt();
        World world = World.getByUUID(SerialUtil.unpackUUID(unpacker));
        Chunk chunk = new Chunk(cPos, world);

        for (int x = 0; x < MagicNumbers.CHUNK_WIDTH; x++) {
            for (int y = 0; y < MagicNumbers.CHUNK_HEIGHT; y++) {
                if (!unpacker.tryUnpackNil()) {
                    Position pos = Position.decompressShort(unpacker.unpackShort());
                    Material material = Material.valueOf(unpacker.unpackString());
                    BlockData data = null;
                    if (!unpacker.tryUnpackNil()) {
                        data = BlockData.unpack(unpacker);
                    }
                    chunk.setBlock(material, pos, data);
                }
            }
        }

        return chunk;
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
