package io.github.seggan.blockyworld.server;

import io.github.seggan.blockyworld.world.World;
import org.msgpack.core.MessageBufferPacker;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.net.InetAddress;

@Getter
public final class WorldRequest extends Request {

    private final World world;

    /**
     * @param world a world
     * @param address the address of the creator of the request
     */
    public WorldRequest(@NonNull World world, @NonNull InetAddress address) {
        super(RequestType.REQUEST_WORLD, true, address);
        this.world = world;
    }

    public WorldRequest(@NonNull InetAddress address) {
        super(RequestType.REQUEST_CHUNK, false, address);
        this.world = null;
    }

    @Override
    protected void pack(@NonNull MessageBufferPacker packer) throws IOException {
        if (server()) {
            world.pack(packer);
        }
    }
}
