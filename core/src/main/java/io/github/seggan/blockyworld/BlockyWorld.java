package io.github.seggan.blockyworld;

import com.badlogic.gdx.Game;
import io.github.seggan.blockyworld.server.Packet;

import lombok.Getter;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class BlockyWorld extends Game {

    @Getter
    private static Connection connection;

    @Override
    public void create() {
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
                throw null;
            } else if (code != 4) {
                System.err.println("Invalid packet: " + code);
                System.exit(1);
                throw null;
            }
        } catch (IOException e) {
            System.err.println("Could not connect to server:");
            e.printStackTrace();
            dispose();
            System.exit(1);
            throw null;
        }

        connection = new Connection(soc);

        setScreen(new MainScreen());
    }
}