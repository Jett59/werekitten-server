package com.mycodefu.werekitten.server.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mycodefu.werekitten.server.netty.NettyServer;
import com.mycodefu.werekitten.server.netty.NettyServerHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelId;
import io.netty.util.CharsetUtil;

public class WorldManager implements NettyServerHandler.ServerConnectionCallback{
	private NettyServer server;
private List<World> worlds = new ArrayList<>();
private Map<ChannelId, AtomicBoolean> channelToHasJoinedWorld = new HashMap<>();
private Map<ChannelId, World> channelToWorld = new HashMap<>();
private Map<String, World> hostNameToWorld = new HashMap<>();

public WorldManager(NettyServer server) {
	this.server = server;
}

	@Override
	public void serverConnectionOpened(ChannelId id, String remoteAddress) {
		channelToHasJoinedWorld.put(id, new AtomicBoolean(false));
	}

private World nextWorld;
private byte[] bytes;

	@Override
	public void serverConnectionMessage(ChannelId id, String sourceIpAddress, ByteBuf message) {
		if(channelToHasJoinedWorld.get(id).get()) {
		if(message.getByte(0) == 0) {
			nextWorld = new World(server);
			bytes = new byte[message.getByte(1)];
			message.getBytes(2, bytes);
			String hostName = new String(bytes, CharsetUtil.UTF_8);
			nextWorld.setHost(hostName);
			worlds.add(nextWorld);
			channelToHasJoinedWorld.get(id).set(true);
			channelToWorld.put(id, nextWorld);
			hostNameToWorld.put(hostName, nextWorld);
		}else if(message.getByte(0) == 1) {
			bytes = new byte[message.getByte(1)];
			message.getBytes(2, bytes);
			String hostName = new String(bytes, CharsetUtil.UTF_8);
			hostNameToWorld.get(hostName).serverConnectionOpened(id, sourceIpAddress);
			channelToHasJoinedWorld.get(id).set(true);
			channelToWorld.put(id, hostNameToWorld.get(hostName));
		}else {
			throw new IllegalArgumentException("unknown message passed by connection "+sourceIpAddress);
		}
	}else {
		channelToWorld.get(id).serverConnectionMessage(id, sourceIpAddress, message);
	}
	}

	@Override
	public void serverConnectionClosed(ChannelId id) {
		if(hostNameToWorld.containsKey(channelToWorld.get(id).getHost())) {
			hostNameToWorld.remove(channelToWorld.get(id).getHost());
		}
		channelToWorld.get(id).serverConnectionClosed(id);
		channelToWorld.remove(id);
		channelToHasJoinedWorld.remove(id);
	}
}
