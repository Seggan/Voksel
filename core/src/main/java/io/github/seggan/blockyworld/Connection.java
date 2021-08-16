package io.github.seggan.blockyworld;

import io.github.seggan.blockyworld.server.Packet;
import io.github.seggan.blockyworld.server.PacketType;
import io.github.seggan.blockyworld.server.WorldPacket;
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

public final class Connection {

    private static final int HEADER_SIZE = Short.BYTES + Integer.BYTES;

    private final Socket socket;
    private final OutputStream out;
    private final InputStream in;
    private final InetAddress address;

    public Connection(@NonNull Socket socket) {
        this.socket = socket;
        try {
            out = this.socket.getOutputStream();
            in = this.socket.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        address = socket.getInetAddress();
    }

    /**
     * Sends the packet
     * @param toSend the packet to send
     * @return a packet sent pack
     */
    @Nullable
    public Packet sendPacket(@NonNull Packet toSend) {
        try {
            socket.getOutputStream().write(toSend.serialize());

            byte[] header = in.readNBytes(HEADER_SIZE);
            ByteBuffer buffer = ByteBuffer.wrap(header);
            short code = buffer.getShort();
            int length = buffer.getInt();
            byte[] body = in.readNBytes(length);
            return PacketType.getByCode(code).unpack(MessagePack.newDefaultUnpacker(body), true, address);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public World requestWorld() {
       if (sendPacket(new WorldPacket(address)) instanceof WorldPacket worldPacket) {
           return worldPacket.world();
       }

       return null;
    }

}
