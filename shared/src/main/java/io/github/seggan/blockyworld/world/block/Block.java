package io.github.seggan.blockyworld.world.block;

import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.util.SerialUtil;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.io.IOException;

@ToString
@EqualsAndHashCode
public class Block {

    @Getter
    private final Position position;
    @Getter
    private final Chunk chunk;

    private Material material;
    private final Object matLock = new Object();

    @Nullable
    private volatile BlockData blockData;
    private final Object bdLock = new Object();

    public Block(@NonNull Material material, @NonNull Position position, @NonNull Chunk chunk, @Nullable BlockData data) {
        Validate.exclusiveBetween(-1, MagicNumbers.CHUNK_WIDTH, position.x());
        Validate.exclusiveBetween(-1, MagicNumbers.CHUNK_HEIGHT, position.y());
        this.material = material;
        this.position = position;
        this.chunk = chunk;
        this.blockData = data;
    }

    public Block(@NonNull Material material, int x, int y, @NonNull Chunk chunk) {
        this(material, x, y, chunk, null);
    }

    public Block(@NonNull Material material, int x, int y, @NonNull Chunk chunk, @Nullable BlockData data) {
        this(material, new Position(x, y), chunk, data);
    }

    @NotNull
    public static Block unpack(@NonNull MessageUnpacker unpacker) throws IOException {
        Position pos = Position.decompressShort(unpacker.unpackShort());
        Material mat = Material.valueOf(unpacker.unpackString());
        World world = World.getByUUID(SerialUtil.unpackUUID(unpacker));
        Chunk chunk = world.getChunk(unpacker.unpackInt());
        BlockData data = null;
        if (!unpacker.tryUnpackNil()) {
            data = BlockData.unpack(unpacker);
        }
        return new Block(mat, pos, chunk, data);
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        packer.packShort(position.compressShort());
        packer.packString(material.name());
        SerialUtil.packUUID(packer, chunk.world().uuid());
        packer.packInt(chunk.position());
        if (blockData == null) {
            packer.packNil();
        } else {
            blockData.pack(packer);
        }
    }

    public Material material() {
        return this.material;
    }

    @Nullable
    public BlockData blockData() {
        synchronized (bdLock) {
            return blockData;
        }
    }

    public Block material(Material material) {
        synchronized (matLock) {
            this.material = material;
        }
        return this;
    }

    public Block blockData(@Nullable BlockData blockData) {
        synchronized (bdLock) {
            this.blockData = blockData;
        }
        return this;
    }
}
