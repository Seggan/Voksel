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
import io.github.seggan.blockyworld.world.entity.Entity;
import io.github.seggan.blockyworld.world.entity.Player;
import org.jetbrains.annotations.NotNull;

import lombok.NonNull;

import java.net.InetAddress;

public final class Ticker implements Runnable {

    private static final Vector MOVE_INCL = new Vector(0, 0.5);

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
        this.time = System.nanoTime();
    }


    @Override
    public void run() {
        try {
            long tempTime = System.nanoTime();
            double delta = (tempTime - time) / 1e9;
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
        Vector deltaGravity = MagicNumbers.GRAVITY.copy().multiply(delta);
        Vector deltaVector = dir.copy().add(deltaGravity);
        if (entity instanceof Player player) {
            Vector moving = player.moving();
            if (!moving.isZero()) {
                deltaVector.add(moving.add(MOVE_INCL));
                moving.zero();
            }
        }
        deltaVector.multiply(delta);
        Vector newPos = entity.position().copy().add(deltaVector);
        double newPosX = newPos.x();
        double newPosY = newPos.y();
        int wholeX = (int) newPosX;
        int wholeY = (int) newPosY;

        for (int x = wholeX - 6; x < wholeX + 6; x++) {
            for (int y = wholeY - 6; y < wholeY + 6; y++) {
                if (!world.blockAt(x, y).isPassable() && NumberUtil.rectIntersect(
                    newPosX + 0.6,
                    newPosY + 1,
                    0.9,
                    2,
                    x,
                    y,
                    1,
                    1
                )) {
                    dir.zero();
                    return;
                }
            }
        }

        entity.gravity(true);

        entity.position().set(newPos);
        dir.add(deltaGravity);
        server.send(new EntityMovePacket(entity.uuid(), newPos, thisAddress), null);
    }

}
