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

package io.github.seggan.blockyworld.world.block;

import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.NumberUtil;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.util.SerialUtil;
import io.github.seggan.blockyworld.util.Vector;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.io.IOException;

@ToString
@EqualsAndHashCode
public class Block {

    @Getter
    private final Position position;
    @Getter
    private final Chunk chunk;
    private final Object matLock = new Object();
    private final Object bdLock = new Object();
    private Material material;
    @Nullable
    private volatile BlockData blockData;

    public Block(@NonNull Material material, @NonNull Position position, @NonNull Chunk chunk, @Nullable BlockData data) {
        Validate.inclusiveBetween(0, MagicNumbers.CHUNK_WIDTH, position.x());
        Validate.inclusiveBetween(0, MagicNumbers.CHUNK_HEIGHT, position.y());
        this.material = material;
        this.position = position;
        this.chunk = chunk;
        this.blockData = data;
    }

    public Block(@NonNull Material material, int x, int y, @NonNull Chunk chunk) {
        this(material, x, y, chunk, null);
    }

    public Block(@NonNull Material material, int x, int y, @NonNull Chunk chunk, @Nullable BlockData data) {
        this(material, new Position(x, y), chunk, data);
    }

    @NotNull
    public static Block unpack(@NonNull MessageUnpacker unpacker) throws IOException {
        Position pos = Position.unpack(unpacker);
        Material mat = Material.valueOf(unpacker.unpackString());
        World world = World.byUUID(SerialUtil.unpackUUID(unpacker));
        Chunk chunk = world.chunkAt(unpacker.unpackInt());
        BlockData data = null;
        if (!unpacker.tryUnpackNil()) {
            data = BlockData.unpack(unpacker);
        }
        return new Block(mat, pos, chunk, data);
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        position.pack(packer);
        packer.packString(material.name());
        SerialUtil.packUUID(packer, chunk.world().uuid());
        packer.packInt(chunk.position());
        if (blockData == null) {
            packer.packNil();
        } else {
            blockData.pack(packer);
        }
    }

    public Material material() {
        return this.material;
    }

    @Nullable
    public BlockData blockData() {
        synchronized (bdLock) {
            return blockData;
        }
    }

    public Block material(Material material) {
        synchronized (matLock) {
            this.material = material;
        }
        return this;
    }

    public Block blockData(@Nullable BlockData blockData) {
        synchronized (bdLock) {
            this.blockData = blockData;
        }
        return this;
    }

    @NotNull
    public Block relativeToThis(@NonNull BlockSide side) {
        return relativeToThis(side.direction());
    }

    @NotNull
    public Block relativeToThis(@NonNull Vector vector) {
        Position rel = NumberUtil.chunkToWorld(chunk.position(), position);
        return chunk.world().blockAt((int) (rel.x() + vector.x()), (int) (rel.y() + vector.y()));
    }

    public boolean isPassable() {
        Material material = material();
        return material == Material.AIR;
    }
}
