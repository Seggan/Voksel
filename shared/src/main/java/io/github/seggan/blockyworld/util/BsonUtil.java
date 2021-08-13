package io.github.seggan.blockyworld.util;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonReader;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;

@UtilityClass
public final class BsonUtil {

    public static byte[] serialize(@NonNull BsonDocument document) {
        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(buffer);
        new BsonDocumentCodec().encode(writer, document, EncoderContext.builder().isEncodingCollectibleDocument(true).build());
        return buffer.toByteArray();
    }

    public static BsonDocument deserialize(byte[] bytes) {
        BsonReader reader = new BsonBinaryReader(ByteBuffer.wrap(bytes));
        return new BsonDocumentCodec().decode(reader, DecoderContext.builder().build());
    }
}
