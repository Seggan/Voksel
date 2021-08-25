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
import com.google.common.collect.Sets;
import io.github.seggan.blockyworld.entity.Player;
import io.github.seggan.blockyworld.util.NumberUtil;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.util.SerialUtil;
import io.github.seggan.blockyworld.world.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.Getter;
import lombok.NonNull;

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

    private final String name;
    private final UUID uuid;

    private final Set<Player> players = Sets.newConcurrentHashSet();

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

    public abstract Chunk chunk(int pos);

    public abstract void removeChunk(int pos);

    public abstract boolean isChunkLoaded(int pos);

    public abstract Set<Chunk> chunks();

    @NotNull
    public Block blockAt(@NonNull Position position) {
        return chunk(NumberUtil.worldToChunk(position.x())).getBlock(NumberUtil.worldToInChunk(position));
    }

    @NotNull
    public Block blockAt(int x, int y) {
        return blockAt(new Position(x, y));
    }

    public void addPlayer(@NonNull Player p) {
        players.add(p);
    }

    public void removePlayer(@NonNull Player p) {
        players.remove(p);
    }

    public Set<Player> players() {
        return ImmutableSet.copyOf(players);
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        packer.packString(name());
        SerialUtil.packUUID(packer, uuid());
    }

}
