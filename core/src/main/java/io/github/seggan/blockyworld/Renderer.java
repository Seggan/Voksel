package io.github.seggan.blockyworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    private double xRatio = 1;
    private double yRatio = 1;

    public Renderer(SpriteBatch batch) {
        this.batch = batch;
    }

    public void render(@NonNull Block block) {
        Material material = block.material();
        if (material == Material.AIR) return;
        if (!cache.containsKey(material)) {
            Pixmap orig = new Pixmap(Gdx.files.internal("blocks/" + material.defaultTexture() + ".png"));
            Pixmap newPix = new Pixmap(
                (int) (MagicNumbers.WORLD_SCREEN_RATIO * xRatio),
                (int) (MagicNumbers.WORLD_SCREEN_RATIO * yRatio),
                orig.getFormat()
            );
            newPix.drawPixmap(orig, 0, 0, orig.getWidth(), orig.getHeight(), 0, 0, newPix.getWidth(), newPix.getHeight());

            cache.put(material, new Texture(newPix));

            orig.dispose();
            newPix.dispose();
        }

        Position pos = BlockyWorld.worldToScreen(block.position());
        batch.draw(cache.get(material), pos.x(), pos.y());
    }

    public void render(@NonNull Chunk chunk) {
        for (Block b : chunk.blocks()) {
            render(b);
        }
    }

    void dispose() {
        clearCache();
    }

    public void aspectRatio(double x, double y) {
        this.xRatio = x;
        this.yRatio = y;
        clearCache();
    }

    public void clearCache() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();
    }
}
