package io.github.seggan.blockyworld.server;

import io.github.seggan.blockyworld.world.Chunk;
import io.github.seggan.blockyworld.world.World;
import io.github.seggan.blockyworld.world.block.Block;
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
        Block b = new Block(Material.STONE, 1, 12, new Chunk(123, new World("world")));
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        b.pack(packer);
        System.out.println(new String(packer.toByteArray()));
        Block unp = Block.unpack(MessagePack.newDefaultUnpacker(packer.toByteArray()));
        System.out.println(unp);
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