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

package io.github.seggan.blockyworld.world.chunk;

import io.github.seggan.blockyworld.util.MagicValues;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.util.SerialUtil;
import io.github.seggan.blockyworld.world.World;
import io.github.seggan.blockyworld.world.block.BlockData;
import io.github.seggan.blockyworld.world.block.Material;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessageUnpacker;

import lombok.NonNull;

import java.io.IOException;

final class ChunkUnpacker {

    static Chunk unpack0(@NonNull MessageUnpacker unpacker, @Nullable World world) throws IOException {
        int cPos = unpacker.unpackInt();
        World w = World.byUUID(SerialUtil.unpackUUID(unpacker));

        if (world != null) {
            if (w != null && !world.equals(w)) {
                throw new IllegalStateException(String.format(
                    "World supplied is not the chunk's world; w: %s, world: %s",
                    w.uuid().toString(),
                    world.uuid().toString()
                ));
            }

            w = world;
        }

        Chunk chunk = new Chunk(cPos, w, false);

        for (int x = 0; x < MagicValues.CHUNK_WIDTH; x++) {
            for (int y = 0; y < MagicValues.CHUNK_HEIGHT; y++) {
                if (!unpacker.tryUnpackNil()) {
                    Position pos = Position.unpack(unpacker);
                    Material material = Material.valueOf(unpacker.unpackString());
                    BlockData data = null;
                    if (!unpacker.tryUnpackNil()) {
                        data = BlockData.unpack(unpacker);
                    }
                    chunk.setBlock(material, pos, data);
                }
            }
        }

        return chunk;
    }
}