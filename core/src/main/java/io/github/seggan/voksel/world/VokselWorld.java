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

package io.github.seggan.voksel.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.google.common.collect.ImmutableSet;
import io.github.seggan.voksel.util.MagicValues;
import io.github.seggan.voksel.util.NumberUtil;
import io.github.seggan.voksel.util.Position;
import io.github.seggan.voksel.util.SerialUtil;
import io.github.seggan.voksel.world.block.Block;
import io.github.seggan.voksel.world.block.Material;
import io.github.seggan.voksel.world.chunk.Chunk;
import io.github.seggan.voksel.world.entity.Entity;
import io.github.seggan.voksel.world.entity.player.Player;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Lombok;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ToString
public final class VokselWorld {

    private static final Map<UUID, VokselWorld> worlds = new ConcurrentHashMap<>();

    protected final Map<UUID, Player> players = new ConcurrentHashMap<>();
    protected final Map<UUID, Entity> entities = new ConcurrentHashMap<>();
    private final Int2ObjectMap<Chunk> chunks = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    private final File fldr;
    @Getter
    private final World box2dWorld = new World(MagicValues.GRAVITY, true);
    @Setter
    @Getter
    private String name;
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private UUID uuid;

    public VokselWorld(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;

        worlds.put(this.uuid, this);

        File cwd = new File(System.getProperty("user.dir"));
        File resolved = null;
        for (File file : cwd.listFiles()) {
            if (file.isDirectory()) {
                File propFile = new File(file.getAbsolutePath(), "world.properties");
                if (propFile.exists()) {
                    Properties properties = new Properties();
                    try (InputStream in = new FileInputStream(propFile)) {
                        properties.load(in);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    if (UUID.fromString(properties.getProperty("uuid")).equals(uuid)) {
                        name(properties.getProperty("name", name));
                        uuid(UUID.fromString(properties.getProperty("uuid", uuid.toString())));
                        resolved = file;
                        break;
                    }
                }
            }
        }

        if (resolved == null) {
            this.fldr = new File(cwd, name);
            this.fldr.mkdir();
            save();
        } else {
            fldr = resolved;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
    }

    public VokselWorld(String name) {
        this(name, UUID.randomUUID());
    }

    @Contract("null -> null")
    public static VokselWorld byUUID(@Nullable UUID uuid) {
        if (uuid == null) return null;
        return worlds.get(uuid);
    }

    public static Set<VokselWorld> worlds() {
        return ImmutableSet.copyOf(worlds.values());
    }

    public static VokselWorld unpack(@NonNull MessageUnpacker unpacker) throws IOException {
        String name = unpacker.unpackString();
        UUID uuid = SerialUtil.unpackUUID(unpacker);

        return new VokselWorld(name, uuid);
    }

    public Chunk chunkAt(int pos) {
        return chunks.computeIfAbsent(pos, i -> {
            File file = new File(fldr, i + ".chunk");
            if (file.exists()) {
                try {
                    MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(Files.readAllBytes(file.toPath()));
                    return Chunk.unpack(unpacker, this);
                } catch (IOException e) {
                    throw Lombok.sneakyThrow(e);
                }
            }

            return new Chunk(i, this, true);
        });
    }

    @SneakyThrows(IOException.class)
    public void removeChunk(int pos) {
        Chunk chunk = chunks.remove(pos);
        File save = new File(fldr, pos + ".chunk");

        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        chunk.pack(packer);
        Files.write(save.toPath(), packer.toByteArray());

        for (Block b : chunk.blocks()) {
            b.dispose();
        }
    }

    public boolean isChunkLoaded(int pos) {
        return chunks.containsKey(pos);
    }

    public Set<Chunk> chunks() {
        return ImmutableSet.copyOf(chunks.values());
    }

    @SneakyThrows(IOException.class)
    private void save() {
        Properties properties = new Properties();
        properties.setProperty("name", this.name);
        properties.setProperty("uuid", this.uuid.toString());

        File propFile = new File(fldr, "world.properties");
        propFile.createNewFile();
        try (OutputStream out = new FileOutputStream(propFile)) {
            properties.store(out, "World settings");
        }

        for (int pos : this.chunks.keySet()) {
            removeChunk(pos);
        }
    }

    @NotNull
    public Block blockAt(int x, int y) {
        return chunkAt(NumberUtil.worldToChunk(x)).getBlock(NumberUtil.worldToInChunk(x), y);
    }

    @NotNull
    public Block blockAt(@NonNull Position position) {
        return blockAt(position.x(), position.y());
    }

    @NotNull
    public Block blockAt(@NonNull Vector2 vector) {
        return blockAt((int) vector.x, (int) vector.y);
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
        packer.packString(this.name);
        SerialUtil.packUUID(packer, this.uuid);
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof VokselWorld other)) return false;
        return this.uuid.equals(other.uuid);
    }
}
