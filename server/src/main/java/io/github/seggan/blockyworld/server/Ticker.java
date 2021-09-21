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

package io.github.seggan.blockyworld.server;

import io.github.seggan.blockyworld.server.packets.EntityMovePacket;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.NumberUtil;
import io.github.seggan.blockyworld.util.Vector;
import io.github.seggan.blockyworld.world.ServerWorld;
import io.github.seggan.blockyworld.world.block.Material;
import io.github.seggan.blockyworld.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import lombok.NonNull;

import java.net.InetAddress;

public final class Ticker implements Runnable {

    private final MainThread mainThread;
    private final ServerWorld world;
    private final IServer server;
    private final InetAddress thisAddress;
    private long time;

    public Ticker(@NonNull MainThread mainThread) {
        this.mainThread = mainThread;
        this.world = mainThread.world();
        this.server = mainThread.server();
        this.thisAddress = mainThread.thisAddress();
        this.time = System.currentTimeMillis();
    }


    @Override
    public void run() {
        try {
            long tempTime = System.currentTimeMillis();
            double delta = 1_000D / (tempTime - time);
            time = tempTime;
            for (Entity entity : world.entities()) {
                processEntity(entity, delta);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void processEntity(@NotNull Entity entity, double delta) {
        Vector dir = entity.direction();
        Vector deltaVector = dir.copy().multiply(delta);
        Vector newPos = entity.position().copy().add(deltaVector).add(MagicNumbers.GRAVITY);
        double newPosX = newPos.x();
        double newPosY = newPos.y();
        long wholeX = (long) newPosX;
        long wholeY = (long) newPosY;

        for (int x = (int) (wholeX - 1); x <= wholeY + 1; x++) {
            for (int y = (int) (wholeY + 1); y > wholeY - 3; y--) {
                if (NumberUtil.rectIntersect(newPosX, newPosY, 1, 2, x, y, 1, 1) &&
                    world.blockAt(x, y).material() != Material.AIR) {
                    dir.zero();
                    return;
                }
            }
        }

        entity.position().set(newPos);
        server.send(new EntityMovePacket(entity.uuid(), newPos, thisAddress), null);
    }

}
