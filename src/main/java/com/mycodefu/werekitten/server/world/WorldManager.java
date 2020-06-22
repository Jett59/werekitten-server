package com.mycodefu.werekitten.server.world;

import com.mycodefu.werekitten.server.netty.NettyServer;
import com.mycodefu.werekitten.server.netty.NettyServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelId;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorldManager implements NettyServerHandler.ServerConnectionCallback {
    private NettyServer server;
    private List<World> worlds = new ArrayList<>();
    private Map<ChannelId, AtomicBoolean> channelToHasJoinedWorld = new HashMap<>();
    private Map<ChannelId, World> channelToWorld = new HashMap<>();
    private Map<String, World> hostNameToWorld = new HashMap<>();
	private World nextWorld;
	private byte[] bytes;

    @Override
    public void serverConnectionOpened(ChannelId id, String remoteAddress) {
        System.out.printf("Connection received (%s): %s\n\n", remoteAddress, id.asShortText());
        channelToHasJoinedWorld.put(id, new AtomicBoolean(false));
    }

    @Override
    public void serverConnectionMessage(ChannelId id, String sourceIpAddress, ByteBuf message) {
        if (!channelToHasJoinedWorld.get(id).get()) {
            if (message.getByte(0) == 65) {
                nextWorld = new World(server);
                bytes = new byte[message.getByte(1)];
                message.getBytes(2, bytes);
                String hostName = new String(bytes, CharsetUtil.UTF_8);
                System.out.printf("A message received (%s), id %s:\n%s\n", sourceIpAddress, id.asShortText(), hostName);

                nextWorld.setHost(hostName);
                nextWorld.serverConnectionOpened(id, sourceIpAddress);
                worlds.add(nextWorld);
                channelToHasJoinedWorld.get(id).set(true);
                channelToWorld.put(id, nextWorld);
                hostNameToWorld.put(hostName, nextWorld);
                System.out.printf("%s hosting world\n", hostName);
            } else if (message.getByte(0) == 66) {
                System.out.println(message.readableBytes());
                bytes = new byte[message.getByte(1)];
                message.getBytes(2, bytes);
                String hostName = new String(bytes, CharsetUtil.UTF_8);
                System.out.printf("B message received (%s), id %s:\n%s\n", sourceIpAddress, id.asShortText(), hostName);
                hostNameToWorld.get(hostName).serverConnectionOpened(id, sourceIpAddress);
                channelToHasJoinedWorld.get(id).set(true);
                channelToWorld.put(id, hostNameToWorld.get(hostName));
                System.out.println("player joining world");
            } else {
                System.err.println("error: unknown message " + message.getByte(0) + ", expected host (65) or join (66)");
                throw new IllegalArgumentException("unknown message passed by connection " + sourceIpAddress);
            }
        } else {
            byte messageType = message.getByte(0);
            long timestamp = message.getLong(1);
            if (messageType == 7) {
                byte length = message.getByte(9);
                byte[] messageBytes = new byte[length];
                message.getBytes(10, messageBytes);
                String messageString = new String(messageBytes, StandardCharsets.UTF_8);
                System.out.printf("Chat message received from %s: %s\n", id.asShortText(), messageString);
            } else {
                System.out.printf("Message received from %s, type: %d, timestamp: %d\n", id.asShortText(), messageType, timestamp);
            }
            channelToWorld.get(id).serverConnectionMessage(id, sourceIpAddress, message);
        }
    }

    @Override
    public void serverConnectionClosed(ChannelId id) {
        if (hostNameToWorld.containsKey(channelToWorld.get(id).getHost())) {
            hostNameToWorld.remove(channelToWorld.get(id).getHost());
        }
        channelToWorld.get(id).serverConnectionClosed(id);
        channelToWorld.remove(id);
        channelToHasJoinedWorld.remove(id);
    }

    public void setServer(NettyServer server) {
        this.server = server;
    }
}
