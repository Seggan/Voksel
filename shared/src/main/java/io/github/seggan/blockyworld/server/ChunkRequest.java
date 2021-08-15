package io.github.seggan.blockyworld.server;

import io.github.seggan.blockyworld.util.SerialUtil;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import org.msgpack.core.MessageBufferPacker;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.net.InetAddress;

@Getter
public final class ChunkRequest extends Request {

    private final Chunk chunk;
    private final int position;
    private final World world;

    public ChunkRequest(@NonNull Chunk chunk, @NonNull InetAddress address) {
        super(RequestType.REQUEST_CHUNK, true, address);
        this.chunk = chunk;
        this.position = 0;
        this.world = null;
    }

    public ChunkRequest(int position, @NonNull World world, @NonNull InetAddress address) {
        super(RequestType.REQUEST_CHUNK, false, address);
        this.position = position;
        this.world = world;
        this.chunk = null;
    }

    @Override
    protected void pack(@NonNull MessageBufferPacker packer) throws IOException {
        if (server()) {
            chunk.pack(packer);
        } else {
            packer.packInt(position);
            SerialUtil.packUUID(packer, world.uuid());
        }
    }
}
