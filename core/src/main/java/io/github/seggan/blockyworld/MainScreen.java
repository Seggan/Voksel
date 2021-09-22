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

    private static final int LOAD_RADIUS = 5;

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
        world.chunkAt(0);

        player = new Player();
        player.position(0, world.highestBlockYAt(0) + 2);
        //player.direction().set(-7.9, 10);
        //player.direction().set(0.5, 0);

        connection.connectPlayer(player);

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

        Vector pos = worldToScreen(player.position());

        camera.position.set((float) (pos.x() + (MagicNumbers.WORLD_SCREEN_RATIO / 2D)), (float) (pos.y() + MagicNumbers.WORLD_SCREEN_RATIO), 0);
        camera.update();
    }

    public Position worldToScreen(@NonNull Position position) {
        int x = position.x() * MagicNumbers.WORLD_SCREEN_RATIO;
        int y = position.y() * MagicNumbers.WORLD_SCREEN_RATIO + SCREEN_OFFSET_Y;
        return new Position(x, y);
    }

    public Vector worldToScreen(@NonNull Vector location) {
        double x = location.x() * MagicNumbers.WORLD_SCREEN_RATIO;
        double y = location.y() * MagicNumbers.WORLD_SCREEN_RATIO + SCREEN_OFFSET_Y;
        return new Vector(x, y);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        doStuff();

        ScreenUtils.clear(new Color(0x1EA1FFFF));

        Vector pos = worldToScreen(player.position());

        float x = (float) (pos.x() + (MagicNumbers.WORLD_SCREEN_RATIO / 2F));
        float y = (float) (pos.y() + MagicNumbers.WORLD_SCREEN_RATIO);
        camera.position.set(x, y, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Chunk chunk : world.chunks()) {
            renderer.render(chunk);
        }

        batch.draw(playerTex, x, y);

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            connection.sendPlayerMove(player, new Vector(0, 1.5));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            connection.sendPlayerMove(player, new Vector(4, 0));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            connection.sendPlayerMove(player, new Vector(-4, 0));
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
        int chunkpos = (int) (player.position().x() / MagicNumbers.CHUNK_WIDTH);
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

    private void doStuff() {
        Packet packet = connection.nextReceived();
        if (packet instanceof EntityMovePacket movePacket && movePacket.uuid().equals(player.uuid())) {
            Vector v = movePacket.vector();
            player.position().set(v);
        }
    }
}
