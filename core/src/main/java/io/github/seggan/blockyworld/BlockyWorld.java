package io.github.seggan.blockyworld;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.seggan.blockyworld.server.Packet;
import io.github.seggan.blockyworld.util.MagicNumbers;
import io.github.seggan.blockyworld.util.Position;
import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;

import lombok.Getter;
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

    @Getter
    private static Connection connection;
    private static SpriteBatch batch;
    private static OrthographicCamera camera;
    private static World world;
    private static Renderer renderer;

    private static Chunk chunk;

    public static Position worldToScreen(@NonNull Position position) {
        int x = position.x() * MagicNumbers.WORLD_SCREEN_RATIO + BlockyWorld.SCREEN_OFFSET_X;
        int y = position.y() * MagicNumbers.WORLD_SCREEN_RATIO + BlockyWorld.SCREEN_OFFSET_Y;
        return new Position(x, y);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);
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
        chunk = connection.requestChunk(0, world);
    }

    @Override
    public void render() {
        ScreenUtils.clear(new Color(0x1EA1FFFF));

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderer.render(chunk);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        renderer.aspectRatio(width / 600D, height / 600D);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        batch.dispose();
    }
}