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

package io.github.seggan.blockyworld.util;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import it.unimi.dsi.fastutil.doubles.DoubleDoublePair;
import org.jetbrains.annotations.NotNull;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;

import java.io.IOException;

@Getter(onMethod_ = @Synchronized("lock"))
@Setter(onMethod_ = @Synchronized("lock"))
@AllArgsConstructor
public final class Vector {

    private final Object lock = new Object();

    private double x;
    private double y;

    public static Vector unpack(@NonNull MessageUnpacker unpacker) throws IOException {
        return new Vector(unpacker.unpackDouble(), unpacker.unpackDouble());
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        packer.packDouble(x);
        packer.packDouble(y);
    }

    @NotNull
    public Vector set(double x, double y) {
        synchronized (lock) {
            this.x = x;
            this.y = y;
        }

        return this;
    }

    @NotNull
    public DoubleDoublePair get() {
        synchronized (lock) {
            //noinspection SuspiciousNameCombination
            return new DoubleDoubleImmutablePair(x, y);
        }
    }

    @NotNull
    public Vector add(@NonNull Vector other) {
        DoubleDoublePair pair = other.get();
        synchronized (lock) {
            this.x += pair.leftDouble();
            this.y += pair.rightDouble();
        }

        return this;
    }

    @NotNull
    public Vector multiply(double d) {
        synchronized (lock) {
            this.x *= d;
            this.y *= d;
        }

        return this;
    }

    @NotNull
    public Vector divide(double d) {
        if (d != 0) {
            synchronized (lock) {
                this.x /= d;
                this.y /= d;
            }
        }

        return this;
    }

    public double magnitudeSquared() {
        synchronized (lock) {
            return x * x + y * y;
        }
    }

    public double magnitude() {
        return Math.sqrt(magnitudeSquared());
    }

    public boolean isZero() {
        synchronized (lock) {
            return NumberUtil.isDoubleZero(x) && NumberUtil.isDoubleZero(y);
        }
    }

    @NotNull
    public Vector zero() {
        return set(0, 0);
    }

    @NotNull
    public Vector normalize() {
        // using another block to keep modification from happening while transferring data
        synchronized (lock) {
            return divide(magnitude());
        }
    }

    @NotNull
    public Vector copy() {
        synchronized (lock) {
            return new Vector(x, y);
        }
    }

    @NotNull
    public Position toPosition() {
        synchronized (lock) {
            return new Position((int) x, (int) y);
        }
    }
}
