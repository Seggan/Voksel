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
import io.github.seggan.blockyworld.world.chunk.Chunk;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import lombok.Lombok;
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
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@ToString
public final class ServerWorld extends World {

    private final Int2ObjectMap<Chunk> chunks = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    private final File fldr;

    public ServerWorld(String name, UUID uuid) {
        super(name, uuid);

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

    public ServerWorld(String name) {
        this(name, UUID.randomUUID());
    }

    @Override
    public Chunk chunkAt(int pos) {
        return chunks.computeIfAbsent(pos, i -> {
            File file = new File(fldr, i + ".chunk");
            if (!file.exists()) return new Chunk(i, this);
            try {
                MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(Files.readAllBytes(file.toPath()));
                return Chunk.unpack(unpacker, this);
            } catch (IOException e) {
                throw Lombok.sneakyThrow(e);
            }
        });
    }

    @Override
    @SneakyThrows(IOException.class)
    public void removeChunk(int pos) {
        Chunk chunk = chunks.remove(pos);
        File save = new File(fldr, pos + ".chunk");

        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        chunk.pack(packer);
        Files.write(save.toPath(), packer.toByteArray());
    }

    @Override
    public boolean isChunkLoaded(int pos) {
        return chunks.containsKey(pos);
    }

    @Override
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

}
