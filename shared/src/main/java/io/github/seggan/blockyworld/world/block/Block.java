package io.github.seggan.blockyworld.world.block;

import io.github.seggan.blockyworld.BsonSerializable;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonNull;
import org.bson.BsonString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;

@Getter(onMethod_ = @Synchronized)
@Setter(onMethod_ = @Synchronized)
public class Block implements BsonSerializable {

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
    public static Block fromBson(@NonNull BsonDocument document) {
        Position pos = Position.decompressShort(document.getInt32("p").getValue());
        Material material = Material.valueOf(document.getString("m").getValue());
        World world = World.getByUUID(document.getBinary("w").asUuid());
        Chunk chunk = new Chunk(document.getInt32("c").getValue(), world);
        return new Block(material, pos, chunk, BlockData.fromBson(document.get("d")));
    }

    @Override
    public BsonDocument toBson() {
        BsonDocument object = new BsonDocument();
        object.put("p", new BsonInt32(position.compressShort())); // position
        object.put("m", new BsonString(material.name())); // material
        object.put("c", new BsonInt32(chunk.position())); // chunk
        object.put("w", new BsonBinary(chunk.world().uuid())); // world
        if (blockData == null) {
            object.put("d", new BsonNull()); // data
        } else {
            object.put("d", blockData.toBson()); // data
        }
        return object;
    }
}
