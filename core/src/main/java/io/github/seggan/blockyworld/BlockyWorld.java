package io.github.seggan.blockyworld;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import io.github.seggan.blockyworld.world.block.Block;
import io.github.seggan.blockyworld.world.block.Material;

import lombok.NonNull;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class BlockyWorld extends ApplicationAdapter {

    private static int SCREEN_OFFSET_X = 0;
    private static int SCREEN_OFFSET_Y = 0;

    public static Position worldToScreen(@NonNull Position position) {
        int x = position.x() * MagicNumbers.WORLD_SCREEN_RATIO + BlockyWorld.SCREEN_OFFSET_X;
        int y = position.y() * MagicNumbers.WORLD_SCREEN_RATIO + BlockyWorld.SCREEN_OFFSET_Y;
        return new Position(x, y);
    }

    private SpriteBatch batch;
    private OrthographicCamera camera;

    private Block block;
    private Block block2;
    private Chunk chunk;
    private Renderer renderer;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);
        chunk = new Chunk(0, new World("test"));
        block = new Block(Material.STONE, 0, 0, chunk);
        block2 = new Block(Material.STONE, 1, 0, chunk);
        renderer = new Renderer(batch);
    }

    @Override
    public void render() {
        ScreenUtils.clear(new Color(0x1EA1FFFF));

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderer.render(block);
        renderer.render(block2);
        batch.end();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        batch.dispose();
    }
}