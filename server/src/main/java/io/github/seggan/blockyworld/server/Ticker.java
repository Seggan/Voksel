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

import io.github.seggan.blockyworld.util.Vector;
import io.github.seggan.blockyworld.world.ServerWorld;
import io.github.seggan.blockyworld.world.entity.Entity;

import lombok.NonNull;

public final class Ticker implements Runnable {

    private final MainThread mainThread;
    private final ServerWorld world;
    private long time;

    public Ticker(@NonNull MainThread mainThread) {
        this.mainThread = mainThread;
        this.world = mainThread.world();
        this.time = System.currentTimeMillis();
    }


    @Override
    public void run() {
        long tempTime = System.currentTimeMillis();
        double delta = 1_000D / (tempTime - time);
        time = tempTime;
        for (Entity entity : world.entities()) {
            Vector deltaVector = entity.direction().copy().multiply(delta);

        }
    }

}
