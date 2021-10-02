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

package io.github.seggan.voksel.screens;

import box2dLight.PointLight;
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
import io.github.seggan.voksel.Renderer;
import io.github.seggan.voksel.Voksel;
import io.github.seggan.voksel.util.FilterValues;
import io.github.seggan.voksel.util.MagicValues;
import io.github.seggan.voksel.util.NumberUtil;
import io.github.seggan.voksel.util.Position;
import io.github.seggan.voksel.util.TextureUtils;
import io.github.seggan.voksel.world.VokselWorld;
import io.github.seggan.voksel.world.block.Block;
import io.github.seggan.voksel.world.block.Material;
import io.github.seggan.voksel.world.chunk.Chunk;
import io.github.seggan.voksel.world.entity.player.Player;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class MainScreen implements Screen {

    private static final int LOAD_RADIUS = 1;

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
    private final Voksel game;

    private float delta = 0;
    private float stepDelta = 0;

    private final RayHandler rayHandler;
    private final PointLight sun;

    private InventoryScreen inventoryScreen;

    public MainScreen(@NonNull Voksel game) {
        this.game = game;
        inst = this;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);
        viewport = new ScreenViewport(camera);
        renderer = new Renderer(this);

        world = new VokselWorld("world");
        rayHandler = new RayHandler(world.box2dWorld());
        rayHandler.setAmbientLight(0.1F);
        RayHandler.useDiffuseLight(true);
        RayHandler.setGammaCorrection(true);
        rayHandler.setCulling(true);
        rayHandler.setBlur(true);
        rayHandler.setBlurNum(1);
        rayHandler.setShadows(true);
        world.chunkAt(0);

        player = new Player(world.box2dWorld(), new Vector2(0, world.highestBlockYAt(0) + 2));

        sun = new PointLight(rayHandler, 500, Color.WHITE, 500, player.body().getPosition().x, player.body().getPosition().y + 20);
        sun.setSoftnessLength(4);
        sun.setContactFilter(FilterValues.SUN_CATEGORY, (short) 0, FilterValues.SUN_MASK);

        playerTex = TextureUtils.load("player.png", 1, 2);
        selector = TextureUtils.load("selector.png");

        Vector2 pos = NumberUtil.bodyToScreen(player);
        camera.position.set((float) (pos.x + (MagicValues.WORLD_SCREEN_RATIO / 2D)), pos.y + MagicValues.WORLD_SCREEN_RATIO, 0);
        camera.update();
    }

    public Position worldToScreen(@NonNull Position position) {
        int x = position.x() * MagicValues.WORLD_SCREEN_RATIO;
        int y = position.y() * MagicValues.WORLD_SCREEN_RATIO;
        return new Position(x, y);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(new Color(0x91faeeff));

        Vector2 playerPos = player.body().getPosition();
        Vector2 pos = NumberUtil.bodyToScreen(player);

        camera.position.set(pos.x, pos.y, 0);
        camera.update();

        Matrix4 scaledMatrix = new Matrix4(camera.combined);
        scaledMatrix.scale(MagicValues.WORLD_SCREEN_RATIO, MagicValues.WORLD_SCREEN_RATIO, 0);
        if (MagicValues.DEBUG) {
            Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer(true, true, true, false, true, true);
            debugRenderer.render(world.box2dWorld(), scaledMatrix);
        }

        Vector3 unprojected = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        int mX = (int) Math.floor(unprojected.x / MagicValues.WORLD_SCREEN_RATIO);
        int mY = (int) (unprojected.y / MagicValues.WORLD_SCREEN_RATIO);
        Block hovering = world.blockAt(mX, mY);

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        if (!MagicValues.DEBUG) {
            for (Chunk chunk : world.chunks()) {
                renderer.render(chunk);
            }
        }

        batch.setColor(Color.WHITE);

        batch.draw(selector, mX * MagicValues.WORLD_SCREEN_RATIO, mY * MagicValues.WORLD_SCREEN_RATIO);

        if (!MagicValues.DEBUG) batch.draw(playerTex, pos.x, pos.y);
        batch.end();

        Vector2 v = player.body().getLinearVelocity();
        if ((Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.W)) &&
           v.y == 0) {
            player.body().applyLinearImpulse(0, 300, playerPos.x, playerPos.y, true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) && v.x < Player.MAX_SPEED) {
            player.body().applyLinearImpulse(10, 0, playerPos.x, playerPos.y, true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) && v.x > -Player.MAX_SPEED) {
            player.body().applyLinearImpulse(-10, 0, playerPos.x, playerPos.y, true);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            MagicValues.DEBUG = !MagicValues.DEBUG;
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (hovering.material() != Material.AIR) {
                hovering.material(Material.AIR);
            }
        } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            if (hovering.material() == Material.AIR) {
                Rectangle selector = new Rectangle(mX, mY, 1, 1);
                Rectangle playerRect = new Rectangle(
                    playerPos.x,
                    playerPos.y,
                    1,
                    2
                );
                if (!selector.overlaps(playerRect)) {
                    hovering.material(Material.STONE);
                }
            }
        } else if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            if (hovering.material() == Material.AIR) {
                Rectangle selector = new Rectangle(mX, mY, 1, 1);
                Rectangle playerRect = new Rectangle(
                    playerPos.x,
                    playerPos.y,
                    1,
                    2
                );
                if (!selector.overlaps(playerRect)) {
                    hovering.material(Material.LIGHT);
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            inventoryScreen = new InventoryScreen(this);
            this.game.setScreen(inventoryScreen);
        }

        sun.setPosition(playerPos.x, Math.max(playerPos.y, MagicValues.SEA_LEVEL) + 20);

        rayHandler.setCombinedMatrix(scaledMatrix);
        rayHandler.updateAndRender();

        this.delta += delta;
        if (this.delta > 1 / 20f) {
            this.delta = 0;
            updateChunks();
        }

        doPhysicsStep(delta);
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
        batch.dispose();
        rayHandler.dispose();
        if (inventoryScreen != null) inventoryScreen.dispose();
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

    private void doPhysicsStep(float deltaTime) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.25f);
        stepDelta += frameTime;
        while (stepDelta >= MagicValues.TIME_STEP) {
            world.box2dWorld().step(MagicValues.TIME_STEP, 6, 4);
            stepDelta -= MagicValues.TIME_STEP;
        }
    }
}
