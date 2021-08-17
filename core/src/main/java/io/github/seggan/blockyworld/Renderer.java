package io.github.seggan.blockyworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Rectangle;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.block.Block;
import io.github.seggan.blockyworld.world.block.Material;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.EnumMap;
import java.util.Map;

@Getter
public final class Renderer {

    @Getter(AccessLevel.NONE)
    private final Map<Material, Texture> cache = new EnumMap<>(Material.class);

    private final SpriteBatch batch;

    public Renderer(SpriteBatch batch) {
        this.batch = batch;
    }

    public void render(@NonNull Block block, int offset) {
        Rectangle rectangle = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Material material = block.material();
        if (material == Material.AIR) return;
        if (!cache.containsKey(material)) {
            Pixmap orig = new Pixmap(Gdx.files.internal("blocks/" + material.defaultTexture() + ".png"));
            Pixmap newPix = new Pixmap(
                MagicNumbers.WORLD_SCREEN_RATIO,
                MagicNumbers.WORLD_SCREEN_RATIO,
                orig.getFormat()
            );
            newPix.drawPixmap(orig, 0, 0, orig.getWidth(), orig.getHeight(), 0, 0, newPix.getWidth(), newPix.getHeight());

            cache.put(material, new Texture(newPix));

            orig.dispose();
            newPix.dispose();
        }

        Position pos = BlockyWorld.worldToScreen(block.position());
        int x = pos.x() + offset;
        int y = pos.y();
        Frustum frustum = BlockyWorld.viewport().getCamera().frustum;
        if (frustum.pointInFrustum(x, y, 0) ||
            frustum.pointInFrustum(
                x + MagicNumbers.WORLD_SCREEN_RATIO,
                y + MagicNumbers.WORLD_SCREEN_RATIO,
                0
            ) || frustum.pointInFrustum(x + MagicNumbers.WORLD_SCREEN_RATIO, y, 0) ||
            frustum.pointInFrustum(x, y + MagicNumbers.WORLD_SCREEN_RATIO, 0)
        ) {
            batch.draw(cache.get(material), x, y);
        }
    }

    public void render(@NonNull Chunk chunk) {
        int offset = chunk.position() * MagicNumbers.CHUNK_WIDTH * MagicNumbers.WORLD_SCREEN_RATIO;
        for (Block b : chunk.blocks()) {
            render(b, offset);
        }
    }

    void dispose() {
        clearCache();
    }

    public void clearCache() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();
    }
}
