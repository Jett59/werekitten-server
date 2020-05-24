package com.mycodefu.werekitten.server.world;

import com.mycodefu.werekitten.server.netty.NettyServer;
import com.mycodefu.werekitten.server.netty.NettyServerHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelId;

public class World implements NettyServerHandler.ServerConnectionCallback{
private ChannelId[] players = new ChannelId[2];
private NettyServer server;
private String hostName;

public World(NettyServer server) {
	this.server = server;
}

@Override
public void serverConnectionOpened(ChannelId id, String remoteAddress) {
	if(players[0] == null) {
		players[0] = id;
	}else if(players[1] == null) {
		players[1] = id;
	}else {
		throw new IllegalStateException("only two players are supported at a time");
	}
}

@Override
public void serverConnectionMessage(ChannelId id, String sourceIpAddress, ByteBuf message) {
	if(players[0] == id) {
		server.sendMessage(players[1], message);
	}else {
		server.sendMessage(players[0], message);
	}
}

@Override
public void serverConnectionClosed(ChannelId id) {
	if(players[0] == id) {
		if(players[1] != null) {
			server.sendMessage(players[1], ByteBufAllocator.DEFAULT.buffer(1).writeByte(-1));
		}
	}else {
		players[1] = null;
	}
}

public String getHost() {
	return hostName;
}

public void setHost(String hostName) {
	this.hostName = hostName;
}
}
