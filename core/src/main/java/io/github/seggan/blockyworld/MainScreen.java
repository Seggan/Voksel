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

package io.github.seggan.blockyworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.seggan.blockyworld.server.packets.EntityMovePacket;
import io.github.seggan.blockyworld.server.packets.Packet;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.util.Vector;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import io.github.seggan.blockyworld.world.entity.Player;

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

    private final Player player;
    private final Texture playerTex;
    private final BitmapFont font = new BitmapFont();
    private int SCREEN_OFFSET_X = 0;
    private int SCREEN_OFFSET_Y;
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
        world.chunk(0);

        player = new Player();
        connection.connectPlayer(player);

        player.position(0, world.highestBlockYAt(0) + 1);

        Pixmap orig = new Pixmap(Gdx.files.internal("player.png"));
        Pixmap newPix = new Pixmap(
            MagicNumbers.WORLD_SCREEN_RATIO,
            MagicNumbers.WORLD_SCREEN_RATIO * 2,
            orig.getFormat()
        );
        newPix.drawPixmap(orig, 0, 0, orig.getWidth(), orig.getHeight(), 0, 0, newPix.getWidth(), newPix.getHeight());
        playerTex = new Texture(newPix);

        orig.dispose();
        newPix.dispose();

        Position pos = worldToScreen(player.position());

        camera.position.set((float) (pos.x() + (MagicNumbers.WORLD_SCREEN_RATIO / 2D)), pos.y() + MagicNumbers.WORLD_SCREEN_RATIO, 0);
        camera.update();
    }

    public Position worldToScreen(@NonNull Position position) {
        int x = position.x() * MagicNumbers.WORLD_SCREEN_RATIO + SCREEN_OFFSET_X;
        int y = position.y() * MagicNumbers.WORLD_SCREEN_RATIO + SCREEN_OFFSET_Y;
        return new Position(x, y);
    }

    public Position worldToScreen(@NonNull Vector location) {
        int x = (int) location.x() * MagicNumbers.WORLD_SCREEN_RATIO + SCREEN_OFFSET_X;
        int y = (int) location.y() * MagicNumbers.WORLD_SCREEN_RATIO + SCREEN_OFFSET_Y;
        return new Position(x, y);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        doStuff();

        ScreenUtils.clear(new Color(0x1EA1FFFF));

        Position pos = worldToScreen(player.position());
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Chunk chunk : world.chunks()) {
            renderer.render(chunk);
        }

        batch.draw(playerTex, pos.x(), pos.y());

        font.draw(batch, "Speed: " + speed, 0, camera.viewportHeight);
        font.draw(batch, "X: " + -SCREEN_OFFSET_X / MagicNumbers.WORLD_SCREEN_RATIO, 0, camera.viewportHeight - 20);
        font.draw(batch, "Y: " + -SCREEN_OFFSET_Y / MagicNumbers.WORLD_SCREEN_RATIO, 0, camera.viewportHeight - 40);
        batch.end();

        if (player.direction().magnitudeSquared() < 1) {
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                player.direction().add(new Vector(0, 1)).normalize();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                player.direction().add(new Vector(0, -1)).normalize();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                player.direction().add(new Vector(0, 1)).normalize();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                player.direction().add(new Vector(0, -1)).normalize();
            }
        }

        player.position().add(player.direction().copy().divide(delta));

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
        int onSides = (int) Math.ceil(Math.abs(viewport.getScreenWidth() / MagicNumbers.WORLD_SCREEN_RATIO / MagicNumbers.CHUNK_WIDTH) / 2F) + 2;
        for (Chunk chunk : world.chunks()) {
            if (Math.abs(chunk.position() - chunkpos) > onSides) {
                world.removeChunk(chunk.position());
                System.out.println("Unloaded chunk " + chunk.position());
            }
            for (int pos = chunkpos - onSides; pos <= chunkpos + onSides; pos++) {
                if (!world.isChunkLoaded(pos)) {
                    world.chunk(pos);
                    System.out.println("Loaded chunk " + pos);
                }
            }
        }
    }

    private void doStuff() {
        Packet packet = connection.nextReceived();
        if (packet instanceof EntityMovePacket movePacket && movePacket.uuid().equals(player.uuid())) {
            Vector v = movePacket.vector();
            player.direction().set(v.x(), v.y());
        }
    }
}
