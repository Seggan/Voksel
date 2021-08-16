package io.github.seggan.blockyworld.world;

import io.github.seggan.blockyworld.util.NumberUtil;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.util.SerialUtil;
import io.github.seggan.blockyworld.world.block.Block;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ToString
public final class World {

    private static final Map<UUID, World> worlds = new ConcurrentHashMap<>();

    private final Int2ObjectMap<Chunk> chunks = new Int2ObjectOpenHashMap<>();
    @Getter
    private final UUID uuid;
    @Getter
    private final String name;

    public World(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        worlds.put(this.uuid, this);
    }

    public World(String name) {
        this(UUID.randomUUID(), name);
    }

    @Nullable
    @Contract("null -> null")
    public static World getByUUID(@Nullable UUID uuid) {
        if (uuid == null) return null;
        return worlds.get(uuid);
    }

    public static Collection<World> getWorlds() {
        return worlds.values();
    }

    public Chunk getChunk(int pos) {
        synchronized (chunks) {
            return chunks.computeIfAbsent(pos, i -> new Chunk(i, this));
        }
    }

    void addChunk(@NotNull Chunk chunk) {
        synchronized (chunks) {
            chunks.put(chunk.position(), chunk);
        }
    }

    @NotNull
    public Block getBlockAt(@NonNull Position position) {
        synchronized (chunks) {
            return chunks.get(NumberUtil.worldToChunk(position.x())).getBlock(NumberUtil.worldToInChunk(position));
        }
    }

    @NotNull
    public Block getBlockAt(int x, int y) {
        return getBlockAt(new Position(x, y));
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        packer.packString(name);
        SerialUtil.packUUID(packer, uuid);
    }

    public static World unpack(@NonNull MessageUnpacker unpacker) throws IOException {
        String name = unpacker.unpackString();
        return new World(SerialUtil.unpackUUID(unpacker), name);
    }
}
