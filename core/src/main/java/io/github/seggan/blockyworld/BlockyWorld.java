package io.github.seggan.blockyworld;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.seggan.blockyworld.server.Packet;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;

import lombok.NonNull;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class BlockyWorld extends ApplicationAdapter {

    private static int SCREEN_OFFSET_X = 0;
    private static int SCREEN_OFFSET_Y = 0;

    private Connection connection;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private World world;
    private Renderer renderer;

    private Chunk chunk;

    private int speed = 1;

    private static BlockyWorld instance;

    public static Position worldToScreen(@NonNull Position position) {
        int x = position.x() * MagicNumbers.WORLD_SCREEN_RATIO + BlockyWorld.SCREEN_OFFSET_X;
        int y = position.y() * MagicNumbers.WORLD_SCREEN_RATIO + BlockyWorld.SCREEN_OFFSET_Y;
        return new Position(x, y);
    }

    @Override
    public void create() {
        instance = this;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);
        viewport = new ScreenViewport(camera);
        renderer = new Renderer(batch);

        Socket soc;
        try {
            soc = new Socket("localhost", 16255);
            soc.getOutputStream().write(ByteBuffer.allocate(2).putShort(Packet.PROTOCOL_VERSION).array());
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    soc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            short code = ByteBuffer.wrap(soc.getInputStream().readNBytes(2)).getShort();
            if (code == 3) {
                short version = ByteBuffer.wrap(soc.getInputStream().readNBytes(Short.BYTES)).getShort();
                System.err.println("Incompatible protocol version; server " + version + " client " + Packet.PROTOCOL_VERSION);
                System.exit(0);
                return;
            } else if (code != 4) {
                System.err.println("Invalid packet: " + code);
                System.exit(1);
                return;
            }
        } catch (IOException e) {
            System.err.println("Could not connect to server:");
            e.printStackTrace();
            dispose();
            System.exit(1);
            return;
        }

        connection = new Connection(soc);

        world = connection.requestWorld();
        connection.requestChunk(0, world);
        connection.requestChunk(-1, world);
        connection.requestChunk(1, world);
    }

    @Override
    public void render() {
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
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.update();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        batch.dispose();
    }

    public static Connection connection() {
        return instance.connection;
    }

    public static Viewport viewport() {
        return instance.viewport;
    }
}