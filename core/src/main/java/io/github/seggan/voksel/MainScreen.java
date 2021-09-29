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

package io.github.seggan.voksel;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.seggan.voksel.util.MagicValues;
import io.github.seggan.voksel.util.NumberUtil;
import io.github.seggan.voksel.util.Position;
import io.github.seggan.voksel.world.VokselWorld;
import io.github.seggan.voksel.world.block.Block;
import io.github.seggan.voksel.world.block.Material;
import io.github.seggan.voksel.world.chunk.Chunk;
import io.github.seggan.voksel.world.entity.Player;

import lombok.Getter;
import lombok.NonNull;

@Getter
class MainScreen implements Screen {

    private static final int LOAD_RADIUS = 5;

    @Getter
    private static MainScreen inst;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final VokselWorld world;
    private final Renderer renderer;

    private final Player player;
    private final Texture playerTex;
    private final Texture selector;
    private final BitmapFont font = new BitmapFont();
    private int SCREEN_OFFSET_Y;
    private float delta = 0;
    private int speed = 1;

    private final RayHandler rayHandler;

    MainScreen() {
        inst = this;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);
        viewport = new ScreenViewport(camera);
        renderer = new Renderer(this);

        world = new VokselWorld("world");
        world.chunkAt(0);
        rayHandler = new RayHandler(world.box2dWorld());

        player = new Player(world.box2dWorld(), new Vector2(0, world.highestBlockYAt(0) + 2));

        playerTex = TextureUtils.load("player.png", 1, 2);
        selector = TextureUtils.load("selector.png");

        Vector2 pos = NumberUtil.bodyToScreen(player);
        camera.position.set((float) (pos.x + (MagicValues.WORLD_SCREEN_RATIO / 2D)), pos.y + MagicValues.WORLD_SCREEN_RATIO, 0);
        camera.update();
    }

    public Position worldToScreen(@NonNull Position position) {
        int x = position.x() * MagicValues.WORLD_SCREEN_RATIO;
        int y = position.y() * MagicValues.WORLD_SCREEN_RATIO + SCREEN_OFFSET_Y;
        return new Position(x, y);
    }

    public Vector2 worldToScreen(@NonNull Vector2 location) {
        float x = location.x * MagicValues.WORLD_SCREEN_RATIO;
        float y = location.y * MagicValues.WORLD_SCREEN_RATIO + SCREEN_OFFSET_Y;
        return new Vector2(x, y);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer(true, true, true, false, true, true);
        Matrix4 debugMatrix = new Matrix4(camera.combined);
        debugMatrix.scale(MagicValues.WORLD_SCREEN_RATIO, MagicValues.WORLD_SCREEN_RATIO, 0);
        debugRenderer.render(world.box2dWorld(), debugMatrix);

        world.box2dWorld().step(delta, 6, 4);

        Vector3 unprojected = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        int mX = (int) Math.floor(unprojected.x / MagicValues.WORLD_SCREEN_RATIO);
        int mY = (int) (unprojected.y / MagicValues.WORLD_SCREEN_RATIO);
        Block hovering = world.blockAt(mX, mY);

        Vector2 pos = NumberUtil.bodyToScreen(player);

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        for (Chunk chunk : world.chunks()) {
            //renderer.render(chunk);
        }

        batch.setColor(Color.WHITE);

        batch.draw(selector, mX * MagicValues.WORLD_SCREEN_RATIO, mY * MagicValues.WORLD_SCREEN_RATIO);

        //batch.draw(playerTex, x, y);
        batch.end();

        Vector2 v = new Vector2(0, 0);
        if ((Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.W)) &&
           player.body().getLinearVelocity().y == 0) {
            player.body().applyLinearImpulse(0, 300, pos.x, pos.y, true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            v.add(400, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            v.add(-400, 0);
        }

        //camera.position.add(v.x, v.y, 0);
        camera.position.set(pos.x, pos.y, 0);
        player.body().applyForceToCenter(v, true);
        camera.update();

        //player.body().applyForceToCenter(v, true);

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (hovering.material() != Material.AIR) {
                hovering.material(Material.AIR);
            }
        } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            if (hovering.material() == Material.AIR) {
                Rectangle selector = new Rectangle(mX, mY, 1, 1);
                Rectangle playerRect = new Rectangle(
                    pos.x + 1.1F,
                    pos.y + 1,
                    1,
                    1.9F
                );
                if (!selector.overlaps(playerRect)) {
                    hovering.material(Material.STONE);
                }
            }
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
        rayHandler.dispose();
    }

    private void updateChunks() {
        int chunkpos = Math.floorDiv((int) player.body().getPosition().x, MagicValues.CHUNK_WIDTH);
        for (Chunk chunk : world.chunks()) {
            if (Math.abs(chunk.position() - chunkpos) > LOAD_RADIUS) {
                world.removeChunk(chunk.position());
                System.out.println("Unloaded chunk " + chunk.position());
            }
            for (int pos = chunkpos - LOAD_RADIUS; pos <= chunkpos + LOAD_RADIUS; pos++) {
                if (!world.isChunkLoaded(pos)) {
                    world.chunkAt(pos);
                    System.out.println("Loaded chunk " + pos);
                }
            }
        }
    }
}
