package io.github.seggan.blockyworld.server;

import com.google.common.primitives.Bytes;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

@Getter
public abstract class Request {

    private final RequestType type;
    private final boolean server;
    private final InetAddress address;

    /**
     * @param type the type of the request
     * @param server if the creator of the request is the server
     * @param address the address of the creator of the request
     */
    protected Request(@NonNull RequestType type, boolean server, InetAddress address) {
        this.type = type;
        this.server = server;
        this.address = address;
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
