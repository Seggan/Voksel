package io.github.seggan.blockyworld.world.block;

import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import lombok.ToString;

import java.io.IOException;
import java.util.UUID;

@ToString
@EqualsAndHashCode
@Getter(onMethod_ = @Synchronized)
@Setter(onMethod_ = @Synchronized)
public class Block {

    private final Position position;
    private final Chunk chunk;

    private Material material;

    @Nullable
    private BlockData blockData;

    public Block(@NonNull Material material, @NonNull Position position, @NonNull Chunk chunk, @Nullable BlockData data) {
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
        World world = World.getByUUID(new UUID(unpacker.unpackLong(), unpacker.unpackLong()));
        Chunk chunk = new Chunk(unpacker.unpackInt(), world);
        BlockData data = null;
        if (!unpacker.tryUnpackNil()) {
            data = BlockData.unpack(unpacker);
        }
        return new Block(mat, pos, chunk, data);
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        packer.packShort(position.compressShort());
        packer.packString(material.name());
        UUID uuid = chunk.world().uuid();
        packer.packLong(uuid.getMostSignificantBits());
        packer.packLong(uuid.getLeastSignificantBits());
        packer.packInt(chunk.position());
        if (blockData == null) {
            packer.packNil();
        } else {
            blockData.pack(packer);
        }
    }
}
