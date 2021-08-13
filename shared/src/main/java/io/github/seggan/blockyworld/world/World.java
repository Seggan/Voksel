package io.github.seggan.blockyworld.world;

import io.github.seggan.blockyworld.BsonSerializable;
import io.github.seggan.blockyworld.util.NumberUtil;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.world.block.Block;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ToString
public final class World implements BsonSerializable {

    private static final Map<UUID, World> worlds = new ConcurrentHashMap<>();

    private final Map<Integer, Chunk> chunks = new ConcurrentHashMap<>();
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

    public synchronized Chunk getChunk(int pos) {
        return chunks.computeIfAbsent(pos, i -> new Chunk(i, this));
    }

    @NotNull
    public synchronized Block getBlockAt(@NonNull Position position) {
        return chunks.get(NumberUtil.worldToChunk(position.x())).getBlock(NumberUtil.worldToInChunk(position));
    }

    @NotNull
    public synchronized Block getBlockAt(int x, int y) {
        return getBlockAt(new Position(x, y));
    }

    @Override
    public BsonDocument toBson() {
        BsonDocument document = new BsonDocument();
        document.put("n", new BsonString(name)); // name
        document.put("i", new BsonBinary(uuid)); // id

        BsonArray array = new BsonArray();
        for (Chunk chunk : chunks.values()) {
            array.add(chunk.toBson());
        }
        document.put("c", array); // chunks

        return document;
    }
}
