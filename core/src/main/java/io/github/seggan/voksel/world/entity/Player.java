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
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import org.jetbrains.annotations.NotNull;

import lombok.NonNull;

import java.util.UUID;

public final class Player extends Entity {

    public Player(@NonNull World world, @NonNull Vector2 pos) {
        super(world, pos, UUID.randomUUID());
    }

    @NotNull
    @Override
    protected Body createBody(@NonNull Vector2 pos) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(pos);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5F, 1);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 30;
        fixtureDef.friction = 0.5F;
        fixtureDef.restitution = 0.1F;
        fixtureDef.shape = shape;

        Body body = this.world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        body.setAwake(true);

        shape.dispose();

        return body;
    }
}
