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

package io.github.seggan.voksel.world.entity.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import io.github.seggan.voksel.util.BodyEditorLoader;
import io.github.seggan.voksel.util.FilterValues;
import io.github.seggan.voksel.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import lombok.NonNull;

import java.util.UUID;

public final class Player extends Entity {

    public static final int MAX_SPEED = 20;

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

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 30;
        fixtureDef.friction = 1;
        fixtureDef.restitution = 0.1F;

        fixtureDef.filter.categoryBits = FilterValues.ENTITY_CATEGORY;
        fixtureDef.filter.maskBits = FilterValues.ENTITY_MASK;

        Body body = this.world.createBody(bodyDef);

        BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("models/player.json"));
        loader.attachFixture(body, "player", fixtureDef, 1);

        body.setAwake(true);

        return body;
    }
}
