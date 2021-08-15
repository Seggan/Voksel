package io.github.seggan.blockyworld.server;

import org.msgpack.core.MessagePack;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientRecvThread extends Thread {

    private static final int HEADER_SIZE = Short.BYTES + Integer.BYTES; // 6

    private final Socket client;

    public ClientRecvThread(Socket client) {
        super();
        setDaemon(true);
        this.client = client;
    }

    @Override
    @SneakyThrows(IOException.class)
    public void run() {
        InputStream in = client.getInputStream();
        //noinspection InfiniteLoopStatement
        while (true) {
            byte[] header = in.readNBytes(HEADER_SIZE);
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE).put(header);
            short code = buffer.getShort();
            int length = buffer.getInt();
            byte[] body = in.readNBytes(length);
            Request request = RequestType.getByCode(code).unpack(MessagePack.newDefaultUnpacker(body), false, client.getInetAddress());
            Server.mainThread().addRequest(request);
        }
    }
}
