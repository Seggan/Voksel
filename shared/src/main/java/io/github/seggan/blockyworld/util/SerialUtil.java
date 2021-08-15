package io.github.seggan.blockyworld.util;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.UUID;

@UtilityClass
public final class SerialUtil {

    public static void packUUID(@NonNull MessageBufferPacker packer, @NonNull UUID uuid) throws IOException {
        packer.packLong(uuid.getMostSignificantBits());
        packer.packLong(uuid.getLeastSignificantBits());
    }

    public static UUID unpackUUID(@NonNull MessageUnpacker unpacker) throws IOException {
        return new UUID(unpacker.unpackLong(), unpacker.unpackLong());
    }
}
