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

package io.github.seggan.voksel.world.chunk;

import io.github.seggan.voksel.util.MagicValues;
import io.github.seggan.voksel.util.Position;
import io.github.seggan.voksel.util.SerialUtil;
import io.github.seggan.voksel.world.VokselWorld;
import io.github.seggan.voksel.world.block.Block;
import io.github.seggan.voksel.world.block.BlockData;
import io.github.seggan.voksel.world.block.Material;
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

    public static final short CHUNK_VERSION = 0;

    private final Block[][] blocks;
    private final Object bLock = new Object();
    @Getter
    private final int position;
    @Getter
    private final VokselWorld world;

    public Chunk(int position, VokselWorld world, boolean generate) {
        this.blocks = new Block[MagicValues.CHUNK_WIDTH][MagicValues.CHUNK_HEIGHT + 1];
        this.position = position;
        this.world = world;
        if (generate) {
            ChunkGenerator generator = new ChunkGenerator(this);
            generator.generateChunk(this);
        }
    }

    public static Chunk unpack(@NonNull MessageUnpacker unpacker, VokselWorld world) throws IOException {
        short version = unpacker.unpackShort();

        return switch (version) {
            case 0 -> ChunkUnpacker.unpack0(unpacker, world);
            default -> throw new IllegalStateException("Unexpected value: " + version);
        };
    }

    public void setBlock(@NonNull Material material, int x, int y, @Nullable BlockData data) {
        setBlock(material, new Position(x, y), data);
    }

    public void setBlock(@NonNull Material material, @NonNull Position position, @Nullable BlockData data) {
        setBlock(new Block(material, position, this, data));
    }

    public void setBlock(@NonNull Block block) {
        Position position = block.position();
        int x = position.x();
        int y = position.y();
        if (x >= 0 && x < MagicValues.CHUNK_WIDTH && y >= 0 && y <= MagicValues.CHUNK_HEIGHT) {
            synchronized (bLock) {
                blocks[x][y] = block;
            }
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
        synchronized (bLock) {
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
        if (x >= 0 && x < MagicValues.CHUNK_WIDTH && y >= 0 && y <= MagicValues.CHUNK_HEIGHT) {
            synchronized (bLock) {
                Block b = blocks[x][y];
                return b == null ? new Block(Material.AIR, x, y, this, null) : b;
            }
        }

        return new Block(Material.AIR, x, y, this, null);
        /*
        throw new IndexOutOfBoundsException(String.format(
            "Chunk index out of bounds: %d, %d",
            x,
            y
        ));

         */
    }

    @NotNull
    public Block getBlock(@NonNull Position position) {
        return getBlock(position.x(), position.y());
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        packer.packShort(CHUNK_VERSION);

        packer.packInt(position);
        SerialUtil.packUUID(packer, world.uuid());
        for (Block[] arr : blocks) {
            for (Block b : arr) {
                if (b == null || b.material() == Material.AIR) {
                    packer.packNil();
                } else {
                    // Reason I'm doing this instead of Block#pack is because I don't need
                    // to pack duplicate UUIDs/chunk positions, reducing size
                    b.position().pack(packer);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        else if (!(obj instanceof Chunk chunk)) return false;
        else return chunk.position == this.position;
    }

    @Override
    public String toString() {
        return "Chunk(position=" + this.position() + ", world=" + this.world().name() + ")";
    }
}
