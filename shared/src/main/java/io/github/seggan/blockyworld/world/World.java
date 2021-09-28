/*
 * A light 2D Minecraft clone
 * Copyright (C) 2021 Seggan (segganew@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.seggan.blockyworld.world;

import com.google.common.collect.ImmutableSet;
import io.github.seggan.blockyworld.util.MagicValues;
import io.github.seggan.blockyworld.util.NumberUtil;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.util.SerialUtil;
import io.github.seggan.blockyworld.util.Vector;
import io.github.seggan.blockyworld.world.block.Block;
import io.github.seggan.blockyworld.world.block.Material;
import io.github.seggan.blockyworld.world.chunk.Chunk;
import io.github.seggan.blockyworld.world.entity.Entity;
import io.github.seggan.blockyworld.world.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class World {

    private static final Map<UUID, World> worlds = new ConcurrentHashMap<>();
    private static final Constructor<? extends World> ctor;

    static {
        Constructor<? extends World> ctor1;
        try {
            Class<? extends World> clazz = Class.forName("io.github.seggan.blockyworld.world.ClientWorld").asSubclass(World.class);
            ctor1 = clazz.getConstructor(String.class, UUID.class);
            ctor1.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            ctor1 = null;
        }

        ctor = ctor1;
    }

    @Setter
    protected String name;
    @Setter(AccessLevel.PROTECTED)
    protected UUID uuid;

    protected final Map<UUID, Player> players = new ConcurrentHashMap<>();
    protected final Map<UUID, Entity> entities = new ConcurrentHashMap<>();

    protected World(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;

        worlds.put(this.uuid, this);
    }

    @Nullable
    @Contract("null -> null")
    public static World byUUID(@Nullable UUID uuid) {
        if (uuid == null) return null;
        return worlds.get(uuid);
    }

    public static Set<World> worlds() {
        return ImmutableSet.copyOf(worlds.values());
    }

    public static World unpack(@NonNull MessageUnpacker unpacker) throws IOException {
        String name = unpacker.unpackString();
        UUID uuid = SerialUtil.unpackUUID(unpacker);

        if (ctor != null) {
            try {
                return ctor.newInstance(name, uuid);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        } else {
            return new ServerWorld(name, uuid);
        }
    }

    public abstract Chunk chunkAt(int pos);

    public abstract void removeChunk(int pos);

    public abstract boolean isChunkLoaded(int pos);

    public abstract Set<Chunk> chunks();

    @NotNull
    public Block blockAt(int x, int y) {
        return chunkAt(NumberUtil.worldToChunk(x)).getBlock(NumberUtil.worldToInChunk(x), y);
    }

    @NotNull
    public Block blockAt(@NonNull Position position) {
        return blockAt(position.x(), position.y());
    }

    @NotNull
    public Block blockAt(@NonNull Vector vector) {
        return blockAt((int) vector.x(), (int) vector.y());
    }

    public int highestBlockYAt(int x) {
        Chunk chunk = chunkAt(NumberUtil.worldToChunk(x));
        int inChunk = NumberUtil.worldToInChunk(x);
        for (int y = MagicValues.CHUNK_HEIGHT; y >= 0; y--) {
            if (chunk.getBlock(inChunk, y).material() != Material.AIR) {
                return y;
            }
        }

        return 0;
    }

    @NotNull
    public Block highestBlockAt(int x) {
        Chunk chunk = chunkAt(NumberUtil.worldToChunk(x));
        int inChunk = NumberUtil.worldToInChunk(x);
        for (int y = MagicValues.CHUNK_HEIGHT; y >= 0; y--) {
            Block b = chunk.getBlock(inChunk, y);
            if (b.material() != Material.AIR) {
                return b;
            }
        }

        return chunk.getBlock(inChunk, 0);
    }

    public void addPlayer(@NonNull Player p) {
        players.put(p.uuid(), p);
        entities.put(p.uuid(), p);
    }

    public void removePlayer(@NonNull Player p) {
        players.remove(p.uuid());
        entities.remove(p.uuid());
    }

    @Nullable
    public Player player(@NonNull UUID uuid) {
        return players.get(uuid);
    }

    public Set<Player> players() {
        return ImmutableSet.copyOf(players.values());
    }

    public void addEntity(@NonNull Entity e) {
        entities.put(e.uuid(), e);
    }

    public void removeEntity(@NonNull Entity e) {
        entities.remove(e.uuid());
    }

    @Nullable
    public Entity entity(@NonNull UUID uuid) {
        return entities.get(uuid);
    }

    public Set<Entity> entities() {
        return ImmutableSet.copyOf(entities.values());
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        packer.packString(name());
        SerialUtil.packUUID(packer, uuid());
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof World other)) return false;
        return this.uuid.equals(other.uuid);
    }
}
