package io.github.seggan.blockyworld.server;

import org.jetbrains.annotations.NotNull;
import org.msgpack.core.MessageUnpacker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum RequestType {
    REQUEST_CHUNK(0x01, false) {
        @NotNull
        @Override
        Request unpack(@NonNull MessageUnpacker unpacker) {
            return null;
        }
    },
    REQUEST_WORLD(0x02, false) {
        @NotNull
        @Override
        Request unpack(@NonNull MessageUnpacker unpacker) {
            return null;
        }
    },
    BLOCK_UPDATE(0x03, true) {
        @NotNull
        @Override
        Request unpack(@NonNull MessageUnpacker unpacker) {
            return null;
        }
    };

    private final short code;
    private final boolean toClient;

    RequestType(int code, boolean toClient) {
        this((short) code, toClient);
    }

    @NotNull
    abstract Request unpack(@NonNull MessageUnpacker unpacker);

    @NotNull
    public static RequestType getByCode(short code) {
        for (RequestType type : values()) {
            if (code == type.code) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
