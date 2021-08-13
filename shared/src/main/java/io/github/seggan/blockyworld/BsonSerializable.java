package io.github.seggan.blockyworld;

import org.bson.BsonDocument;

public interface BsonSerializable {

    BsonDocument toBson();
}
