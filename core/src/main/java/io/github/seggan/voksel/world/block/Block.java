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

package io.github.seggan.voksel.world.block;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Disposable;
import com.google.common.base.Objects;
import io.github.seggan.voksel.MainScreen;
import io.github.seggan.voksel.util.FilterValues;
import io.github.seggan.voksel.util.MagicValues;
import io.github.seggan.voksel.util.NumberUtil;
import io.github.seggan.voksel.util.Position;
import io.github.seggan.voksel.util.SerialUtil;
import io.github.seggan.voksel.world.BodyHolder;
import io.github.seggan.voksel.world.VokselWorld;
import io.github.seggan.voksel.world.chunk.Chunk;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;

@Getter
public class Block implements Disposable, BodyHolder {

    private final Position position;
    private final Chunk chunk;
    private final Body body;
    private Material material;
    @Nullable
    private BlockData blockData;
    @Setter
    private PointLight light;

    public Block(@NonNull Material material, @NonNull Position position, @NonNull Chunk chunk, @Nullable BlockData data) {
        Validate.inclusiveBetween(0, MagicValues.CHUNK_WIDTH, position.x());
        this.material = material;
        this.position = position;
        this.chunk = chunk;
        this.blockData = data;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(NumberUtil.chunkToWorld(this.chunk.position(), position).x() + .5F, position.y() + 0.5F);
        bodyDef.type = BodyDef.BodyType.StaticBody;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5F, 0.5F);

        this.body = chunk.world().box2dWorld().createBody(bodyDef);

        update();

        this.body.setActive(!this.isPassable());
    }

    public Block(@NonNull Material material, int x, int y, @NonNull Chunk chunk) {
        this(material, x, y, chunk, null);
    }

    public Block(@NonNull Material material, int x, int y, @NonNull Chunk chunk, @Nullable BlockData data) {
        this(material, new Position(x, y), chunk, data);
    }

    @NotNull
    public static Block unpack(@NonNull MessageUnpacker unpacker) throws IOException {
        Position pos = Position.unpack(unpacker);
        Material mat = Material.valueOf(unpacker.unpackString());
        VokselWorld world = VokselWorld.byUUID(SerialUtil.unpackUUID(unpacker));
        Chunk chunk = world.chunkAt(unpacker.unpackInt());
        BlockData data = null;
        if (!unpacker.tryUnpackNil()) {
            data = BlockData.unpack(unpacker);
        }
        return new Block(mat, pos, chunk, data);
    }

    public void pack(@NonNull MessageBufferPacker packer) throws IOException {
        position.pack(packer);
        packer.packString(material.name());
        SerialUtil.packUUID(packer, chunk.world().uuid());
        packer.packInt(chunk.position());
        if (blockData == null) {
            packer.packNil();
        } else {
            blockData.pack(packer);
        }
    }

    public void material(@NonNull Material material) {
        this.material = material;
        body.setActive(!this.isPassable());
        update();
    }

    @NotNull
    public Block relativeToThis(@NonNull BlockSide side) {
        return relativeToThis(side.direction());
    }

    @NotNull
    public Block relativeToThis(@NonNull Vector2 vector) {
        Position rel = NumberUtil.chunkToWorld(chunk.position(), position);
        return chunk.world().blockAt((int) (rel.x() + vector.x), (int) (rel.y() + vector.y));
    }

    public boolean isPassable() {
        Material material = this.material;
        return material == Material.AIR || material == Material.LIGHT;
    }

    @Override
    public String toString() {
        return "Block(position=" + this.position() + ", chunk=" + this.chunk + ", material=" + this.material.name() + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(material, position, chunk);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        else if (!(obj instanceof Block other)) return false;
        else return this.material == other.material && this.position.equals(other.position);
    }

    @Override
    public void dispose() {
        this.chunk.world().box2dWorld().destroyBody(body);
    }

    private void update() {
        if (material != Material.LIGHT && light != null) {
            light.remove();
            light = null;
        } else if (material == Material.LIGHT) {
            Position converted = NumberUtil.chunkToWorld(this.chunk.position(), this.position);
            light = new PointLight(MainScreen.inst().rayHandler(), 20, Color.YELLOW, 15, converted.x(), converted.y());
            light.setSoftnessLength(3);
            light.setContactFilter(FilterValues.SUN_CATEGORY, (short) 0, FilterValues.SUN_MASK);
        }

        this.body.getFixtureList().forEach(this.body::destroyFixture);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5F, 0.5F);

        FixtureDef def = new FixtureDef();
        def.density = 0;
        def.restitution = 0;
        def.shape = shape;

        def.filter.maskBits = FilterValues.BLOCK_MASK;

        def.filter.categoryBits = FilterValues.BLOCK_CATEGORY;
        if (!isPassable()) {
            def.filter.categoryBits |= FilterValues.NON_PASSABLE_BLOCK_CATEGORY;
        }
        if (!material.transparent()) {
            def.filter.categoryBits |= FilterValues.NON_TRANSPARENT_BLOCK_CATEGORY;
        }

        this.body.createFixture(def);
        shape.dispose();
    }

}
