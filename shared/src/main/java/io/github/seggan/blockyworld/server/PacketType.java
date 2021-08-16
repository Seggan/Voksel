package io.github.seggan.blockyworld.server;

import io.github.seggan.blockyworld.util.SerialUtil;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import org.jetbrains.annotations.NotNull;
import org.msgpack.core.MessageUnpacker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.net.InetAddress;

@Getter
@AllArgsConstructor
public enum PacketType {
    REQUEST_CHUNK(0x01) {
        @NotNull
        @Override
        public Packet unpack(@NonNull MessageUnpacker unpacker, boolean server, @NonNull InetAddress address) throws IOException {
            if (server) {
                return new ChunkPacket(Chunk.unpack(unpacker), address);
            } else {
                return new ChunkPacket(
                    unpacker.unpackInt(),
                    World.getByUUID(SerialUtil.unpackUUID(unpacker)),
                    address
                );
            }
        }
    },
    REQUEST_WORLD(0x02) {
        @NotNull
        @Override
        public Packet unpack(@NonNull MessageUnpacker unpacker, boolean server, @NonNull InetAddress address) throws IOException {
            if (server) {
                return new WorldPacket(World.unpack(unpacker), address);
            } else {
                return new WorldPacket(address);
            }
        }
    };
    /*
        0x03 is reserved for the invalid protocol packet, 0x04 for the OK packet
     */

    private final short code;

    PacketType(int code) {
        this((short) code);
    }

    /**
     * Unpacks the request
     * @param unpacker the {@link MessageUnpacker} with the data in it
     * @param server if the creator is the server
     * @param address the address of the creator
     * @return the unpacked request
     */
    @NotNull
    public abstract Packet unpack(@NonNull MessageUnpacker unpacker, boolean server, @NonNull InetAddress address) throws IOException;

    @NotNull
    public static PacketType getByCode(short code) {
        for (PacketType type : values()) {
            if (code == type.code) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
