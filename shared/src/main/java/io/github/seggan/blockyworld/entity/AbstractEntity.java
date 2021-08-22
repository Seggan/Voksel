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

package io.github.seggan.blockyworld.entity;

import com.badlogic.gdx.math.Vector2;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.SerialUtil;
import org.msgpack.core.MessageBufferPacker;

import lombok.Getter;
import lombok.NonNull;

import java.awt.geom.Point2D;
import java.io.IOException;

@Getter
public abstract class AbstractEntity {

    private final Point2D position;
    private final Vector2 direction;

    protected AbstractEntity(@NonNull Point2D position) {
        this.position = position;
        direction = Vector2.Zero;
    }

    public void applyGravity() {
        direction.add(MagicNumbers.GRAVITY);
    }

    @OverridingMethodsMustInvokeSuper
    protected void pack(@NonNull MessageBufferPacker packer) throws IOException {
        SerialUtil.packPoint(packer, position);
        SerialUtil.packVector(packer, direction);
    }
}
