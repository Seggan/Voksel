package io.github.seggan.blockyworld.world.block;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.NonNull;

import java.io.IOException;

public class BlockData {

    public static BlockData unpack(@NonNull MessageUnpacker unpacker) throws IOException {
        unpacker.unpackNil();
        return new BlockData();
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        packer.packNil();
    }
}
