package io.github.seggan.blockyworld.world.block;

import io.github.seggan.blockyworld.BsonSerializable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import lombok.NonNull;

public class BlockData implements BsonSerializable {

    @Override
    public BsonDocument toBson() {
        return null;
    }

    public static BlockData fromBson(@NonNull BsonValue value) {
        if (!(value instanceof BsonDocument document)) return null;
        return new BlockData();
    }
}
