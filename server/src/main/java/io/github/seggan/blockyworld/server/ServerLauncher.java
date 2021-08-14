package io.github.seggan.blockyworld.server;

import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import io.github.seggan.blockyworld.world.block.Material;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Launches the server application.
 */
public class ServerLauncher {

    public static final Map<InetAddress, ClientRecvThread> RECEIVING_THREADS = new HashMap<>();
    public static final Map<InetAddress, ClientSendThread> SENDING_THREADS = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Chunk chunk = new Chunk(1, new World("hi"));
        chunk.setBlock(Material.STONE, 0, 0, null);
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        chunk.pack(packer);
        System.out.println(new String(packer.toByteArray()));
        Chunk unp = Chunk.unpack(MessagePack.newDefaultUnpacker(packer.toByteArray()));
        System.out.println(unp);
        if (!chunk.world().uuid().equals(chunk.world().uuid())) {
            throw new AssertionError();
        }
        /*ServerSocket server = new ServerSocket(16255);
        while (!server.isClosed()) {
            Socket socket = server.accept();
            InetAddress address = socket.getInetAddress();
            ClientRecvThread thread = new ClientRecvThread(socket);
            RECEIVING_THREADS.put(address, thread);
            thread.start();
            ClientSendThread thread1 = new ClientSendThread(socket);
            SENDING_THREADS.put(address, thread1);
            thread1.start();
        }*/
    }
}