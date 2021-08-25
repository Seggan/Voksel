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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import lombok.ToString;

import java.util.Set;
import java.util.UUID;

@ToString
public final class ServerWorld extends World {

    private final Int2ObjectMap<Chunk> chunks = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    public ServerWorld(String name, UUID uuid) {
        super(name, uuid);

    }

    public ServerWorld(String name) {
        this(name, UUID.randomUUID());
    }

    @Override
    public Chunk chunk(int pos) {
        return chunks.computeIfAbsent(pos, i -> new Chunk(i, this));
    }

    @Override
    public void removeChunk(int pos) {
        chunks.remove(pos);
    }

    @Override
    public boolean isChunkLoaded(int pos) {
        return chunks.containsKey(pos);
    }

    @Override
    public Set<Chunk> chunks() {
        return ImmutableSet.copyOf(chunks.values());
    }

}
