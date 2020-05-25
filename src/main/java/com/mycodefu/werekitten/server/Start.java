package com.mycodefu.werekitten.server;

import com.mycodefu.werekitten.server.netty.NettyServer;
import com.mycodefu.werekitten.server.world.WorldManager;

public class Start {
public static void main(String[] args) {
	WorldManager manager = new WorldManager();
	NettyServer server = new NettyServer(51273, manager);
	manager.setServer(server);
	server.listen();
}
}
