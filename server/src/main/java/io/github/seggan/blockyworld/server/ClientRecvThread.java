package io.github.seggan.blockyworld.server;

import org.msgpack.core.MessagePack;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientRecvThread extends ClientThread {

    private static final int HEADER_SIZE = Short.BYTES + Integer.BYTES; // 6

    public ClientRecvThread(Socket client) {
        super(client, "Receiving thread for " + client.getInetAddress().getHostAddress());
    }

    @Override
    @SneakyThrows(IOException.class)
    public void run() {
        InputStream in = client.getInputStream();
        while (!client.isClosed() && !stop) {
            byte[] header = in.readNBytes(HEADER_SIZE);
            if (header.length == 0) break;
            ByteBuffer buffer = ByteBuffer.wrap(header);
            short code = buffer.getShort();
            if (code == 4) {
                continue;
            }
            int length = buffer.getInt();
            byte[] body = in.readNBytes(length);
            Packet packet = PacketType.getByCode(code).unpack(MessagePack.newDefaultUnpacker(body), false, client.getInetAddress());
            Server.mainThread().addRequest(packet);
        }
        ClientSendThread thr = Server.SENDING_THREADS.get(client.getInetAddress());
        thr.stopThread();
        Server.SENDING_THREADS.remove(client.getInetAddress());
        Server.RECEIVING_THREADS.remove(client.getInetAddress());
    }
}
