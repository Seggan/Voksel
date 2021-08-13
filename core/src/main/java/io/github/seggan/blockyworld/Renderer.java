package io.github.seggan.blockyworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.world.block.Block;
import io.github.seggan.blockyworld.world.block.Material;

import lombok.NonNull;

import java.util.EnumMap;
import java.util.Map;

public final class Renderer {

    private final Map<Material, Texture> cache = new EnumMap<>(Material.class);

    private final SpriteBatch batch;

    public Renderer(SpriteBatch batch) {
        this.batch = batch;
    }

    public void render(@NonNull Block block) {
        Material material = block.material();
        if (material == Material.AIR) return;
        if (!cache.containsKey(material)) {
            Pixmap orig = new Pixmap(Gdx.files.internal("blocks/" + material.defaultTexture() + ".png"));
            Pixmap newPix = new Pixmap(MagicNumbers.WORLD_SCREEN_RATIO, MagicNumbers.WORLD_SCREEN_RATIO, orig.getFormat());
            newPix.drawPixmap(orig, 0, 0, orig.getWidth(), orig.getHeight(), 0, 0, newPix.getWidth(), newPix.getHeight());

            cache.put(material, new Texture(newPix));

            orig.dispose();
            newPix.dispose();
        }

        Position pos = BlockyWorld.worldToScreen(block.position());
        batch.draw(cache.get(material), pos.x(), pos.y());
    }

    void dispose() {
        for (Texture texture : this.cache.values()) {
            texture.dispose();
        }
    }
}
