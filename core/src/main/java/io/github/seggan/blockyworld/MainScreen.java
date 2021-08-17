package io.github.seggan.blockyworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;

import lombok.Getter;
import lombok.NonNull;

@Getter
class MainScreen implements Screen {

    @Getter
    private static MainScreen inst;
    private final Connection connection;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final World world;
    private final Renderer renderer;

    private int SCREEN_OFFSET_X = 0;
    private int SCREEN_OFFSET_Y = 0;

    private float delta = 0;

    private int speed = 1;

    MainScreen() {
        inst = this;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);
        viewport = new ScreenViewport(camera);
        renderer = new Renderer(this);

        connection = BlockyWorld.connection();

        world = connection.requestWorld();
        connection.requestChunk(0, world);
        connection.requestChunk(-1, world);
        connection.requestChunk(1, world);
    }

    public Position worldToScreen(@NonNull Position position) {
        int x = position.x() * MagicNumbers.WORLD_SCREEN_RATIO + SCREEN_OFFSET_X;
        int y = position.y() * MagicNumbers.WORLD_SCREEN_RATIO + SCREEN_OFFSET_Y;
        return new Position(x, y);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(new Color(0x1EA1FFFF));

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Chunk chunk : world.chunks()) {
            renderer.render(chunk);
        }
        batch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            SCREEN_OFFSET_Y -= speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            SCREEN_OFFSET_Y += speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            SCREEN_OFFSET_X -= speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            SCREEN_OFFSET_X += speed;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
            speed++;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            speed--;
        }

        this.delta += delta;
        if (this.delta > 1 / 20f) {
            this.delta = 0;
            updateChunks();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        renderer.dispose();
        batch.dispose();
    }

    private void updateChunks() {
        int chunkpos = -(SCREEN_OFFSET_X / MagicNumbers.WORLD_SCREEN_RATIO / MagicNumbers.CHUNK_WIDTH);
        System.out.println(chunkpos);
        for (Chunk chunk : world.chunks()) {
            if (Math.abs(chunk.position() - chunkpos) > 2) {
                world.removeChunk(chunk.position());
                System.out.println("Unloaded chunk " + chunk.position());
            }
            for (int pos = chunkpos - 2; pos <= chunkpos + 2; pos++) {
                if (!world.isChunkLoaded(pos)) {
                    BlockyWorld.connection().requestChunk(pos, world);
                    System.out.println("Loaded chunk " + pos);
                }
            }
        }
    }
}
