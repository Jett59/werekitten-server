package com.mycodefu.werekitten.server.world;

import com.mycodefu.werekitten.server.netty.NettyServer;
import com.mycodefu.werekitten.server.netty.NettyServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelId;
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
	@Override
    public void serverConnectionOpened(ChannelId id, String remoteAddress) {
        System.out.printf("Connection received (%s): %s\n\n", remoteAddress, id.asShortText());
        channelToHasJoinedWorld.put(id, new AtomicBoolean(false));
    }

    @Override
    public void serverConnectionMessage(ChannelId id, String sourceIpAddress, ByteBuf message) {
        if (!channelToHasJoinedWorld.get(id).get()) {
            message.readByte();
            message.readLong();
            byte length = message.readByte();
            byte[] bytes = new byte[length];
            message.readBytes(bytes);
            String name = new String(bytes, StandardCharsets.UTF_8);
            if(!hostNameToWorld.containsKey(name)) {
            	World world = new World(server);
            	world.setHost(name);
            	hostNameToWorld.put(name, world);
            }
            World world = hostNameToWorld.get(name);
            channelToHasJoinedWorld.get(id).set(true);
            channelToWorld.put(id, world);
            world.serverConnectionOpened(id, sourceIpAddress);
            worlds.add(world);
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
