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

package io.github.seggan.voksel.world.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.github.seggan.voksel.util.SerialUtil;
import io.github.seggan.voksel.world.BodyHolder;
import org.jetbrains.annotations.NotNull;
import org.msgpack.core.MessageBufferPacker;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.util.UUID;

@Getter
public abstract class Entity implements BodyHolder {

    protected final World world;
    private final UUID uuid;
    private Body body;

    protected Entity(@NonNull World world, @NonNull Vector2 pos, @NonNull UUID uuid) {
        this.world = world;
        this.uuid = uuid;
        this.body = createBody(pos);
    }

    @OverridingMethodsMustInvokeSuper
    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        Vector2 pos = this.body.getPosition();
        Vector2 dir = this.body.getLinearVelocity();
        SerialUtil.packUUID(packer, uuid);
    }

    @NotNull
    protected abstract Body createBody(@NonNull Vector2 pos);

    public void setPosition(@NonNull Vector2 pos) {
        this.body = createBody(pos);
        this.body.setAwake(true);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Entity other)) return false;
        return this.uuid.equals(other.uuid);
    }
}
