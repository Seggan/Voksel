package io.github.seggan.blockyworld.world.block;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.NonNull;

public class BlockData {

    public static BlockData unpack(@NonNull MessageUnpacker unpacker) {
        return new BlockData();
    }

    public void pack(@NonNull MessageBufferPacker packer) {
    }
}
