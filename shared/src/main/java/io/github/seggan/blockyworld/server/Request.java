package io.github.seggan.blockyworld.server;

import com.google.common.primitives.Bytes;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

@Getter
public abstract class Request {

    private final RequestType type;

    protected Request(RequestType type) {
        this.type = type;
    }

    public byte[] serialize() throws IOException {
        MessageBufferPacker secret = MessagePack.newDefaultBufferPacker();
        pack(secret);
        byte[] data = secret.toByteArray();
        byte[] code = ByteBuffer.allocate(Short.BYTES).putShort(type.code()).array();
        byte[] len = ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array();
        return Bytes.concat(code, len, data);
    }

    protected abstract void pack(@NonNull MessageBufferPacker packer) throws IOException;
}
