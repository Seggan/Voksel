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

    private static final int MOVE_INCL = 1;

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
        Vector deltaVector = dir.copy();
        boolean isMoving = false;
        if (entity instanceof Player player) {
            Vector moving = player.moving();
            if (!moving.isZero()) {
                isMoving = true;
                deltaVector.add(moving.add(0, MOVE_INCL));
                moving.zero();
            }
        }

        if (isMoving) {
            moveCheck:
            {
                Vector newPos = entity.position().copy().add(deltaVector.multiply(delta));
                double newPosX = newPos.x();
                double newPosY = newPos.y();
                int wholeX = (int) newPosX;
                int wholeY = (int) newPosY;

                for (int x = wholeX - 2; x < wholeX + 3; x++) {
                    for (int y = wholeY - 2; y < wholeY + 4; y++) {
                        if (!world.blockAt(x, y).isPassable() && NumberUtil.rectIntersect(
                            newPosX + 1.1,
                            newPosY + 1,
                            0.8,
                            2,
                            x,
                            y,
                            1,
                            1
                        )) {
                            break moveCheck;
                        }
                    }
                }

                entity.position().set(newPos);
            }
        }

        Vector deltaGravity = MagicNumbers.GRAVITY.copy().multiply(delta);
        deltaVector = dir.copy().add(deltaGravity).multiply(delta);
        Vector newPos = entity.position().copy().add(deltaVector);
        double newPosX = newPos.x();
        double newPosY = newPos.y();
        int wholeX = (int) newPosX;
        int wholeY = (int) newPosY;

        for (int x = wholeX - 2; x < wholeX + 3; x++) {
            for (int y = wholeY - 2; y < wholeY + 4; y++) {
                if (!world.blockAt(x, y).isPassable() && NumberUtil.rectIntersect(
                    newPosX + 1.1,
                    newPosY + 1,
                    0.8,
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

        entity.position().set(newPos);
        dir.add(deltaGravity);
        server.send(new EntityMovePacket(entity.uuid(), newPos), null);
    }

}
