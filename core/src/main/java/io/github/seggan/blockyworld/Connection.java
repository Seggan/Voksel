package io.github.seggan.blockyworld;

import io.github.seggan.blockyworld.server.ChunkPacket;
import io.github.seggan.blockyworld.server.Packet;
import io.github.seggan.blockyworld.server.PacketType;
import io.github.seggan.blockyworld.server.WorldPacket;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessagePack;

import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class Connection {

    private static final int HEADER_SIZE = Short.BYTES + Integer.BYTES;

    private final Socket socket;
    private final OutputStream out;
    private final InputStream in;
    private final InetAddress address;

    private final Queue<Packet> queue = new ConcurrentLinkedDeque<>();

    public Connection(@NonNull Socket socket) {
        this.socket = socket;
        try {
            out = this.socket.getOutputStream();
            in = this.socket.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        address = socket.getInetAddress();

        Thread thread = new Thread(() -> {
            while (!socket.isInputShutdown()) {
                try {
                    byte[] header;
                    header = in.readNBytes(HEADER_SIZE);
                    ByteBuffer buffer = ByteBuffer.wrap(header);
                    short code = buffer.getShort();
                    if (code == 4) continue;
                    int length = buffer.getInt();
                    byte[] body = in.readNBytes(length);
                    Packet pack = PacketType.getByCode(code).unpack(MessagePack.newDefaultUnpacker(body), true, address);
                    queue.add(pack);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Sends the packet
     *
     * @param toSend the packet to send
     * @return a packet sent pack
     */
    @Nullable
    public <T extends Packet> T sendPacket(@NonNull Packet toSend, @Nullable Class<T> returnType) {
        try {
            socket.getOutputStream().write(toSend.serialize());

            while (true) {
                Packet pack = queue.poll();
                if (pack != null) {
                    if (pack.getClass().equals(returnType)) {
                        return returnType.cast(pack);
                    }
                    queue.add(pack);
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public World requestWorld() {
        WorldPacket packet = sendPacket(new WorldPacket(address), WorldPacket.class);
        if (packet == null) return null;
        return packet.world();
    }

    @Nullable
    public Chunk requestChunk(int pos, @NonNull World world) {
        ChunkPacket packet = sendPacket(new ChunkPacket(pos, world, address), ChunkPacket.class);
        if (packet == null) return null;
        return packet.chunk();
    }

    public Packet nextReceived() {
        return queue.poll();
    }

}
